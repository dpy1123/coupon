package top.devgo.coupon.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import top.devgo.coupon.utils.MongoDBUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;


public class ApiService {

	private static final String mongodbUrl = "mongodb://localhost:27017";
	private static final String dbName = "coupon";

	private enum ApiMethod {
		LIST
	}
	
	public static String processRequest(String collection, String function,
			Map<String, String> paramers) throws JsonProcessingException {
		String jsonResult = null; 
		ApiMethod method = ApiMethod.valueOf(function.toUpperCase());
		switch (method) {
		case LIST:
			String page = paramers.get("page")==null?"1":paramers.get("page");
			String pageSize = paramers.get("size")==null?"10":paramers.get("size");
			String query = paramers.get("query");
			System.out.println(query);
			FindIterable<Document> iterable = MongoDBUtil.find((Bson) JSON.parse(query), mongodbUrl, dbName, collection.toLowerCase())
					.skip((Integer.parseInt(page)-1)*Integer.parseInt(pageSize))
					.limit(Integer.parseInt(pageSize));
			long total = MongoDBUtil.count((Bson) JSON.parse(query), mongodbUrl, dbName, collection.toLowerCase());
			List<Document> list = new ArrayList<Document>();
			for (Document document : iterable) {
				list.add(document);
			}
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("total", total);
			data.put("data", list);
			
			ObjectMapper mapper = new ObjectMapper(); 
			jsonResult = mapper.writeValueAsString(data);
			break;

		default:
			break;
		}
		return jsonResult;
	}

}
