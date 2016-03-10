package top.devgo.coupon.core.task.smzdm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;
import top.devgo.coupon.utils.DateUtil;
import top.devgo.coupon.utils.MongoDBUtil;

public class SMZDMCommentTask extends TaskBase {
	
	private String url;
	private String mongoURI;
	private String dbName;
	
	private int currentPage = 0;
	private int totalPage = 0;
	
	/**
	 * 评论对应的商品id
	 */
	private String productId;
	
	
	/**
	 * 初始化smzdm商品详细页评论抓取任务
	 * @param productId 评论对应的商品id
	 * @param url "http://www.smzdm.com/p/744723/p1"
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 */
	public SMZDMCommentTask(int priority, String productId, String url, String mongoURI, String dbName) {
		super(priority);
		this.productId = productId;
		this.url = url;
		this.mongoURI = mongoURI;
		this.dbName = dbName;
	}
	
	/**
	 * 初始化smzdm商品详细页评论抓取任务
	 * @param productId 评论对应的商品id
	 * @param url "http://www.smzdm.com/p/744723/p1"
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 */
	public SMZDMCommentTask(String productId, String url, String mongoURI, String dbName) {
		this(1, productId, url, mongoURI, dbName);
	}
	
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri(getUrl())
				.setHeader("Host", "www.smzdm.com")
				.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.build();
		return request;
	}


	@Override
	protected void process(Page page) {
		String htmlStr = getHtmlStr(page);
		Document doc = Jsoup.parse(htmlStr);
		
		Element pageCurrent = doc.select("#commentTabBlockNew > ul.pagination > li > a.pageCurrent").first();
		if(pageCurrent != null){
			this.currentPage = Integer.parseInt(pageCurrent.text());
		}
		
		Element pageTotal = null;
		if(doc.select("#commentTabBlockNew > ul.pagination > li.pagedown").first() == null){//没有“下一页”，则取倒数第3个
			pageTotal = doc.select("#commentTabBlockNew > ul.pagination > li:nth-last-child(3) > a").first();
		}else{
			pageTotal = doc.select("#commentTabBlockNew > ul.pagination > li:nth-last-child(4) > a").first();
		}
		if(pageTotal != null){
			this.totalPage = Integer.parseInt(pageTotal.text());
		}
		
		System.out.println(Thread.currentThread().getName()+"---"+currentPage+"/"+totalPage);
		
		List<Map<String, String>> data = extractData(doc);
		page.setData(data);
		
		//指定主键
		for (Map<String, String> d : data) {
			d.put("_id", d.get("id"));
		}
		MongoDBUtil.insertMany(data, false, this.mongoURI, this.dbName, "smzdm_comment");
	}

	private List<Map<String, String>> extractData(Document doc) {
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		Elements lists = doc.select("#commentTabBlockNew > ul.comment_listBox > li");
		for (Element element : lists) {
			Map<String,String> comment = new HashMap<String,String>();
			
			String commentId = element.attr("id");//li_comment_63134890
			commentId = commentId.substring("li_comment_".length());
			comment.put("id", commentId);
			
			//设置商品id
			comment.put("article_id", this.productId);

			Element parent = element.select("div.comment_conBox > div.blockquote_wrap > blockquote:last-child").first();
			if(parent != null){
				String parentId = parent.attr("blockquote_cid");//63133030
				comment.put("parentId", parentId);
			}
			
			Element time = element.select("div.comment_conBox > div.comment_avatar_time > div.time").first();
			comment.put("time", time.text());
			
			Element content = element.select("div.comment_conBox > div.comment_conWrap > div.comment_con").first();
			comment.put("content", content.text());
			
			Element positive = element.select("div.comment_conBox > div.comment_conWrap > div.comment_action > a.dingNum > span").first();
			comment.put("positive", positive.text().substring(1, positive.text().length()-1));//(0)
			
			Element negative = element.select("div.comment_conBox > div.comment_conWrap > div.comment_action > a.caiNum > span").first();
			comment.put("negative", negative.text().substring(1, negative.text().length()-1));
			
			comment.put("create_date", DateUtil.getDateString(new Date()));
			
			data.add(comment);
		}
		
		return data;
	}

	@Override
	protected List<Task> buildNewTask(Page page) {
		List<Task> newTasks = new ArrayList<Task>();
		//抓取下一页评论
		if(this.currentPage < this.totalPage){
			int p = this.currentPage+1;
			String newUrl = this.url.substring(0, this.url.lastIndexOf("/p"+currentPage))+"/p"+p;
			SMZDMCommentTask task = new SMZDMCommentTask(this.priority, this.productId, newUrl, this.mongoURI, this.dbName);
			newTasks.add(task);
		}
		return newTasks;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
