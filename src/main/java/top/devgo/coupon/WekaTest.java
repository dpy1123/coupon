package top.devgo.coupon;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import top.devgo.coupon.utils.MongoDBUtil;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

public class WekaTest {
	private static final String mongodbUrl = "mongodb://localhost:27017";
	private static final String dbName = "coupon";
	
	public static void main(String[] args) throws IOException {
		Writer outputWriter = new FileWriter("test.csv") ;
		CsvWriterSettings settings = new CsvWriterSettings();
	    // Sets the character sequence to write for the values that are null.
	    settings.setNullValue("?");
		CsvWriter writer = new CsvWriter(outputWriter, settings);
		
		
		FindIterable<Document> iterable = MongoDBUtil.find(null, mongodbUrl, dbName, "view_log");
		HashMap<String, Object> viewResult = new HashMap<String, Object>();
		for (Document document : iterable) {
			viewResult.put(document.getString("article_id"), document.get("action"));
		}
		
		MongoClient mongoClient = MongoDBUtil.getMongoClient(mongodbUrl);
		MongoDatabase db = mongoClient.getDatabase(dbName);
		
		String[] id = viewResult.keySet().toArray(new String[0]);
		String ids = "";
		for (int i = 0; i < id.length; i++) {
			ids+="'"+id[i]+"'";
			if(i<id.length-1) ids+=",";
		}
		String query = "{'_id':{$in: ["+ids+"]}}";
//		FindIterable<Document> it = MongoDBUtil.find((Bson) JSON.parse(query), mongodbUrl, dbName, "smzdm_data");
		FindIterable<Document> it = MongoDBUtil.find((Bson) JSON.parse("{'article_date_full':{$gt:'2016-02-16'},'article_channel':{$nin:['资讯','原创']}}"),
				mongodbUrl, dbName, "smzdm_data");
		
		boolean header = false;
		for (Document document : it) {
			//TODO ' % 这两个要transcode掉 否则weka报错
			String action = (String) viewResult.get(document.getString("article_id"));
			document.append("action", action==null?"normal":action);
			
			document.remove("article_pic");
			document.remove("article_pic_local");
			document.remove("article_content");
			document.remove("article_content_all");
//			Map<String, Object> data = new HashMap<String, Object>();
//			for (String key : document.keySet()) {
//				data.put(key, document.get(key));
//			}
			
			if(!header){
				writer.writeHeaders(document.keySet());
				header = true;
			}
			
			writer.writeRow(document);
		}
		writer.close();
		
		// Write the record headers of this file
//		writer.writeHeaders("Year", "Make", "Model", "Description", "Price");
		
		// Here we just tell the writer to write everything and close the given output Writer instance.
	}
}
