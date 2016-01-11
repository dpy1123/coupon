package top.devgo.coupon.core.task.smzdm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;
import top.devgo.coupon.utils.JsonUtil;
import top.devgo.coupon.utils.MongoDBUtil;
import top.devgo.coupon.utils.TextUtil;

public class SMZDMTask extends TaskBase {

	private String timesort;
	private String mongoURI;
	private String dbName;
	
	/**
	 * 初始化smzdm首页抓取任务
	 * @param timesort 时间戳
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 */
	public SMZDMTask(String timesort, String mongoURI, String dbName) {
		super(1);
		this.timesort = timesort;
		this.mongoURI = mongoURI;
		this.dbName = dbName;
	}
	
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://www.smzdm.com/json_more")
				.addParameter("timesort", getTimesort())
				.setHeader("Host", "www.smzdm.com")
				.setHeader("Referer", "http://www.smzdm.com/")
				.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.setHeader("X-Requested-With", "XMLHttpRequest").build();
		return request;
	}


	@Override
	protected Page buildPage(CloseableHttpResponse response) {
		Page page = new Page();
		String htmlStr = getHtmlStr(response);
		page.setOriginalHtml(htmlStr);
		return page;
	}

	@Override
	protected void process(Page page) {
		String htmlStr = page.getOriginalHtml();
		htmlStr = TextUtil.decodeUnicode(htmlStr);
		htmlStr = JsonUtil.formateDoubleQuotationMarks(htmlStr);
		List<Map<String, String>> data = extractData(htmlStr);
		page.setData(data);
		
		int dataSize = data.size();
		if (dataSize > 0) {
			MongoClient mongoClient = MongoDBUtil.getMongoClient(mongoURI);
			MongoDatabase db = mongoClient.getDatabase(dbName);
			MongoCollection<Document> collection = db.getCollection("smzdm");
			List<Document> documents = new ArrayList<Document>();
			for (int i = 0; i < dataSize; i++) {
				Map<String, String> pair = data.get(i);
				Document doc = new Document();
				doc.putAll(pair);
			    documents.add(doc);
			}
			collection.insertMany(documents);
		}
	}

	@Override
	protected List<Task> buildNewTask(Page page) {
		List<Map<String, String>> data = page.getData();
		List<Task> newTasks = new ArrayList<Task>();
		if (data.size() > 0) {
			String timesort = data.get(data.size()-1).get("timesort");
			String article_date = data.get(data.size()-1).get("article_date");
			if(article_date.length() <= 5){//"article_date":"22:31",只要当日的
				SMZDMTask task = new SMZDMTask(timesort, getMongoURI(), getDbName());
				newTasks.add(task);
			}
		}
		return newTasks;
	}


	public String getTimesort() {
		return timesort;
	}

	public void setTimesort(String timesort) {
		this.timesort = timesort;
	}

	public String getMongoURI() {
		return mongoURI;
	}

	public void setMongoURI(String mongoURI) {
		this.mongoURI = mongoURI;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
}
