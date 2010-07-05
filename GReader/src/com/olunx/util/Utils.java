package com.olunx.util;

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
		// TODO Auto-generated method stub
		Utils u = new Utils();
		// Calendar cal = Calendar.getInstance();
		// cal.set(1970, 1, 1, 0, 0, 0);
		// System.out.println(Locale.US.getDisplayName());
		// cal.setTimeZone(TimeZone.getDefault());
		// System.out.println("cal: " + cal.getTime());
		System.out.println(u.getTimestampFromDaysAgo(5));
	}

	public static Utils utils;

	public static Utils init() {
		if (utils == null) {
			utils = new Utils();
		}
		return utils;
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
}
