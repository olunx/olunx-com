package com.olunx.stardict;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.teesoft.javadict.DictItem;
import com.teesoft.javadict.ItemList;
import com.teesoft.javadict.stardict.starDict;
import com.teesoft.javadict.stardict.starIndex;
import com.teesoft.javadict.stardict.startDictFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.util.HtmlConvertor;


public class SeekWord {

	FileAccessBase fileAccess;
	startDictFactory factory;
	starDict dict;
	starIndex index;
	
	private String dictDir;
	private String dictName;
	
	private StringBuilder result;
	
	public SeekWord(String dictPath) {
		dictDir = dictPath.substring(0, dictPath.lastIndexOf(File.separator));
		System.out.println("dictDir: " + dictDir);
		dictName = dictPath.substring(dictPath.lastIndexOf(File.separator) + 1, dictPath.lastIndexOf("."));
		try {
			fileAccess = FileFactory.openFileAccess(dictDir, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		factory = startDictFactory.getInstance();
		
		dict = new starDict(factory, fileAccess, dictName, null);
		dict.setDictName(dictName);
		
		System.out.println(dict.getName());
		System.out.println(dict.getDictName());
		
		try {
			index = new starIndex(factory.openIndex(fileAccess, dict.getDictName()), dict);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getTrans(String word) {
		
		ItemList items = index.search(word.getBytes(), 1);
		result = new StringBuilder();
		for(int i=0;i<items.size();i++) {
			DictItem item = items.getItem(i);
			if(item.toString().equalsIgnoreCase(word)) {
//				System.out.println(item);
//				System.out.println(item.getExplains().getString());
				result.append(HtmlConvertor.ConvertHtmlToText(item.getExplains().getString()));
			}
		}
		
		return result.toString();
	}
	
	private Map<String, String> word;
	
	public Map<String, String> getWordTrans(String value) {
		word = new HashMap<String, String>();
		
		word.put("单词", value);
		String result = getTrans(value);
		
		Pattern p = Pattern.compile("/(.*?)/");
		Matcher m = p.matcher(result);
		
		if(m.find()) {
			String phonetic = m.group();
			word.put("音标", phonetic.replace("/", ""));
			
			word.put("解释", result.replace(phonetic, "").replaceAll("\\*", "\n\n\\*"));
		}else {
			word.put("解释", result);
		}
		
		return word;
	}
}
