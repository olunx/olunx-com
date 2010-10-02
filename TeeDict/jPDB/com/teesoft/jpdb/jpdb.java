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

package com.teesoft.jpdb;

import com.teesoft.jfile.CharsetEncodingFactory;
import com.teesoft.jfile.FileAccessBase;
import java.io.IOException;


/**
 *
 * @author wind
 */
public class jpdb {

    private static int COUNT_BITS = 3;
    private String dictName;
    private FileAccessBase file;
    private String charset;
    private String keyWord;
    private int[] secs;
    private long secLengthSessionStart;

    /** Creates a new instance of Main */
    public jpdb(FileAccessBase file, String charset) throws IOException {
        this.file =  file;
        this.charset = charset;
        secs = null;
        if (validate(file,charset))
            dekdic();
        else
            throw new IOException("Not a validate pdb file.");
    }

    public int getStart(int index) {
        if (index >=0 && index < size()-1)
        {
            if (secs[index]==0)
            {
                try {
                    byte[] buf = new byte[4];
                    file.absolute(secLengthSessionStart + index*8);
                    file.read(buf);
                    secs[index] = lzss.bytesToInt(buf, 0, 4);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return secs[index];
        }
        return 0;    
    }
    public boolean isValidate()
    {
        return secs!=null;
    }

    public void loadIndex(int start, int end) {
        try {
            byte[] buf = new byte[8 * end - start];
            file.absolute(secLengthSessionStart + start * 8);
            file.read(buf);
            for (int i = start; i < end && i < secs.length; ++i) {
                secs[i] = lzss.bytesToInt(buf, (i - start) * 8, 4);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    public int size()
    {
        if (isValidate())
            return secs.length;
        return 0;
    }
    public int getLength(int index)
    {
        if (index >=0 && index < size()-1)
            return -1 * getStart(index) + getStart(index+1);
        else if (index == size() -1)
            return (int) (file.fileSize() - getStart(index));
        return 0;
    }
    public byte[] getBytes(int index) throws IOException
    {
        int len = getLength(index);
        return getBytes(index,0,len);
    }
    public byte[] getBytes(int index,int start,int length) throws IOException
    {
        byte [] b = new byte[length];
        file.absolute(getStart(index) + start);
        file.read(b);
        return b;
    }
    private static int getRevIndexOf(String name, char c) {
        for (int i = name.length() - 1; i >= 0; i--) {
            if (name.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }



    private boolean validate(FileAccessBase file, String charset) {
        try {
            String fileName = file.getName();
            int dotPos = getRevIndexOf(fileName, '.');
            if (dotPos == -1) {
                return false;
            }
            String extName = fileName.substring(dotPos + 1).toLowerCase();
            if (!extName.equals("pdb")) {
                return false;
            }
            String showName = fileName.substring(0, dotPos);
            if (showName.length() == 0) {
                return false;
            }

            if (!file.exists()) {
                return false;
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
            try{
                dictName = CharsetEncodingFactory.newString(head, 0, pos, chset);
            }catch(java.io.UnsupportedEncodingException ex)
            {
                dictName = new String(head, 0, pos);
            }
            dictName = dictName + "(" + showName + ")";

            ////System.out.println(CharsetEncodingFactory.newString(indexBuf,"utf-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
        //System.out.println(result.dictName);
        return true;
    }



    public void dekdic() throws IOException {
        FileAccessBase input = file;
        if (!input.exists()) {
            throw new IOException("file " + input.getAbsolutePath() + " doesn't exist!");
        }
        input.absolute(0);
        byte[] buf = new byte[18];
        byte[] head = new byte[0x3c];
        try {

            input.read(head, 0, 0x3c);
            head = null;
            input.read(buf, 0, 18);
            keyWord = new String(buf, 0, 8);
            //if (!dictKeyWord.equals("DictKdic")) {
            //    throw new IOException("file " + dict.getFile().getAbsolutePath() + "isn' a kdic file!");
            //}


            int secCount = lzss.bytesToInt(buf, 16, 2);
            //int secLengthSessionStart = lzss.bytesToInt(buf, 18, 4) + 0x10;

            secs = new int[secCount];
            
            secLengthSessionStart = input.getOffset();
            for (int i = 0; i < secCount; i++) {
                //input.read(buf, 0, 8);
                secs[i] = 0;// lzss.bytesToInt(buf, 0, 4);
            }
            input.close();
            buf=null;
            //System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

  
}
