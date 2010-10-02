/*
 * kdicDict.java
 * 
 * Created on Aug 5, 2007, 12:28:12 PM
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

package com.teesoft.javadict.kdic;

import com.teesoft.javadict.Dict;
import com.teesoft.javadict.DictItem;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.javadict.ItemList;
import com.teesoft.javadict.Properties;
import java.io.IOException;

/**
 *
 * @author wind
 */
public class kdicDict extends Dict {

    public boolean open() throws IOException {
        if (!this.isOpened())
            kdic.dekdic(this);
        return super.open();
    }

    public ItemList search(byte[] word,int maxCount) {
        try {
            open();
            return index.search(word,maxCount);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new ItemList();
    }

    public kdicDict(FileAccessBase file, String name, Properties properties) {
            super(file, name, properties);
        try {
            java.lang.String charset = null;
            if (properties != null) {
                charset = (java.lang.String) properties.getProperty("charset");
            }
            if (charset == null || charset.length() < 0) {
                charset = com.teesoft.javadict.kdic.kdic.getCharset(file);
            }
            this.setEncoding(charset);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
                
    }
    
    public void setIndex(kdicIndex index)
    {
        this.index = index;
    }
    private kdicIndex index;
    public kdicIndexItem getIndex(int i) {
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
