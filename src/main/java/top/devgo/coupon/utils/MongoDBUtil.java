package top.devgo.coupon.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.DBObject;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;

/**
 *  <p>MongoDB的工具类 单例模式 <p>
 *  <p>MongoDB的Java驱动是线程安全的，对于一般的应用，只要一个Mongo实例即可，Mongo有个内置的连接池（池大小默认为10个）。
 *  
 * @author DD
 */  
public class MongoDBUtil {
	private static MongoClient mongoClient = null;
	
	private MongoDBUtil(){}
	
	/**
	 * 得到一个单例的MongoClient对象
	 * @param mongoClientURI 使用MongoClientURI初始化MongoClient。eg: "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @return
	 */
	public static synchronized MongoClient getMongoClient(String mongoClientURI){
		//无论以何种形式，都不应使用双重检查锁定，因为您不能保证它在任何 JVM 实现上都能顺利运行。
		//对此问题最佳的解决 方案是接受同步或者使用一个 static field 。
		if (mongoClient == null) {
			mongoClient = new MongoClient(new MongoClientURI(mongoClientURI));
		}
		return mongoClient;
	}
	
	/**
	 * 将data插入到mongodb中指定的collection中，如果主键（"_id"）在数据库中存在，则不做任何处理。
	 * @param data
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 * @param collectionName
	 */
	public static void insertMany(List<Map<String, String>> data, String mongoURI, String dbName, String collectionName) {
		int dataSize = data.size();
		if (dataSize > 0) {
			MongoClient mongoClient = MongoDBUtil.getMongoClient(mongoURI);
			MongoDatabase db = mongoClient.getDatabase(dbName);
			MongoCollection<Document> collection = db.getCollection(collectionName);
			List<Document> documents = new ArrayList<Document>();
			for (int i = 0; i < dataSize; i++) {
				Map<String, String> pair = data.get(i);
				Document doc = new Document();
				doc.putAll(pair);
			    documents.add(doc);
			}
			try {
				collection.insertMany(documents);
			} catch (MongoBulkWriteException e) {
				//忽略重复id的exception
				//Bulk write operation error on server localhost:27017. Write errors: [BulkWriteError{index=0, code=11000, message='E11000 duplicate key error collection: coupon.smzdm_comment index: _id_ dup key: { : "7078969" }', details={ }}]. 
				List<BulkWriteError> errors = e.getWriteErrors();
				for (BulkWriteError err : errors) {
					if(ErrorCategory.fromErrorCode(err.getCode()) != ErrorCategory.DUPLICATE_KEY){
						System.out.println(err.getMessage());
					}
				}
			}
		}
	}
	
	/**
	 * 从collection中find数据
	 * @param query 可以为null.如果是原生query语句，可以用JSON.parse(query)转换成Bson
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 * @param collectionName
	 */
	public static FindIterable<Document> find(Bson query, String mongoURI, String dbName, String collectionName) {
		MongoClient mongoClient = MongoDBUtil.getMongoClient(mongoURI);
		MongoDatabase db = mongoClient.getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		return query == null ? collection.find() : collection.find(query);
	}
	
	/**
	 * 从collection中count数据
	 * @param query 可以为null.如果是原生query语句，可以用JSON.parse(query)转换成Bson
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 * @param collectionName
	 */
	public static long count(Bson query, String mongoURI, String dbName, String collectionName) {
		MongoClient mongoClient = MongoDBUtil.getMongoClient(mongoURI);
		MongoDatabase db = mongoClient.getDatabase(dbName);
		MongoCollection<Document> collection = db.getCollection(collectionName);
		return query == null ? collection.count() : collection.count(query);
	}
	
}
