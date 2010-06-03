/*
 *author:olunx
 *date:2009-10-12
 */

package com.olunx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import com.olunx.R;
import com.olunx.db.CsvHelper;
import com.olunx.db.RememberHelper;
import com.olunx.option.mandict.GetCsvInfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Config {

	private static Config c = null;

	public static final String DICTTYPE_CSV = "csv";
	public static final String FILENAME_CONFIG = "config";
	public static final String FILENAME_DATABASE = "data.db";

	private static final String SYSTEM_PATH = "/data/data/com.olunx/";
	private static final String SDCARD_PATH = "/sdcard/iremember/";

	public static final String DATABASE_FILE = FILENAME_DATABASE;
	public static final String FILE_SYSTEM_DATABASE = SYSTEM_PATH + "databases/" + DATABASE_FILE;
	public static final String FILE_SDCARD_DATABASE = SDCARD_PATH + DATABASE_FILE;

	public static final String CONFIG_FILE = Config.FILENAME_CONFIG + ".xml";
	public static final String FILE_SYSTEM_CONFIG = SYSTEM_PATH + "shared_prefs/" + CONFIG_FILE;
	public static final String FILE_SDCARD_CONFIG = SDCARD_PATH + CONFIG_FILE;

	public static Config getConfig() {
		if (c == null) {
			c = new Config();
		}
		return c;
	}

	private SharedPreferences sp;

	/**
	 * 设置配置
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public void setCon(Context context, String key, String value) {
		sp = context.getSharedPreferences(FILENAME_CONFIG, 0);
		sp.edit().putString(key, String.valueOf(value)).commit();
	}

	/**
	 * 获取配置
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return
	 */
	public String getCon(Context context, String key, String defValue) {
		sp = context.getSharedPreferences(FILENAME_CONFIG, 0);
		return sp.getString(key, defValue);
	}

	/**
	 * 设置词典路径
	 * 
	 * @param context
	 * @param dictDir
	 */
	public void setDictDir(Context context, String dictDir) {
		this.setCon(context, "config_dict_dir", dictDir);
	}

	public String getDictDir(Context context) {
		return getCon(context, "config_dict_dir", "/sdcard/");
	}

	/**
	 * 设置每组单词数
	 * 
	 * @param context
	 * @param wordCount
	 */
	public void setEachLessonWordCount(Context context, String wordCount) {
		this.setCon(context, "config_each_lesson_word_count", wordCount);
	}

	public String getEachLessonWordCount(Context context) {
		return this.getCon(context, "config_each_lesson_word_count", "25");
	}

	/**
	 * 设置课程数
	 * 
	 * @param context
	 * @param lessonCount
	 */
	public void setLessonCount(Context context, String lessonCount) {
		this.setCon(context, "config_lesson_count", lessonCount);
	}

	public String getLessonCount(Context context) {

		String wordCount = this.getCurrentUseDictWordCount(context);
		String eachLessonWordCount = this.getEachLessonWordCount(context);

		int lessonCount = 0;
		int temp;
		if (eachLessonWordCount != null && eachLessonWordCount != "" && wordCount != "" && wordCount != null) {
			if (Integer.parseInt(eachLessonWordCount) >= (temp = Integer.parseInt(wordCount)) && temp > 0) {// 如果每组数大于总词数，则设组数为1。
				lessonCount = 1;
			} else {
				int intWordCount = Integer.parseInt(wordCount);
				int intEachLessonWordCount = Integer.parseInt(eachLessonWordCount);
				if (intEachLessonWordCount > 0) {
					lessonCount = intWordCount / intEachLessonWordCount;
					if (intWordCount % intEachLessonWordCount > 0) {
						lessonCount++;
					}
				}
			}
		}
		String strLessonCount = String.valueOf(lessonCount);
		this.setLessonCount(context, strLessonCount);

		return strLessonCount;
	}

	/**
	 * 设置词典列表
	 * 
	 * @param context
	 * @param dictPathList
	 * @param dictType
	 */
	public void setDictList(Context context, ArrayList<String> dictPathList, String dictType) {
		// 保存数据
		String dictsArray = "";// 保存词库名称

		GetCsvInfo gci = null;
		int dictListSize = dictPathList.size();
		Log.i("dictListSize", String.valueOf(dictListSize));
		String path = null;
		String dictSize = null;
		String dictName = null;
		for (int i = 0; i < dictListSize; i++) {
			path = dictPathList.get(i);
			gci = new GetCsvInfo(path);
			dictSize = gci.getFileSize();
			dictName = gci.getDictName();
			dictsArray = dictsArray + dictName + "|";// 将词库名称作为数组，方便获取

			this.setDictPath(context, dictName, path);
			// this.setDictWordCount(context, dictName, wordCount);
			this.setDictType(context, dictName, dictType);
			this.setDictDesc(context, dictName, "大小：" + dictSize + "   类型：" + dictType);

		}
		this.setDictStringArray(context, dictsArray, dictType);
	}

	public ArrayList<HashMap<String, Object>> getDictList(Context context) {

		ArrayList<HashMap<String, Object>> resultItems = new ArrayList<HashMap<String, Object>>();
		// 获取词典字符串
		String dictListArray = Config.getConfig().getDictStringArray(context, Config.DICTTYPE_CSV);
		if (dictListArray != "" && dictListArray != null) {
			String[] dictNameList = dictListArray.split("\\|");

			HashMap<String, Object> result = null;
			String title = context.getString(R.string.title);
			String des = context.getString(R.string.description);
			for (int i = 0; i < dictNameList.length; i++) {
				Log.i("split", dictNameList[i]);
				result = new HashMap<String, Object>();
				result.put(title, dictNameList[i]);
				result.put(des, Config.getConfig().getDictDesc(context, dictNameList[i]));
				resultItems.add(result);
			}
			Log.i("dictList.length", String.valueOf(dictNameList.length));
		}
		return resultItems;
	}

	/**
	 * 设置词典字符串数组
	 * 
	 * @param context
	 * @param dictsArray
	 * @param dictType
	 */
	public void setDictStringArray(Context context, String dictsArray, String dictType) {
		this.setCon(context, dictType + "_dicts_string_array", dictsArray);
	}

	public String getDictStringArray(Context context, String dictType) {
		return this.getCon(context, dictType + "_dicts_string_array", "");
	}

	// /**
	// * 设置词典单词数
	// *
	// * @param context
	// * @param dictName
	// * @param wordCount
	// */
	// public void setDictWordCount(Context context, String dictName, String
	// wordCount) {
	// this.setCon(context, dictName + "_count", wordCount);
	// }
	//
	// public String getDictWordCount(Context context, String dictName) {
	// return this.getCon(context, dictName + "_count", "0");
	// }

	/**
	 * 设置词典路径
	 * 
	 * @param context
	 * @param dictName
	 * @param dictPath
	 */
	public void setDictPath(Context context, String dictName, String dictPath) {
		this.setCon(context, dictName + "_path", dictPath);
	}

	public String getDictPath(Context context, String dictPath) {
		return this.getCon(context, dictPath + "_path", "");
	}

	/**
	 * 设置词典描述
	 * 
	 * @param context
	 * @param dictFileName
	 * @param dictDesc
	 */
	public void setDictDesc(Context context, String dictFileName, String dictDesc) {
		this.setCon(context, dictFileName + "_desc", dictDesc);
	}

	public String getDictDesc(Context context, String dictFileName) {
		return this.getCon(context, dictFileName + "_desc", "");
	}

	/**
	 * 设置词典类型
	 * 
	 * @param context
	 * @param dictName
	 * @param dictType
	 */
	public void setDictType(Context context, String dictName, String dictType) {
		this.setCon(context, dictName + "_type", dictType);
	}

	public String getDictType(Context context, String dictName) {
		return this.getCon(context, dictName + "_type", "");
	}

	// // 当前使用词典文件名
	// public void setCurrentUseDictFileName(Context context, String fileName) {
	// this.setCon(context, "current_use_dict_file_name", fileName);
	// }
	//
	// public String getCurrentUseDictFileName(Context context) {
	// return this.getCon(context, "current_use_dict_file_name", "");
	// }

	/**
	 * 设置当前使用词典名称
	 * 
	 * @param context
	 * @param dictName
	 */
	public void setCurrentUseDictName(Context context, String dictName) {
		this.setCon(context, "current_use_dict_name", dictName);
	}

	public String getCurrentUseDictName(Context context) {
		return this.getCon(context, "current_use_dict_name", "");
	}

	/**
	 * 当前使用词典单词数
	 * 
	 * @param context
	 * @return
	 */
	public void setCurrentUseDictWordCount(Context context, String wordCount) {
		this.setCon(context, "current_dict_count", wordCount);
	}

	public String getCurrentUseDictWordCount(Context context) {
		return this.getCon(context, "current_dict_count", "0");
	}

	/**
	 * 当前使用词典文件路径
	 * 
	 * @param context
	 * @return
	 */
	public String getCurrentUseDictPath(Context context) {
		return this.getDictPath(context, this.getCurrentUseDictName(context));
	}

	/**
	 * 当前使用词典文件类型
	 * 
	 * @param context
	 * @return
	 */
	public String getCurrentUseDictType(Context context) {
		return this.getDictType(context, this.getCurrentUseDictName(context));
	}

	/**
	 * 设置当前背诵完成的课程号
	 * 
	 * @param context
	 * @param lessonNo
	 */
	public void setNextStudyLesson(Context context, int lessonNo) {
		this.setCon(context, "next_study_lesson", String.valueOf(lessonNo));
	}

	public int getNextStudyLesson(Context context) {
		return Integer.parseInt(this.getCon(context, "next_study_lesson", "0"));
	}

	/**
	 * 分组信息描述
	 * 
	 * @param context
	 * @return
	 */
	public String getEachLessonWordCountDes(Context context) {
		return "每组: " + this.getEachLessonWordCount(context) + "   共词数: " + this.getCurrentUseDictWordCount(context) + "，组数: "
				+ this.getLessonCount(context) + " ";
	}

	private SimpleDateFormat sdf = null;
	private Calendar cal = null;

	/**
	 * 判断复习时间是否在今天内
	 * 
	 * @param date
	 * @return
	 * @throws ParseException
	 */
	public boolean isStudyTimeInToday(String date) throws ParseException {
		if (sdf == null) {
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		}
		if (cal == null) {
			cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
		}
		cal.setTime(sdf.parse(date));
		Log.i("today", sdf.format(new Date()));
		Log.i("studyDate", String.valueOf(Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00")).after(cal)));
		return Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00")).after(cal);
		// return true;
	}

	/**
	 * 初始化配置文件
	 * 
	 * @param context
	 */
	public void setDefaultConfig(Context context) {
		this.setDictDir(context, "/sdcard/");
		this.setEachLessonWordCount(context, "50");
		this.setLessonCount(context, "0");
		this.setCurrentUseDictName(context, "");
		this.setDictStringArray(context, "", "csv");

		this.cleanRememberLine(context);
	}

	/**
	 * 清空记忆曲线的相关数据
	 * 
	 * @param context
	 */
	public void cleanRememberLine(Context context) {
		this.setNextStudyLesson(context, 0);

		RememberHelper helper = new RememberHelper(context);
		helper.dropTable();
		helper.close();
	}

	/**
	 * 读取assets的文件内容
	 * 
	 * @param context
	 * @param filePath
	 * @return
	 */
	public String getDataFromAssets(Context context, String filePath) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(context.getAssets().open(filePath)));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 * 更新记忆曲线
	 * 
	 * @param context
	 * @param lessonNo
	 * @param ignoreWords
	 *            不再记忆的单词编号
	 */
	public void setRememberLine(Context context, int lessonNo, String ignoreWords) {
		RememberHelper helper = new RememberHelper(context);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));// 获取当前时间
		// 判断此课程是否存在
		if (helper.isExistsLessonNo(lessonNo)) {// 存在
			try {
				Calendar oldTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
				oldTime.setTime(sdf.parse(helper.getStudyTimeByLessonNo(lessonNo)));

				int times;
				switch (times = helper.getTimesByLessonNo(lessonNo)) {
				case 1:
					cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 30);// 第二个记忆周期30分钟
					break;
				case 2:
					cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 12);// 第三个记忆周期12小时
					break;
				case 3:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);// 第四个记忆周期1天
					break;
				case 4:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 2);// 第四五个记忆周期2天
					break;
				case 5:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 4);// 第六个记忆周期4天
					break;
				case 6:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 7);// 第七个记忆周期7天
					break;
				case 7:
					cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 15);// 第八个记忆周期15天
					break;
				default:
					cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 3);// 不在记忆周期内三个月后再记
					break;
				}
				helper.deleteRecord(lessonNo);
				helper.addRecord(lessonNo, sdf.format(oldTime.getTime()), sdf.format(cal.getTime()), ++times, ignoreWords);

			} catch (ParseException e) {
				e.printStackTrace();
			}

		} else {// 不存在
			String studyTime = sdf.format(cal.getTime());
			cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 5);// 第一个记忆周期5分钟
			helper.addRecord(lessonNo, studyTime, sdf.format(cal.getTime()), 1, ignoreWords);
		}
		helper.close();
	}

	public void setRememberLine(Context context, int lessonNo) {
		this.setRememberLine(context, lessonNo, this.getIgnoreWordsStr(context, lessonNo));
	}

	// 返回不需要再次记忆的单词编号
	public String getIgnoreWordsStr(Context context, int lessonNo) {
		RememberHelper helper = new RememberHelper(context);
		String ignoreWordsStr = helper.getIgnoreWords(lessonNo);
		helper.close();
		Log.i("ignoreWordsStr", ignoreWordsStr);
		return ignoreWordsStr.toLowerCase();
	}

	// public int[] getIgnoreWords(Context context, int lessonNo) {
	//		
	// String ignoreWordsStr = this.getIgnoreWordsStr(context, lessonNo);
	//		
	// if(ignoreWordsStr.equals("") || ignoreWordsStr == "") {
	// return null;
	// }
	//		
	// String[] ignoreWords = ignoreWordsStr.split("\\,");
	// int[] wordNos = new int[ignoreWords.length];
	// for(int i=0;i<ignoreWords.length;i++) {
	// wordNos[i] = Integer.parseInt(ignoreWords[i]);
	// }
	//		
	// return wordNos;
	// }

	/**
	 * 设置是否可以联网
	 * 
	 * @param context
	 * @param value
	 */
	public void setCanConNetWord(Context context, Boolean value) {
		this.setCon(context, "can_get_net_word", String.valueOf(value));
	}

	public boolean getCanConNetWord(Context context) {
		return Boolean.parseBoolean(this.getCon(context, "can_get_net_word", "false"));
	}

	/**
	 * 设置发音类型
	 * 
	 * @param context
	 * @param which
	 */
	public void setSpeechType(Context context, int which) {
		switch (which) {
		case 0: {
			this.setCon(context, "speech_type", "null");
			break;
		}
		case 1: {
			this.setCon(context, "speech_type", "tts");
			break;
		}
		case 2: {
			this.setCon(context, "speech_type", "real");
			break;
		}
		}

	}

	public String getSpeechType(Context context) {
		return this.getCon(context, "speech_type", "null");
	}

	/**
	 * 是否可发音
	 * @param context
	 * @param flag
	 */
	public void setCanSpeech(Context context, boolean flag) {
		this.setCon(context, "is_can_speech", String.valueOf(flag));
	}
	
	public boolean isCanSpeech(Context context) {
		return Boolean.parseBoolean(this.getCon(context, "is_can_speech", "false"));
	}

	/**
	 * 读取词库数据
	 * 
	 * @param context
	 * @param currentLessonNo
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> getWordsFromFileByLessonNo(Context context, int currentLessonNo) {
		ArrayList<HashMap<String, Object>> wordList = null;

		// 计算偏移量和单词数
		String strEachLessonWordCount = Config.getConfig().getEachLessonWordCount(context);
		int eachLessonWordCount = 0;// 每课单词数
		if (strEachLessonWordCount != null && !strEachLessonWordCount.equals("")) {
			eachLessonWordCount = Integer.parseInt(strEachLessonWordCount);
		}
		int index = currentLessonNo * eachLessonWordCount;// 偏移量
		String dictType = Config.getConfig().getCurrentUseDictType(context);
		if (dictType.equalsIgnoreCase("csv")) {
			CsvHelper helper = new CsvHelper(context);
			wordList = helper.getWords(index, eachLessonWordCount);
		}

		// 处理不再记忆的单词
		String ignoreWords = Config.getConfig().getIgnoreWordsStr(context, currentLessonNo);
		if (ignoreWords != null && !ignoreWords.equals("")) {
			int length = wordList.size();
			String ignoreWord;
			ignoreWords = ignoreWords.toLowerCase();
			Log.i("ignoreWords", ignoreWords);
			for (int i = 0; i < length; i++) {
				ignoreWord = (String) wordList.get(i).get("单词");
//				Log.i("ignoreWord", ignoreWord);
				if (ignoreWords.contains(ignoreWord.toLowerCase())) {
					Log.i("remove", String.valueOf(i));
					wordList.remove(i);
					length = wordList.size();
				}
			}
		}

		return wordList;
	}

	/**
	 * 设置词库文件编码
	 * 
	 * @param context
	 * @param charset
	 */
	public void setDictCharset(Context context, String charset) {
		this.setCon(context, "config_dict_charset", charset);
	}

	public String getDictCharset(Context context) {
		return getCon(context, "config_dict_charset", "GBK");
	}
}
