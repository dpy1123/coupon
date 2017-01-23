package top.devgo.coupon.core.task.bilibili;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.log4j.Logger;
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
 * 抓取收藏夹, 只能抓取公开的
 * Created by dd on 17/1/23.
 */
public class FavBoxTask extends TaskBase {
    private static Logger logger = Logger.getLogger(FavBoxTask.class);

    private int uId;//用户uid
    private int maxUId;//本次抓取最大的用户uid
    private String mongoURI;
    private String dbName;
    private boolean updateRecord;//是否更新已有的记录
    private boolean fetchFavList;//是否抓取收藏夹下的记录


    /**
     * 抓取收藏夹任务
     * @param priority
     * @param uId 用户uid
     * @param maxUId 本次抓取最大的用户uid
     * @param mongoURI
     * @param dbName
     * @param updateRecord 是否更新已有的记录
     * @param fetchFavList 是否抓取收藏夹下的记录
     */
    public FavBoxTask(int priority, int uId, int maxUId, String mongoURI, String dbName,
                      boolean updateRecord, boolean fetchFavList) {
        super(priority);
        this.uId = uId;
        this.maxUId = maxUId;
        this.mongoURI = mongoURI;
        this.dbName = dbName;
        this.updateRecord = updateRecord;
        this.fetchFavList = fetchFavList;
    }

    /**
     * 抓取收藏夹任务
     * @param uId 用户uid
     * @param maxUId 本次抓取最大的用户uid
     * @param mongoURI
     * @param dbName
     * @param updateRecord 是否更新已有的记录
     * @param fetchFavList 是否抓取收藏夹下的记录
     */
    public FavBoxTask(int uId, int maxUId, String mongoURI, String dbName, boolean updateRecord, boolean fetchFavList) {
        this(1, uId, maxUId, mongoURI, dbName, updateRecord, fetchFavList);
    }

    @Override
    public HttpUriRequest buildRequest() {
        HttpUriRequest request = RequestBuilder
                .get()
                .setUri("http://space.bilibili.com/ajax/fav/getboxlist")
                .addParameter("mid", uId+"")
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
                .build();
        return request;
    }

    @Override
    protected void process(Page page) {
        String htmlStr = getHtmlStr(page);
        htmlStr = TextUtil.decodeUnicode2(htmlStr);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = null;
        try {
            result = mapper.readValue(htmlStr, new TypeReference<Map<String, Object>>() { } );
        } catch (IOException e) {
            logger.error("解析错误", e);
        }

        if (result!=null && (boolean)result.get("status")) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            int count = (int) data.get("count");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            if (count > 0){
                //调整数据
                for (Map item : list){
                    item.put("_id", item.get("fav_box")+"");
                    item.put("uid", uId);//增加uid即mid
                    item.remove("videos");//去掉预览用的videos信息
                }

                page.setData(data);

                MongoDBUtil.insertMany(list, this.updateRecord, this.mongoURI, this.dbName, "fav_box");
            }
        }
    }

    @Override
    protected List<Task> buildNewTask(Page page) {
        List<Task> newTasks = new ArrayList<Task>();
        Map<String, Object> data = page.getData();
        if (data!=null) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("list");
            for (int i = 0; i < list.size(); i++) {
                if (fetchFavList) {
                    int fId = (int) list.get(i).get("fav_box");
                    newTasks.add(new FavListTask(this.priority+1, uId, fId, 1, 30, this.mongoURI, this.dbName, this.updateRecord));
                }
            }
        }

        //增加新任务
        if (uId < maxUId){
            newTasks.add(new FavBoxTask(this.priority, (uId+1), maxUId, this.mongoURI, this.dbName, this.updateRecord, this.fetchFavList));
        }

        return newTasks;
    }
}
