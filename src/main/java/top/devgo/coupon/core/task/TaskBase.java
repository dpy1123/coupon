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
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import top.devgo.coupon.core.page.Page;

/**
 * 所有业务Task类继承本类
 * @author DD
 *
 */
public abstract class TaskBase implements Task, Comparable<Task> {

	protected int priority;//优先级
	
	public TaskBase(int priority) {
		this.priority = priority;
	}
	
	/**
	 * 获取内容
	 * @param response
	 * @return
	 */
	protected abstract Page buildPage(CloseableHttpResponse response);
	/**
	 * 处理获取的内容
	 * @param page
	 */
	protected abstract void process(Page page);
	/**
	 * 获取新的任务
	 * @param page
	 * @return
	 */
	protected abstract List<Task> buildNewTask(Page page);

	
	public List<Task> process(CloseableHttpResponse response) {
		//1.获取内容
		Page page = buildPage(response);
		//2.处理获取的内容
		process(page);
		//3.返回获取的新任务
		return buildNewTask(page);
	}
	
	/**
	 * 从jsonString获取数据
	 * @param jsonString
	 * @return data.size()==0表示无数据
	 */
	protected List<Map<String, String>> extractData(String jsonString){
		List<Map<String, String>> data = new ArrayList<Map<String,String>>();
		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
		mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;  
		mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		JsonNode root = null;
		try {
			root = mapper.readTree(jsonString);
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
	
	/**
	 * 获取htmlString
	 * @param response
	 * @return
	 */
	protected String getHtmlStr(CloseableHttpResponse response) {
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



	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * 优先级比较逻辑
	 */
	public int compareTo(Task o) {
		return this.getPriority()-o.getPriority();
	}

}
