package top.devgo.coupon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Random;

import org.bson.Document;
import org.bson.conversions.Bson;

import top.devgo.coupon.utils.MongoDBUtil;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class WekaTest {
	private static final String mongodbUrl = "mongodb://localhost:27017";
	private static final String dbName = "coupon";
	
	public static void main(String[] args) throws IOException, Exception {
		buildTrainingCsvFile("train.csv");
		buildUnlabeledCsvFile("unlabeled.csv");
		
		Classifier cf = evaluateClassifier("train.csv");
		
		// load unlabeled data and set class attribute
		CSVLoader loader = new CSVLoader();
	    loader.setSource(new File("unlabeled.csv"));
	    Instances data = loader.getDataSet();
	    
	    int classPos = data.numAttributes() - 1;
	    data.deleteAttributeAt(classPos);
	    FastVector classAttr = new FastVector();
//	    classAttr.addElement("normal");
	    classAttr.addElement("view");
	    classAttr.addElement("dislike");
	    classAttr.addElement("buy");
	    data.insertAttributeAt(new Attribute("action", classAttr), classPos);
	    
	    data.setClassIndex(data.numAttributes() - 1);
	    System.out.println(data.classAttribute());
	    // create copy
	    Instances result = new Instances(data);
	    result.setClassIndex(result.numAttributes() - 1);
	    System.out.println(result.classAttribute());
	    // label instances
	    for (int i = 0; i < data.numInstances(); i++) {
	    	try {
				double clsLabel = cf.classifyInstance(data.instance(i));
				if(clsLabel>0)System.out.println(clsLabel);
				result.instance(i).setClassValue(clsLabel);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	// save newly labeled data
    	DataSink.write("labeled.csv", result);
	    
		
	}

	private static Classifier evaluateClassifier(String path) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, Exception {
		// load CSV
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(path));
	    Instances data = loader.getDataSet();
	    data.setClassIndex(data.numAttributes() - 1);
	    
	    //filtering
	    /*
	     * 1	normal	1900
	     * 2	view	11
	     * 3	dislike	25
	     * 4	buy		16
	     */
//	    SMOTE smote = new SMOTE();
//	    smote.setClassValue("2");//view
//	    smote.setPercentage(50*100);
//	    smote.setInputFormat(data);
//	    Instances tmpData = Filter.useFilter(data, smote);
//	    smote.setClassValue("3");//dislike
//	    smote.setPercentage(10*100);
//	    smote.setInputFormat(tmpData);
//	    tmpData = Filter.useFilter(tmpData, smote);
//	    smote.setClassValue("4");//buy
//	    smote.setPercentage(10*100);
//	    smote.setInputFormat(tmpData);
//	    Instances filteredData = Filter.useFilter(tmpData, smote);
	    Instances filteredData = data;
	    
//	    Classifier cf  = (Classifier) Class.forName("weka.classifiers.trees.RandomForest").newInstance(); 
	    Classifier cf  = (Classifier) Class.forName("weka.classifiers.trees.J48").newInstance(); 
//	    Classifier cf  = (Classifier) Class.forName("weka.classifiers.bayes.NaiveBayes").newInstance(); 
	    cf.buildClassifier(filteredData);
	    
	    Evaluation eval = new Evaluation(filteredData);
        eval.crossValidateModel(cf, filteredData, 10, new Random(1));
        
        System.out.println(eval.toSummaryString("Results\n", false));
        System.out.println(eval.toClassDetailsString());
        System.out.println(eval.toMatrixString());
        
        return cf;
	}

	private static void buildUnlabeledCsvFile(String path) throws IOException {
		Writer outputWriter = new FileWriter(path) ;
		CsvWriterSettings settings = new CsvWriterSettings();
	    // Sets the character sequence to write for the values that are null.
	    settings.setNullValue("?");
		CsvWriter writer = new CsvWriter(outputWriter, settings);
		
		
		FindIterable<Document> it =  MongoDBUtil.find((Bson) JSON.parse("{'article_date_full':{$gt:'2016-03-09'}}"), mongodbUrl, dbName, "smzdm_data")
				.sort((Bson) JSON.parse("{'create_date':-1}"))
				.limit(20);
		
		
		boolean header = false;
		for (Document document : it) {
			document.append("action", null);
			
			document.remove("article_pic");
			document.remove("article_pic_local");
			document.remove("article_pic_style");
			document.remove("article_content");
			document.remove("article_content_all");
			
			// ' % 这两个要transcode掉 否则weka报错
			for (String key : document.keySet()) {
				if(document.getString(key)!=null)
					document.put(key, document.getString(key).replaceAll("'", " ").replaceAll("%", "-"));
			}
			
			if(!header){
				writer.writeHeaders(document.keySet());
				header = true;
			}
			writer.writeRow(document);
		}
		writer.close();
	}
	
	private static void buildTrainingCsvFile(String path) throws IOException {
		Writer outputWriter = new FileWriter(path) ;
		CsvWriterSettings settings = new CsvWriterSettings();
		// Sets the character sequence to write for the values that are null.
		settings.setNullValue("?");
		CsvWriter writer = new CsvWriter(outputWriter, settings);
		
		int views=0;
		FindIterable<Document> iterable = MongoDBUtil.find((Bson) JSON.parse("{'user':'dpy1123'}"), mongodbUrl, dbName, "view_log");
		HashMap<String, Object> viewResult = new HashMap<String, Object>();
		for (Document document : iterable) {
			views++;
			viewResult.put(document.getString("article_id"), document.get("action"));
		}
		System.out.println(views);
		System.out.println(viewResult.size());
		
//		String[] id = viewResult.keySet().toArray(new String[0]);
//		String ids = "";
//		for (int i = 0; i < id.length; i++) {
//			ids+="'"+id[i]+"'";
//			if(i<id.length-1) ids+=",";
//		}
//		String query = "{'_id':{$in: ["+ids+"]}}";
//		FindIterable<Document> it = MongoDBUtil.find((Bson) JSON.parse(query), mongodbUrl, dbName, "smzdm_data");
//		FindIterable<Document> it = MongoDBUtil.find((Bson) JSON.parse("{'article_date_full':{$gt:'2016-02-18'},'article_channel':{$nin:['资讯','原创']}}"),
		FindIterable<Document> it = MongoDBUtil.find((Bson) JSON.parse("{'article_date_full':{$gt:'2016-02-18'}}"),
				mongodbUrl, dbName, "smzdm_data");
		
		boolean header = false;
		for (Document document : it) {
			String action = (String) viewResult.get(document.getString("article_id"));
			document.append("action", action==null?"normal":action);
			
			document.remove("article_pic");
			document.remove("article_pic_local");
			document.remove("article_pic_style");
			document.remove("article_content");
			document.remove("article_content_all");
			
			// ' % 这两个要transcode掉 否则weka报错
			for (String key : document.keySet()) {
				document.put(key, document.getString(key).replaceAll("'", " ").replaceAll("%", "-"));
			}
			
			if(!header){
				writer.writeHeaders(document.keySet());
				header = true;
			}
			if(action!=null)//去掉normal
			writer.writeRow(document);
		}
		writer.close();
	}
}
