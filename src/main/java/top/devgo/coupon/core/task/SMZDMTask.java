package top.devgo.coupon.core.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import top.devgo.coupon.utils.JsonUtil;
import top.devgo.coupon.utils.TextUtil;

public class SMZDMTask implements Task, Comparable<Task> {

	private String timesort;
	private int priority;
	
	public SMZDMTask(String timesort) {
		this(timesort, 1);
	}
	public SMZDMTask(String timesort, int priority) {
		this.timesort = timesort;
		this.priority = priority;
	}
	
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://www.smzdm.com/json_more")
				.addParameter("timesort", getTimesort())
				.setHeader("Host", "www.smzdm.com")
				.setHeader("Referer", "http://www.smzdm.com/")
				.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.setHeader("X-Requested-With", "XMLHttpRequest").build();
		return request;
	}


	public List<Task> process(CloseableHttpResponse response) {
		String htmlStr = getHtmlStr(response);
		
		htmlStr = TextUtil.decodeUnicode(htmlStr);
		htmlStr = JsonUtil.formateDoubleQuotationMarks(htmlStr);
		
		List<Map<String, String>> data = extractData(htmlStr);
		//TODO do sth with data
		System.out.println("get: "+data.size());
		
		List<Task> newTasks = new ArrayList<Task>();
		if (data.size() > 0) {
			SMZDMTask task = new SMZDMTask(data.get(data.size()-1).get("timesort"));
			newTasks.add(task);
		}
		return newTasks;
	}
	
	private List<Map<String, String>> extractData(String htmlStr){
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;  
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		JsonNode root = null;
		try {
			root = mapper.readTree(htmlStr);
			for (int i = 0; i < root.size(); i++) {
				JsonNode item = root.get(i);
				Map<String, String> map = new HashMap<String, String>();
				Iterator<Entry<String, JsonNode>> it = item.fields();
				while (it.hasNext()) {
					Entry<String, JsonNode> entry = (Entry<String, JsonNode>) it.next();
					map.put(entry.getKey(), entry.getValue().asText());
				}
				data.add(map);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	private String getHtmlStr(CloseableHttpResponse response) {
		String htmlStr = null;
		try {
			HttpEntity entity = response.getEntity();
		
			if (entity != null) {
				Header contentType = entity.getContentType();
				String encoding = "utf-8";
				if(contentType != null){
					String type = contentType.getValue();
					if(type != null){
						encoding = type.substring(type.lastIndexOf("charset=") + "charset=".length());
					}
				}
				htmlStr = EntityUtils.toString(entity, encoding);
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return htmlStr;
	}


	public String getTimesort() {
		return timesort;
	}


	public void setTimesort(String timesort) {
		this.timesort = timesort;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int compareTo(Task o) {
		return this.getPriority()-o.getPriority();
	}

}
