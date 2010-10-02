/*
 * mdictFactory.java
 *
 * Created on 2007-9-29, 23:50:22
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.javadict.mdict;

import com.teesoft.javadict.Dict;
import com.teesoft.javadict.Properties;
import com.teesoft.javadict.stardict.starDict;
import com.teesoft.javadict.stardict.startDictFactory;
import com.teesoft.jfile.FileAccessBase;
import java.io.IOException;

/**
 *
 * @author wind
 */
public class mdictFactory extends startDictFactory {

    public Dict AcceptDict(FileAccessBase f, Properties properties) throws IOException {
        if (f == null) {
            return null;
        }
        if (f.isDirectory()) {
            return null;
        }
        String name;
        name = f.getName();

        int dotPos = getRevIndexOf(name, '.');
        if (dotPos == -1) {
            return null;
        }
        String dictName = name.substring(0,dotPos);
        if (dictName.length() == 0) {
            return null;
        }
        FileAccessBase idx = null;
        FileAccessBase dictFile = null;
        idx = openIndex(f, dictName);
        try {
            if (idx != null && idx.exists() && !idx.isDirectory()) {
                dictFile = openDict(f, dictName);
                if (dictFile != null && dictFile.exists() && !dictFile.isDirectory()) {

                    Dict dict = new starDict(this, f, dictName, properties);
                    if (dict != null) {
                        dict.setFactoryClass(this.getClass().getName());
                        dict.setFormat(this.getFormat());
                    }


                    return dict;
                }
            }
        } finally {
            if (idx != null) {
                idx.close();
            }
            if (dictFile != null) {
                dictFile.close();
            }
        }



        return null;
    }

    public Dict CreateDict(FileAccessBase file, String name, Properties properties) throws IOException {
        return super.CreateDict(file, name, properties);
    }

    public String getFormat() {
        return "mdict";
    }

    public FileAccessBase openDict(FileAccessBase f, String dictName) throws IOException, IOException {
        String name;
        name = f.getName();

        int dotPos = getRevIndexOf(name, '.');
        if (dotPos == -1) {
            return null;
        }
        FileAccessBase idx = null;
        String filename = name.substring(0,dotPos);
        if (filename.length() == 0) {
            return null;
        }
        FileAccessBase dict = f.parent().child(filename + ".dict");
        return dict;
    }

    public FileAccessBase openIndex(FileAccessBase f, String dictName) throws IOException, IOException {
        return f;
    }

    public boolean supportFolderDict() {
        return false;
    }
}