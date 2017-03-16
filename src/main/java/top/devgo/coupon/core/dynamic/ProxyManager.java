package top.devgo.coupon.core.dynamic;

import org.apache.log4j.Logger;
import top.devgo.coupon.core.Config;
import top.devgo.coupon.core.CrawlerManager;
import top.devgo.coupon.core.dynamic.job.IpValidator;
import top.devgo.coupon.core.dynamic.task.CnProxy;
import top.devgo.coupon.core.dynamic.task.Cz88;
import top.devgo.coupon.core.task.Task;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dd on 17/2/28.
 */
public class ProxyManager {
    private static Logger logger = Logger.getLogger(ProxyManager.class.getName());

    private boolean started = false;
    private ScheduledThreadPoolExecutor executor;
    private CrawlerManager manager;
    private Config config;
    private String dbName;

    public ProxyManager(Config config, String dbName, CrawlerManager manager) {
        this.config = config;
        this.dbName = dbName;
        this.manager = manager;
        executor = new ScheduledThreadPoolExecutor(2);
    }

    public void start(){
        if(started){//已被启动
            logger.info("ProxyManager already started");
            return;
        }

        executor.scheduleWithFixedDelay(new IpValidator(config.getMongoUrl(), dbName), 3, 3, TimeUnit.HOURS);
        executor.scheduleWithFixedDelay(() -> {
            config.setBeginningTasks(Arrays.asList(
                new Cz88(config.getMongoUrl(), dbName),
                new CnProxy(config.getMongoUrl(), dbName)
            ));
            manager.start(config);
        }, 0, 3, TimeUnit.HOURS);

        started = true;
        logger.info("ProxyManager start, at: "+ new Date());
    }

    public void stop() {
        if(!started){//未启动
            return;
        }

        started = false;
        executor.shutdown();

        logger.info("ProxyManager stop, at: "+ new Date());
    }
}
