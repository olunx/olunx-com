/*
 * startDictFactory.java
 *
 * Created on Aug 5, 2007, 4:58:28 PM
 *
Copyright (C) 2007  Yong Li. All rights reserved.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

 */

package com.teesoft.javadict.dicf;

import com.teesoft.javadict.Dict;
import com.teesoft.javadict.DictFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.javadict.Properties;
import java.io.IOException;


/**
 *
 * @author wind
 */
public class dicfDictFactory extends DictFactory {

    public Properties SupportedProperties() {
        return super.SupportedProperties();
    }

    public boolean supportFileDict() {
        return true;
    }

    public Dict AcceptDict(FileAccessBase file, Properties properties) throws IOException {
        if (file == null)
            return null;
        if (file.isDirectory()) {
            return AcceptDict(file.child(file.getName() + ".index"), properties);
        }
        if(!file.exists())
            return null;
        String fileName = file.getName();
        int dotPos = getRevIndexOf(fileName, '.');
        if (dotPos == -1) {
            return null;
        }
        String extName = fileName.substring(dotPos + 1);
        if (!extName.equals("index")) {
            return null;
        }
        String dictName = fileName.substring(0, dotPos);
        if (dictName.length() == 0) {
            return null;
        }

        FileAccessBase dictFile;
        String path = file.getAbsolutePath();
        path = path.substring(0, path.length() - fileName.length()) + dictName + ".dict";

        dictFile = FileFactory.openFileAccess(path,true);
        
        if (dictFile!= null && dictFile.exists() && !dictFile.isDirectory()) {
            Dict dict = new dicfDict(file, dictName, dictName, properties);
            if (dict != null) {
                dict.setFactoryClass(this.getClass().getName());
                dict.setFormat(this.getFormat());
            }
            return dict;
        }


        return null;
    }

    private int getRevIndexOf(String name, char c) {
        for (int i = name.length() - 1; i >= 0; i--) {
            if (name.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    public Dict CreateDict(FileAccessBase file, String name, Properties properties) throws IOException {
        if (file==null || !file.exists())
            return null;
        String fileName = file.getName();
        int dotPos = getRevIndexOf(fileName, '.');
        if (dotPos == -1) {
            return null;
        }

        String baseName = fileName.substring(0, dotPos);
        if (baseName.length() == 0) {
            return null;
        }
        Dict dict = new dicfDict(file, name, baseName, properties);
        if (dict != null) {
            dict.setFactoryClass(this.getClass().getName());
            dict.setFormat(this.getFormat());
        }
        return dict;
    }



    public boolean supportFolderDict() {
        return true;
    }

    public dicfDictFactory() {
    }

    public String getFormat() {
        return "dicf";
    }
}
