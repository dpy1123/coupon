package top.devgo.coupon;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ansj.app.web.AnsjServlet;
import org.ansj.util.MyStaticValue;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpHead;
import org.bson.Document;
import org.nlpcn.commons.lang.util.StringUtil;

import top.devgo.coupon.utils.MongoDBUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
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
				String path = httpExchange.getRequestURI().getPath();
//				if (path != null && path.startsWith("/page")) {
//					writeToClient(httpExchange, readFileToString(path));
//					return;
//				}

				String responseMsg = "test"; // 响应信息
//				Map<String, String> paramers = parseParamers(httpExchange);
//				String input = paramers.get("input");
//				String method = paramers.get("method");
//				String nature = paramers.get("nature");
//				if (StringUtil.isNotBlank(input)) {
//					responseMsg = AnsjServlet.processRequest(input, method, nature);
//				}
				
				FindIterable<Document> iterable = MongoDBUtil.find(null, "mongodb://localhost:27017", "coupon", "smzdm_data");
				int size = 0;
				List<Document> list = new ArrayList<Document>();
				for (Document document : iterable) {
					size++;
					list.add(document);
//			        System.out.println(document);
				}
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("total", size);
				data.put("data", list);
				
				
				ObjectMapper mapper = new ObjectMapper(); 
				String json = mapper.writeValueAsString(data);
				System.out.println(size);
				writeToClient(httpExchange, json);

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				try {
					writeToClient(httpExchange, e.getMessage());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("eee");
				}

			} finally {
				httpExchange.close();
			}
		}
		
		private void writeToClient(HttpExchange httpExchange, String responseMsg) throws IOException {
			byte[] bytes = responseMsg.getBytes("utf-8");
			httpExchange.sendResponseHeaders(200, bytes.length); // 设置响应头属性及响应信息的长度
			OutputStream out = httpExchange.getResponseBody(); // 获得输出流
			out.write(bytes);
			out.flush();
		}
		
	}
}
