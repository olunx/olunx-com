/*
 * starDict.java
 *
 * Created on Aug 5, 2007, 3:19:20 PM
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
import com.teesoft.javadict.DictItem;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.javadict.ItemList;
import com.teesoft.javadict.Properties;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class dicfDict extends Dict {

    public dicfDict(FileAccessBase file, String name, String fileBasename, Properties properties) {
            super(file, name, properties);
            this.fileBasename = fileBasename;
        try {
            path = file.getAbsolutePath();
            path = path.substring(0, path.length() - file.getName().length());
            index = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        dictStream = null;
        super.close();
    }

    public boolean open() throws IOException {
        if (!this.isOpened()) {
            index = new dicfIndex(this);
            index.loadIndex(this.getFile());
            super.open();
        }
        return isOpened();
    }

    public ItemList search(byte[] word,int maxCount) {
        return index.search(word,maxCount);
    }

    public FileAccessBase getDictStream() throws IOException {
        if (dictStream == null) {
            dictStream = FileFactory.openFileAccess(path + fileBasename +".dict",true);
        }
        
        return dictStream;
    }

    public synchronized byte[] getTextAt(int start, int length) {
        
        byte[] b = null;
        try {
            if (getDictStream() == null) {
                return null;
            }
            
            if (FileFactory.isMicroedition()) {
                explainGetCount++;
                //if this is running on microedtion then we reload the stream every 10 times
                if (explainGetCount % 2 == 0) {
                    getDictStream().close();
                    dictStream = null;
                }
            }
            getDictStream().absolute(start);
            b = new byte[length];
            getDictStream().read(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return b;
    }
    private FileAccessBase dictStream;

    private dicfIndex index;
    private String fileBasename;
    private String path;

    private int explainGetCount=0;

    public Vector getFiles() {
        Vector retValue;
        
        retValue = super.getFiles();
        try {
            if (this.getFile().isFile())
                retValue.addElement(getDictStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return retValue;
    }
    public dicfIndexItem getIndex(int i) {
        if (this.index!=null)
        {
            if (i>=0 && this.index.size()>i)
                return index.getIndexItem(i);
        }
        return null;
    }
    public DictItem getFirstItem() {
        return this.getIndex(0);
    }

}
