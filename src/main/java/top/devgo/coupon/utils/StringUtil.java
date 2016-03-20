package top.devgo.coupon.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具类
 * @author dd
 *
 */
public class StringUtil {
	/**
	 * 判断字符串是否为空
	 *
	 * @param cs
	 * @return
	 */
	public static boolean isBlank(CharSequence cs) {
		int strLen;
		if (cs == null || (strLen = cs.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if (Character.isWhitespace(cs.charAt(i)) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断字符串是否不为空
	 *
	 * @param cs
	 * @return
	 */
	public static boolean isNotBlank(CharSequence cs) {
		return !isBlank(cs);

	}
	

    /**
     * 判断字符是否是中文
     *
     * @param c 字符
     * @return 是否是中文
     */
    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }
 
    /**
     * 判断字符串是否是乱码
     *
     * @param strName 字符串
     * @return 是否是乱码
     */
    public static boolean isMessyCode(String strName) {
        Pattern p = Pattern.compile("\\s*|t*|r*|n*");
        Matcher m = p.matcher(strName);
        String after = m.replaceAll("");
        String temp = after.replaceAll("\\p{P}", "");
        char[] ch = temp.trim().toCharArray();
        float chLength = ch.length;
        float count = 0;
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (!Character.isLetterOrDigit(c)) {
                if (!isChinese(c)) {
                    count = count + 1;
                }
            }
        }
        float result = count / chLength;
        if (result > 0.4) {
            return true;
        } else {
            return false;
        }
 
    }
    
    /**
     * Levenshtein Distance 算法实现 
     * @param source
     * @param target
     * @return 两个字符串的编辑距离
     */
	public static int editDistance(String source, String target) {
		int i, j, MAX_STRING_LEN = Math.max(source.length(), target.length())+1;
		int d[][] = new int[MAX_STRING_LEN][MAX_STRING_LEN];

		for (i = 0; i <= source.length(); i++)
			d[i][0] = i;
		for (j = 0; j <= target.length(); j++)
			d[0][j] = j;

		for (i = 1; i <= source.length(); i++) {
			for (j = 1; j <= target.length(); j++) {
				if (source.charAt(i - 1) == target.charAt(j - 1)) {
					d[i][j] = d[i - 1][j - 1]; // 不需要编辑操作
				} else {
					int edIns = d[i][j - 1] + 1; // source 插入字符
					int edDel = d[i - 1][j] + 1; // source 删除字符
					int edRep = d[i - 1][j - 1] + 1; // source 替换字符
					d[i][j] = Math.min(Math.min(edIns, edDel), edRep);
				}
			}
		}
		return d[source.length()][target.length()];
	}
	
}
