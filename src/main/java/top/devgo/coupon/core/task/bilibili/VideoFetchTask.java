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
import top.devgo.coupon.core.task.DownloadTask;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.TaskBase;

/**
 * 视频预下载task，本task会获得真实下载地址，由DownloadTask真正完成下载。
 * @author DD
 *
 */
public class VideoFetchTask extends TaskBase {

	private static Logger logger = Logger.getLogger(VideoFetchTask.class);
	
	private String archiveId;
	private int p;
	private boolean tryToFetchNextP = false;
	
	/**
	 * 初始化抓取任务
	 * @param priority
	 * @param archiveId 
	 * @param p 分p数 
	 */
	public VideoFetchTask(int priority, String archiveId, int p) {
		super(priority);
		this.archiveId = archiveId;
		this.p = p;
	}

	@Override
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://api.bilibili.com/playurl")
				.addParameter("type", "json")
				.addParameter("platform", "html5")
				.addParameter("aid", this.archiveId)
				.addParameter("page", this.p+"")//分p
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
		
		if (result!=null){
			if("suee".equals(result.get("result"))) {
				page.setData(result);
				tryToFetchNextP = true;
			}else if("error".equals(result.get("result"))){
				tryToFetchNextP = false;
			}
		}
		
	}

	@Override
	protected List<Task> buildNewTask(Page page) {
		List<Task> newTasks = new ArrayList<Task>();
		Map<String, Object> data = page.getData();
		
		if (data!=null) {
			List<Map<String, Object>> durls = (List<Map<String, Object>>) data.get("durl");
			if (durls.size() > 0) {
				String format = (String) data.get("format");
				String fileName = this.archiveId+"_"+this.p+"."+format;
				
				String url = (String) durls.get(0).get("url");
				if (durls.get(0).containsKey("backup_url")) {//如果有backup_url，尽量取hdmp4
					List<String> backupUrls = (List<String>) durls.get(0).get("backup_url");
					for (String backupUrl : backupUrls) {
						if (backupUrl.contains("hd.mp4")) {
							url = backupUrl;
						}
					}
				}
				newTasks.add(new DownloadTask(priority-1, url, BilibiliConfig.VIDEO_PATH, fileName));
			}	
		}
		
		if (tryToFetchNextP) {
			newTasks.add(new VideoFetchTask(priority, archiveId, p+1));
		}
		return newTasks;
	}

}
