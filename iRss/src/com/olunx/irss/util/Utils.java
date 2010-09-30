package com.olunx.irss.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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

	public String getCstTimeBeforeToday(int days) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - days);
		return getCstTime(cal.getTime());
	}

	/**
	 * 格式化日期为本地习惯
	 * 
	 * @param time
	 * @param format
	 *            MM月dd日 HH:mm
	 * @return
	 */
	public String formatCstTimeToLocal(String time, String format) {
		if (time == null || time.equals(""))
			return null;
		SimpleDateFormat source = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
		SimpleDateFormat dest = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = source.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dest.format(date);
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
			ori = sdf.parse("Thu Jan 01 00:00:00 CST 1970");

			date = sdf.parse(dateStr);

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
	 * 复制二进制文件
	 * 
	 * @param fileFromPath
	 * @param fileToPath
	 */
	public void copyBinFile(String fileFromPath, String fileToPath) {
		// 如果文件不存在则创建它
		createFileIfNotExist(fileFromPath);
		createNewFile(fileToPath);

		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(fileFromPath);
			out = new FileOutputStream(fileToPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.copyBinFile(in, out);
	}

	public void copyBinFile(InputStream in, OutputStream out) {
		// 开始复制文件
		InputStream inBuffer = null;
		OutputStream outBuffer = null;

		inBuffer = new BufferedInputStream(in);
		outBuffer = new BufferedOutputStream(out);

		int byteData = 0;
		try {
			while (true) {
				byteData = inBuffer.read();
				if (byteData == -1)
					break;
				out.write(byteData);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inBuffer != null)
					inBuffer.close();
				if (outBuffer != null)
					outBuffer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 复制文本文件
	 * 
	 * @param in
	 * @param targetFilePath
	 */
	public void copyFile(InputStream in, String targetFilePath) {
		createNewFile(targetFilePath);

		OutputStream out = null;

		try {
			out = new FileOutputStream(targetFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.copyFile(in, out);
	}

	public void copyFile(InputStream in, OutputStream out) {

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String data = null;
		try {
			while ((data = br.readLine()) != null) {
				bw.write(data + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将字符串保存到指定位置
	 * 
	 * @param content
	 * @param targetFilePath
	 */
	public void copyFile(String content, String targetFilePath) {

		createNewFile(targetFilePath);

		OutputStream out = null;

		try {
			out = new FileOutputStream(targetFilePath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			bw.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 删除旧的文件，创建新的空白的文件。
	 * 
	 * @param targetFilePath
	 */
	public void createNewFile(String targetFilePath) {
		createFileIfNotExist(targetFilePath);

		// 删除已有的目标文件
		File targetFile = new File(targetFilePath);
		targetFile.delete();
		try {
			targetFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
				new File(path.substring(0, path.lastIndexOf(File.separator))).mkdirs();
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
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><meta http-equiv='content-type' content='text/html; charset=");
		sb.append(charset);
		sb.append("' /></head><body><h3>");
		sb.append(title);
		sb.append("</h3>");
		sb.append(content);
		if (desc != null) {
			sb.append(desc);
		}
		sb.append("</body></html>");

		return sb.toString();
	}

	/**
	 * 将系统的ARGB颜色转换为网页支持的RGB颜色
	 * 
	 * @param color
	 * @return
	 */
	public String ArgbToHexRgb(int color) {
		String hexColor = Integer.toHexString((color & 0xFFFFFF));
		int length = hexColor.length();
		if (length < 6) {
			for (int i = 0; i < 6 - length; i++) {
				hexColor = "0" + hexColor;
			}
		}
		return "#" + hexColor;
	}
}
