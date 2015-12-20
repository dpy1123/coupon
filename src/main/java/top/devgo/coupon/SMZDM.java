package top.devgo.coupon;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SMZDM {
	public static void main(String[] args) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpget = new HttpGet("http://www.smzdm.com/p1");
		httpget.setHeader("Host", "www.smzdm.com");
		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
		
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String htmlStr = EntityUtils.toString(entity, "UTF-8");
//				System.out.println(htmlStr);
				
				Document doc = Jsoup.parse(htmlStr);
				Elements lists = doc.select("body > section > div.leftWrap > div.list:not(body > section > div.leftWrap > div.list.top.topSpace)");
				for (Element element : lists) {
					Element title = element.select("div.listTitle > h4 > a").first();
					if(title != null){
						String t = title.text();
						String detailUrl = title.attr("href");
						String p = null;
						Element prise = title.child(0);
						if(prise != null){
							p = prise.text();
							t = t.substring(0, t.lastIndexOf(p));
						}
						System.out.print(t+" || "+p+" || "+detailUrl);
					}
					
					Element time = element.select("div.listRight > div.lrTop > span.lrTime").first();
					if(time != null){
						System.out.print(" || "+time.text());
					}
					
					Element img = element.select("a > img").first();
					if(img != null){
						System.out.print(" || "+img.attr("src"));
					}
					
					Element bref = element.select("div.listRight > div.lrInfo").first();
					if(bref != null){
						System.out.print(" || "+bref.text());
					}
					Element shoppingUrl = element.select("div.listRight > div.lrBot > div.botPart > div > a").first();
					if(shoppingUrl != null){
						System.out.print(" || "+shoppingUrl.attr("href"));
					}
					
					System.out.print("\r");
				}
				
			}	
		} finally {
			response.close();
		}
	}
}
