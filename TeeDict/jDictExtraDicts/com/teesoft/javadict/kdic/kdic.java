/*
 * kdic.java
 *
 * Created on 2007-8-5, 4:32 PM
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

import com.teesoft.jpdb.lzss;
import com.teesoft.javadict.ByteArrayString;
import com.teesoft.jfile.FileAccess;
import com.teesoft.javadict.kdic.kdic.section;
import com.teesoft.javadict.tabParser;
import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 *
 * @author wind
 */
public class kdic {

    private static int COUNT_BITS = 3;

    /** Creates a new instance of Main */
    private kdic() {
    }


    private static int getRevIndexOf(String name, char c) {
        for (int i = name.length() - 1; i >= 0; i--) {
            if (name.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    public static class ValidateResult {

        String dictName;
        String charset;
    }

    public static ValidateResult validate(FileAccess file, String charset) {
        ValidateResult result = new ValidateResult();
        try {
            String fileName = file.getName();
            int dotPos = getRevIndexOf(fileName, '.');
            if (dotPos == -1) {
                return null;
            }
            String extName = fileName.substring(dotPos + 1).toLowerCase();
            if (!extName.equals("pdb")) {
                return null;
            }
            String dictName = fileName.substring(0, dotPos);
            if (dictName.length() == 0) {
                return null;
            }

            if (!file.exists()) {
                return null;
            }
            file.absolute(0);
            byte[] b = new byte[26];
            byte[] head = new byte[60];

            file.read(head, 0, 60);
            file.read(b, 0, 26);
            java.lang.String dict = new java.lang.String(b, 0, 8);
            //shoud I remove this check?
            //if (!dict.equals("DictKdic")) {
            //    return null;
            //}
            int pos = 0;

            while (head[pos] != 0 && pos < 60) {
                pos++;
            }

            String chset = charset;
            if (chset == null || chset.length() == 0) {
                chset = getCharset(file);
            }
            result.charset = chset.toLowerCase();
            try{
                result.dictName = CharsetEncodingFactory.newString(head, 0, pos, chset);
            }catch(java.io.UnsupportedEncodingException ex)
            {
                result.dictName = new String(head, 0, pos);
            }
            result.dictName = result.dictName + "(" + dictName + ")";

            ////System.out.println(CharsetEncodingFactory.newString(indexBuf,"utf-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        //System.out.println(result.dictName);
        return result;
    }


    public static String getCharset(FileAccess file) throws IOException {
        FileAccess input = file;
        if (!input.exists()) {
            throw new IOException("file " + file + " doesn't exist!");
        }
        input.absolute(0);
        byte[] b = new byte[1024];
        byte[] head = new byte[0x3c];
        try {

            input.read(head, 0, 0x3c);
            input.read(b, 0, 26);
            String dict = new String(b, 0, 8);
            //if (!dict.equals("DictKdic")) {
            //    throw new IOException("file " + file.getAbsolutePath() + " isn' a kdic file!");
            //}

            int zPos = 0;
            while (head[zPos] != 0 && zPos < 0x3c) {
                zPos++;
            }

            int secCount = lzss.bytesToInt(b, 16, 2) - 1;
            int secLengthSessionStart = lzss.bytesToInt(b, 18, 4) + 0x10;

            int readCount = secCount > 3 ? 3 : secCount;
            section[] secs = new section[readCount];
            byte[] buf = new byte[8];

            for (int i = 0; i < readCount; i++) {
                input.read(buf, 0, 8);
                secs[i] = new section();
                secs[i].sectionStart = lzss.bytesToInt(buf, 0, 4);
            }
            input.absolute(secLengthSessionStart);
            for (int i = 0; i < readCount; i++) {
                input.read(buf, 0, 2);
                secs[i].sectionLength = lzss.bytesToInt(buf, 0, 2);
            }

            int lang = nsPSMDetector.ALL;
            nsDetector det = new nsDetector(lang);

// The Notify() will be called when a matching charset is found.
            class detectionObserver implements nsICharsetDetectionObserver {

                public void Notify(String charset) {
                    //System.out.println("CHARSET = " + charset);
                    this.charset = charset;
                }
                String charset = "";
            }
            detectionObserver observer = new detectionObserver();
            det.Init(observer);


            boolean done = false; //det.DoIt(indexBuf, indexBuf.length, false);
            int sectionIndex = 0;
            while (!done && sectionIndex < readCount) {
                input.absolute(secs[sectionIndex].sectionStart);
                buf = new byte[secs[sectionIndex].sectionLength];
                input.read(buf);
                ByteArrayOutputStream bOut = new lzss().uncompress(buf);
                done = det.DoIt(bOut.toByteArray(), bOut.size(), false);
                tabParser parser = new tabParser(bOut.toByteArray(), "utf-8");
                if (done && observer.charset.length() == 0) {
                    det.Reset();
                    for (int i = 0; i < parser.size(); ++i) {
                        byte[] value = parser.getValueByte(i);
                        done = det.DoIt(value, value.length, false);
                        if (done) {
                            break;
                        }
                    }
                }
                sectionIndex++;
            }
            det.DataEnd();
            if (done) {
                return observer.charset;
            }
            String[] prob = det.getProbableCharsets();
            for (int i = 0; i < prob.length; i++) {
                //System.out.println("Probable Charset = " + prob[i]);
            }
            det = null;
            System.gc();
            return prob[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "utf-8";
    }

    public static void dekdic(kdicDict dict) throws IOException {
        FileAccess input = dict.getFile();
        if (!input.exists()) {
            throw new IOException("file " + dict.getFile().getAbsolutePath() + " doesn't exist!");
        }
        input.absolute(0);
        byte[] buf = new byte[26];
        byte[] head = new byte[0x3c];
        try {

            input.read(head, 0, 0x3c);
            head = null;
            input.read(buf, 0, 26);
            String dictKeyWord = new String(buf, 0, 8);
            //if (!dictKeyWord.equals("DictKdic")) {
            //    throw new IOException("file " + dict.getFile().getAbsolutePath() + "isn' a kdic file!");
            //}


            int secCount = lzss.bytesToInt(buf, 16, 2) - 1;
            int secLengthSessionStart = lzss.bytesToInt(buf, 18, 4) + 0x10;

            section[] secs = new section[secCount];
            
            for (int i = 0; i < secCount; i++) {
                input.read(buf, 0, 8);
                secs[i] = new section();
                secs[i].sectionStart = lzss.bytesToInt(buf, 0, 4);
            }
            input.absolute(secLengthSessionStart);
            for (int i = 0; i < secCount; i++) {
                input.read(buf, 0, 2);
                secs[i].sectionLength = lzss.bytesToInt(buf, 0, 2);
            }

            //System.out.println(input.getOffset());
            
            int pos = secLengthSessionStart + (secCount) * 2;
            int wordListLength = secs[0].sectionStart - pos;
            
            int index = 0;
            //System.out.println("Index size:" + wordListLength);
            kdicIndex kIndex = new kdicIndex(dict, secCount);
            indexParser parser = new indexParser(input, wordListLength);
            for (int i = 0; i < secCount; ++i) {

                byte[] startValue = parser.nextstr(false);
                //ignore end word
                byte[] endValue = parser.nextstr(true);
                //System.out.println("parser index  " + i  + ": "+ CharsetEncodingFactory.newString(startValue,dict.getEncoding()) + " to " + CharsetEncodingFactory.newString(endValue,dict.getEncoding()));
                //System.out.println(Runtime.getRuntime().freeMemory());
                //System.out.println(Runtime.getRuntime().totalMemory());

                kIndex.addIndexItem(new kdicIndexItem(dict, startValue, secs[i].sectionStart, secs[i].sectionLength));
            }


            dict.setIndex(kIndex);
            input.close();
            parser=null;
            buf=null;
            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class indexParser {

        byte[] indexBuf = new byte[1024*4];
        int currPos = 0;

        indexParser(FileAccess file, int length) {
            try {
                this.file = file;
                this.indexLength = length;
                bufLength = file.read(indexBuf, currPos, indexBuf.length);
                indexLength -= bufLength;
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        private byte[] nextstr(boolean ignore) {
            byte[] str = null;
            int newPos = currPos;
            while (newPos <= bufLength - 1 && indexBuf[newPos] != 0) {
                newPos++;
            }
            if (newPos == bufLength) {
                
                //read next buf
                //System.out.println("Load next buf");
                ByteArrayString.byteCopy(indexBuf, currPos, indexBuf, 0, bufLength - currPos);
                bufLength = bufLength - currPos;
                currPos = 0;
                //if (indexLength > 0) 
                {
                    try {
                        int len = file.read(indexBuf, bufLength, indexBuf.length - bufLength );
                        if (len>0)
                        {
                            indexLength -= len;
                            bufLength += len;
                        }
                        java.lang.System.gc();
                        return nextstr(ignore);
                    } catch (IOException ex) {
                        return null;
                    }
                } 
                //else {
                //    return null;
                //}
            }
            if (!ignore) {
                str = new byte[newPos - currPos];
                ByteArrayString.byteCopy(indexBuf, currPos, str, 0, str.length);
            }
            currPos = newPos+1;
            return str;
        }
        private FileAccess file;
        private int indexLength;
        private int bufLength;
    }

    public static ByteArrayOutputStream uncompressSection(FileAccess input, int sectionStart, int sectionLength) throws IOException {
        input.absolute(sectionStart);
        byte[] buf = new byte[sectionLength];
        input.read(buf,0,buf.length);

        ByteArrayOutputStream bOut = new lzss().uncompress(buf);
        return bOut;
    }

    public static class section {

        int sectionStart;
        int sectionLength;
    }
}
