package top.devgo.coupon.utils;

/**
 * json字符串处理的工具类
 * 
 * @author DD
 *
 */
public class JsonUtil {
	/**
	 * 将内容中的双引号'"'替换为'”',以确保jsonStr的正确解析
	 * @param jsonStr
	 * @return
	 */
	public static String formateDoubleQuotationMarks(String jsonStr) {
		char[] temp = jsonStr.toCharArray();
		int n = temp.length;
		for (int i = 0; i < n; i++) {
			if (temp[i] == ':' && temp[i + 1] == '"') {
				for (int j = i + 2; j < n; j++) {
					if (temp[j] == '"') {
						if (temp[j + 1] != ',' && temp[j + 1] != '}') {
							temp[j] = '”';
						} else if (temp[j + 1] == ',' || temp[j + 1] == '}') {
							break;
						}
					}
				}
			}
		}
		return new String(temp);
	}
}
