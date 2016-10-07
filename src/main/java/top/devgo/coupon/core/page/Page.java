package top.devgo.coupon.core.page;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;

/**
 * 抓取结果的描述
 * @author DD
 *
 */
public class Page {
	/**
	 * The content of this page in binary format.
	 */
	private byte[] contentData;
	/**
	 * The ContentType of this page. For example: "text/html; charset=UTF-8"
	 */
	private String contentType;

	/**
	 * The charset of the content. For example: "UTF-8"
	 */
	private String contentCharset;
	
	private Map<String, Object> parsedData;//从originalHtml中抽取的有意义的数据集合
	
	/**
	 * Loads the content of this page from a fetched HttpEntity.
	 * @param entity HttpEntity
	 * @throws IOException 
	 */
	public void load(HttpEntity entity) throws IOException  {
		Header type = entity.getContentType();
		if (type != null) {
			setContentType(type.getValue());
		}
		Charset charset = ContentType.getOrDefault(entity).getCharset();
		if (charset != null) {
			setContentCharset(charset.displayName());
		}
		setContentData(EntityUtils.toByteArray(entity));
	}
	
	public Map<String, Object> getData() {
		return parsedData;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> getDataList() {
		if (parsedData==null) {
			return null;
		}
		return (List<Map<String, String>>) parsedData.get("data");
	}
	
	/**
	 * 从contentData中提取的数据集
	 * @param data
	 */
	public void setData(Map<String, Object> data) {
		this.parsedData = data;
	}
	
	/**
	 * 从contentData中提取的数据集
	 * @param data
	 */
	public void setData(List<Map<String, String>> data) {
		//如果数据集是list则以<"data", list>的map形式保存。
		Map<String, Object> pageData = new HashMap<String, Object>();
		pageData.put("data", data);
		setData(pageData);
	}

	public byte[] getContentData() {
		return contentData;
	}

	public void setContentData(byte[] contentData) {
		this.contentData = contentData;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentCharset() {
		return contentCharset;
	}

	public void setContentCharset(String contentCharset) {
		this.contentCharset = contentCharset;
	}
}
