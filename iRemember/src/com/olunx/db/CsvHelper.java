package com.olunx.db;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;


public class CsvHelper {
	
	public ArrayList<HashMap<String, Object>> getWords(String dictPath, String fileCharset, int index, int count) {
		ArrayList<HashMap<String, Object>> words = new ArrayList<HashMap<String, Object>>();
		
		BufferedReader br;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		
		try {
			fis=new FileInputStream(dictPath);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			isr = new InputStreamReader(fis, fileCharset);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		br = new BufferedReader(isr);

//		Log.i("setLineNumber", String.valueOf(index));
		int no = 1;
		int lineNo = 1;
		int firSep, secSep;
		String line;
		HashMap<String, Object> word = null;
		
		try {
			while((line = br.readLine()) != null) {
//				Log.i("getLineNumber", String.valueOf(lineNo));
				if(lineNo < index) {
					lineNo++;
					continue;
				}
//				firSep = line.indexOf(",[");
//				secSep = line.indexOf("],");
				
				
				firSep = line.indexOf(",");
				secSep = line.indexOf(",", firSep + 1);
				
				word = new HashMap<String, Object>();
				word.put("单词", line.substring(0, firSep));
				word.put("音标", line.substring(firSep + 1, secSep).replaceAll("\\[", "").replaceAll("\\]", ""));
				word.put("解释", line.substring(secSep + 1).replaceAll("//", "\n"));
				words.add(word);
//				Log.i("count", String.valueOf(no));
				if(++no > count) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				br.close();
				isr.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return words;
	}
}
