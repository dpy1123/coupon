package top.devgo.coupon.core.dynamic.task;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import top.devgo.coupon.core.dynamic.IpProxy;
import top.devgo.coupon.core.dynamic.UserAgent;
import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;
import top.devgo.coupon.utils.MongoDBUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * http://www.xicidaili.com/nn/1
 * http://www.xicidaili.com/nt/1
 * http://www.xicidaili.com/wn/1
 * http://www.xicidaili.com/wt/1
 * 这个要上代理
 * Created by dd on 17/3/22.
 */
public class XiciDaili extends TaskBase {

    private String url;
    private Type type;
    private int page;
    private int totalPage;
    private String mongoURI;
    private String dbName;

    public enum Type{
        /**
         * 国内高匿
         */
        nn,
        /**
         * 国内透明
         */
        nt,
        /**
         * 国外高匿
         */
        wn,
        /**
         * 国外透明
         */
        wt
    }

    public XiciDaili(XiciDaili.Type type, int page, String mongoURI, String dbName){
        super(1);
        this.type = type;
        this.url = "http://www.xicidaili.com/"+type.name()+"/";
        this.page = page;
        this.totalPage = page;
        this.mongoURI = mongoURI;
        this.dbName = dbName;
    }

    @Override
    public HttpUriRequest buildRequest() {
        HttpUriRequest request = RequestBuilder
                .get()
                .setUri(url + page)
                .setHeader("User-Agent", UserAgent.getUA())
                .setConfig(RequestConfig.custom()
                        .setConnectTimeout(5*1000)
                        .setSocketTimeout(5*1000)
                        .setProxy(HttpHost.create(IpProxy.getProxyHost(mongoURI)))
                        .build())
                .build();
        return request;
    }

    @Override
    protected void process(Page page) {
        String htmlStr = getHtmlStr(page);

        List list = Jsoup.parse(htmlStr).select("#ip_list > tbody > tr").stream()
                .filter(element -> element.select("td").size() > 0)
                .map(this::convertToMap)
                .collect(Collectors.toList());

        totalPage = Integer.parseInt(Jsoup.parse(htmlStr).select("#body > div.pagination > :nth-last-child(2)").text());

        MongoDBUtil.insertMany(list, false, this.mongoURI, this.dbName, "ip_pool");
    }

    public Map<String, Object> convertToMap(Element element) {
        Map<String, Object> map = new HashMap<>();
        String ip = element.child(1).text();
        String port = element.child(2).text();
        String addr = element.child(3).text();
        String type = element.child(4).text();
        map.put("_id", ip + ":" + port);
        map.put("ip", ip);
        map.put("port", port);
        map.put("type", type);
        map.put("addr", addr);
        map.put("state", "grab");
        map.put("source", url);
        map.put("fetch_date", new Date());
        return map;
    }

    @Override
    protected List<Task> buildNewTask(Page page) {
        List<Task> newTasks = new ArrayList<Task>();

        //增加新任务
        if (this.page < totalPage){
            newTasks.add(new XiciDaili(type, this.page+1, this.mongoURI, this.dbName));
        }

        return newTasks;
    }
}
