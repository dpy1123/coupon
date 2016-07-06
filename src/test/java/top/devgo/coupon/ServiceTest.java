package top.devgo.coupon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ansj.app.web.AnsjServlet;
import org.ansj.util.MyStaticValue;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpHead;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;

import top.devgo.coupon.utils.DateUtil;
import top.devgo.coupon.utils.MongoDBUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

public class ServiceTest {
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
				String responseMsg = "test"; // 响应信息
				
				// /v1/smzdm_data/list?page=1&size=10&query=
				String path = httpExchange.getRequestURI().getPath().trim();
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
				
				System.out.println(path);

				Map<String, String> paramers = parseParamers(httpExchange);
				String page = paramers.get("page")==null?"1":paramers.get("page");
				String pageSize = paramers.get("size")==null?"10":paramers.get("size");
				String query = paramers.get("query");
//				if (StringUtil.isNotBlank(input)) {
//					responseMsg = AnsjServlet.processRequest(input, method, nature);
//				}
				Date today = DateUtil.getDateFromString("00:00");
				System.out.println(today);
//				FindIterable<Document> iterable = MongoDBUtil.find(Filters.gt("article_date_full", DateUtil.getDateString(today)), "mongodb://localhost:27017", "coupon", "smzdm_data")
				FindIterable<Document> iterable = MongoDBUtil.find((Bson) JSON.parse("{\"article_date_full\": {\"$gt\": \""+DateUtil.getDateString(today)+"\"}}"), "mongodb://localhost:27017", "coupon", "smzdm_data")
						.skip((Integer.parseInt(page)-1)*Integer.parseInt(pageSize))
						.limit(Integer.parseInt(pageSize));
				List<Document> list = new ArrayList<Document>();
				for (Document document : iterable) {
					list.add(document);
//			        System.out.println(document);
				}
				Map<String, Object> data = new HashMap<String, Object>();
//				data.put("total", size);
				data.put("data", list);
				
				
				ObjectMapper mapper = new ObjectMapper(); 
				String json = mapper.writeValueAsString(data);
				writeToClient(httpExchange, 200, json);

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				try {
					writeToClient(httpExchange, 400, e.getMessage());
				} catch (IOException e1) {
					System.out.println("eee");
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
