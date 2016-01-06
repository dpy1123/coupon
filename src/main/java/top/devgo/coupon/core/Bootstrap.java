package top.devgo.coupon.core;

import java.util.ArrayList;
import java.util.List;

import top.devgo.coupon.core.task.SMZDMTask;
import top.devgo.coupon.core.task.Task;

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
		beginningTasks.add(new SMZDMTask("255070648939"));
		beginningTasks.add(new SMZDMTask("155070648939"));
		beginningTasks.add(new SMZDMTask("125070648939"));
		config.setBeginningTasks(beginningTasks);
		manager.start(config);
	}
}
