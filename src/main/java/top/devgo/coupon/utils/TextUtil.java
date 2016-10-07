package top.devgo.coupon.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文字编码转换的工具类
 * @author DD
 *
 */
public class TextUtil {
	private TextUtil() {
	}
	
	/**
	 * Unicode编码转换成中文<br>
	 * eg: "\u7b80\u4ecb" -> "中国"<br>
	 * 【这种方法有问题，复杂jsonstr有可能错误解析】用decodeUnicode2
	 * @param theString
	 * @return
	 */
	@Deprecated
	public static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuilder outBuffer = new StringBuilder(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
	
	/**
	 * unicode转字符串
	 * ps: 2016\\u5df4\\u9ece
	 * @param unicode
	 * @return
	 */
	public static String decodeUnicode2(String unicode) {
		Charset set = Charset.forName("UTF-16");
		Pattern p = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
		Matcher m = p.matcher(unicode);
		int start = 0;
		int start2;
		StringBuilder sb = new StringBuilder();
		while (m.find(start)) {
			start2 = m.start();
			if (start2 > start) {
				String seg = unicode.substring(start, start2);
				sb.append(seg);
			}
			String code = m.group(1);
			int i = Integer.valueOf(code, 16);
			byte[] bb = new byte[4];
			bb[0] = (byte) ((i >> 8) & 0xFF);
			bb[1] = (byte) (i & 0xFF);
			ByteBuffer b = ByteBuffer.wrap(bb);
			sb.append(String.valueOf(set.decode(b)).trim());
			start = m.end();
		}
		start2 = unicode.length();
		if (start2 > start) {
			String seg = unicode.substring(start, start2);
			sb.append(seg);
		}
		return sb.toString();
	}

}
