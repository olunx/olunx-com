/*
 * kdicBucketItem.java
 * 
 * Created on Aug 5, 2007, 6:21:37 PM
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
import com.teesoft.javadict.byteArray;
import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author wind
 */
public class dicfBucketItem extends DictItem{
    private dicfIndexItem parentIndex;

    public dicfBucketItem(dicfIndexItem parentIndex,int index,byte[]word,int explainStart,int explainSize) {
        super();
        setDict( parentIndex.getdicfDict());
        this.index = index;
        this.parentIndex = parentIndex;
        this.word = word;
        this.explainStart = explainStart;
        this.explainSize = explainSize;
    }
    public dicfDict getdicfDict()
    {
        return (dicfDict) getDict();
    }
    public byteArray getExplains() {
        return new ByteArrayString(getdicfDict().getTextAt(explainStart, explainSize), getDict().getEncoding());
    }

    public byte[] getBytes() {
        return word;
    }

    public String getString() {
        try {
            return CharsetEncodingFactory.newString(word, getDict().getEncoding());
        } catch (UnsupportedEncodingException ex) {
             return new java.lang.String(word);
        }
    }
    public String toString()
    {
        return getString();
    }
    private byte[] word;
    private byte[] explains;
    private int explainStart;
    private int explainSize;

    public DictItem getNext() {
        return this.parentIndex.getBucketItem(index+1);
    }

    public DictItem getPrevious() {
        return this.parentIndex.getBucketItem(index-1);
    }

}
