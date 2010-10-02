/*
 * dicfIndexParser.java
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

package com.teesoft.javadict.dicf;

import com.teesoft.javadict.ByteArrayString;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class dicfIndexParser {

    
    public dicfIndexParser(byte[] buf,String charset) {
        this(buf, 0, buf.length,charset);
    }
    Vector records = new Vector();

    public byte[] getKeyByte(int index)
    {
        record r= (record) records.elementAt(index);
        if (r.keyLength<=0)
            return new byte[0];
        byte [] b =new byte[r.keyLength];
        ByteArrayString.byteCopy(buf, r.start, b, 0, r.keyLength);
        return b;
    }
    public int getExplainStart(int index)
    {
        record r= (record) records.elementAt(index);
        return r.explainStart;
    }    
    public int getExplainSize(int index)
    {
        record r= (record) records.elementAt(index);
        return r.explainSize;
    }    
    public int size()
    {
        return records.size();
    }
    public int getKeyStart(int index)
    {
        record r = (record) records.elementAt(index);
        return r.start;
    }
        
    public dicfIndexParser(byte[] buf, int start, int length,String charset) {
        this(buf,start,length,charset,Integer.MAX_VALUE);
    }
    public dicfIndexParser(byte[] buf, int start, int length,String charset,int maxLines) {
        this.charset = charset;
        this.buf = buf;
        this.start = start;
        int endPos = start + length - 1;
        this.end = endPos;

        int beginning = start;
        int tab1 = -1;
        int tab2 = -1;
        int cr = -1;
        int tabSize=1;
        int count = 0;
        int state =0;
        
        for (int i = start; i <= endPos &&count < maxLines; ++i) {
            if (state<2 && buf[i] == '\t') {
                state++;
                if (state == 1)
                {
                    tab1 = i;
                }else if (state ==2)
                    tab2 = i;
            } else if (buf[i] == '\n') {
                cr = i;
                if (tab1 != -1 && tab2 != -1) {
                    int eStart = base64Decode(buf,tab1+1,tab2-1);
                    int eSize = base64Decode(buf,tab2+1,cr-1)  ;
                    records.addElement(new record(beginning, tab1 - beginning, eStart,eSize));
                    count++;
                }
                beginning = i+1;
                tab1 = tab2 = cr = -1;
                state=0;
            }
        }
        if (beginning < endPos) {
            if (tab1 != -1 && tab2 != -1) {
                    int eStart = base64Decode(buf,tab1+1,tab2-1);
                    int eSize = base64Decode(buf,tab2+1,cr-1);
                    records.addElement(new record(beginning, tab1 - beginning, eStart,eSize));
            }
        }
    }
    private byte[] buf;
    private int start;
    private int end;

    public static class record {

        int start;
        int keyLength;
    
        private record(int beginning, int keylen,int explainStart, int explainSize) {
            this.start = beginning;
            this.keyLength = keylen;
            this.explainStart = explainStart;
            this.explainSize = explainSize;
        }
        private int explainStart;
        private int explainSize;
    }
    private String charset;

static int XX= 100;

static int []b64_index = {
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,62, XX,XX,XX,63,
    52,53,54,55, 56,57,58,59, 60,61,XX,XX, XX,XX,XX,XX,
    XX, 0, 1, 2,  3, 4, 5, 6,  7, 8, 9,10, 11,12,13,14,
    15,16,17,18, 19,20,21,22, 23,24,25,XX, XX,XX,XX,XX,
    XX,26,27,28, 29,30,31,32, 33,34,35,36, 37,38,39,40,
    41,42,43,44, 45,46,47,48, 49,50,51,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX,
    XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX, XX,XX,XX,XX
};


    public static int byteToInt(byte b) {
        if (b >= 0) {
            return b;
        } else {
            return 256 + b;
        }
    }
    private static int base64Decode(byte[] buf, int start, int end) {
        int v = 0;
        int i;
        int offset = 0;

        for (i = end; i >= start; i--) {
            int tmp = b64_index[  byteToInt(buf[i]) ];


            v |= tmp << offset;
            offset += 6;
        }

        return v;
    }
}
