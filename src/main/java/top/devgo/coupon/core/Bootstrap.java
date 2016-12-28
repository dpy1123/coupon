package top.devgo.coupon.core;

import com.udojava.jmx.wrapper.JMXBeanWrapper;
import top.devgo.coupon.core.mx.JobMXBean;
import top.devgo.coupon.core.mx.MXServer;
import top.devgo.coupon.web.SimpleHttpServer;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Date;
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

//		List<Task> beginningTasks = new ArrayList<Task>();
//		//抓SMZDM
//		beginningTasks.add(new SMZDMTask("999999999999", "2016-12-15 12:00:00", "mongodb://localhost:27017", "coupon", false, true));
//
//		//抓Bilibili
////		String[] channels = new String[]{"鬼畜调教", "音MAD", "人力VOCALOID", "宅舞"};
///*		String[] channels = BilibiliConfig.getAllTids();
//		for (int i = 0; i < channels.length; i++) {
//			beginningTasks.add(new ArchiveTask(1, BilibiliConfig.getTid(channels[i]), 1, "mongodb://localhost:27017", "bilibili", false, true));
//		}*/
//		//run at 2016-10-05 13:46:54
////		beginningTasks.add(new ArchiveTask(1, "22", 1, "mongodb://localhost:27017", "bilibili", false, true));
////		beginningTasks.add(new VideoFetchTask(1, "6539460", 1));
//
//		config.setBeginningTasks(beginningTasks);
//		manager.start(config);

        SimpleHttpServer.start(8877, 100, Executors.newFixedThreadPool(config.getMaxCrawlers()));

        MXServer.registerMBean(new JMXBeanWrapper(new JobMXBean(manager, config)), new ObjectName("top.devgo.coupon.core.mx:name=JobMXBean, type=JobMXBean"));
        MXServer.start(8899, 8898, "couponMxServer");

	}
}
