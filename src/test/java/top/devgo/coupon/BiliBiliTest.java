package top.devgo.coupon;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import top.devgo.coupon.utils.TextUtil;

//GET http://www.bilibili.com/widget/ajaxGetComment?aid=6481958   ["", ""] size-20

//GET http://api.bilibili.com/x/tag/hots?rid=124&type=0&jsonp=json 获取tag组 rid是tInfo中的tid
//GET http://api.bilibili.com/x/tag/ranking/archives?jsonp=json&tag_id=13760&rid=98&pn=1 获取某个tag下的视频
//GET http://api.bilibili.com/archive_stat/stat?aid=2107393&type=json 获取某个视频的state
//GET http://api.bilibili.com/x/v2/reply?jsonp=json&type=1&sort=0&oid=2107393&pn=1&nohot=1 获取某个视频的回复

//GET https://interface.bilibili.com/playurl?cid=3269551&player=1&ts=1475458846&sign=05eb52ff858e04a8c78b5db71eba70c0
//获取真实地址，cid是视频文件名，player=1是appkey，ts和sign必有，是check用的
//cid从http://www.bilibili.com/video/av2107393/ 的html中抓取<script type='text/javascript'>EmbedPlayer('player', "http://static.hdslb.com/play.swf", "cid=3269551&aid=2107393&pre_ad=0");</script>
//ts=1475460664&sign=3901f33fdd7cc601b20a72f6e840ec29
//ts=Math.round(new Date().getTime()/1000)

// http://www.bilibili.com/m/html5?aid=2107393 
// http://api.bilibili.com/playurl?aid=2107393&platform=html5&type=json 

//GET http://comment.bilibili.com/3269551.xml  3269551视频文件名，得到对应的弹幕静态文件
//GET http://comment.bilibili.com/playtag,3269551  3269551视频文件名，得到相关推荐视频展示信息

//GET http://api.bilibili.com/x/tag/archive/tags?jsonp=json&aid=2107393 获取视频tag的详细信息

public class BiliBiliTest {
	
	/**
	 * (tname,tid)
	 */
	public static Map<String, String> tInfo = new HashMap<String, String>();
	
	public static void main(String[] args) throws ParseException, IOException {
		//影视
		tInfo.put("电影相关", "82");
		tInfo.put("短片", "85");
		tInfo.put("欧美电影", "145");
		tInfo.put("日本电影", "146");
		tInfo.put("国产电影", "147");
		tInfo.put("其他国家", "83");
		tInfo.put("连载剧集", "15");
		tInfo.put("完结剧集", "34");
		tInfo.put("特摄·布袋", "86");
		tInfo.put("电视剧相关", "128");
		//娱乐
		tInfo.put("综艺", "71");
		tInfo.put("明星", "137");
		tInfo.put("Korea相关", "131");
		//时尚
		tInfo.put("美妆", "157");
		tInfo.put("服饰", "158");
		tInfo.put("健身", "164");
		tInfo.put("资讯", "159");
		//生活
		tInfo.put("搞笑", "138");
		tInfo.put("日常", "21");
		tInfo.put("美食圈", "76");
		tInfo.put("动物圈", "75");
		tInfo.put("手工", "161");
		tInfo.put("绘画", "162");
		tInfo.put("运动", "163");
		//广告
		tInfo.put("广告", "166");
		//游戏
		tInfo.put("单机联机", "17");
		tInfo.put("网游·电竞", "65");
		tInfo.put("音游", "136");
		tInfo.put("Mugen", "19");
		tInfo.put("GMV", "121");
		//舞蹈
		tInfo.put("宅舞", "20");
		tInfo.put("三次元舞蹈", "154");
		tInfo.put("舞蹈教程", "156");
		//番剧
		tInfo.put("连载动画", "33");
		tInfo.put("完结动画", "32");
		tInfo.put("资讯", "51");
		tInfo.put("官方延伸", "152");
		//动画
		tInfo.put("MAD·AMV", "24");
		tInfo.put("MMD·3D", "25");
		tInfo.put("短片·手书·配音", "47");
		tInfo.put("综合", "27");
		//音乐
		tInfo.put("原创音乐", "28");
		tInfo.put("翻唱", "31");
		tInfo.put("VOCALOID·UTAU", "30");
		tInfo.put("演奏", "59");
		tInfo.put("三次元音乐", "29");
		tInfo.put("OP/ED/OST", "54");
		tInfo.put("音乐选集", "130");
		//科技
		tInfo.put("纪录片", "37");
		tInfo.put("趣味科普人文", "124");
		tInfo.put("野生技术协会", "122");
		tInfo.put("演讲•公开课", "39");
		tInfo.put("星海", "96");
		tInfo.put("数码", "95");
		tInfo.put("机械", "98");
		//鬼畜
		tInfo.put("鬼畜调教", "22");
		tInfo.put("音MAD", "26");
		tInfo.put("人力VOCALOID", "126");
		tInfo.put("教程演示", "127");
		
		fetch(tInfo.get("人力VOCALOID"), "1");
	}

	private static void fetch(String tid, String page) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createDefault();


		HttpUriRequest request = RequestBuilder
				.get()
				.setUri("http://api.bilibili.com/archive_rank/getarchiverankbypartion")
				.addParameter("type", "json")
				.addParameter("tid", tid)
				.addParameter("pn", page)
				.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36")
				.build();
		
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		try {
			HttpEntity entity = response.getEntity();
			
			Header contentType = entity.getContentType();
			String encoding = "utf-8";
			
			if (entity != null) {
				String htmlStr = EntityUtils.toString(entity, encoding);
				
				htmlStr = TextUtil.decodeUnicode2(htmlStr);
				System.out.println(htmlStr);
				
				ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
				mapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true) ;  
				mapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
				Map<String, Object> result = mapper.readValue(htmlStr, new TypeReference<Map<String, Object>>() { } );
				
				if ((int)result.get("code") == 0) {
					List data = (List) ((Map)result.get("data")).get("archives");
					Map p = (Map) ((Map)result.get("data")).get("page");
					System.out.println(data.size());
					System.out.println(p.get("count"));
					System.out.println(p.get("num"));
					System.out.println(p.get("size"));
				}
			}
		}finally {
			if (response != null) {
				response.close();
			}
		}
	}
}
