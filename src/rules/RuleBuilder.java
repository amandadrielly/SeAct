package rules;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RuleBuilder {
	public int cont = 0;
	
	public RuleBuilder(){
		
        writeToFile("/Users/amandadrielly/eclipse-workspace/SeAct/src/rulesActivities.txt",
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        + "@prefix owl: <http://www.w3.org/2002/07/owl#>\n"
        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n"
        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
        + "@prefix ACTION: <http://www.action.onto/ACTION.owl#>\n");
	}
	    
	
	public void generateJenaRule(ArrayList<String> events, String activity){
		
		cont++;
		String rule="\n";
		rule+="[rule"+cont+":";
	
		for(int i=0; i<events.size(); i++){
			
			rule += "(?user ACTION:hasEvent ?event"+i+"),\n"
					+ "(?event"+i+" rdf:type ACTION:Event),\n"
					+ "(?event"+i+" ACTION:hasValue \""+events.get(i)+"\"),\n"
					+ "(?event"+i+" ACTION:relatedTime ?time"+i+"),\n"
					+ "(?time"+i+"  ACTION:hasValue ?tval"+i+"),\n";
		}
		
		rule += "makeTemp(?activity)\n";
		rule += "->\n";
		
		rule += "(?activity rdf:type ACTION:Activity),\n"
				+"(?user ACTION:hasActivity ?activity),\n"
				+"(?activity ACTION:hasValue \""+activity+"\"),\n"
				+"(?activity ACTION:relatedTime ?time1),\n" 
				+"(?activity ACTION:composedOf ?event0),\n"
				+"(?event0 ACTION:partOf ?activity),\n"
				+"(?activity ACTION:composedOf ?event1),\n"
				+"(?event1 ACTION:partOf ?activity)]\n";
		
        writeToFile("/Users/amandadrielly/eclipse-workspace/SeAct/src/rulesActivities.txt", rule.concat("\n"));
	}
	
	 public void writeToFile(String fileName, String s) {
	        FileWriter output;
	        try {
	            output = new FileWriter(fileName, true); 
	            BufferedWriter writer = new BufferedWriter(output);
	            writer.write(s);
	            writer.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
}
