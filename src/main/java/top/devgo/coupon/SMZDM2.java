package top.devgo.coupon;

import java.io.IOException;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import top.devgo.coupon.utils.TextUtil;

public class SMZDM2 {
	public static void main(String[] args) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpget = new HttpGet("http://www.smzdm.com/json_more?timesort=145070648939");
		httpget.setHeader("Host", "www.smzdm.com");
		httpget.setHeader("Referer", "http://www.smzdm.com/");
		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
		httpget.setHeader("X-Requested-With", "XMLHttpRequest");
		
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
				if(type != null){
					encoding = type.substring(type.lastIndexOf("charset=") + "charset=".length());
				}
			}
//			System.out.println(encoding);
			
			if (entity != null) {
				String htmlStr = EntityUtils.toString(entity, encoding);
				htmlStr = TextUtil.decodeUnicode(htmlStr);
				System.out.println(htmlStr);
				
				//GSON从json转java不好使
//				Gson gson = new Gson();
//				List<ZDMItem> lists = gson.fromJson("[{\"article_id\":\"379261\",\"article_title\":\"VERA WANG 王薇薇\"}]", new TypeToken<List<ZDMItem>>(){}.getType());
				
				ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
				JavaType javaType = mapper.getTypeFactory().constructParametrizedType(List.class, List.class, ZDMItem.class);
				List<ZDMItem> lists = mapper.readValue(htmlStr, javaType);
				
				System.out.println(lists.size());
			}	
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	


}
