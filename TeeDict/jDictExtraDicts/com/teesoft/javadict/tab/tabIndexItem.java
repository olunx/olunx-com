/*
 * tabIndexItem.java
 * 
 * Created on Aug 5, 2007, 5:35:44 PM
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

package com.teesoft.javadict.tab;

import com.teesoft.javadict.DictItem;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.javadict.bucketLet;
import com.teesoft.javadict.byteArray;
import com.teesoft.javadict.tabParser;
import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class tabIndexItem extends DictItem implements bucketLet{

    Vector /*<tabBucketItem>*/ bucket=new Vector/*<tabBucketItem>*/();
    
    public tabIndexItem(tabDict dict,byte [] startValue,int sectionStart,int sectionLength) {
        super();
        setDict(dict);
        this.startValue = startValue;
        this.sectionStart = sectionStart;
        this.sectionLength = sectionLength;
        allLoaded = false;
    }

    public tabDict getTabDict()
    {
        return (tabDict) getDict();
    }
    public byteArray getExplains() {
        return null;
    }

    public byte[] getBytes() {
        return startValue;
    }

    public String getString() {
        try {
            return CharsetEncodingFactory.newString(getBytes(), getDict().getEncoding());
        } catch (UnsupportedEncodingException ex) {
            return new java.lang.String(getBytes());
        }
    }
    public String toString()
    {
        return getString();
    }
    public Vector getBucket() {
        if (!allLoaded)
        {
            try {
                loadOthers();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return bucket;
    }
    public void clearBucket() {
        allLoaded = false;
        bucket.removeAllElements();
    }

    int size() {
        return getBucket().size();
    }

    private void loadOthers() throws IOException {
        
        if (allLoaded)
            return;
        //System.gc();
        FileAccessBase file = getDict().getFile();
        byte []b= new byte[sectionLength];
        file.absolute(sectionStart);
        file.read(b);
        tabParser parser = new tabParser(b,getDict().getEncoding());
        parser.setDealEscapse(true);
        loadItems(parser);
        allLoaded=true;
    }

    public void loadItems(final tabParser parser) {
        
        for(int i=0;i<parser.size();++i)
        {
            bucket.addElement(new tabBucketItem( this, bucket.size(), parser.getKeyByte(i),parser.getValueByte(i)));
        }
        allLoaded=true;
    }
    
    private byte[] startValue;
    private int sectionStart;
    private int sectionLength;
    private boolean allLoaded;

    public DictItem getNext() {
        if (((tabBucketItem) getBucket().elementAt(0))!=null)
            return ((tabBucketItem) getBucket().elementAt(0)).getNext();
        else
            return getTabDict().getIndex(index+1);
    }

    public DictItem getPrevious() {
        if (index>0)
        {
            return getTabDict().getIndex(index-1);
        }
        return null;
    }
    public DictItem getBucketItem(int i) {
        if (i>=0 && i<size())
        {
            return (DictItem) getBucket().elementAt(i);
        }else
        if (i <0)
        {
            tabIndexItem previous = this.getTabDict().getIndex(index-1);
            if (previous!=null)
            {
                return previous.getBucketItem(i + previous.size());
            }
        }else
        if (i >= size())
        {
            tabIndexItem next = getTabDict().getIndex(index+1);
            if (next!=null)
            {
                return next.getBucketItem(i - size());
            }
        }      
        return null;
    }

}
