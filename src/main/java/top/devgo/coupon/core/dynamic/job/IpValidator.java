package top.devgo.coupon.core.dynamic.job;

import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import top.devgo.coupon.utils.DateUtil;
import top.devgo.coupon.utils.MongoDBUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 定时任务,验证ip是否可用
 * 每日
 * Created by dd on 17/2/24.
 */
public class IpValidator implements Runnable {

    private static Logger logger = Logger.getLogger(IpValidator.class.getName());

    private String mongoUrl;
    private String dbName;

    public IpValidator(String mongoUrl, String dbName) {
        this.mongoUrl = mongoUrl;
        this.dbName = dbName;
    }

    @Override
    public void run() {
        String queryString = "{" +
                "   $or:[" +
                "       {'state':{$eq: 'grab'}}," +
                "       {'validate_date':{$lt: '"+DateUtil.getBeginOfDay(new Date())+"'}}" +
                "   ]" +
                "}";
        FindIterable<Document> iterable = MongoDBUtil.find((Bson) JSON.parse(queryString), mongoUrl, dbName, "ip_pool");
//        List<Document> list = new ArrayList<Document>();
//        for (Document d: iterable) {
//            list.add(d);
//        }
        List<Document> list = StreamSupport.stream(iterable.spliterator(), false)
                .collect(Collectors.toList());

        List result = list.parallelStream()
//                .filter(ProxyHostValidator::isValid)
                .map(ProxyHostValidator::mark)
                .map(ProxyHostValidator::convertToMap)
                .collect(Collectors.toList());

        MongoDBUtil.insertMany(result, true, mongoUrl, dbName, "ip_pool");
        logger.info("本次验证了 "+result.size()+" 条记录");
    }


    public static class ProxyHostValidator {
        public static boolean isValid(Document doc) {
            try {
                Response rep = Jsoup.connect("http://httpbin.org/ip")
                        .method(Method.GET)
                        .ignoreContentType(true)
                        .proxy(doc.getString("ip"), Integer.parseInt(doc.getString("port")))
                        .execute();
                return rep.statusCode() == 200;
            } catch (IOException e) {
            }
            return false;
        }

        private static long timing(Document doc){
            Date s = new Date();
            try {
                Response rep = Jsoup.connect("http://www.baidu.com")
                        .method(Method.GET)
                        .ignoreContentType(true)
                        .execute();
                if (rep.statusCode() == 200)
                    return new Date().getTime() - s.getTime();
            } catch (IOException e) {
            }
            return -1;
        }

        public static Document mark(Document doc){
            doc.put("state", isValid(doc)?"valid":"invalid");
            doc.put("validate_date", new Date());
            doc.put("timing", timing(doc));
            return doc;
        }

        public static Map<String, Object> convertToMap(Document doc) {
            Map<String, Object> map = new HashMap<>();
            doc.keySet().stream().forEach(k -> map.put(k, doc.get(k)));
            return map;
        }
    }

}
