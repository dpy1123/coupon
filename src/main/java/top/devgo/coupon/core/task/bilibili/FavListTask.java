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
 * 抓取收藏夹下的记录
 * Created by dd on 17/1/23.
 */
public class FavListTask extends TaskBase {
    private static Logger logger = Logger.getLogger(FavListTask.class);

    private int uId;//用户uid
    private int fId;//fav_box id
    private int pageNo;
    private int pageSize;
    private String mongoURI;
    private String dbName;
    private boolean updateRecord;//是否更新已有的记录


    /**
     * 抓取收藏夹下的记录
     * @param priority
     * @param uId 用户uid
     * @param fId fav_box id
     * @param pageNo 从1开始
     * @param pageSize
     * @param mongoURI
     * @param dbName
     * @param updateRecord 是否更新已有的记录
     */
    public FavListTask(int priority, int uId, int fId, int pageNo, int pageSize, String mongoURI,
                       String dbName, boolean updateRecord) {
        super(priority);
        this.uId = uId;
        this.fId = fId;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.mongoURI = mongoURI;
        this.dbName = dbName;
        this.updateRecord = updateRecord;
    }

    @Override
    public HttpUriRequest buildRequest() {
        HttpUriRequest request = RequestBuilder
                .get()
                .setUri("http://space.bilibili.com/ajax/fav/getList")
                .addParameter("mid", uId+"")
                .addParameter("fid", fId+"")
                .addParameter("pid", pageNo+"")
                .addParameter("pagesize", pageSize+"")
                .addParameter("kw", "")
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
            result = mapper.readValue(htmlStr, new TypeReference<Map<String, Object>>() { });
        } catch (IOException e) {
            logger.error("解析错误", e);
        }

        if (result != null && (boolean) result.get("status")) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            int count = (int) data.get("count");
            List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("vlist");
            if (count > 0){
                //调整数据
                for (Map item : list){
                    item.put("_id", fId+"/"+item.get("aid"));
                    item.put("uid", uId);//增加uid即mid
                    item.put("fav_box_id", fId);
                }

                page.setData(data);

                MongoDBUtil.insertMany(list, this.updateRecord, this.mongoURI, this.dbName, "fav_list");
            }
        }
    }

    @Override
    protected List<Task> buildNewTask(Page page) {
        List<Task> newTasks = new ArrayList<Task>();
        Map<String, Object> data = page.getData();
        if (data==null) return null;

        int totalPage = (int) data.get("pages");
        if (this.pageNo < totalPage) {
            newTasks.add(new FavListTask(this.priority, this.uId, this.fId, this.pageNo+1, this.pageSize, this.mongoURI, this.dbName, this.updateRecord));
        }

        return newTasks;
    }
}
