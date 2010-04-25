package com.olunx.option.mandict;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GetGdsInfo implements GetDictInfo {

	String dictPath = null;

	public GetGdsInfo(String dictPath) {
		super();
		this.dictPath = dictPath;
	}

	@Override
	public String getDictName() {
		
		String dictName = null;
		
		FileInputStream is = null;
		BufferedInputStream bis = null;

		try {
			is = new FileInputStream(new File(dictPath));
			bis = new BufferedInputStream(is);

			// 记录文件大小
			long fileSize = bis.available();
			System.out.println(fileSize);

			// 跳过首部
			bis.skip(12);

			// 读取文件名
			byte[] bookFirPie;
			bookFirPie = new byte[20];
			bis.read(bookFirPie);

			bis.skip(18);

			byte[] bookSecPie;
			bookSecPie = new byte[14];
			bis.read(bookSecPie);

			bis.skip(18);

			byte[] bookThiPie;
			bookThiPie = new byte[6];
			bis.read(bookThiPie);

			byte[] bookName;
			bookName = new byte[40];

			System.arraycopy(bookFirPie, 0, bookName, 0, bookFirPie.length);
			System.arraycopy(bookSecPie, 0, bookName, bookFirPie.length, bookSecPie.length);
			System.arraycopy(bookThiPie, 0, bookName, bookFirPie.length + bookSecPie.length, bookThiPie.length);

			dictName = new String(bookName, "gb2312").replace("|", "").substring(0, 14).trim();

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
		return dictName;
	}

	@Override
	public String getFileNameNoExtension() {
		String dictName = new File(dictPath).getName();
		return dictName.substring(0, dictName.lastIndexOf("."));
	}

	@Override
	public String getFileNameWithExtension() {
		return new File(dictPath).getName();
	}

	@Override
	public String getWordCount() {

		int wordCount = 0;
		
		FileInputStream is = null;
		BufferedInputStream bis = null;

		try {
			is = new FileInputStream(new File(dictPath));
			bis = new BufferedInputStream(is);

			//跳过前面描述
			bis.skip(290);
			
			wordCount = bis.available() / 128;

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

		return String.valueOf(wordCount);
	}

}
