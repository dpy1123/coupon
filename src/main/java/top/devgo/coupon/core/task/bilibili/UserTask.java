package top.devgo.coupon.core.task.bilibili;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.log4j.Logger;
import top.devgo.coupon.core.dynamic.IpProxy;
import top.devgo.coupon.core.dynamic.UserAgent;
import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;
import top.devgo.coupon.utils.MongoDBUtil;
import top.devgo.coupon.utils.TextUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 抓取公开的用户信息
 * Created by dd on 17/1/23.
 */
public class UserTask extends TaskBase {
    private static Logger logger = Logger.getLogger(FavBoxTask.class);

    private int uId;//用户uid
    private int maxUId;//本次抓取最大的用户uid
    private String mongoURI;
    private String dbName;
    private boolean updateRecord;//是否更新已有的记录


    /**
     * 抓取用户信息
     * @param priority
     * @param uId 用户uid
     * @param maxUId 本次抓取最大的用户uid
     * @param mongoURI
     * @param dbName
     * @param updateRecord 是否更新已有的记录
     */
    public UserTask(int priority, int uId, int maxUId, String mongoURI, String dbName, boolean updateRecord) {
        super(priority);
        this.uId = uId;
        this.maxUId = maxUId;
        this.mongoURI = mongoURI;
        this.dbName = dbName;
        this.updateRecord = updateRecord;
    }

    /**
     * 抓取用户信息
     * @param uId 用户uid
     * @param maxUId 本次抓取最大的用户uid
     * @param mongoURI
     * @param dbName
     * @param updateRecord 是否更新已有的记录
     */
    public UserTask(int uId, int maxUId, String mongoURI, String dbName, boolean updateRecord) {
        this(1, uId, maxUId, mongoURI, dbName, updateRecord);
    }

    @Override
    public HttpUriRequest buildRequest() {
        HttpUriRequest request = RequestBuilder
                .post()
                .setUri("http://space.bilibili.com/ajax/member/GetInfo")
                .addParameter("mid", uId+"")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .setHeader("Referer", "http://space.bilibili.com/"+uId+"/")
                .setHeader("X-Requested-With", "XMLHttpRequest")
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
        if (htmlStr != null)
            htmlStr = TextUtil.decodeUnicode2(htmlStr);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = null;
        try {
            result = mapper.readValue(htmlStr, new TypeReference<Map<String, Object>>() { } );
        } catch (Exception e) {
            logger.error("解析错误", e);
        }


        if (result != null && Boolean.valueOf(String.valueOf(result.get("status")))) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            //调整数据
            data.put("_id", uId+"");//指定主键
            data.put("uid", uId);//增加数值类型mid,便于统计

            page.setData(data);

            List list = new ArrayList(1);
            list.add(data);
            MongoDBUtil.insertMany(list, this.updateRecord, this.mongoURI, this.dbName, "user");
        }
    }

    @Override
    protected List<Task> buildNewTask(Page page) {
        List<Task> newTasks = new ArrayList<Task>();

        //增加新任务
        if (uId < maxUId){
            newTasks.add(new UserTask(this.priority, (uId+1), maxUId, this.mongoURI, this.dbName, this.updateRecord));
        }

        return newTasks;
    }
}
