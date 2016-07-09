package top.devgo.coupon.core.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;

import top.devgo.coupon.core.page.Page;

/**
 * 所有业务Task类继承本类
 * @author DD
 *
 */
public abstract class TaskBase implements Task, Comparable<Task> {

	protected static Logger logger = Logger.getLogger(TaskBase.class.getName());
	
	protected int priority;//优先级
	
	public TaskBase(int priority) {
		this.priority = priority;
	}
	
	/**
	 * 获取内容
	 * @param response
	 * @return
	 */
	protected Page buildPage(CloseableHttpResponse response) {
		Page page = new Page();
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			try {
				page.load(entity);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		return page;
	}
	
	/**
	 * 处理获取的内容
	 * @param page
	 */
	protected abstract void process(Page page);
	/**
	 * 获取新的任务
	 * @param page
	 * @return
	 */
	protected abstract List<Task> buildNewTask(Page page);

	
	public List<Task> process(CloseableHttpResponse response) {
		//1.获取内容
		Page page = buildPage(response);
		//2.处理获取的内容
		process(page);
		//3.返回获取的新任务
		return buildNewTask(page);
	}
	
	
	/**
	 * 从Page中获取htmlString
	 * @param page
	 * @return
	 */
	protected String getHtmlStr(Page page) {
		String htmlStr = null;
		String type = page.getContentType();
		if(type != null && type.startsWith("text/html")){//text/html;charset=utf-8;
			try {
				htmlStr = new String(page.getContentData(), page.getContentCharset());
			} catch (UnsupportedEncodingException e) {
				logger.error("", e);
			}
		}
		return htmlStr;
	}



	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * 优先级比较逻辑
	 */
	public int compareTo(Task o) {
		return this.getPriority()-o.getPriority();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + priority;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskBase other = (TaskBase) obj;
		if (priority != other.priority)
			return false;
		return true;
	}

}
