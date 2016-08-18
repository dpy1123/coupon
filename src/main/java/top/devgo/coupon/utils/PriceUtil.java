package top.devgo.coupon.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceUtil {
	
	/**
	 * 从priceStr中抽取价格str
	 * @param priceStr 形如：
	  			"$59.99（约￥475）",
				"$315（需用码，约￥2140.09）",
				"619元包邮（669-50）",
				"69元，可99-50",
				"£22.08+£10.09直邮中国（约￥285）"
	 * @return 如果priceStr为null，或者没有匹配项，默认返回"0"
	 */
	public static String getRealPrice(String priceStr){
		String realPrice = "0";
		if (priceStr == null) {
			return realPrice;
		}
		Matcher rmbMatcher = Pattern.compile("￥\\d+(\\.\\d{1,2})?").matcher(priceStr);
		if (rmbMatcher.find()) {
			realPrice = rmbMatcher.group().replaceFirst("￥", "");
			return realPrice;
		}
		Matcher discountMatcher = Pattern.compile("\\d*-\\d*").matcher(priceStr);
		if (discountMatcher.find()) {
			priceStr = discountMatcher.replaceAll("");
		}
		Matcher numberMatcher = Pattern.compile("\\d+(\\.\\d{1,2})?").matcher(priceStr);
		if (numberMatcher.find()) {
			realPrice = numberMatcher.group();
		}
		return realPrice;
	}
	
	public static void main(String[] args) {
		String[] priceStr = {
				"$59.99（约￥475）",
				"$315（需用码，约￥2140.09）",
				"619元包邮（669-50）",
				"69元，可99-50",
				"£22.08+£10.09直邮中国（约￥285）",
				};
		String[] priceArry = new String[priceStr.length];
		
		for (int i = 0; i < priceStr.length; i++) {
			priceArry[i] = getRealPrice(priceStr[i]);
		}
		
		for (String str : priceArry) {
			System.out.println(str);
		}
	}
}
