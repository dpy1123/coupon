package top.devgo.coupon.core;

import com.udojava.jmx.wrapper.JMXBeanWrapper;
import top.devgo.coupon.core.dynamic.ProxyManager;
import top.devgo.coupon.core.dynamic.task.CnProxy;
import top.devgo.coupon.core.dynamic.task.Cz88;
import top.devgo.coupon.core.mx.JobMXBean;
import top.devgo.coupon.core.mx.MXServer;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.bilibili.FavBoxTask;
import top.devgo.coupon.core.task.bilibili.UserTask;
import top.devgo.coupon.web.SimpleHttpServer;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * 启动器
 * @author DD
 *
 */
public class Bootstrap {
	public static void main(String[] args) throws Exception {
		CrawlerManager manager = new CrawlerManager();
		Config config = new Config();
		config.setMaxCrawlers(40);
		config.setMaxConnections(20);
		config.setTaskQueueCapacity(100);
		config.setTaskScanInterval(1000);
		config.setConnectionTimeout(1000);
		if (args!=null && args.length>0)
		    config.setMongoUrl(args[0]);

		List<Task> beginningTasks = new ArrayList<Task>();
		//抓SMZDM
//		beginningTasks.add(new SMZDMTask("999999999999", "2016-12-15 12:00:00", "mongodb://localhost:27017", "coupon", false, true));

		//抓Bilibili
//		String[] channels = new String[]{"鬼畜调教", "音MAD", "人力VOCALOID", "宅舞"};
/*		String[] channels = BilibiliConfig.getAllTids();
		for (int i = 0; i < channels.length; i++) {
			beginningTasks.add(new ArchiveTask(1, BilibiliConfig.getTid(channels[i]), 1, "mongodb://localhost:27017", "bilibili", false, true));
		}*/
		//run at 2016-10-05 13:46:54
//		beginningTasks.add(new ArchiveTask(1, "22", 1, "mongodb://localhost:27017", "bilibili", false, true));
//		beginningTasks.add(new VideoFetchTask(1, "6539460", 1));

//        beginningTasks.add(new UserTask(1, 0, 222380, "mongodb://localhost:27017", "bilibili", true));
//        beginningTasks.add(new UserTask(1, 0, 222380, "mongodb://172.16.6.112:32633", "bilibili", true));
//        beginningTasks.add(new FavBoxTask(1, 222380, 222380, "mongodb://172.16.6.112:32633", "bilibili", true, true));

		config.setBeginningTasks(beginningTasks);
		manager.start(config);

        new ProxyManager(config, "proxy", manager).start();

        new SimpleHttpServer(config.getMongoUrl(), "coupon").start(8877, 100, Executors.newFixedThreadPool(5));

        MXServer.registerMBean(new JMXBeanWrapper(new JobMXBean(manager, config)), new ObjectName("top.devgo.coupon.core.mx:name=JobMXBean, type=JobMXBean"));
        MXServer.start(8899, 8898, "couponMxServer");

	}
}
