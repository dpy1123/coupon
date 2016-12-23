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
import top.devgo.coupon.utils.TextUtil;

/**
 * 抓取bilibili的archive（即av）
 * @author DD
 *
 */
public class ArchiveTask extends TaskBase {
	private static Logger logger = Logger.getLogger(ArchiveTask.class);
	
	private String tId;
	private int pageNo;
	
	private String mongoURI;
	private String dbName;
	private boolean updateRecord;//是否更新已有的记录
	private boolean fetchComment;//是否抓取相关的评论
	
	/**
	 * 初始化抓取任务
	 * @param priority
	 * @param tId 
	 * @param pageNo 起始页数
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 * @param updateRecord 是否更新已有的记录
	 * @param fetchComment 是否抓取相关的评论
	 */
	public ArchiveTask(int priority, String tId, int pageNo, String mongoURI, String dbName, 
			boolean updateRecord, boolean fetchComment) {
		super(priority);
		this.tId = tId;
		this.pageNo = pageNo;
		this.mongoURI = mongoURI;
		this.dbName = dbName;
		this.updateRecord = updateRecord;
		this.fetchComment = fetchComment;
	}

	@Override
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://api.bilibili.com/archive_rank/getarchiverankbypartion")
				.addParameter("type", "json")
				.addParameter("tid", this.tId)
				.addParameter("pn", this.pageNo+"")
				.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.build();
		return request;
	}

	@Override
	protected void process(Page page) {
		String htmlStr = getHtmlStr(page);
		htmlStr = TextUtil.decodeUnicode2(htmlStr);
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
			List<Map<String, Object>> archives = (List<Map<String, Object>>) data.get("archives");
			//调整数据
			for (Map<String, Object> d : archives) {
				d.put("_id", d.get("aid")+"");//指定主键
			}
			page.setData(data);
			
			MongoDBUtil.insertMany(archives, this.updateRecord, this.mongoURI, this.dbName, "archive");
		}
	}

	@Override
	protected List<Task> buildNewTask(Page page) {
		List<Task> newTasks = new ArrayList<Task>();
		Map<String, Object> data = page.getData();
		if (data==null) return null;
		
		List<Map<String, Object>> archives = (List<Map<String, Object>>) data.get("archives");
		for (int i = 0; i < archives.size(); i++) {
			if (fetchComment) {
				//增加相应的评论的抓取任务
				int archiveId = (int) archives.get(i).get("aid");
				newTasks.add(new ReplyTask(this.priority+1, archiveId+"", 1, this.mongoURI, this.dbName, this.updateRecord));
				
			}
		}
		
		Map<String, Object> p = (Map<String, Object>) data.get("page");
		int totalPage = (int) Math.ceil((int)p.get("count")*1.0 / (int)p.get("size"));
		if (this.pageNo < totalPage) {
			newTasks.add(new ArchiveTask(this.priority, this.tId, this.pageNo+1, this.mongoURI, this.dbName, this.updateRecord, this.fetchComment));
		}
		
		return newTasks;
	}

}
