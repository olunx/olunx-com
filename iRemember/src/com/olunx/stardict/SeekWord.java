package com.olunx.stardict;

import java.io.File;
import java.io.IOException;

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
//			System.out.println(item);
//			System.out.println(item.getExplains().getString());
			result.append(HtmlConvertor.ConvertHtmlToText(item.getExplains().getString()));
		}
		
		return result.toString();
	}
}
