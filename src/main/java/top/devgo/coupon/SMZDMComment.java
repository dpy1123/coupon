package top.devgo.coupon;

import java.io.IOException;

import org.apache.http.Header;
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

public class SMZDMComment {
	public static void main(String[] args) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpget = new HttpGet("http://www.smzdm.com/p/744723/p1");
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
			
			Header contentType = entity.getContentType();
			String encoding = "utf-8";
			if(contentType != null){
				String type = contentType.getValue();
				if(type != null){//Content-Type:text/html;charset=utf-8;
					encoding = type.substring(type.lastIndexOf("charset=") + "charset=".length());
					int pos = encoding.lastIndexOf(";");
					if(pos > -1){
						encoding = encoding.substring(0, pos);
					}
				}
			}
//			System.out.println(encoding);
			
			if (entity != null) {
				String htmlStr = EntityUtils.toString(entity, encoding);
//				System.out.println(htmlStr);
				
				Document doc = Jsoup.parse(htmlStr);
				Element pageCurrent = doc.select("#commentTabBlockNew > ul.pagination > li > a.pageCurrent").first();
				String currnetPage = pageCurrent.text();
				
				Element pageTotal = doc.select("#commentTabBlockNew > ul.pagination > li:nth-last-child(4) > a").first();
				String totalPage = pageTotal.text();
				
				System.out.println(currnetPage+"/"+totalPage);
				
				Elements lists = doc.select("#commentTabBlockNew > ul.comment_listBox > li");
				for (Element element : lists) {
					String commentId = element.attr("id");
					System.out.print(commentId);
					Element parent = element.select("div.comment_conBox > div.blockquote_wrap > blockquote:last-child").first();
					if(parent != null){
						String parentId = parent.attr("blockquote_cid");
						System.out.print("---"+parentId);
					}
					
					Element content = element.select("div.comment_conBox > div.comment_conWrap > div.comment_con").first();
					Element positive = element.select("div.comment_conBox > div.comment_conWrap > div.comment_action > a.dingNum > span").first();
					Element negative = element.select("div.comment_conBox > div.comment_conWrap > div.comment_action > a.caiNum > span").first();
					System.out.print("---"+content.text());
					System.out.print("---"+positive.text());
					System.out.print("---"+negative.text());
					System.out.print("\r");
				}
				
			}	
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
}
