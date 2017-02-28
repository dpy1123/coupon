package top.devgo.coupon.core.dynamic.task;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import top.devgo.coupon.core.dynamic.UserAgent;
import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;
import top.devgo.coupon.utils.DateUtil;
import top.devgo.coupon.utils.MongoDBUtil;

import java.util.*;

/**
 * http://cn-proxy.com/
 * http://cn-proxy.com/archives/218
 * Created by dd on 17/2/28.
 */
public class CnProxy extends TaskBase {

    private String url;
    private String mongoURI;
    private String dbName;


    private CnProxy(String url, String mongoURI, String dbName) {
        super(1);
        this.url = url;
        this.mongoURI = mongoURI;
        this.dbName = dbName;
    }

    public CnProxy(String mongoURI, String dbName) {
        this("http://cn-proxy.com/", mongoURI, dbName);
    }

    @Override
    public HttpUriRequest buildRequest() {
        HttpUriRequest request = RequestBuilder
                .get()
                .setUri(url)
                .setHeader("User-Agent", UserAgent.getUA())
                .build();
        return request;
    }

    @Override
    protected void process(Page page) {
        String htmlStr = getHtmlStr(page);
        Document doc = Jsoup.parse(htmlStr);

        List list = new ArrayList();
        Elements lists = doc.select("tbody > tr");
        for (int i = 0; i < lists.size(); i++) {
            Element tr = lists.get(i);
            String ip = tr.select("td").first().text();
            String port = tr.select("td").get(1).text();

            Map<String, Object> data = new HashMap<>();
            data.put("_id", ip+":"+port);
            data.put("ip", ip);
            data.put("port", port);
            if (!url.equals("http://cn-proxy.com/archives/218")) {
                data.put("type", "高度匿名");
                data.put("addr", tr.select("td").get(2).text());
            } else {
                data.put("type", tr.select("td").get(2).text());
                data.put("addr", tr.select("td").get(3).text());
            }
            data.put("state", "grab");
            data.put("source", "cn-proxy.com");
            data.put("fetch_date", DateUtil.getDateString(new Date()));
            data.put("validate_date", "");

            list.add(data);
        }
//        page.setData(list);
        MongoDBUtil.insertMany(list, true, this.mongoURI, this.dbName, "ip_pool");
    }

    @Override
    protected List<Task> buildNewTask(Page page) {
        List<Task> newTasks = new ArrayList<Task>();

        //增加新任务
        if (!url.equals("http://cn-proxy.com/archives/218")) {
            newTasks.add(new CnProxy("http://cn-proxy.com/archives/218", this.mongoURI, this.dbName));
        }

        return newTasks;
    }
}
