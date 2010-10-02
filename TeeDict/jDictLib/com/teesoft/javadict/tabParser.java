/*
 * tabParser.java
 *
 * Created on Aug 5, 2007, 11:51:24 PM
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

package com.teesoft.javadict;

import com.teesoft.javadict.tabParser.record;
import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class tabParser {

    
    public tabParser(byte[] buf,String charset) {
        this(buf, charset,'\t');
    }
    public tabParser(byte []buf,String charset,char separatorChar)
    {
        this(buf, 0, buf.length,charset,separatorChar);
    }
    Vector records = new Vector();

    private boolean dealEscapse;

    public byte[] getKeyByte(int index)
    {
        record r= (record) records.elementAt(index);
        if (r.keyLength<=0)
            return new byte[0];
        byte [] b =new byte[r.keyLength];
        ByteArrayString.byteCopy(buf, r.start, b, 0, r.keyLength);
        return b;
    }
    public byte[] getValueByte(int index)
    {
        record r= (record) records.elementAt(index);
       int tabsize=1;
       if (r.start + 1 + r.keyLength < buf.length && buf[r.start + 1 + r.keyLength] == 0)
       {
           tabsize  = 2;
       }
        if (r.valueLength<=0)
            return new byte[0];
        byte [] b =new byte[r.valueLength];
        if (isDealEscapse() && !r.escaped)
        {
            convertEscapse(buf,r.start + tabsize+r.keyLength,r.valueLength);
            r.escaped=true;
        }
        ByteArrayString.byteCopy(buf, r.start + tabsize+r.keyLength, b, 0, r.valueLength);
        return b;
    }    
    public String getKey(int index)
    {
        com.teesoft.javadict.tabParser.record r = (com.teesoft.javadict.tabParser.record) records.elementAt(index);
        try {
            return CharsetEncodingFactory.newString(buf, r.start, r.keyLength, charset);
        } catch (UnsupportedEncodingException ex) {
            return new java.lang.String(buf, r.start, r.keyLength);
        }
    }
    public String getValue(int index)
    {
        com.teesoft.javadict.tabParser.record r = (com.teesoft.javadict.tabParser.record) records.elementAt(index);
       int tabsize=1;
       if (r.start + 1 + r.keyLength < buf.length && buf[r.start + 1 + r.keyLength] == 0)
       {
           tabsize  = 2;
       }
        
       if (isDealEscapse() && !r.escaped)
        {
            convertEscapse(buf,r.start + tabsize+r.keyLength,r.valueLength);
            r.escaped=true;
        }
               
        try {
            return CharsetEncodingFactory.newString(buf, r.start + tabsize + r.keyLength, r.valueLength, charset);
        } catch (UnsupportedEncodingException ex) {
            return new java.lang.String(buf, r.start + tabsize + r.keyLength, r.valueLength);
        }
    }
    public int getKeyStart(int index)
    {
        com.teesoft.javadict.tabParser.record r = (com.teesoft.javadict.tabParser.record) records.elementAt(index);
        return r.start;
    }
    public int size()
    {
        return records.size();
    }

    public void sort() {
        int i,j;
        int len = records.size();
        int min=0;
        for(i=0;i<len-1;++i)
        {
            min=len-1;
            for(j=len-2;j>=i;j--)
            {
                if (lessThen(j,min))
                    min=j;
            }
            if(min!=i)
                switchItem(i,min);
            else
                return;
        }
    }

    private boolean lessThen(int j, int min) {
        com.teesoft.javadict.tabParser.record left = (com.teesoft.javadict.tabParser.record) records.elementAt(j);
        com.teesoft.javadict.tabParser.record right = (com.teesoft.javadict.tabParser.record) records.elementAt(min);
        
        return (ByteArrayString.compareToIgnoreCase(buf,left.start,left.keyLength,buf,right.start,right.keyLength) <0 );
    }

    private void switchItem(int i, int min) {
        com.teesoft.javadict.tabParser.record left = (com.teesoft.javadict.tabParser.record) records.elementAt(i);
        com.teesoft.javadict.tabParser.record right = (com.teesoft.javadict.tabParser.record) records.elementAt(min);
        int tmp = left.start;
        left.start = right.start;
        right.start=tmp;
        
        tmp = left.keyLength;
        left.keyLength = right.keyLength;
        right.keyLength=tmp;
        
        tmp = left.valueLength;
        left.valueLength = right.valueLength;
        right.valueLength=tmp;
    }
    public tabParser(byte[] buf, int start, int length,String charset,char separatorChar) {
        this(buf,start,length,charset,Integer.MAX_VALUE,separatorChar);
    }
    public tabParser(byte[] buf, int start, int length,String charset,int maxLines,char separatorChar) {
        this.charset = charset;
        this.buf = buf;
        this.start = start;
        int endPos = start + length - 1;
        this.end = endPos;

        int beginning = start;
        int tab = -1;
        int cr = -1;
        int tabSize=1;
        int count = 0;
        for (int i = start; i <= endPos &&count < maxLines; ++i) {
            if (tab ==-1 && buf[i] == separatorChar) {
                tab = i;
                if (tab<endPos && buf[tab+1] == 0)
                {
                    tabSize=2;
                }
                
            } else if (buf[i] == '\n') {
                cr = i;
                if (tab != -1) {
                    records.addElement(new record(beginning, tab - beginning, cr - tab - tabSize));
                } else {
                    records.addElement(new record(beginning, cr - beginning, 0));
                }
                beginning = i+1;
                if (beginning<endPos && buf[beginning] == 0)
                {
                    beginning++;
                    i++;
                }
                tab = cr = -1;
                tabSize=1;
                count++;
            }
        }
        if (beginning < endPos) {
            if (tab != -1) {
                records.addElement(new record(beginning, tab - beginning, endPos - tab - tabSize));
            } else {
                records.addElement(new record(beginning, endPos - beginning, 0));
            }
        }
    }
    private byte[] buf;
    private int start;
    private int end;

    public static class record {

        int start;
        int keyLength;
        int valueLength;
        boolean escaped=false;
        private record(int beginning, int keylen, int valuelen) {
            this.start = beginning;
            this.keyLength = keylen;
            this.valueLength = valuelen;
        }
    }
    private String charset;

    public boolean isDealEscapse() {
        return dealEscapse;
    }

    public void setDealEscapse(boolean dealEscapse) {
        this.dealEscapse = dealEscapse;
    }

    private void convertEscapse(byte[] buf,int start,int length) {
        boolean meetSlash=false;
        int i=start;
        int end = start + length;
        while(i < end)
        {
            if (meetSlash)
            {
                if (buf[i] == '\\')
                {
                    buf[i-1]=' ';
                }else if (buf[i] == 't')
                {
                    buf[i-1]=' ';
                    buf[i]='\t';
                }else if (buf[i] == 'n')
                {
                    buf[i-1]=' ';
                    buf[i]='\n';
                }else if (buf[i] == 'r')
                {
                    buf[i-1]=' ';
                    buf[i]='\n';
                }
                
                meetSlash=false;
            }else
                meetSlash = (buf[i] == '\\');
            i++;
        }
    }
}
