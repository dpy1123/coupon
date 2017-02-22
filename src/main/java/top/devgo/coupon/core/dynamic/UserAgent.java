package top.devgo.coupon.core.dynamic;

import java.util.Random;

/**
 * Created by dd on 17/2/22.
 */
public class UserAgent {
    private UserAgent() {
    }

    protected static String[] userAgents = {
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36",
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1"
    };

    public static String getUA() {
        return userAgents[new Random().nextInt(userAgents.length)];
    }
}
