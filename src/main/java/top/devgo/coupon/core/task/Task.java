package top.devgo.coupon.core.task;

import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * 任务描述
 * @author DD
 *
 */
public interface Task{
	
	/**
	 * 获得任务优先级，数字越大优先级越低
	 * @return
	 */
	public int getPriority();

	public HttpUriRequest buildRequest();

	public List<Task> process(CloseableHttpResponse response);
}
