package top.devgo.coupon.core.dynamic;


import com.mongodb.client.FindIterable;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;
import top.devgo.coupon.utils.MongoDBUtil;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by dd on 17/2/22.
 */
public class IpProxy {

    private IpProxy() {
    }

    /**
     * 获取可用的代理ip
     * @return
     * @param mongoURI
     */
    public static String getProxyHost(String mongoURI) {
        FindIterable<Document> iterable = MongoDBUtil.find((Bson) JSON.parse("{'state':{$eq: 'valid'}}"),
                mongoURI, "proxy", "ip_pool");
        List<String> list = StreamSupport.stream(iterable.spliterator(), false)
                .map(doc -> doc.getString("_id"))
                .collect(Collectors.toList());
        return list.get(new Random().nextInt(list.size()));
    }

}
