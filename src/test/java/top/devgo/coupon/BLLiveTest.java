package top.devgo.coupon;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import top.devgo.coupon.utils.TextUtil;

public class BLLiveTest {
	public static void main(String[] args) throws ParseException, IOException {
		for (int i = 0; i < 10; i++) {
			
			send("MDZZ"+i);
		}
	}

	private static void send(String msg) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();


		HttpUriRequest request = RequestBuilder
				.post()
				.setUri("http://live.bilibili.com/msg/send")
				.addParameter("color", "16777215")
				.addParameter("msg", msg)
				.addParameter("rnd", "1464166551")
				.addParameter("roomid", "267725")
				.setHeader("cookie", "Cookie:pgv_pvi=9535324160; sid=d3r72dnj; fts=1438928620; DedeUserID=222380; DedeUserID__ckMd5=d21b53359fa24131; SESSDATA=374ea5ef%2C1471583510%2C6232e58b; LIVE_BUVID=8c3d9b274fe9b5089b258fa4ffd25039; LIVE_BUVID__ckMd5=74cd15c9569f5dbd; DedeID=4705703; pgv_si=s9134603264; rlc_time=1464163917487; LIVE_LOGIN_DATA=64a23d30056df97d3b02204799c37768abcf7a7e; LIVE_LOGIN_DATA__ckMd5=4389d3327f347f3a; time_tracker=20160525; CNZZDATA2724999=cnzz_eid%3D765314300-1449051466-%26ntime%3D1464166576; user_face=http%3A%2F%2Fi0.hdslb.com%2Fbfs%2Fface%2F0dc27ef09af923ce9be73198898e2aa8db41b6cc.jpg; _dfcaptcha=fb498c4ef9d6c319f9bb8b22b1bd9af4; _cnt_dyn=null; _cnt_pm=0; _cnt_notify=15; uTZ=-480; attentionData=%7B%22code%22%3A0%2C%22msg%22%3A%22%22%2C%22data%22%3A%7B%22count%22%3A0%2C%22open%22%3A1%2C%22has_new%22%3A0%7D%7D")
				
				.setHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.build();
		
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		try {
			HttpEntity entity = response.getEntity();
			
			Header contentType = entity.getContentType();
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
