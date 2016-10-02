package top.devgo.coupon.core;

import java.util.List;

import top.devgo.coupon.core.task.Task;

/**
 * 配置
 * @author DD
 *
 */
public class Config {
	/**
	 * 设置taskQueue的容量，默认100个任务
	 */
	private int taskQueueCapacity = 100;

	/**
	 * 设置PoolingHttpClientConnectionManager的connection re-validate time，默认5000ms
	 */
	private int connectionValidateInterval = 5000;
	/**
	 * 设置PoolingHttpClientConnectionManager的connection数量，默认10个
	 */
	private int maxConnections = 10;
	/**
	 * 设置Crawler线程的数量，默认10个
	 */
	private int maxCrawlers = 10;
	
	private List<Task> beginningTasks;
	/**
	 * 设置扫描task队列的时间间隔，默认1000ms
	 */
	private int taskScanInterval = 1000;

	public int getTaskQueueCapacity() {
		return taskQueueCapacity;
	}

	public void setTaskQueueCapacity(int taskQueueCapacity) {
		this.taskQueueCapacity = taskQueueCapacity;
	}

	public int getConnectionValidateInterval() {
		return connectionValidateInterval;
	}

	public void setConnectionValidateInterval(int connectionValidateInterval) {
		this.connectionValidateInterval = connectionValidateInterval;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getMaxCrawlers() {
		return maxCrawlers;
	}

	public void setMaxCrawlers(int maxCrawlers) {
		this.maxCrawlers = maxCrawlers;
	}

	public List<Task> getBeginningTasks() {
		return beginningTasks;
	}

	public void setBeginningTasks(List<Task> beginningTasks) {
		this.beginningTasks = beginningTasks;
	}

	public int getTaskScanInterval() {
		return taskScanInterval;
	}

	public void setTaskScanInterval(int taskScanInterval) {
		this.taskScanInterval = taskScanInterval;
	}
	
}
