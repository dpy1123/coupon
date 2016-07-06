package top.devgo.coupon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import top.devgo.coupon.utils.JsonUtil;

public class MGPYH {

	public static void main(String[] args) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://www.mgpyh.com/api/v3/post/")
				.addParameter("after", "1466061600")
				.setHeader("Host", "www.mgpyh.com")
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
			
			
			if (entity != null) {
				Header contentType = entity.getContentType();
				String encoding = "utf-8";
				if(contentType != null){
					String type = contentType.getValue();
					if(type != null && type.startsWith("text/html")){//Content-Type:text/html;charset=utf-8;
						encoding = type.substring(type.lastIndexOf("charset=") + "charset=".length());
						int pos = encoding.lastIndexOf(";");
						if(pos > -1){
							encoding = encoding.substring(0, pos);
						}
					}
				}
//				System.out.println(encoding);
				
				String htmlStr = EntityUtils.toString(entity, encoding);
//				htmlStr = TextUtil.decodeUnicode(htmlStr);
//				System.out.println(htmlStr);
				
				htmlStr = JsonUtil.formateDoubleQuotationMarks(htmlStr);
				System.out.println(htmlStr);
				
				List<Map<String, String>> data = extractData(htmlStr);
				System.out.println(data);

			}	
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	private static List<Map<String, String>> extractData(String jsonString){
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;  
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		JsonNode root = null;
		try {
			root = mapper.readTree(jsonString);
		} catch (IOException e) {
			if (e instanceof JsonParseException) {
				try {
					String after = JsonUtil.formateJsonArrays(jsonString);
					root = mapper.readTree(after);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}else {
				e.printStackTrace();
			}
		}
		if (root != null) {
			root = root.get("objects");
			for (int i = 0; i < root.size(); i++) {
				JsonNode item = root.get(i);
				if(item != null){
					Map<String, String> map = new HashMap<String, String>();
					Iterator<Entry<String, JsonNode>> it = item.fields();
					while (it.hasNext()) {
						Entry<String, JsonNode> entry = (Entry<String, JsonNode>) it.next();
						map.put(entry.getKey(), entry.getValue().asText());
					}
					data.add(map);
				}
			}
		}
		return data;
	}
}
