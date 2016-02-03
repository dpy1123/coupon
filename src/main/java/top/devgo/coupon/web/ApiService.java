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
		LIST, LOG
	}
	
	public static String processRequest(String collection, String function,
			Map<String, String> paramers) throws JsonProcessingException {
		String jsonResult = null; 
		ObjectMapper mapper = new ObjectMapper(); 
		ApiMethod method = ApiMethod.valueOf(function.toUpperCase());
		switch (method) {
		// 查询数据
		// /v1/smzdm_data/list?page=1&size=10&query={"article_date_full":{"$gt":"2016-01-20"}} 有冒号要url编码
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
			
			jsonResult = mapper.writeValueAsString(data);
			break;
		//记录用户log
		// /v1/view_log/log?user=dpy1123&article_id=123&site=smzdm&action=dislike/comment/buy
		case LOG:
			String user = paramers.get("user");
			String id = paramers.get("article_id");
			String site = paramers.get("site");
			String action = paramers.get("action");
			
			MongoDBUtil.insertOne(
					new Document("user", user)
						.append("article_id", id)
						.append("site", site)
						.append("action", action),
					mongodbUrl, dbName, collection.toLowerCase()
			);
			
			jsonResult = mapper.writeValueAsString("success");
			break;
		default:
			break;
		}
		return jsonResult;
	}

}
