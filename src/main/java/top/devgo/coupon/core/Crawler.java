package top.devgo.coupon.core;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import top.devgo.coupon.core.task.Task;
/**
 * crawl执行类
 * @author DD
 *
 */
public class Crawler implements Runnable {
	private CloseableHttpClient httpClient;
	private Task task;
	private CrawlerManager stage;
	
	public Crawler(CloseableHttpClient httpClient, Task task, CrawlerManager stage) {
		this.httpClient = httpClient;
		this.task = task;
		this.stage = stage;
	}
	
	/**
	 * 执行crawl任务，流程processTask->submitNewTask
	 */
	public void run() {
		HttpUriRequest request = task.buildRequest();
		
		List<Task> newTasks = null;
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(request, new BasicHttpContext());
			newTasks = task.process(response);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		
		if (newTasks!=null) {
			for (Iterator<Task> it = newTasks.iterator(); it.hasNext();) {
				Task task = it.next();
				stage.addTaskToQueue(task);
			}
		}
	}

	

}
