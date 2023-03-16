package data;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.*;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.Remove;

public class kNN extends IBk {
	    protected DistanceFunction dist;
	    double[][] distMatrix;
	    boolean storeDistance;

	   public kNN() {
	   //Defaults to Euclidean distance 1NN without attribute normalisation		
	        super();
	        super.setKNN(1);
	        EuclideanDistance ed = new EuclideanDistance();
	        ed.setDontNormalize(true);
	    }

	    public double distance(Instance first, Instance second) {
	        return dist.distance(first, second);
	    }

	    //Only use with a Euclidean distance method
	    public void normalise(boolean v) {
	        if (dist instanceof NormalizableDistance)
	            ((NormalizableDistance) dist).setDontNormalize(!v);
	        else
	            throw new RuntimeException("ERROR in kNN classifier when setting dist. Distance function " + dist.getClass().getSimpleName() + " is not normalisable");
	    }

	    public double[] getPredictions(Instances test) throws Exception {
	        double[] pred = new double[test.numInstances()];
	        for (int i = 0; i < test.numInstances(); i++)
	            pred[i] = classifyInstance(test.instance(i));
	        return pred;
	    }

	    public static void main(String[] args) {
	     
	    	IBk ibk = new IBk(100);
	        ibk.setCrossValidate(true);
	       
	        try {
	        InputStream fis;
	        InputStream fisTest;
	    	fis = new FileInputStream("/Users/amandadrielly/eclipse-workspace/SeAct/src/train.arff");		
	    	InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
	    	fisTest = new FileInputStream("/Users/amandadrielly/eclipse-workspace/SeAct/src/test.arff");		
	    	InputStreamReader isrTest = new InputStreamReader(fisTest, Charset.forName("UTF-8"));
	    	 
	    	// load CSV
	        /*CSVLoader loader = new CSVLoader();
	        loader.setSource(new File();
	        Instances data = loader.getDataSet();

	        // save ARFF
	        ArffSaver saver = new ArffSaver();
	        saver.setInstances(data);
	        saver.setFile(new File(args[1]));
	        saver.setDestination(new File(args[1]));
	        saver.writeBatch();*/
	    	
	        Instances train = new Instances(isr);
	        Instances test = new Instances(isrTest);
	        DecimalFormat df = new DecimalFormat("####.###");
	        
	        if (train.classIndex() == -1) {
	            System.out.println("reset index...");
	            int cIdx=train.numAttributes()-1;
	            train.setClassIndex(cIdx);
	           
	        }
	        if (test.classIndex() == -1) {
	            System.out.println("reset index...");
	            int cIdx=test.numAttributes()-1;
	            test.setClassIndex(cIdx);
	           
	        }
	        
	        ibk.buildClassifier(train);
	        System.out.println(ibk);
	        
	        double acc = accuracy(test, ibk);
	        System.out.println("ibk accuracy = " + df.format(acc));
	        } catch (Exception e) {
	                e.printStackTrace();
	                System.exit(0);
	            }
	       
	    }


  public static double accuracy(Instances test, Classifier c){
	double a=0;
	int size=test.numInstances();
	Instance d;
	double predictedClass,trueClass;
	for(int i=0;i<size;i++){
		d=test.instance(i);
		try{
			predictedClass=c.classifyInstance(d);
			trueClass=d.classValue();
			if(trueClass==predictedClass)
				a++;

		}catch(Exception e){
             System.out.println(" Error with instance "+i+" with Classifier "+c.getClass().getName()+" Exception ="+e);
             e.printStackTrace();
             System.exit(0);
        }
	}
	return a/size;
}	


}
