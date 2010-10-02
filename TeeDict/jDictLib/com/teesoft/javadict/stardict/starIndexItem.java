/*
 * starIndexItem.java
 *
 * Created on 2006-9-24, 1:42 AM
Copyright (C) 2006,2007  Yong Li. All rights reserved.
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

import com.teesoft.javadict.ByteArrayString;
import com.teesoft.javadict.byteArray;
import com.teesoft.javadict.DictItem;
import com.teesoft.javadict.bucketLet;
import com.teesoft.util.HtmlConvertor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author ly
 */
public class starIndexItem extends DictItem implements bucketLet {

    public static final int BucketSize = 128;
    private int start;
    private int length;
    boolean allLoaded = false;
    Vector bucket = new /*<starBucketItem>*/ Vector /*<starBucketItem>*/ ();

    /** Creates a new instance of indexItem */
    public starIndexItem(starDict dict, int index, byte[] word, int byteStart, int byteLen, int start, int length, int fileOffset) throws UnsupportedEncodingException {
        this.setDict(dict);
        this.index = index;
        //this.word=CharsetEncodingFactory.newString(word,byteStart,byteLen,"utf-8");
        bucket.addElement(new starBucketItem(this,bucket.size(),  word, byteStart, byteLen, start, length));
        this.setStart(fileOffset); //next word start
        this.setLength(0);
    }

    public String getString() {
        return ((starBucketItem) bucket.elementAt(0)).getString();
    }

    public void setWord(String word) {
        //this.word = word;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String toString() {
        return getString();
    }

    public starDict getStarDict() {
        return (starDict) getDict();
    }

    public void setStarDict(starDict dict) {
        setDict(dict);
    }



    public byte[] getBytes() {
        return ((starBucketItem) bucket.elementAt(0)).getBytes();
    }

    byte[] getExplains(int i) {
        return ((starBucketItem) getBucket().elementAt(i)).getExplains().getBytes();
    }

    public Vector getBucket() {
        if (!allLoaded) {
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
        Object o = bucket.elementAt(0);
        bucket.removeAllElements();
        bucket.addElement(o);
    }
    int size() {
        return getBucket().size();
    }

    private void loadOthers() throws IOException {
        //System.out.println("load others");
        byte[] buf = new byte[length];
        getStarDict().getIndexStream().absolute(start);
        int ret = getStarDict().getIndexStream().read(buf);
        parseBuf(buf, ret);
        buf = null;
        //System.gc();
        allLoaded = true;
    }

    private void parseBuf(byte[] buf, int contentLength) {
        int pos;
        int wordPos;
        int wordSize;
        int startPos = 0;
        int searchLen = contentLength - 8;
        for (pos = startPos; pos < searchLen; pos++) {
            if (buf[pos] == 0) {
                pos++;
                wordPos = starIndex.getIntFromByte(pos, buf);
                wordSize = starIndex.getIntFromByte(pos + 4, buf);
                bucket.addElement(new starBucketItem(this,bucket.size(), buf, startPos, pos - startPos - 1, wordPos, wordSize));
                startPos = pos + 8;
                pos = startPos;
            }
        }
    }

    Object get(int i) {
        if (i >= 1) {
            return getBucket().elementAt(i);
        } else {
            return this.bucket.elementAt(0);
        }
    }

    public starBucketItem getbucket(int i) {
        return (starBucketItem) get(i);
    }

    void addWord(byte[] buf, int start, int len, int wordPos, int wordSize, int fileOffset) {
        //here will not realy add the words
        length += len + 9; //1 byte 0,4 byte wordPos, 4 byte wordSize
    }

    public byteArray getExplains() {
        if (this.getStarDict().isXDict()) {
            java.lang.String ret = this.getStarDict().convertXDictToText(getStarDict().getTextAt(getStart(), getLength()));
            return new com.teesoft.javadict.ByteArrayString(ret);
        } else if (this.getStarDict().isHtml()) {
            java.lang.String html = "";
            try {
                html = new java.lang.String(getStarDict().getTextAt(getStart(), getLength()), "utf-8");
            } catch (UnsupportedEncodingException ex) {
                html = new java.lang.String(getStarDict().getTextAt(getStart(), getLength()));
            }
            com.teesoft.util.HtmlConvertor con = new com.teesoft.util.HtmlConvertor((html).toCharArray());
            return new com.teesoft.javadict.ByteArrayString(con.getString());
        }else
            return new ByteArrayString(getStarDict().getTextAt(getStart(), getLength()));
    }

    public byteArray getHtmlExplains() {
        if (!this.getStarDict().isXDict()) {
            return super.getHtmlExplains();
        } else {
            java.lang.String ret = this.getStarDict().convertXDictToHtml(getStarDict().getTextAt(getStart(), getLength()));
            return new com.teesoft.javadict.ByteArrayString(ret);
        }
    }

    public DictItem getNext() {
        if (((starBucketItem) bucket.elementAt(0))!=null)
            return ((starBucketItem) bucket.elementAt(0)).getNext();
        else
            return getStarDict().getIndex(index+1);
    }

    public DictItem getPrevious() {
        if (index>0)
        {
            return getStarDict().getIndex(index-1);
        }
        return null;
    }
    public DictItem getBucketItem(int i) {
        if (i>=0 && i<size())
        {
            return (DictItem) bucket.elementAt(i);
        }else
        if (i <0)
        {
            starIndexItem previous = getStarDict().getIndex(index-1);
            if (previous!=null)
            {
                return previous.getBucketItem(i + previous.size());
            }
        }else
        if (i >= size())
        {
            starIndexItem next = getStarDict().getIndex(index+1);
            if (next!=null)
            {
                return next.getBucketItem(i - size());
            }
        }      
        return null;
    }


    
}
