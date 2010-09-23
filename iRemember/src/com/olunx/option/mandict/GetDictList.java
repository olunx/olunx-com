package com.olunx.option.mandict;

import java.io.File;
import java.util.ArrayList;

import com.olunx.util.Config;

public class GetDictList {

	private ArrayList<String> csvPathList = null;// 保存csv词库的绝对路径
	private ArrayList<String> stardictPathList = null;// 保存stardict词典的绝对路径

	// 获取词典目录下的所有词库和词典文件
	public void getList() {
		csvPathList = new ArrayList<String>();
		stardictPathList = new ArrayList<String>();

		String dictDir = Config.init().getDictDir();
		if (dictDir != null && dictDir != "/") {
			getDictsList(new File(dictDir));
		}
		Config.init().setDictList(csvPathList, Config.DICTTYPE_CSV);
		Config.init().setDictList(stardictPathList, Config.DICTTYPE_STARDICT);
	}

	// 列出所csv文件
	private void getDictsList(File file) {
		String fileName;
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				if(f.getPath().contains(Config.SDCARD_SOUND_PATH))
					continue;
				getDictsList(f);
			}
		} else {
			fileName = file.getName().toLowerCase();
			if (fileName.endsWith("." + Config.DICTTYPE_CSV)) {
				csvPathList.add(file.getAbsolutePath());
			}
			if (fileName.endsWith("." + Config.DICTTYPE_STARDICT)) {
				stardictPathList.add(file.getAbsolutePath());
			}
		}
	}

}
