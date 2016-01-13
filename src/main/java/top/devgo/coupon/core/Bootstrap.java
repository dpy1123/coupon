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
	public static void main(String[] args) {
		CrawlerManager manager = new CrawlerManager();
		Config config = new Config();
		
		List<Task> beginningTasks = new ArrayList<Task>();
		beginningTasks.add(new SMZDMTask("255070648939", "mongodb://localhost:27017", "coupon"));
		
		config.setBeginningTasks(beginningTasks);
		manager.start(config);
	}
}
