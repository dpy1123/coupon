package top.devgo.coupon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.dic.LearnTool;
import org.ansj.domain.Nature;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.nlpcn.commons.lang.tire.GetWord;
import org.nlpcn.commons.lang.util.StringUtil;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import top.devgo.coupon.utils.JsonUtil;
import top.devgo.coupon.utils.TextUtil;

public class SMZDMImage {
	public static void main(String[] args) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
//		HttpGet httpget = new HttpGet("http://www.smzdm.com/json_more?timesort=255070648939");
//		httpget.setHeader("Host", "www.smzdm.com");
//		httpget.setHeader("Referer", "http://www.smzdm.com/");
//		httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36");
//		httpget.setHeader("X-Requested-With", "XMLHttpRequest");
		
		
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://y.zdmimg.com/201602/10/56bb5a25cc4815433.png_d200.jpg")
//				.setHeader("Host", "y.zdmimg.com")
//				.setHeader("Referer", "http://www.smzdm.com/")
				.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.setHeader("X-Requested-With", "XMLHttpRequest").build();
		
		CloseableHttpResponse response = null;
		try {
//			response = httpclient.execute(httpget);
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
					
					switch (type) {
					case "image/png":
						InputStream in = entity.getContent();
						
					    // get a unique name for storing this image
						String url = request.getURI().toString();
					    String extension = url.substring(url.lastIndexOf('.'));
					    String hashedName = UUID.randomUUID() + extension;
					
					    // store image
					    File storageFolder = new File(System.getProperty("user.dir") +"\\img");
					    if (!storageFolder.exists()) {
					      storageFolder.mkdirs();
					    }
					    String filename = storageFolder.getAbsolutePath() + "/" + hashedName;
					    
						byte[] buffer = new byte[1024];
						int read = 0;
						OutputStream fos = new FileOutputStream(new File(filename));
						while ((read  = in.read(buffer)) > -1) {
							fos.write(buffer, 0, read);
						}
						fos.flush();
						fos.close();
						in.close();
						
						break;

					default://Content-Type:text/html;charset=utf-8;
						encoding = type.substring(type.lastIndexOf("charset=") + "charset=".length());
						int pos = encoding.lastIndexOf(";");
						if(pos > -1){
							encoding = encoding.substring(0, pos);
						}
						
						String htmlStr = EntityUtils.toString(entity, encoding);
						htmlStr = TextUtil.decodeUnicode(htmlStr);
						
						htmlStr = JsonUtil.formateDoubleQuotationMarks(htmlStr);
						
						System.out.println(htmlStr);

						
						ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
						
						mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;  
						mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
						
						
						JsonNode root = mapper.readTree(htmlStr);
						System.out.println(root.size());
						
						KeyWordComputer kwc = new KeyWordComputer(5);
						LearnTool learnTool = new LearnTool() ;
						for (int i = 0; i < root.size(); i++) {
							JsonNode item = root.get(i);
							String title = item.get("article_title").asText();
							String content = item.get("article_content_all").asText();
							Collection<Keyword> result = kwc.computeArticleTfidf(title, StringUtil.rmHtmlTag(content));
							
					        System.out.println(title+"\t"+result);
					        
						}
						break;
					}
				}
			}	
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}
	
	
}
