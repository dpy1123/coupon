package top.devgo.coupon.core.task.bilibili;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置类
 * @author DD
 *
 */
public class BilibiliConfig {

	private static final Map<String, String> tInfo = new HashMap<String, String>();
	
	static {
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
	}

	/**
	 * 根据频道名称获取tid
	 * @param channelName
	 * @return
	 */
	public static String getTid(String channelName){
		return tInfo.get(channelName);
	}
	
	public static String[] getAllTids(){
		return tInfo.keySet().toArray(new String[tInfo.size()]);
	}
}
