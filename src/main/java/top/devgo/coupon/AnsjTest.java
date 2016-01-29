package top.devgo.coupon;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.DownLibrary;
import org.nlpcn.commons.lang.tire.GetWord;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.library.Library;

public class AnsjTest {

	public static void main(String[] args) {
		String text = "SONY 索尼MDR-1A 头戴式耳机采用40mm的动圈单元，单元振膜采用了镀铝液晶高分子振膜，在全频段减少声音的失真，加上CCAW音圈使得整体可传递出雄浑、强劲的低音和优秀的高音。阻抗24欧姆、灵敏度105dB使得耳机很容易推动。";
		List<Term> parse = ToAnalysis.parse(text );
	    System.out.println(parse);
//	    try {
//			DownLibrary.main(null);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	    List<Term> parse2 = NlpAnalysis.parse(text );
//	    System.out.println(parse2);
//	    List<Term> parse3 = IndexAnalysis.parse(text );
//	    System.out.println(parse3);
	    
	    
//	    KeyWordComputer kwc = new KeyWordComputer(5);
	    String title = "维基解密否认斯诺登接受委内瑞拉庇护";
	    String t = "SONY 索尼 MDR-1A 头戴式耳机 999元包邮";
	    String t2 = "JVC 杰伟世 HA-FXT200LTD 限量版 双动圈 入耳式耳机9080日元（约￥540）";
	    String content = "有俄罗斯国会议员，9号在社交网站推特表示，美国中情局前雇员斯诺登，已经接受委内瑞拉的庇护，不过推文在发布几分钟后随即删除。俄罗斯当局拒绝发表评论，而一直协助斯诺登的维基解密否认他将投靠委内瑞拉。　　俄罗斯国会国际事务委员会主席普什科夫，在个人推特率先披露斯诺登已接受委内瑞拉的庇护建议，令外界以为斯诺登的动向终于有新进展。　　不过推文在几分钟内旋即被删除，普什科夫澄清他是看到俄罗斯国营电视台的新闻才这样说，而电视台已经作出否认，称普什科夫是误解了新闻内容。　　委内瑞拉驻莫斯科大使馆、俄罗斯总统府发言人、以及外交部都拒绝发表评论。而维基解密就否认斯诺登已正式接受委内瑞拉的庇护，说会在适当时间公布有关决定。　　斯诺登相信目前还在莫斯科谢列梅捷沃机场，已滞留两个多星期。他早前向约20个国家提交庇护申请，委内瑞拉、尼加拉瓜和玻利维亚，先后表示答应，不过斯诺登还没作出决定。　　而另一场外交风波，玻利维亚总统莫拉莱斯的专机上星期被欧洲多国以怀疑斯诺登在机上为由拒绝过境事件，涉事国家之一的西班牙突然转口风，外长马加略]号表示愿意就任何误解致歉，但强调当时当局没有关闭领空或不许专机降落。";
	    String content2 = "有俄罗斯国会议员，9号在社交网站推特表示，美国中情局前雇员斯诺登，已经接受委内瑞拉的庇护，不过推文在发布几分钟后随即删除。俄罗斯当局拒绝发表评论，而一直协助斯诺登的维基解密否认他将投靠委内瑞拉。　　俄罗斯国会国际事务委员会主席普什科夫，在个人推特率先披露斯诺登已接受委内瑞拉的庇护建议，令外界以为斯诺登的动向终于有新进展。　　不过推文在几分钟内旋即被删除，普什科夫澄清他是看到俄罗斯国营电视台的新闻才这样说，而电视台已经作出否认，称普什科夫是误解了新闻内容。　　委内瑞拉驻莫斯科大使馆、俄罗斯总统府发言人、以及外交部都拒绝发表评论。而维基解密就否认斯诺登已正式接受委内瑞拉的庇护，说会在适当时间公布有关决定。　　斯诺登相信目前还在莫斯科谢列梅捷沃机场，已滞留两个多星期。他早前向约20个国家提交庇护申请，委内瑞拉、尼加拉瓜和玻利维亚，先后表示答应，不过斯诺登还没作出决定。　　而另一场外交风波，玻利维亚总统莫拉莱斯的专机上星期被欧洲多国以怀疑斯诺登在机上为由拒绝过境事件，涉事国家之一的西班牙突然转口风，外长马加略]号表示愿意就任何误解致歉，但强调当时当局没有关闭领空或不许专机降落。";
//        Collection<Keyword> result = kwc.computeArticleTfidf(t, text);
//        System.out.println(result);
	    
	    
	    UserDefineLibrary.insertWord("头戴式耳机", "nl", 1000);
	    UserDefineLibrary.insertWord("mdr-1a", "nz", 1000);
        List<Term> parse2 = ToAnalysis.parse(text);
        System.out.println(parse2);
        
        new NatureRecognition(parse2).recognition(); //词性标注
        System.out.println(parse2);

	}

}
