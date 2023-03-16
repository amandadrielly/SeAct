import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.log4j.BasicConfigurator;

import knowledge.WindowManager;
import rules.RuleBuilder;


public class Start {

	public static void main(String[]args) {
	
			try {
			//offline step
			//classificando novas atividades 
			//chamar aqui o knn
				
			//gerando as regras
			//ruleBuilder();
				
		    //Log4J
		    //BasicConfigurator.configure();
				
			//online step
		    // WindowManager.loadDataset("/Users/amandadrielly/eclipse-workspace/SeAct/src/kasteren.csv");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
	}
	
	public static void ruleBuilder() throws IOException {
		
        HashMap<String, ArrayList<String>> eventosAtuais = new HashMap<String, ArrayList<String>>();
		//inicializa a classe que gera as regras
		RuleBuilder rule = new RuleBuilder();
		//leitura do dataset
		InputStream fis;	
		fis = new FileInputStream("/Users/amandadrielly/eclipse-workspace/SeAct/src/kasteren.csv");		
	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);	    
	    ArrayList<String> eventos; 
	    String line;		
	    String valueObj = null;
	    String valuePos;
	    String activity;
	    
	    while((line = br.readLine()) != null){
	    	eventos = new ArrayList<String>();
	    	String[] register = line.split(","); 
	    	//extraindo valores
	    	register[2] = removeQuotes(register[2]);
	    	register[0] = removeQuotes(register[0]);
	    	register[1] = removeQuotes(register[1]);
	    	activity = register[2];	    	
            valueObj = register[0];
            valuePos = register[1];
            eventos.add(valueObj);
            eventos.add(valuePos);
            
            //não repete eventos relacionados a uma atividade nas regras
            if(!eventosAtuais.keySet().contains(valuePos)){
            	eventosAtuais.put(valuePos, new ArrayList<String>());
            }else if (!eventosAtuais.get(valuePos).contains(valueObj)) {
            	rule.generateJenaRule(eventos, activity);
            	eventosAtuais.get(valuePos).add(valueObj);
            }
 
	}
}
	 public static String removeQuotes(String str){
	        String s = str.replace("\"", "");
	        return s;
	    }
}