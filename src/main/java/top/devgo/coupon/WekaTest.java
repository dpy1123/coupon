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
import weka.core.Instances;
import weka.core.converters.CSVLoader;

import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class WekaTest {
	private static final String mongodbUrl = "mongodb://localhost:27017";
	private static final String dbName = "coupon";
	
	public static void main(String[] args) throws IOException, Exception {
		String path = "test.csv";
		buildCsvFile(path);
		
//		evaluateClassifier(path);
	}

	private static void evaluateClassifier(String path) throws IOException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException, Exception {
		// load CSV
	    CSVLoader loader = new CSVLoader();
	    loader.setSource(new File(path));
	    Instances data = loader.getDataSet();
	    data.setClassIndex(data.numAttributes() - 1);
	    
	    Classifier cf  = (Classifier) Class.forName("weka.classifiers.trees.RandomForest").newInstance(); 
//	    cf.buildClassifier(data);
	    
	    Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(cf, data, 10, new Random(1));
        
        System.out.println(eval.toSummaryString("Results\n", false));
        System.out.println(eval.toClassDetailsString());
        System.out.println(eval.toMatrixString());
	}

	private static void buildCsvFile(String path) throws IOException {
		Writer outputWriter = new FileWriter(path) ;
		CsvWriterSettings settings = new CsvWriterSettings();
	    // Sets the character sequence to write for the values that are null.
	    settings.setNullValue("?");
		CsvWriter writer = new CsvWriter(outputWriter, settings);
		
		
		FindIterable<Document> iterable = MongoDBUtil.find(null, mongodbUrl, dbName, "view_log");
		HashMap<String, Object> viewResult = new HashMap<String, Object>();
		for (Document document : iterable) {
			viewResult.put(document.getString("article_id"), document.get("action"));
		}
		
//		String[] id = viewResult.keySet().toArray(new String[0]);
//		String ids = "";
//		for (int i = 0; i < id.length; i++) {
//			ids+="'"+id[i]+"'";
//			if(i<id.length-1) ids+=",";
//		}
//		String query = "{'_id':{$in: ["+ids+"]}}";
//		FindIterable<Document> it = MongoDBUtil.find((Bson) JSON.parse(query), mongodbUrl, dbName, "smzdm_data");
		FindIterable<Document> it = MongoDBUtil.find((Bson) JSON.parse("{'article_date_full':{$gt:'2016-02-16'},'article_channel':{$nin:['资讯','原创']}}"),
				mongodbUrl, dbName, "smzdm_data");
		
		boolean header = false;
		for (Document document : it) {
			String action = (String) viewResult.get(document.getString("article_id"));
			document.append("action", action==null?"normal":action);
			
			document.remove("article_pic");
			document.remove("article_pic_local");
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
			
			writer.writeRow(document);
		}
		writer.close();
	}
}
