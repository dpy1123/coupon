package top.devgo.coupon;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SMZDM {
	public static void main(String[] args) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URI uri = null;
		try {
			uri = new URIBuilder().setScheme("http")
					.setHost("www.smzdm.com").setPath("/p1").build();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		HttpGet httpget = new HttpGet(uri);
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
				String html = EntityUtils.toString(entity, "UTF-8");
				System.out.println(html);
			}	
		} finally {
			if(response != null){
				response.close();
			}
		}
	}
}
