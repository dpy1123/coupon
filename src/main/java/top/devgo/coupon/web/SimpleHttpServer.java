package top.devgo.coupon.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

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
	public static void main(String[] args) throws IOException {
		HttpServerProvider provider = HttpServerProvider.provider();
		HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(8877), 100);// 监听端口6666,能同时接
																									// 受100个请求
		httpserver.createContext("/", new CouponHttpHandler());
		httpserver.setExecutor(null);
		httpserver.start();
		System.out.println("server started");
	}
	
	private static class CouponHttpHandler implements HttpHandler {

		public void handle(HttpExchange httpExchange) throws IOException {
			try {
				String responseMsg = null; // 响应信息
				
				// /v1/smzdm_data/list?page=1&size=10&query=
				String path = httpExchange.getRequestURI().getPath().trim();
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
				// v1/smzdm_data/list
				path = path.substring(path.indexOf("/")+1, path.contains("?")?path.indexOf("?"):path.length());
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
					responseMsg = ApiService.processRequest(collection, function, paramers);
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
		
	}
}
