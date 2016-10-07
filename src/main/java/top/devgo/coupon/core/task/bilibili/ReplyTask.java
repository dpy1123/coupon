package top.devgo.coupon.core.task.bilibili;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;
import top.devgo.coupon.utils.JsonUtil;
import top.devgo.coupon.utils.MongoDBUtil;

public class ReplyTask extends TaskBase {
	private static Logger logger = Logger.getLogger(ReplyTask.class);
	
	private String archiveId;
	private int pageNo;
	
	private String mongoURI;
	private String dbName;
	private boolean updateRecord;//是否更新已有的记录
	
	/**
	 * 初始化抓取任务
	 * @param priority
	 * @param archiveId 
	 * @param pageNo 页数
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 * @param updateRecord 是否更新已有的记录
	 */
	public ReplyTask(int priority, String archiveId, int pageNo, String mongoURI, String dbName, 
			boolean updateRecord) {
		super(priority);
		this.archiveId = archiveId;
		this.pageNo = pageNo;
		this.mongoURI = mongoURI;
		this.dbName = dbName;
		this.updateRecord = updateRecord;
	}

	@Override
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://api.bilibili.com/x/v2/reply")
				.addParameter("jsonp", "json")
				.addParameter("type", "1")
				.addParameter("sort", "0")//按时间0倒序，1升序
				.addParameter("oid", this.archiveId)
				.addParameter("pn", this.pageNo+"")
				.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.build();
		return request;
	}

	@Override
	protected void process(Page page) {
		String htmlStr = getHtmlStr(page);
//		htmlStr = JsonUtil.formateDoubleQuotationMarks(htmlStr);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;  
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		Map<String, Object> result = null;
		try {
			result = mapper.readValue(htmlStr, new TypeReference<Map<String, Object>>() { } );
		} catch (IOException e) {
			logger.error("解析错误", e);
		}
		
		if (result!=null && (int)result.get("code") == 0) {
			Map<String, Object> data = (Map<String, Object>) result.get("data");
			List<Map<String, Object>> replies = (List<Map<String, Object>>) data.get("replies");
			//调整数据
			for (Map<String, Object> d : replies) {
				d.put("_id", d.get("rpid")+"");//指定主键
				d.put("aid", d.remove("oid")+"");//原来关联archive的oid名字换成aid
			}
			page.setData(data);
			
			MongoDBUtil.insertMany(replies, this.updateRecord, this.mongoURI, this.dbName, "reply");
		}
	}

	@Override
	protected List<Task> buildNewTask(Page page) {
		List<Task> newTasks = new ArrayList<Task>();
		Map<String, Object> data = page.getData();
		if (data==null) return null;
		
		Map<String, Object> p = (Map<String, Object>) data.get("page");
		int totalPage = (int) Math.ceil((int)p.get("count")*1.0 / (int)p.get("size"));
		if (this.pageNo < totalPage) {
			newTasks.add(new ReplyTask(this.priority, this.archiveId, this.pageNo+1, this.mongoURI, this.dbName, this.updateRecord));
		}
		
		return newTasks;
	}

}
