package com.olunx.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

import com.olunx.util.Config;

public class GdsHelper {
	
	Context context = null;
	
	public GdsHelper(Context context) {
		this.context = context;
	}

	/*
	 * gds词库格式：
	 * 	首部:前12
	 * 	词典名称大小：44 (跳跃式，12\*20\16\*16\16\*8)
	 * 	整个首部大小：290
	 * 	单词大小：30
	 * 	音标大小：30
	 * 	解释大小：40
	 * 	课数大小：4
	 * 	序数大小：22
	 * */
	// 返回单词，index为索引值，count为数目，当count为0时返回所有结果
	public ArrayList<HashMap<String, Object>> getWords(int index, int count) {
		
		ArrayList<HashMap<String, Object>> words = new ArrayList<HashMap<String, Object>>();
		
		FileInputStream is = null;
		BufferedInputStream bis = null;

		try {
			is = new FileInputStream(new File(Config.getConfig().getCurrentUseDictPath(context)));
			bis = new BufferedInputStream(is);

			//跳过前面描述
			bis.skip(290);
			
			bis.skip(index * 128);
			
			HashMap<String, Object> map = null;
			
			byte[] word;
			word = new byte[30];
			byte[] phonetics;
			phonetics = new byte[30];
			byte[] translation;
			translation = new byte[40];
			
			for(int i=0; i<count; i++) {
				if(bis.available() > 128) {
					map = new HashMap<String, Object>();
					bis.read(word);
					bis.read(phonetics);
					bis.read(translation);
					map.put("单词", new String(word, "gb2312").trim());
					map.put("音标", new String(phonetics, "gb2312").trim());
					map.put("解释", new String(translation, "gb2312").trim());
					bis.skip(28);//跳过多余字段
					words.add(map);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			try {
				bis.close();
				is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		return words;
	}
}
