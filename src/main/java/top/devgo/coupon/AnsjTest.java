package top.devgo.coupon;

import java.util.List;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.DownLibrary;

public class AnsjTest {

	public static void main(String[] args) {
		String text = "SONY 索尼MDR-1A 头戴式耳机采用40mm的动圈单元，单元振膜采用了镀铝液晶高分子振膜，在全频段减少声音的失真，加上CCAW音圈使得整体可传递出雄浑、强劲的低音和优秀的高音。阻抗24欧姆、灵敏度105dB使得耳机很容易推动。";
		List<Term> parse = ToAnalysis.parse(text );
	    System.out.println(parse);
	    List<Term> parse2 = NlpAnalysis.parse(text );
	    System.out.println(parse2);
//	    List<Term> parse3 = IndexAnalysis.parse(text );
//	    System.out.println(parse3);
	}

}
