package top.devgo.coupon.web;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import top.devgo.coupon.utils.IOUtil;
import top.devgo.coupon.utils.StringUtil;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

/**
 * 提供httpService用的简单server
 * @author dd
 *
 */
public class SimpleHttpServer {

	private String mongoUrl;
	private String dbName;

	public SimpleHttpServer(String mongoUrl, String dbName) {
		this.mongoUrl = mongoUrl;
        this.dbName = dbName;
	}

	/**
	 *
	 * @param port 监听端口
	 * @param maxConcurrency 最大并发数, 达到此数值后的请求会被丢弃
	 * @param executor 执行器,可空
	 * @throws IOException
	 */
	public void start(int port, int maxConcurrency, Executor executor) throws IOException {
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(port), maxConcurrency);
		httpserver.createContext("/", new CouponHttpHandler());
		httpserver.setExecutor(executor);
		httpserver.start();
		System.out.println("SimpleHttpServer start!");
	}
	
	public class CouponHttpHandler implements HttpHandler {

		public void handle(HttpExchange httpExchange) throws IOException {
			try {
				String responseMsg = null; // 响应信息
				// http://host/version/collection/function?params
				String path = httpExchange.getRequestURI().getPath().trim();
				
				if (path != null && path.startsWith("/img")) {
					writeToClient(httpExchange, 200, readFileToBytes(path));
					return;
				}
				
				if ("/".equals(path)) {
					//TODO 访问根目录则返回可用的api列表
					/*
					[{
					  "message":   "get collections",
					  "urlPattern":  "http://api.example.com/zoos",
					  "example": "http://api.example.com/zoos",
					}]
					*/
					responseMsg = "";
					writeToClient(httpExchange, 200, responseMsg);
					return;
				}
				// /version/collection/function?params
				path = path.substring(path.indexOf("/")+1, path.contains("?")?path.indexOf("?"):path.length());
				// version/collection/function
				String[] call = path.split("/");
				if (call.length != 3) {
					responseMsg = "{error: \"请求有误，请求格式应为: http://host/version/collection/function?params\"}";
					writeToClient(httpExchange, 400, responseMsg);
					return;
				}
				String version = call[0];
				if (!"v1".equals(version.toLowerCase())) {
					responseMsg = "{error: \"请求有误，目前支持version: v1\"}";
					writeToClient(httpExchange, 400, responseMsg);
					return;
				}
				
				String collection = call[1];
				String function = call[2];
				Map<String, String> paramers = parseParamers(httpExchange);
				if (StringUtil.isNotBlank(collection) && StringUtil.isNotBlank(function)) {
					responseMsg = ApiService.processRequest(mongoUrl, dbName, collection, function, paramers);
				}
				writeToClient(httpExchange, 200, responseMsg);

			} catch (Exception e) {
				e.printStackTrace();
				try {
					writeToClient(httpExchange, 400, e.getMessage());
				} catch (IOException e1) {
				}
			} finally {
				httpExchange.close();
			}
		}
		
		private Map<String, String> parseParamers(HttpExchange httpExchange) throws UnsupportedEncodingException, IOException {
			BufferedReader reader = null;
			try {
				Map<String, String> parameters = new HashMap<String, String>();
				URI requestedUri = httpExchange.getRequestURI();
				String query = requestedUri.getRawQuery();
				// get 请求解析
				parseQuery(query, parameters);
				// post 请求解析
				reader = IOUtil.getReader(httpExchange.getRequestBody(), "utf-8");
				query = IOUtil.getContent(reader).trim();
				parseQuery(query, parameters);
				httpExchange.setAttribute("parameters", parameters);
				return parameters;
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}

		/**
		 * 从get请求中解析参数
		 * 
		 * @param query
		 * @param parameters
		 */
		private void parseQuery(String query, Map<String, String> parameters) {
			if (StringUtil.isBlank(query)) {
				return;
			}
			String[] split = query.split("\\?");
			query = split[split.length - 1];
			split = query.split("&");
			String[] param = null;
			String key = null;
			String value = null;
			for (String kv : split) {
				try {
					param = kv.split("=");
					if (param.length == 2) {
						key = URLDecoder.decode(param[0], "utf-8");
						value = URLDecoder.decode(param[1], "utf-8");
						parameters.put(key, value);
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
		
		private byte[] readFileToBytes(String path) {
			InputStream fin = null;
			try {
				fin = new FileInputStream(System.getProperty("user.dir") + path);
				fin.available();
				return IOUtil.getContent(fin);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				IOUtil.close(fin);
			}
		}
		
		/**
		 * 向客户端回写信息
		 * @param httpExchange
		 * @param status	http状态码
		 * @param responseMsg
		 * @throws IOException
		 */
		private void writeToClient(HttpExchange httpExchange, int status, String responseMsg) throws IOException {
			byte[] bytes = responseMsg.getBytes("utf-8");
			httpExchange.sendResponseHeaders(status, bytes.length); // 设置响应头属性及响应信息的长度
			OutputStream out = httpExchange.getResponseBody(); // 获得输出流
			out.write(bytes);
			out.flush();
		}
		
		private void writeToClient(HttpExchange httpExchange, int status, byte[] bytes) throws IOException {
			httpExchange.sendResponseHeaders(status, bytes.length); // 设置响应头属性及响应信息的长度
			OutputStream out = httpExchange.getResponseBody(); // 获得输出流
			out.write(bytes);
			out.flush();
		}
		
	}
}
