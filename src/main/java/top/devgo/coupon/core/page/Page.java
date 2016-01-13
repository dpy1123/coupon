package top.devgo.coupon.core.page;

import java.util.List;
import java.util.Map;

/**
 * 抓取结果的描述
 * @author DD
 *
 */
public class Page {
	private String originalHtml;//原始htmlString
	private List<Map<String, String>> data;//从originalHtml中抽取的有意义的数据集合
	
	
	public String getOriginalHtml() {
		return originalHtml;
	}
	public void setOriginalHtml(String originalHtml) {
		this.originalHtml = originalHtml;
	}
	public List<Map<String, String>> getData() {
		return data;
	}
	public void setData(List<Map<String, String>> data) {
		this.data = data;
	}
}
