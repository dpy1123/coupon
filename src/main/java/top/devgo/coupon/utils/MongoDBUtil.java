package top.devgo.coupon.utils;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

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
	
}
