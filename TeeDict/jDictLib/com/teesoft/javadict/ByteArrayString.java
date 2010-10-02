/*
 * ByteArrayString.java
 *
 * Created on Aug 5, 2007, 4:04:39 PM
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

import com.teesoft.jfile.CharsetEncodingFactory;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class ByteArrayString implements byteArray {

    public ByteArrayString(String str) {
        this(str.getBytes(),"");
    }

    public ByteArrayString(byte[] b) {
        this(b, "utf-8");
    }

    public ByteArrayString(byte[] b, String charset) {
        this.b = b;
        this.charset = charset;
    }

    public byte[] getBytes() {
        return b;
    }

    public String getString() {
        if (charset.length()==0)
            return new String(b);
        try {
            java.lang.String str = CharsetEncodingFactory.newString(b, charset);
            return str;
        } catch (Throwable ex) {
            java.lang.String str = new java.lang.String(b);
            return str;
        }
    }
    private byte[] b;


    public static int find(Vector wordList, byte[] word, int start, int end, boolean littleFirst, boolean widely) {
        int lowerBound = start;
        int upperBound = end;
        int curIn;
        int comp;

        while (true) {

            curIn = (lowerBound + upperBound) / 2;
            //System.out.println("Checking " + curIn);
            if (curIn>wordList.size()-1)
                return wordList.size()-1;
            if (curIn<0)
                return 0;
            byteArray index = (byteArray) wordList.elementAt(curIn);

            comp = compareToIgnoreCase(word, index.getBytes());
            if (comp == 0) {
                return curIn;
            } else if (lowerBound > upperBound) {
                if (littleFirst)
                    return upperBound;
                return lowerBound;
            } else {
                if (comp > 0) {
                    lowerBound = curIn + 1;
                } else {
                    upperBound = curIn - 1;
                }
            }
        }
    }
    public static int search(Vector wordList, byte[] word, int lastPosition, boolean minFirst) {
        long time1 = System.currentTimeMillis();
        int pos1=find(wordList, word, 0, wordList.size()-1, minFirst, minFirst);
        //int pos2 =search(wordList, word, lastPosition, minFirst, false);
        return pos1;
    }
    public static int compareTo(byteArray left, byteArray right) {
        return compareTo(left.getBytes(), right.getBytes());
    }

    public static int compareTo(byte[] left, byte[] right) {

        return compareTo(left, 0, left.length, right, 0, right.length);
    }

    public static int compareTo(byte[] left, int lStart, int lLength, byte[] right, int rStart, int rLength) {
        byte[] l = left;
        byte[] r = right;
        if (l == null) {
            if (r == null) {
                return 0;
            } else if (r != null) {
                return -1;
            }
        } else if (r == null) {
            return 1;
        }
        int lSize = lLength;
        int rSize = rLength;
        if (rSize < lSize) {
            return -1 * compareToIgnoreCase(right, rStart, rLength, left, lStart, lLength);
        }
        int cmp = 0;
        for (int i = 0; i < lSize; i++) {
            cmp = compareSingle(l[i + lStart], r[i + rStart]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return lSize - rSize;
    }

    public static int compareToIgnoreCase(byteArray left, byteArray right) {
        return compareToIgnoreCase(left.getBytes(), right.getBytes());
    }

    public static int compareToIgnoreCase(byte[] left, byte[] right) {

        return compareToIgnoreCase(left, 0, left.length, right, 0, right.length);
    }

    public static int compareToIgnoreCase(byte[] left, int lStart, int lLength, byte[] right, int rStart, int rLength) {
        byte[] l = left;
        byte[] r = right;
        if (l == null) {
            if (r == null) {
                return 0;
            } else if (r != null) {
                return -1;
            }
        } else if (r == null) {
            return 1;
        }
        int lSize = lLength;
        int rSize = rLength;
        if (rSize < lSize) {
            return -1 * compareToIgnoreCase(right, rStart, rLength, left, lStart, lLength);
        }
        int cmp = 0;
        for (int i = 0; i < lSize; i++) {
            cmp = compareSingleIgnoreCase(l[i + lStart], r[i + rStart]);
            if (cmp != 0) {
                return cmp;
            }
        }
        return lSize - rSize;
    }
    static char[] base = new char[256];
    static {
        for (char i = 0; i < 128; i++) {
            base[i] = (char) (128+i);
        }
        for (char i = 128; i < 256; i++) {
            base[i] = (char) (i - 128);
        }
        for (char i = 'A'; i <= 'Z'; i++) {
            base[i + 128] = (char) (i + ('a' - 'A'));
        }
    }

    public static int compareSingle(byte b, byte b0) {
        return b - b0;
    }
    public static int compareSingleIgnoreCase(byte b, byte b0) {
        return base[b + 128] - base[b0 + 128];
    }

    public static boolean startsWith(byte[] b, byte[] word) {
        if (b.length < word.length) {
            return false;
        }
        for (int i = 0; i < word.length; i++) {
            if (compareSingleIgnoreCase(b[i], word[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    public static void byteCopy(byte[] src, int srcpos, byte[] dst, int dstpos, int length) {
        for (int i = 0; i < length; i++) {
            dst[dstpos + i] = src[srcpos + i];
        }
    }
    private String charset;
}
