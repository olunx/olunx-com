package com.olunx;

import java.io.File;

public class FileFilter extends javax.swing.filechooser.FileFilter {

	private String file;
	
	public FileFilter(String file) {
		this.file = file;
	}
	
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {  
            return true;  
        }  
        //显示满足条件的文件  
        return f.getName().endsWith(file); 
	}

	@Override
	public String getDescription() {
		return "*" + file;
	}

}
