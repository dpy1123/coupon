package top.devgo.coupon.core.task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.utils.JsonUtil;

/**
 * 所有业务Task类继承本类
 * @author DD
 *
 */
public abstract class TaskBase implements Task, Comparable<Task> {

	private static Logger logger = Logger.getLogger(TaskBase.class.getName());
	
	protected int priority;//优先级
	
	public TaskBase(int priority) {
		this.priority = priority;
	}
	
	/**
	 * 获取内容
	 * @param response
	 * @return
	 */
	protected Page buildPage(CloseableHttpResponse response) {
		Page page = new Page();
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			try {
				page.load(entity);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		return page;
	}
	
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
		} catch (IOException e) {
			if (e instanceof JsonParseException) {
				logger.info("jackson json解析报错: "+e+" ，尝试使用 JsonUtil.formateJsonArrays 解析。");
				try {
					String after = JsonUtil.formateJsonArrays(jsonString);
					root = mapper.readTree(after);
				} catch (Exception e1) {
					logger.error("解析失败： str= \r\n"+jsonString, e1);
				}
			}else {
				logger.error(e);
			}
		}
		if (root != null) {
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
	
	/**
	 * 从Page中获取htmlString
	 * @param page
	 * @return
	 */
	protected String getHtmlStr(Page page) {
		String htmlStr = null;
		String type = page.getContentType();
		if(type != null && type.startsWith("text/html")){//text/html;charset=utf-8;
			try {
				htmlStr = new String(page.getContentData(), page.getContentCharset());
			} catch (UnsupportedEncodingException e) {
				logger.error("", e);
			}
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
