package top.devgo.coupon.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期处理工具类
 * @author dd
 *
 */
public class DateUtil {
	private DateUtil() {
	}
	
	/**
	 * 根据字符串生成时间
	 * @param timeStr 接受"2015-12-01 00:24", "01-14 10:11", "11:20"
	 * @return
	 * @throws ParseException
	 */
	public static Date getDateFromString(String timeStr) throws ParseException{
		SimpleDateFormat dateFormat;
		Calendar calendar = Calendar.getInstance();
		Date date;
		switch (timeStr.length()) {
		case 5://11:20
			dateFormat = new SimpleDateFormat("HH:mm");
			date = dateFormat.parse(timeStr);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DATE);
			calendar.setTime(date);
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.DATE, day);
			date = calendar.getTime();
			break;
			
		case 5+6://01-14 10:11
			dateFormat = new SimpleDateFormat("MM-dd HH:mm");
			date = dateFormat.parse(timeStr);
			year = calendar.get(Calendar.YEAR);
			calendar.setTime(date);
			calendar.set(Calendar.YEAR, year);
			date = calendar.getTime();
			break;

		default://2015-12-01 00:24
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			date = dateFormat.parse(timeStr);
			break;
		}
		
		return date;
	}

	/**
	 * 
	 * @param date
	 * @return "yyyy-MM-dd HH:mm:ss" or null(if date==null)
	 */
	public static String getDateString(Date date) {
		return getDateString(date, "yyyy-MM-dd HH:mm:ss");
	}
	
	/**
	 * 
	 * @param date
	 * @param formatString
	 * @return
	 */
	public static String getDateString(Date date, String formatString) {
		if (date == null)
			return null;
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
		return dateFormat.format(date);
	}
	
	/**
	 * 获取一天开始的时间
	 * @param date 日期 "2015-12-01 06:24:59.250"
	 * @return "2015-12-01 00:00:00.000"
	 */
	public static Date getBeginOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		return calendar.getTime();
	}

}
