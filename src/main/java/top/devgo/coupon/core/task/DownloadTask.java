package top.devgo.coupon.core.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.log4j.Logger;

import top.devgo.coupon.core.page.Page;
import top.devgo.coupon.utils.IOUtil;

/**
 * 通用资源下载task
 * @author DD
 *
 */
public class DownloadTask extends TaskBase {

	private static Logger logger = Logger.getLogger(DownloadTask.class);
	private String fetchUrl;
	private String filePath;
	private String fileName;
	
	/**
	 * 初始化task
	 * @param priority 优先级
	 * @param fetchUrl 下载url，如果含有参数也拼在url后面
	 * @param filePath 保存路径
	 * @param fileName 保存文件名
	 */
	public DownloadTask(int priority, String fetchUrl, String filePath, String fileName) {
		super(priority);
		this.fetchUrl = fetchUrl;
		this.filePath = filePath;
		this.fileName = fileName;
	}

	@Override
	public HttpUriRequest buildRequest() {
		HttpUriRequest request = RequestBuilder
				.get()
				.setUri(this.fetchUrl)
				.setHeader( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.build();
		return request;
	}

	@Override
	protected void process(Page page) {
		// store image
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(filePath, fileName));
			fos.write(page.getContentData());
		} catch (IOException e) {
			logger.error("", e);
		} finally {
			IOUtil.close(fos);
		}
	}

	@Override
	protected List<Task> buildNewTask(Page page) {
		return null;
	}

}
