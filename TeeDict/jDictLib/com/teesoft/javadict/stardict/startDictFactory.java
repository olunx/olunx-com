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

package com.teesoft.javadict.stardict;

import com.teesoft.javadict.Dict;
import com.teesoft.javadict.DictFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.javadict.Properties;
import com.teesoft.javadict.SoundFactory;
import com.teesoft.javadict.stardict.sound.StardictSound;
import java.io.IOException;
import java.util.Vector;


/**
 *
 * @author wind
 */
public class startDictFactory extends DictFactory {
    private static startDictFactory instance;

    public static startDictFactory getInstance()
    {
        if (instance==null)
            instance = new startDictFactory();
        return instance;
    }
    public Properties SupportedProperties() {
        return super.SupportedProperties();
    }

    public FileAccessBase openIndex(FileAccessBase f, String dictName) throws IOException, IOException {
        FileAccessBase idx = f.child(dictName + ".idx");
        if (idx == null || !idx.exists()) {
            idx = searchFile(f, dictName, ".idx");
        }
        return idx;
    }
    public FileAccessBase openInfo(FileAccessBase f, String dictName) throws IOException, IOException {
        FileAccessBase idx = f.child(dictName + ".ifo");
        if (idx == null || !idx.exists()) {
            idx = searchFile(f, dictName, ".ifo");
        }
        return idx;
    }
    public FileAccessBase openDict(FileAccessBase f, String dictName) throws IOException, IOException {
        FileAccessBase dict = f.child(dictName + ".dict");
        if (dict == null || !dict.exists()) {
            dict = searchFile(f, dictName, ".dict");
        }
        if (dict == null || !dict.exists()) {
            dict = searchFile(f, "", ".dict");
        }
        return dict;
    }
    public boolean supportFileDict() {
        return true;
    }

    public Dict AcceptDict(FileAccessBase f, Properties properties) throws IOException {
        if (f == null)
            return null;
        if (!f.isDirectory()) {
            if (f.isFile())
                return AcceptDict(f.parent(),properties);
            else
                return null;
        }
        String name;
        name = f.getName();
        int firstDashPos = name.indexOf('-');
        if (firstDashPos == -1) {
            return null;
        }
        int lastDashPos = getRevIndexOf(name, '-');
        if (lastDashPos == -1 || lastDashPos <= firstDashPos) {
            return null;
        }
        FileAccessBase idx=null;
        String dictName = name.substring(firstDashPos + 1, lastDashPos);
        if (dictName.length() == 0) {
            return null;
        }
        idx = openIndex( f, dictName);
        if (idx != null && idx.exists() && !idx.isDirectory()) {
            Dict dict = new starDict(this,f, dictName, properties);
            if (dict != null) {
                dict.setFactoryClass(this.getClass().getName());
                dict.setFormat(this.getFormat());
            }
            return dict;
        }


        return null;
    }

    public static int getRevIndexOf(String name, char c) {
        for (int i = name.length() - 1; i >= 0; i--) {
            if (name.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }
    private static int getRevIndexOf(String name, String c) {
        for (int i = name.length() - c.length(); i >= 0; i--) {
            if (name.substring(i,i+c.length()).equals(c)) {
                return i;
            }
        }
        return -1;
    }

    public Dict CreateDict(FileAccessBase file, String name, Properties properties) throws IOException {
        Dict dict = new starDict(this,file, name, properties);
        if (dict != null) {
            dict.setFactoryClass(this.getClass().getName());
            dict.setFormat(this.getFormat());
        }
        return dict;
    }



    public boolean supportFolderDict() {
        return true;
    }

    public startDictFactory() {
    }

    public String getFormat() {
        return "stardict";
    }

    private static FileAccessBase searchFile(FileAccessBase file, String dictName, String ext) throws IOException {
        String realName = "";
        if (dictName.indexOf("-")>0)
            realName = dictName.substring(dictName.indexOf("-")+1);
        FileAccessBase f=file.child(realName + ext);
        if (f.exists())
            return f;
        
        Vector v = file.listFiles();
        
        FileAccessBase ret = null;
        for(int i=0;v!=null && v.size()>i;++i)
        {
            f = (FileAccessBase) v.elementAt(i);
            if (f != null)
            {
                String name = (f.getName());
                if ( name.indexOf(ext)!=-1 && (name.indexOf(dictName)!=-1  || (realName.length()>0 && name.indexOf(realName)!=-1 )))
                {
                    String path = f.getAbsolutePath();
                    if (path.endsWith(ext))
                    {
                        ret =  f;
                        break;
                    }
                    else
                    {
                        int pos = getRevIndexOf(path, ext);
                        FileAccessBase  findFile= FileFactory.openFileAccess(path.substring(0,pos + ext.length()), true);                        
                        if (findFile != null && findFile.exists())
                            ret = findFile;
                        if (findFile!=null)
                            findFile.close();
                    }
                }
            }
        }
        for(int i=0;v!=null && v.size()>i;++i)
        {
            f = (FileAccessBase) v.elementAt(i);
            if (f != null && f!=ret)
            {
                f.close();
            }
        }
        return ret;
        
    }
}
