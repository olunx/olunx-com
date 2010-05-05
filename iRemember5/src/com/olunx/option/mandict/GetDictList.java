/*
 *author:olunx
 *date:2009-10-13
 */

package com.olunx.option.mandict;

import java.io.File;
import java.util.ArrayList;

import com.olunx.util.Config;

import android.content.Context;

public class GetDictList{

	Context context = null;

	public GetDictList(Context context) {
		this.context = context;
	}

	private ArrayList<String> csvPathList = null;//保存csv词库的绝对路径
	private ArrayList<String> gdsPathList = null;//保存gds词库的绝对路径

	// 获取词典目录下的所有csv文件
	public void getList() {
		csvPathList = new ArrayList<String>();
		gdsPathList = new ArrayList<String>();
		String dictDir =  Config.getConfig().getDictDir(context);
		if (dictDir != null && dictDir != "/") {
			getDictsList(new File(dictDir));
		}
		Config.getConfig().setDictList(context, csvPathList, "csv");
		Config.getConfig().setDictList(context, gdsPathList, "gds");
	}

	// 列出所csv文件
	private void getDictsList(File file) {
		String fileName;
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				getDictsList(f);
			}
		} else {
			fileName = file.getName().toLowerCase();
			if (fileName.endsWith(".csv")) {
				csvPathList.add(file.getAbsolutePath());
			}else if(fileName.endsWith(".gds")) {
				gdsPathList.add(file.getAbsolutePath());
			}
		}
	}

}
