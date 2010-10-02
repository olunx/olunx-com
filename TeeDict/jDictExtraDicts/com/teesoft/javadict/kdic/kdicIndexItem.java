/*
 * kdicIndexItem.java
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
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA.
 */

package com.teesoft.javadict.kdic;

import com.teesoft.javadict.DictItem;
import com.teesoft.javadict.bucketLet;
import com.teesoft.javadict.byteArray;
import com.teesoft.javadict.tabParser;
import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class kdicIndexItem extends DictItem implements bucketLet{

    Vector /*<kdicBucketItem>*/ bucket=new Vector/*<kdicBucketItem>*/();
    
    public kdicIndexItem(kdicDict dict,byte [] kdictValue,int sectionkdict,int sectionLength) {
        super();
        setDict(dict);
        this.kdictValue = kdictValue;
        this.sectionkdict = sectionkdict;
        this.sectionLength = sectionLength;
        allLoaded = false;
    }

    public kdicDict getKdicDict()
    {
        return (kdicDict) getDict();
    }
    public byteArray getExplains() {
        return null;
    }

    public byte[] getBytes() {
        return kdictValue;
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
        ByteArrayOutputStream content = kdic.uncompressSection(getDict().getFile(), sectionkdict, sectionLength);
        tabParser parser = new tabParser(content.toByteArray(),getDict().getEncoding());
        parser.setDealEscapse(true);
        for(int i=0;i<parser.size();++i)
        {
            bucket.addElement(new kdicBucketItem( this,bucket.size(), parser.getKeyByte(i),parser.getValueByte(i)));
        }
        allLoaded=true;
    }
    
    private byte[] kdictValue;
    private int sectionkdict;
    private int sectionLength;
    private boolean allLoaded;
    public DictItem getNext() {
        if (((kdicBucketItem) getBucket().elementAt(0))!=null)
            return ((kdicBucketItem) bucket.elementAt(0)).getNext();
        else
            return getKdicDict().getIndex(index+1);
    }

    public DictItem getPrevious() {
        if (index>0)
        {
            return getKdicDict().getIndex(index-1);
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
            kdicIndexItem previous = getKdicDict().getIndex(index-1);
            if (previous!=null)
            {
                return previous.getBucketItem(i + previous.size());
            }
        }else
        if (i >= size())
        {
            kdicIndexItem next = getKdicDict().getIndex(index+1);
            if (next!=null)
            {
                return next.getBucketItem(i - size());
            }
        }      
        return null;
    }

}
