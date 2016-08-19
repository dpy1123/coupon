package top.devgo.coupon.core;

import java.util.ArrayList;
import java.util.List;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.smzdm.SMZDMTask;

/**
 * 启动器
 * @author DD
 *
 */
public class Bootstrap {
	public static void main(String[] args) throws InterruptedException {
		CrawlerManager manager = new CrawlerManager();
		Config config = new Config();
		config.setMaxCrawlers(40);
		config.setMaxConnections(20);
		config.setTaskQueueCapacity(100);
		config.setTaskScanInterval(1000);
		
		List<Task> beginningTasks = new ArrayList<Task>();
		beginningTasks.add(new SMZDMTask("999999999999", "2016-08-18 00:00:00", "mongodb://localhost:27017", "coupon", false, true));
//		beginningTasks.add(new SMZDMCommentTask("745027", "http://www.smzdm.com/p/745027/p1", "mongodb://localhost:27017", "coupon"));
		config.setBeginningTasks(beginningTasks);
		
		manager.start(config);
		
	}
}
