package knowledge;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

public class WindowManager {
	
	//define atividades para calcular fórmulas e matriz de confusão 
    public static HashMap<String, Integer> initActivities(){
        HashMap<String, Integer> activities = new HashMap<String, Integer>();
        String GoToBed = "GoToBed";
        String UseToilet = "UseToilet";
        String TakeShower = "TakeShower";  
        String LeaveHouse = "LeaveHouse";
        String PrepareBreakfast = "PrepareBreakfast"; 
        String GetDrink = "GetDrink"; 
        String PrepareDinner ="PrepareDinner"; 
        
        activities.put(GoToBed, 0);
   	    activities.put(UseToilet, 1);
   	    activities.put(TakeShower, 2);
   	    activities.put(LeaveHouse, 3);
        activities.put(PrepareBreakfast, 4);
        activities.put(GetDrink, 5);
        activities.put(PrepareDinner, 6);
        
        return activities;
        
    }
    
    //inicializa matriz de confusão
    public static int [][] initMatriz(HashMap<String, Integer> activities){
    	
        int [][] classification = new int[activities.size()][activities.size()+1];
        for (int i=0; i< classification.length; ++i)
            for (int j =0; j<classification[i].length; j++)
                classification[i][j] = 0;
		return classification;
    	
    }
   
	public static void loadDataset(String dirPath) throws IOException{
		HashMap<String, Integer> activities = initActivities();
		int [][] classification = initMatriz(activities);
	
        //leitura do dataset
		InputStream fis;	
		fis = new FileInputStream(dirPath);		
	    InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    BufferedReader br = new BufferedReader(isr);	    
	    ArrayList<String> detectedActivities = new ArrayList<String>();
	    String line;		
	    String valueObj = null;
	    String valuePos;
	    
	    OntologyModel ontology;
	    
	    while((line = br.readLine()) != null){
	    	//inicializa ontologia
	    	ontology = new OntologyModel();   	
	    	String[] register = line.split(","); 
	    	//extraindo valores
	    	register[2] = removeQuotes(register[2]);
	    	String activ = register[2];	    	
            valueObj = register[0];
            valuePos = register[1];
	    	//inserindo eventos na ontologia
            //20.0 valores default para o timestamp dos eventos
            ontology.insertEvent("Amanda",valueObj,20.0);
            ontology.insertEvent("Amanda",valuePos,20.0);
            System.out.println(valueObj);
            System.out.println(valuePos);
	    	//aplicando as regras sobre a ontologia povoada
            ontology.applyRules();
	    	//exportando o grafo de conhecimento 
            ontology.export();
			detectedActivities = ontology.extractActivities();
			if(ontology.extractActivities().size()!=0) {
				System.out.println("ai");
				//inserindo atividade esperada e atividade detectada
                classification[activities.get(activ)][activities.get(detectedActivities.get(0))]+= 1;
                }
    	
	     }
	    
	    generateMatrix(classification, activities);
	    generateFscore(classification, activities);
	    isr.close();
	    
	  }
	    
	   public static void generateMatrix(int[][] classification, HashMap<String, Integer> activities ){
	    
	    System.out.println("Confusion Matrix (RealActivities X ClassifiedActivities)");
        printMatrix(classification);
        float [][] classificationNormalized = new float[activities.size()][activities.size()+1];
        for (int i= 0; i<classification.length; i++){
            float activitySum = 0;
            for(int j=0; j<classification[i].length; ++j)
                activitySum += (float)classification[i][j];
            for(int j=0; j<classification[i].length; ++j)
            	classificationNormalized[i][j] = round(classification[i][j]/activitySum, 3);
        }
        System.out.println("Confusion Matrix normalized (%)(RealActivities X ClassifiedActivities)");
        printMatrix(classificationNormalized);

        writeConfusionMatrixToResultsFile("/Users/amandadrielly/eclipse-workspace/SeAct/src/resultsFile.txt", classificationNormalized);
        
	   }
	   
