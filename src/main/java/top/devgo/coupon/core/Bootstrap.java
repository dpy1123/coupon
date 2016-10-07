package top.devgo.coupon.core;

import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.bilibili.ArchiveTask;
import top.devgo.coupon.core.task.bilibili.BilibiliConfig;
import top.devgo.coupon.core.task.bilibili.VideoFetchTask;
import top.devgo.coupon.core.task.smzdm.SMZDMTask;

import java.util.ArrayList;
import java.util.List;

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
		//抓SMZDM
		//beginningTasks.add(new SMZDMTask("999999999999", "2016-09-27 12:00:00", "mongodb://localhost:27017", "coupon", false, true));
		
		//抓Bilibili
//		String[] channels = new String[]{"鬼畜调教", "音MAD", "人力VOCALOID", "宅舞"};
		String[] channels = BilibiliConfig.getAllTids();
		for (int i = 0; i < channels.length; i++) {
			beginningTasks.add(new ArchiveTask(1, BilibiliConfig.getTid(channels[i]), 1, "mongodb://localhost:27017", "bilibili", false, true));
		}
//		beginningTasks.add(new ArchiveTask(1, "22", 1, "mongodb://localhost:27017", "bilibili", false, true));
//		beginningTasks.add(new VideoFetchTask(1, "6539460", 1));

		config.setBeginningTasks(beginningTasks);
		manager.start(config);
	}
}
