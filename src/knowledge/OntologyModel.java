package knowledge;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.shared.uuid.JenaUUID;
import org.apache.jena.shared.uuid.UUID_V4_Gen;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class OntologyModel {
	
	private static OntologyModel OM_INSTANCE = null;

	//constructor
	public OntologyModel(){
		model = getModel();
		loadData(model);	
	}
	
	//inicializa o modelo
	public static synchronized OntologyModel getInstance()
	{
		if(OM_INSTANCE == null)
		{ OM_INSTANCE = new OntologyModel(); }
		
		return OM_INSTANCE;
	}
	
	//atributos
	
	//caminho para ontologia
	public static final String ONTOPATH = "/Users/amandadrielly/eclipse-workspace/SeAct/src/ACTION.owl";
	
	//caminho para regras
	public static final String RULEPATH ="/Users/amandadrielly/eclipse-workspace/SeAct/src/rulesActivities.txt";
	
	//ontologia
	private OntModel model;
	
	//modelo depois da inferência
	private InfModel actModel;
	
	
	
	//metodos
	
	/**
	 * Cria o modelo ontológico
     * retorna o modelo instaciado
	 */
	private OntModel getModel() {
        return ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
    }
	
	/**
	 * Preenche o modelo m com o conteúdo da ontologia física.
	 */
	private void loadData( Model m ) {
    	String path = new String(ONTOPATH);
        FileManager.get().readModel( m, path );
    }
	
    /**
     * Aplicando as regras
     * 
     */
    public void applyRules()
    {
    	try{
    	Reasoner reasoner = new GenericRuleReasoner( Rule.rulesFromURL( RULEPATH ) );
    	//model = ontologia com as instâncias
    	actModel = ModelFactory.createInfModel( reasoner, model ); 
    	
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}
    }
    	
    
    
    public void insertEvent(String user, String value, Double time){
    	try {
    	UUID_V4_Gen idGen = new UUID_V4_Gen();

    	//Criando uma instância de tempo
    	JenaUUID timeResId = idGen.generate();
    	
    	//adicionar valores
    	String prefix = "prefix rdf: <" + RDF.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n"+
                "prefix xsd: <"+ XSD.getURI() +"> \n"+
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix :<" + "http://www.action.onto/ACTION.owl#" + ">\n" ;  
    	
    	
    	String queryTimeRes =prefix + "insert data { 	"
    			+ ":"+timeResId+" rdf:type :Time . "
    			+ ":"+timeResId+" :hasValue "+time+" "
    			+ "}"; 
    	
    	String query = prefix + "insert { 	"
    			+ "?user :hasEvent _:blanknode . " 
    			+ "_:blanknode rdf:type :Event ."
    			+ "_:blanknode :hasValue "+value+" ."
				+ "_:blanknode :relatedTime :"+timeResId+"  "
    			+ "}"
    			+ "where{ "
    			+ "?user rdf:type :Person . ?user :hasName ?name . FILTER(?name='"+user+"'^^xsd:string)"
				+ "}";
    
    	//adicionando o tempo que correlaciona dois eventos
    	UpdateAction.parseExecute(queryTimeRes, model);
    	//adicionando os eventos
    	UpdateAction.parseExecute(query, model);
    	
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}
    }
      
    /**
     * Salvanto o modelo atual como um arquivo turtle
     */
    public void export()
    {
    	FileWriter out = null;
    	try {

    	  out = new FileWriter( "/Users/amandadrielly/eclipse-workspace/SeAct/src/exportModel.ttl" );
    	  model.write( out, "Turtle" );
    	  
    	  out = new FileWriter( "/Users/amandadrielly/eclipse-workspace/SeAct/src/exportInfModel.ttl" );
    	  actModel.write( out, "Turtle" );
    	} catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    	  if (out != null) {
    	    try {out.close();} catch (IOException ignore) {}
    	  }
    	}
    }
    
    /**
     * retorna as atividades que foram inferidas
     */
    public ArrayList <String> extractActivities(){
    	
    	String prefix = "prefix rdf: <" + RDF.getURI() + ">\n" +
                "prefix owl: <" + OWL.getURI() + ">\n"+
                "prefix xsd: <"+ XSD.getURI() +"> \n"+
                "prefix rdfs: <" + RDFS.getURI() + ">\n" +
                "prefix ACTION:<" + "http://www.action.onto/ACTION.owl#" + ">\n" ;  
    	
    	String queryStr = prefix +
                "select distinct ?activityVal \n "+ 
                "where {"+
                "		 ?activity a ACTION:Activity . \n " +  
                "        ?activity ACTION:composedOf ?evVal . \n" +
                "        ?activity ACTION:hasValue ?activityVal  \n" +       
                "}";
    	
    	 ArrayList <String> res = new ArrayList <String> ();
         Query query = QueryFactory.create( queryStr );
         QueryExecution qexec = QueryExecutionFactory.create( query, actModel );
         try {
             ResultSet results = qexec.execSelect();                
			 if(results.hasNext()) {
             QuerySolution solution = results.nextSolution();
			 String value = solution.get("activityVal").asLiteral().getLexicalForm();
             res.add(value);
			}
         }
         finally {
             qexec.close();
         }
         
         return res;
    }
    
 
}
