package top.devgo.coupon.core.mx;

import com.udojava.jmx.wrapper.JMXBean;
import com.udojava.jmx.wrapper.JMXBeanAttribute;
import com.udojava.jmx.wrapper.JMXBeanOperation;
import com.udojava.jmx.wrapper.JMXBeanParameter;
import top.devgo.coupon.core.Config;
import top.devgo.coupon.core.CrawlerManager;
import top.devgo.coupon.core.task.Task;
import top.devgo.coupon.core.task.bilibili.ArchiveTask;
import top.devgo.coupon.core.task.bilibili.BilibiliConfig;
import top.devgo.coupon.core.task.bilibili.FavBoxTask;
import top.devgo.coupon.core.task.bilibili.UserTask;
import top.devgo.coupon.core.task.smzdm.SMZDMTask;
import top.devgo.coupon.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dd on 16/12/22.
 */
@JMXBean(description = "JobMXBean", sorted = true)
public class JobMXBean {

    private CrawlerManager crawlerManager;
    private Config config;

    public JobMXBean(CrawlerManager crawlerManager, Config config) {
        this.crawlerManager = crawlerManager;
        this.config = config;
    }

    @JMXBeanAttribute(name = "getTaskQueueCount", description = "获取queue中的task数")
    public int getTaskQueueCount() {
        return crawlerManager.getTaskQueueCount();
    }

    @JMXBeanAttribute(name = "getWorkingThreadCount", description = "获取大约的活跃线程数")
    public int getWorkingThreadCount() {
        return crawlerManager.getWorkingThreadCount();
    }

    @JMXBeanOperation(name = "addSMZDMTask", description = "添加smzdm抓取任务", sortValue = "1")
    public void addSMZDMTask(
            @JMXBeanParameter(name = "stopDate", description = "抓取结束日期 \"2015-12-01 00:00:00\"") String stopDate,
            @JMXBeanParameter(name = "updateRecord", description = "是否更新已有记录") boolean updateRecord,
            @JMXBeanParameter(name = "fetchComment", description = "是否抓评论") boolean fetchComment) {
        List<Task> beginningTasks = new ArrayList<Task>();
        beginningTasks.add(new SMZDMTask("999999999999", stopDate, config.getMongoUrl(),
                "coupon", updateRecord, fetchComment));
        config.setBeginningTasks(beginningTasks);
        crawlerManager.start(config);
    }

    @JMXBeanOperation(name = "addBilibiliArchiveTask", description = "添加bilibili抓取任务", sortValue = "2")
    public void addBilibiliArchiveTask(
            @JMXBeanParameter(name = "channels", description = "抓取的频道名称Str, 用逗号分割, eg: " +
                    "\"鬼畜调教,音MAD,人力VOCALOID,宅舞\", 如果不填则抓全部频道") String channelStr,
            @JMXBeanParameter(name = "stopDate", description = "抓取结束日期 \"2015-12-01 00:00:00\"") String stopDate,
            @JMXBeanParameter(name = "updateRecord", description = "是否更新已有记录") boolean updateRecord,
            @JMXBeanParameter(name = "fetchComment", description = "是否抓评论") boolean fetchComment) {
        String[] channels;
        if (StringUtil.isBlank(channelStr)) {
            channels = BilibiliConfig.getAllTids();
        }else {
            channels = channelStr.split(",");
        }
        List<Task> beginningTasks = new ArrayList<Task>();
        for (int i = 0; i < channels.length; i++) {
			beginningTasks.add(new ArchiveTask(BilibiliConfig.getTid(channels[i]), 1, stopDate,
                    config.getMongoUrl(), "bilibili", updateRecord, fetchComment));
		}
        config.setBeginningTasks(beginningTasks);
        crawlerManager.start(config);
    }

    @JMXBeanOperation(name = "addBilibiliUserTask", description = "添加bilibili用户抓取任务", sortValue = "3")
    public void addBilibiliUserTask(
            @JMXBeanParameter(name = "uId", description = "用户uid") int uId,
            @JMXBeanParameter(name = "maxUId", description = "本次抓取最大的用户uid.如果只抓特定用户,和uId设置成一样") int maxUId,
            @JMXBeanParameter(name = "updateRecord", description = "是否更新已有记录") boolean updateRecord) {
        List<Task> beginningTasks = new ArrayList<Task>();
        beginningTasks.add(new UserTask(uId, maxUId, config.getMongoUrl(), "bilibili", updateRecord));
        config.setBeginningTasks(beginningTasks);
        crawlerManager.start(config);
    }

    @JMXBeanOperation(name = "addBilibiliFavTask", description = "添加bilibili收藏抓取任务", sortValue = "4")
    public void addBilibiliFavTask(
            @JMXBeanParameter(name = "uId", description = "用户uid") int uId,
            @JMXBeanParameter(name = "maxUId", description = "本次抓取最大的用户uid.如果只抓特定用户,和uId设置成一样") int maxUId,
            @JMXBeanParameter(name = "updateRecord", description = "是否更新已有记录") boolean updateRecord,
            @JMXBeanParameter(name = "fetchFavList", description = "是否抓取收藏夹下的记录") boolean fetchFavList) {
        List<Task> beginningTasks = new ArrayList<Task>();
        beginningTasks.add(new FavBoxTask(uId, maxUId, config.getMongoUrl(), "bilibili", updateRecord, fetchFavList));
        config.setBeginningTasks(beginningTasks);
        crawlerManager.start(config);
    }
}
