package com.olunx.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Utils u = new Utils();
		// Calendar cal = Calendar.getInstance();
		// cal.set(1970, 1, 1, 0, 0, 0);
		// System.out.println(Locale.US.getDisplayName());
		// cal.setTimeZone(TimeZone.getDefault());
		// System.out.println("cal: " + cal.getTime());
		System.out.println(u.getTimestampFromDaysAgo(5));
	}

	public static final String CST_TIME_FORMATE = "";
	public static Utils utils;

	public static Utils init() {
		if (utils == null) {
			utils = new Utils();
		}
		return utils;
	}

	/**
	 * 格式化为统一的日期
	 * 
	 * @param date
	 * @return
	 */
	public String getCstTime(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
		return sdf.format(date);
	}

	/**
	 * 将日期转换为时间戳
	 * 
	 * @param dateStr
	 * @return
	 */
	public long getTimestamp(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
		Date ori = new Date();
		Date date = new Date();
		try {
			// TimeZone tz = sdf.getTimeZone();
			// System.out.println("Display name: " + tz.getDisplayName());
			ori = sdf.parse("Thu Jan 01 00:00:00 CST 1970");
			// Calendar c = sdf.getCalendar();
			// tz = sdf.getTimeZone();
			// System.out.println("Display name: " + tz.getDisplayName());
			// System.out.println(c.getTime());
			// System.out.println("ori: " + ori);

			date = sdf.parse(dateStr);
			// tz = sdf.getTimeZone();
			// System.out.println("Display name: " + tz.getDisplayName());

		} catch (ParseException e) {
			e.printStackTrace();
		}

		long mills = date.getTime() - ori.getTime();
		mills = mills / 1000;
		System.out.println("mills:" + mills);
		return mills;
	}

	/**
	 * 返回指定天数前的时间戳
	 * 
	 * @param days
	 * @return
	 */
	public long getTimestampFromDaysAgo(int days) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - days);
		System.out.println(cal.getTime());
		return getTimestamp(cal.getTime());
	}

	/**
	 * 将日期转换为时间戳
	 * 
	 * @param dateStr
	 * @return
	 */
	public long getTimestamp(Date date) {
		return getTimestamp(date.toString());
	}

	/**
	 * 如果文件不存在则创建它
	 * 
	 * @param path
	 * @return
	 */
	public File createFileIfNotExist(String path) {
		File file = new File(path);
		if (!file.exists()) {
			// Log.i("srcFile file exists", String.valueOf(file.exists()));
			try {
				new File(path.substring(0, path.lastIndexOf("/"))).mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Log.i("file create finished", String.valueOf(file.exists()));
		return file;
	}

	/**
	 * 生成适合WebView显示的数据
	 * 
	 * @param charset
	 * @param article
	 * @return
	 */
	public String parseTextToHtmlForWebview(String charset, String title, String content, String desc) {
		System.out.println("parseTextToHtmlForWebview()");
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><meta http-equiv='content-type' content='text/html; charset=");
		sb.append(charset);
		sb.append("' /></head><body><h3>");
		sb.append(title);
		sb.append("</h3>");
		sb.append(content);
		if(desc != null) {
			sb.append(desc);
		}
		sb.append("</body></html>");

		String result = sb.toString();
//		try {
//			result = URLEncoder.encode(result, "utf-8").replaceAll("\\+", " ").trim();
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}

		return result;
	}
}
