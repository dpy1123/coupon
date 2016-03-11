package top.devgo.coupon.core.task.smzdm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.bson.Document;

import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;
import top.devgo.coupon.utils.IOUtil;
import top.devgo.coupon.utils.MongoDBUtil;

public class SMZDMImageTask extends TaskBase {

	private String fetchUrl;
	private String dataId;
	private String mongoURI;
	private String dbName;
	
	
	/**
	 * 初始化smzdm图片抓取任务
	 * @param priority
	 * @param fetchUrl 待抓取图片的url
	 * @param dataId 是为哪条记录抓取的图片
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 */
	public SMZDMImageTask(int priority, String fetchUrl, String dataId, String mongoURI, String dbName) {
		super(priority);
		this.fetchUrl = fetchUrl;
		this.dataId = dataId;
		this.mongoURI = mongoURI;
		this.dbName = dbName;
	}
	
	/**
	 * 初始化smzdm图片抓取任务
	 * @param fetchUrl 待抓取图片的url
	 * @param dataId 是为哪条记录抓取的图片
	 * @param mongoURI "mongodb://localhost:27017,localhost:27018,localhost:27019"
	 * @param dbName
	 */
	public SMZDMImageTask(String fetchUrl, String dataId, String mongoURI, String dbName) {
		this(1, fetchUrl, dataId, mongoURI, dbName);
	}
	

	
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri(this.fetchUrl)
//				.setHeader("Referer", "http://www.smzdm.com/")
				.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.setHeader("X-Requested-With", "XMLHttpRequest").build();
		return request;
	}


	@Override
	protected void process(Page page) {
		
		// get a unique name for storing this image
	    String extension = this.fetchUrl.substring(this.fetchUrl.lastIndexOf('.'));
	    String hashedName = UUID.randomUUID() + extension;
	
	    // store image
	    File storageFolder = new File(System.getProperty("user.dir") +"\\img");
	    if (!storageFolder.exists()) {
	      storageFolder.mkdirs();
	    }
	    String filename = storageFolder.getAbsolutePath() + "/" + hashedName;
	    
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(filename));
			fos.write(page.getContentData());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			IOUtil.close(fos);
		}
		
		//更新记录
		MongoDBUtil.updateOne(new Document("_id", this.dataId), 
				new Document("$set", new Document("article_pic_local", hashedName)),
				this.mongoURI, this.dbName, "smzdm_data");
	}


	@Override
	protected List<Task> buildNewTask(Page page) {
		List<Task> newTasks = new ArrayList<Task>(0);
		return newTasks;
	}


	@Override
	public String toString() {
		return "SMZDMImageTask [fetchUrl=" + fetchUrl + ", dataId=" + dataId
				+ ", mongoURI=" + mongoURI + ", dbName=" + dbName + "]";
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

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getFetchUrl() {
		return fetchUrl;
	}

	public void setFetchUrl(String fetchUrl) {
		this.fetchUrl = fetchUrl;
	}
}
