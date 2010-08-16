package com.olunx.stardict;

import java.io.File;
import java.io.FileNotFoundException;

import dict.plugin.stardict.StarDictReader;
import dict.service.core.Word;
import dict.service.exception.NotFoundWordException;

public class SeekWord {

	private StarDictReader star;
	private Word result;
	private String dictDir;
	private String dictName;
	
	public SeekWord(String dictPath) {
		dictDir = dictPath.substring(0, dictPath.lastIndexOf(File.separator));
		dictName = dictPath.substring(dictPath.lastIndexOf(File.separator) + 1, dictPath.lastIndexOf("."));
	}
	
	public String getTrans(String word) {
		
		try {
			star = new StarDictReader(dictDir, dictName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			result = star.lookup(word);
		} catch (NotFoundWordException e) {
			System.out.println("没有该词条");
		}
		
		if(result != null) {
//			System.out.println(result.definition);
			return result.definition.replaceAll("\\*", "\n*");
		}
		return "";
	}
}
