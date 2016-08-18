package top.devgo.coupon.core;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;

import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.utils.MongoDBUtil;

/**
 * crawler的主控类
 * @author DD
 *
 */
public class CrawlerManager {
	private static Logger logger = Logger.getLogger(CrawlerManager.class.getName());
	
	/**
	 * 线程安全的优先级队列，用来存放task
	 */
	private PriorityBlockingQueue<Task> taskQueue;
	/**
	 * 线程安全的HttpClientConnection连接池
	 */
	private PoolingHttpClientConnectionManager connectionManager;
	private CloseableHttpClient httpclient; 
	/**
	 * Crawler线程池
	 */
	private ExecutorService crawlerThreadPool; 
	
	/**
	 * 初始化stage
	 * @param config
	 */
	private void initStage(Config config) {
		
		taskQueue = new PriorityBlockingQueue<Task>(config.getTaskQueueCapacity());

		// Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setValidateAfterInactivity(config.getConnectionValidateInterval());
		connectionManager.setMaxTotal(config.getMaxConnections());
		
		httpclient = HttpClients.custom().setConnectionManager(connectionManager).build();
		
		crawlerThreadPool = Executors.newFixedThreadPool(config.getMaxCrawlers());
		
		List<Task> seeds = config.getBeginningTasks();
		if(seeds!=null){
			for (Iterator<Task> it = seeds.iterator(); it.hasNext();) {
				Task task = it.next();
				addTaskToQueue(task);
			}
		}
	}
	
	private Thread taskManager;//用来管理task的执行 
	private boolean started = false;//记录本容器是否启动
	private final CrawlerManager self = this;
	
	public void start(final Config config) {
		if(started){//已被启动
			return;
		}
		
		started = true;
		initStage(config);
		
		taskManager = new Thread(new Runnable() {
			@Override
			public void run() {
				while (started) {
					int tasks = taskQueue.size();
					int workingThread = ((ThreadPoolExecutor)crawlerThreadPool).getActiveCount();
					if (tasks < 1) {
						logger.info("暂无新任务，尚有"+workingThread+"个任务在执行。");
						if (workingThread < 1) {
							stop();
						}
					}else{
						int jobs = (int) Math.min(tasks, Math.round((config.getMaxCrawlers() - workingThread) * 1.2));//提供当前空余worker数1.2倍的任务
						for (int i = 0; i < jobs; i++) {
							Task task = taskQueue.poll();
							crawlerThreadPool.execute(new Crawler(httpclient, task, self));
						}
						logger.info("新增"+jobs+"个任务，尚有"+workingThread+"个任务在执行。");
					}
					sleep(config.getTaskScanInterval());
				}
			}
			
			
		});
		taskManager.start();
		
		logger.info("started at: "+ new Date());
	}

	public void stop() {
		if(!started){//未启动
			return;
		}
		
		for (Task task : taskQueue) {
			logger.info("未执行task："+task.toString());
		}
		
		started = false;
		waitUntilFinish(10L);
		
		logger.info("stoped at: "+ new Date());
	}
	
	private void waitUntilFinish(long timeout) {
//		int workingThread = ((ThreadPoolExecutor)crawlerThreadPool).getActiveCount();
//		while (workingThread > 0) {
//			sleep(3);
//		}
		try {
			if (!crawlerThreadPool.awaitTermination(timeout, TimeUnit.SECONDS)) {
				
				BlockingQueue<Runnable> tasks = ((ThreadPoolExecutor)crawlerThreadPool).getQueue();
				for (Runnable task : tasks) {
					logger.info("停止或取消task："+task.toString());
				}
				
				crawlerThreadPool.shutdownNow();
			}
			httpclient.close();
			connectionManager.close();
			MongoDBUtil.close();
		} catch (IOException e) {
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.warn("Interrupted!", e);
			Thread.currentThread().interrupt();
		}
	}
	
	protected void sleep(int mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException ignored) {
			logger.warn("Interrupted!", ignored);
			Thread.currentThread().interrupt();
		}
	}
	
	public void addTaskToQueue(Task task) {
		taskQueue.put(task);
	}
}
