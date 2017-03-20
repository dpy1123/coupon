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
 * http://www.cz88.net/proxy
 * http://www.cz88.net/proxy/http_2.shtml --> /http_10.shtml
 * 每3日更新,但基本是同样的835个
 * Created by dd on 17/2/23.
 */
public class Cz88 extends TaskBase {

    private int page;
    private String mongoURI;
    private String dbName;

    private Cz88(int page, String mongoURI, String dbName) {
        super(1);
        this.page = page;
        this.mongoURI = mongoURI;
        this.dbName = dbName;
    }

    public Cz88(String mongoURI, String dbName) {
        this(1, mongoURI, dbName);
    }

    private String buildUrl(int page){
        if (page == 1){
            return "http://www.cz88.net/proxy/";
        }else{
            return "http://www.cz88.net/proxy/http_"+page+".shtml";
        }
    }

    @Override
    public HttpUriRequest buildRequest() {
        HttpUriRequest request = RequestBuilder
                .get()
                .setUri(buildUrl(this.page))
                .setHeader("User-Agent", UserAgent.getUA())
                .build();
        return request;
    }

    @Override
    protected void process(Page page) {
        String htmlStr = getHtmlStr(page, "gb18030");
        Document doc = Jsoup.parse(htmlStr);

        List list = new ArrayList();
        Elements lists = doc.select("#boxright > div > ul > li");
        for (int i = 1; i < lists.size(); i++) {
            Element li = lists.get(i);
            String ip = li.select("div.ip").first().text();
            String port = li.select("div.port").first().text();
            String type = li.select("div.type").first().text();
            String addr = li.select("div.addr").first().text();

            Map<String, Object> data = new HashMap<>();
            data.put("_id", ip+":"+port);
            data.put("ip", ip);
            data.put("port", port);
            data.put("type", type);
            data.put("addr", addr);
            data.put("state", "grab");
            data.put("source", "cz88.net");
            data.put("fetch_date", new Date());
//            data.put("validate_date", "");

            list.add(data);
        }
//        page.setData(list);
        MongoDBUtil.insertMany(list, false, this.mongoURI, this.dbName, "ip_pool");
    }

    @Override
    protected List<Task> buildNewTask(Page page) {
        List<Task> newTasks = new ArrayList<Task>();

        //增加新任务
        if (this.page <= 10){
            newTasks.add(new Cz88((this.page+1), this.mongoURI, this.dbName));
        }

        return newTasks;
    }
}
