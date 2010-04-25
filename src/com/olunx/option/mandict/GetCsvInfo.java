/*
 *author:olunx
 *date:2009-10-14
 */

package com.olunx.option.mandict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class GetCsvInfo implements GetDictInfo {

	String dictPath = null;

	public GetCsvInfo(String dictPath) {
		this.dictPath = dictPath;
	}

	@Override
	public String getDictName() {
		String dictName = new File(dictPath).getName();
		return dictName.substring(0, dictName.lastIndexOf("."));
	}

	@Override
	public String getFileNameWithExtension() {
		return new File(dictPath).getName();
	}

	@Override
	public String getFileNameNoExtension() {
		String dictName = new File(dictPath).getName();
		return dictName.substring(0, dictName.lastIndexOf("."));
	}

	@Override
	public String getWordCount() {
		FileReader fr = null;
		BufferedReader br = null;
		int count = 0;
		try {
			fr = new FileReader(dictPath);
			br = new BufferedReader(fr);

			while (br.readLine() != null) {
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return String.valueOf(count);
	}

}
