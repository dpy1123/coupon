package top.devgo.coupon;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import top.devgo.coupon.utils.TextUtil;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.BiFunction;


public class BiliBiliUserInfoTest {

	public static void main(String[] args) throws ParseException, IOException {
        fetch();
	}

	private static void fetch() throws IOException {
		Response res = Jsoup.connect("http://space.bilibili.com/ajax/fav/getboxlist")
                .method(Method.GET).ignoreContentType(true)
//                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                .data("mid", "222380")
                .execute();
        System.out.println(res.body());


        res = Jsoup.connect("http://space.bilibili.com/ajax/fav/getList")
                .method(Method.GET).ignoreContentType(true)
//                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                .data("mid", "222380")
                .data("fid", "1416587")
                .data("pid", "1")
                .data("pagesize", "30")
                .data("kw", "")
                .execute();
        System.out.println(res.body());

        res = Jsoup.connect("http://api.bilibili.com/x/v2/history")
                .method(Method.GET).ignoreContentType(true)
                .data("jsonp", "json")
                .data("pn", "2")
                .data("ps", "20")
                .cookie("DedeUserID", "222380")
                .cookie("SESSDATA", "374ea5ef%2C1485679672%2C1d8c8eef")
                .execute();
        System.out.println(res.body());

        res = Jsoup.connect("http://space.bilibili.com/ajax/member/MyInfo")
                .method(Method.GET).ignoreContentType(true)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36")
                .cookie("DedeUserID", "222380")
//                .cookie("DedeUserID__ckMd5", "d21b53359fa24131")
                .cookie("SESSDATA", "374ea5ef%2C1485679672%2C1d8c8eef")
                .execute();
        System.out.println(res.body());

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpUriRequest request = RequestBuilder
                .post()
                .setUri("http://space.bilibili.com/ajax/member/GetInfo")
                .addParameter("mid", "77096004")
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
//                .setHeader("Origin", "http://space.bilibili.com")
                .setHeader("Referer", "http://space.bilibili.com/77096004/")
                .setHeader("X-Requested-With", "XMLHttpRequest")
//                .setHeader("Cookie", "fts=1485074837; buvid3=E53AAD20-D750-40F4-8C17-1A215131B0592079infoc; DedeUserID=222380; DedeUserID__ckMd5=d21b53359fa24131; SESSDATA=374ea5ef%2C1485679672%2C1d8c8eef; sid=6g3zigox; CNZZDATA2724999=cnzz_eid%3D346950642-1480649550-http%253A%252F%252Fwww.bilibili.com%252F%26ntime%3D1485143666; _cnt_dyn=null; _cnt_pm=0; _cnt_notify=15; uTZ=-480; _dfcaptcha=5b84ae6e16757f3facee94d381338528")
                .setHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")

                .build();

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            HttpEntity entity = response.getEntity();
            String encoding = "utf-8";
            if (entity != null) {
                String htmlStr = EntityUtils.toString(entity, encoding);
                htmlStr = TextUtil.decodeUnicode(htmlStr);
                System.out.println(htmlStr);
            }
        }finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
