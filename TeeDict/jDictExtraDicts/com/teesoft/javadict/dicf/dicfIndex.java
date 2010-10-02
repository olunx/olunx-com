/*
 * kdicIndex.java
 * 
 * Created on Aug 5, 2007, 5:34:34 PM
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

import com.teesoft.javadict.ByteArrayString;
import com.teesoft.javadict.DictItem;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.javadict.ItemList;
import com.teesoft.javadict.bucketBase;
import com.teesoft.javadict.bucketLet;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class dicfIndex extends bucketBase{

    Vector /*<kdicIndexItem>*/ indexes = new Vector();
    public dicfIndex(dicfDict dict) {
        super(dict);
        this.dict = dict;
    }
    public void addIndexItem(dicfIndexItem item)
    {
        indexes.addElement(item);
    }
    public dicfIndexItem getIndexItem(int index)
    {
        return (dicfIndexItem) indexes.elementAt(index);
    }
  
    public void loadIndex(FileAccessBase input)
    {
        try {
            input.absolute(0);
            byte[] buf = new byte[1024 * 4];
            int len = input.read(buf, 0, 512);
            int start = 0;
            if (len >= 2 && buf[0] == -1 && buf[1] == -2) {
                start = 2;
            }
            int filePos = start;
            int sectionLength = 0;
            com.teesoft.javadict.dicf.dicfIndexParser p = new com.teesoft.javadict.dicf.dicfIndexParser(buf, start, len - start, dict.getEncoding(), 1);
            byte[] startValue = null;
            if (p.size() > 0) {
                startValue = p.getKeyByte(0);
                filePos = start;
                sectionLength = len;
            } else {
                return;
            }


            int skipLen = 1024 * 2;
            while (len > 0) {
                if (skipLen + input.getOffset() > input.fileSize()) {
                    sectionLength += input.fileSize() - filePos;
                    break;
                } else {
                    input.skip(skipLen);
                    sectionLength += skipLen;
                    len = input.read(buf, 0, 1024);
                    p = new com.teesoft.javadict.dicf.dicfIndexParser(buf, 0, len, dict.getEncoding(), 3);
                    if (p.size() >= 3) {
                        sectionLength += p.getKeyStart(1);
                        addIndexItem(new com.teesoft.javadict.dicf.dicfIndexItem(dict, startValue, filePos, sectionLength));
                        filePos += sectionLength;
                        sectionLength = len - p.getKeyStart(1);
                        skipLen = 1024 * 8;
                        startValue = p.getKeyByte(1);
                    } else {
                        skipLen = 0;
                    }
                }
            }

            //System.out.println(dict.getEncoding());
            addIndexItem(new com.teesoft.javadict.dicf.dicfIndexItem(dict, startValue, filePos, sectionLength));

            input.close();
            java.lang.System.gc();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
}
            
    public int size()
    {
        return indexes.size();
    }

    protected  bucketLet get(int pos) {
        if (pos<0 || pos>=indexes.size())
            return null;
       return (bucketLet) indexes.elementAt(pos);
    }
    protected  Vector getIndexes() {
        return indexes;
    }
    private dicfDict dict;




}
