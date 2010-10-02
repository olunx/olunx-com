/*
 * bucketItem.java
 *
 * Created on 2006-9-26, 9:02 PM
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
import com.teesoft.javadict.Dict;
import com.teesoft.javadict.DictItem;
import com.teesoft.javadict.byteArray;
import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author ly
 */
public class starBucketItem extends DictItem
{
    private String word=null;
    private byte[] bytes;
    private int start;
    private starIndexItem parentIndex;
    starBucketItem(starIndexItem parentIndex, int index, byte[] word,int byteStart,int byteLen,int start,int length)
    {
        this.parentIndex = parentIndex;
        this.index = index;
        bytes = new byte[byteLen];
        for(int i=0;i<byteLen;i++)
            bytes[i]=word[byteStart+i];
        this.setStart(start);
        this.setLength(length);
        
    }
    private int length;
    
    public String getString() {
        if(word==null)
            try {
                word = CharsetEncodingFactory.newString(bytes,"utf-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        return word;
    }
    
    public void setWord(String word) {
        this.word = word;
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
    
    public byte[] getBytes() {
        return bytes;
    }


    public starDict getStarDict() {
        return parentIndex.getStarDict();
    }
    public Dict getDict()
    {
         return parentIndex.getDict();
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
            if (this.getStarDict().isHtml()) {
          
                java.lang.String html = "";
                try {
                    html = new java.lang.String(getStarDict().getTextAt(getStart(), getLength()), "utf-8");
                } catch (UnsupportedEncodingException ex) {
                    html = new java.lang.String(getStarDict().getTextAt(getStart(), getLength()));
                }
                return new com.teesoft.javadict.ByteArrayString(html);
            }
                else
                {
                    return super.getHtmlExplains();
                }
        } else {
            java.lang.String ret = this.getStarDict().convertXDictToHtml(getStarDict().getTextAt(getStart(), getLength()));
            return new com.teesoft.javadict.ByteArrayString(ret);
        }
    }

    public DictItem getNext() {
        return this.parentIndex.getBucketItem(index-1);
    }

    public DictItem getPrevious() {
        return this.parentIndex.getBucketItem(index+1);
    }

}