	   public static void generateFscore(int[][] classification, HashMap<String, Integer> activities){
        
        float [][] statisticsTP_TN_FP_FN = new float[activities.size()][6];
        for (int i=0; i< statisticsTP_TN_FP_FN.length; ++i)
            for (int j =0; j<statisticsTP_TN_FP_FN[i].length; j++)
                statisticsTP_TN_FP_FN[i][j] = 0;
        int fn, fp, tp, tn;          
        float precision;
        float recall;
        float fScore;
        fn = 0; 
        fp = 0;
        tp = 0;
        tn = 0;
        for (int i= 0; i<classification.length; i++){
            
            for(int j=0; j<classification[i].length; ++j){
                if(classification[i][j] > 0)
                    if (i==j)
                        tp += classification[i][j];
                    else
                        if (j == activities.size()) 
                            fn += classification[i][j];
                        else
                            fp += classification[i][j];
            }
            if(tp+fp ==0)
                precision = 0;
            else
                precision = (float)tp/(tp+fp);
           
            if (tp+fn ==0)
                recall =0;
            else
                recall = (float)tp/(tp+fn);
            statisticsTP_TN_FP_FN[i][0] = tp;
            statisticsTP_TN_FP_FN[i][1] = tn;
            statisticsTP_TN_FP_FN[i][2] = fp;
            statisticsTP_TN_FP_FN[i][3] = fn;
            statisticsTP_TN_FP_FN[i][4] = precision;
            statisticsTP_TN_FP_FN[i][5] = recall;
        }

   
        float sumPrec =0f;
        float sumRecall = 0f;
        for(int i=0; i<statisticsTP_TN_FP_FN.length; ++i){
            sumPrec += statisticsTP_TN_FP_FN[i][4];
            sumRecall += statisticsTP_TN_FP_FN[i][5];
        }

        precision = round(sumPrec/activities.size(), 3);
        recall = round(sumRecall/activities.size(), 3);
        fScore = round(2*(precision*recall)/(precision+recall), 3);
        writeToFile("/Users/amandadrielly/eclipse-workspace/SeAct/src/StatisticsFilePrecRecallFscore.txt", Float.toString(precision).concat(" , ").concat(Float.toString(recall)).concat(" , ").concat(Float.toString(fScore)).concat("\n"));

	}
	
	  public static float round(float d, int decimalPlace) {
	        BigDecimal bd = new BigDecimal(Float.toString(d));
	        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_EVEN);
	        return bd.floatValue();
	    }

	    public static Double round(Double d, int decimalPlace) {
	        BigDecimal bd = new BigDecimal(Double.toString(d));
	        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_DOWN);
	        return bd.doubleValue();
	    }
	    
	    public static void writeToFile(String fileName, String s) {
	        FileWriter output;
	        try {
	            output = new FileWriter(fileName, true); // second parameter: appends at the end
	            BufferedWriter writer = new BufferedWriter(output);
	            writer.write(s);
	            writer.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public static String removeQuotes(String str){
	        String s = str.replace("\"", "");
	        return s;
	    }
	    
	    protected static void printMatrix(int[][] matrix){
	        for (int i = 0; i < matrix.length; i++) {
	            for (int j = 0; j < matrix[i].length; j++) {
	                System.out.print(matrix[i][j] + "\t");
	            }
	            System.out.print("\n");
	        }
	    }
	    protected static void printMatrix(float[][] matrix){
	        for (int i = 0; i < matrix.length; i++) {
	            for (int j = 0; j < matrix[i].length; j++) {
	                //System.out.printf("%1f ", round(matrix[i][j], 2));
	                System.out.print(matrix[i][j] + "  ");
	            }
	            System.out.print("\n");
	        }
	    }
	    protected static void printMatrix(ArrayList<ArrayList<Float>> matrix){
	        for (int i = 0; i < matrix.size(); i++) {
	            for (int j = 0; j < matrix.get(i).size(); j++) {
	                //System.out.printf("%1f ", round(matrix[i][j], 2));
	                System.out.print(matrix.get(i).get(j) + "  ");
	            }
	            System.out.print("\n");
	        }
	    }
	    protected static void printMatrix(String[][] matrix){
	        for (int i = 0; i < matrix.length; i++) {
	            for (int j = 0; j < matrix[i].length; j++) {
	                System.out.print(matrix[i][j] + "      ");
	            }
	            System.out.print("\n");
	        }
	    }
	    
	    public static void writeConfusionMatrixToResultsFile(String fileName, float[][] confusionMatrix) {
	       
	        String lineToWrite = "";
	        try{
	            for(int i=0; i<confusionMatrix.length; ++i){
	                lineToWrite= "";
	                for(int j=0; j< confusionMatrix[i].length; ++j)
	                    lineToWrite += (Float.toString(confusionMatrix[i][j]).concat(" , "));
	       
	                writeToFile(fileName, lineToWrite.concat("\n"));
	            }
	            lineToWrite = "\n"; 
	            writeToFile(fileName, lineToWrite);
	        }catch (Exception e){
	            System.err.println("Error in addConfusionMatrixToResultsFile: " + e.getMessage());
	        }
	    }


	
}
