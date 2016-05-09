package top.devgo.coupon.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.smzdm.SMZDMTask;
import top.devgo.coupon.utils.IOUtil;

/**
 * 启动器
 * @author DD
 *
 */
public class Bootstrap {
	public static void main(String[] args) {
		CrawlerManager manager = new CrawlerManager();
		Config config = new Config();
		config.setMaxCrawlers(50);
		config.setMaxConnections(50);
		config.setTaskQueueCapacity(100);
		config.setTaskScanInterval(2);
		
		List<Task> beginningTasks = new ArrayList<Task>();
		beginningTasks.add(new SMZDMTask("999999999999", "2016-01-01 00:00:00", "mongodb://localhost:27017", "coupon", false, false));
//		beginningTasks.add(new SMZDMCommentTask("745027", "http://www.smzdm.com/p/745027/p1", "mongodb://localhost:27017", "coupon"));
		
		config.setBeginningTasks(beginningTasks);
		manager.start(config);
		
		Scanner sc = new Scanner(System.in);
		String cmd = sc.next(); 
		switch (cmd) {
		case "stop":
			manager.stop();
			break;

		default:
			break;
		}
		IOUtil.close(sc);
	}
}
