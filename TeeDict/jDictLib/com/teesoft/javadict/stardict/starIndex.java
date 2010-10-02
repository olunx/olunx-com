/*
 * starIndex.java
 *
 * Created on 2006-9-24, 1:34 AM
Copyright (C) 2006.2007  Yong Li. All rights reserved.
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
import com.teesoft.javadict.DictManager;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.javadict.bucketBase;
import com.teesoft.javadict.bucketLet;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author ly
 */
public class starIndex extends bucketBase {

    static final int highestPower = 24;// 256 * 256 * 256;
    static final int midPower = 16 ;//256 * 256;
    static final int smallPower = 8;//256;
    static int bufLen = 1024 * 16;
    private starDict dict;
    String fileName = "";
    Vector wordList = new /*<starIndexItem> */ Vector /*<starIndexItem>*/();
    private int offset = 0;
    private int count = 0;
    public int bucketSize = starIndexItem.BucketSize;
    private FileAccessBase indexFile=null;

    /**
     * Creates a new instance of ByteArrayString
     */
    public starIndex(FileAccessBase indexFile, starDict dict) {
        super(dict);
        if (Runtime.getRuntime().totalMemory()<1024*1024)
            bufLen = 1024 * 8;
        FileAccessBase file = null;
        try {
            this.dict = dict;
            this.indexFile = indexFile;
            this.fileName = indexFile.getAbsolutePath();
            boolean indexOfIndexLoaded = false;
            file = FileFactory.openFileAccess(fileName + ".idx", true);

            try {
                if (file != null && file.isFile() && file.canRead()) {
                    if (file.fileSize() > 0) {
                        loadFromIndexOfIndexFile(file);
                        indexOfIndexLoaded = true;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (file != null) {
                    file.close();
                    file = null;
                }
            }

            if (!indexOfIndexLoaded) {
                try {
                    DictManager.getInstance().NotifyPromptStart("tuningindex");
                    file  = getIndexFile();
                    loadFromIndexFile(file);
                    //System.gc();
                    saveToIndexOfIndexFile(fileName + ".idx");
                    DictManager.getInstance().NotifyPromptEnd("tuningindex");
                } catch (Exception ex) {
                    //System.out.println("Error While load index:" + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public FileAccessBase getIndexFile() throws IOException {
            if (indexFile == null) {
                indexFile = FileFactory.openFileAccess(fileName, true);
            }
        return indexFile;
    }
    public void setIndexFile(FileAccessBase indexFile)
    {
        this.indexFile = indexFile;
    }

    public void close() {
        if (indexFile!=null)
        {
            try {
                indexFile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            indexFile = null;
        }
    }
    
    private void loadFromIndexFile(FileAccessBase file) throws IOException {
        if (file != null && file.isFile() && file.canRead()) {

            byte[] buf = new byte[bufLen];
            int pos = 0;
            int ret = 0;
            int contentLength = 0;
            do {
                ret = file.read(buf, pos, buf.length - pos);
                if (ret <= 0) {
                    break;
                }
                contentLength = pos + ret;
                pos = parseBuf(buf, contentLength);
            //System.out.println( Runtime.getRuntime().freeMemory() + " of " + Runtime.getRuntime().totalMemory() );
            } while (true);
            file.close();
            buf = null;
        //System.gc();
        }
    }

    private void loadFromIndexOfIndexFile(final FileAccessBase file) throws IOException {
        if (file.isFile() && file.canRead()) {

            byte[] buf = new byte[bufLen];
            int pos = 0;
            int ret = 0;
            int contentLength = 0;
            //read the bocket size,it's 4 byte
            ret = file.read(buf, pos, 4);
            bucketSize = getIntFromByte(pos, buf);
            do {
                ret = file.read(buf, pos, buf.length - pos);
                if (ret == -1) {
                    break;
                }
                contentLength = pos + ret;
                pos = parseIndexOfIndexBuf(buf, contentLength);
            } while (true);
            file.close();
            buf = null;
        //System.gc();
        }
    }

    private int parseBuf(byte[] buf, int contentLength) {
        int pos;
        int wordPos;
        int wordSize;
        int start = 0;
        int searchLen = contentLength - 8;
        for (pos = start; pos < searchLen; pos++) {
            if (buf[pos] == 0) {
                pos++;
                wordPos = getIntFromByte(pos, buf);
                wordSize = getIntFromByte(pos + 4, buf);
                try {
                    offset += pos - start + 8;
                    AddWord(buf, start, pos - start - 1, wordPos, wordSize, offset);
                    count++;
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
                start = pos + 8;
                pos = start;
            }
        }
        alignData(buf, start, contentLength);
        return contentLength - start;
    }

    private int parseIndexOfIndexBuf(byte[] buf, int contentLength) {
        int fileBlockLen;
        int pos;
        int wordPos;
        int wordSize;
        int start = 0;
        int searchLen = contentLength - 16;
        for (pos = start; pos < searchLen; pos++) {
            if (buf[pos] == 0) {
                pos++;
                wordPos = getIntFromByte(pos, buf);
                wordSize = getIntFromByte(pos + 4, buf);
                offset = getIntFromByte(pos + 8, buf);
                fileBlockLen = getIntFromByte(pos + 12, buf);
                try {
                    AddWord(buf, start, pos - start - 1, wordPos, wordSize, offset);
                    count += bucketSize;
                    ((starIndexItem) wordList.lastElement()).setLength(fileBlockLen);
                } catch (UnsupportedEncodingException ex) {
                    ex.printStackTrace();
                }
                start = pos + 16;
                pos = start;
            }
        }
        alignData(buf, start, contentLength);
        return contentLength - start;
    }

    public static int getIntFromByte(final int pos, final byte[] buf) {
        int wordPos;
        wordPos = (getIndexFromByte(buf[pos]) << highestPower) | (getIndexFromByte(buf[pos + 1]) << midPower) | (getIndexFromByte(buf[pos + 2]) << smallPower) | (getIndexFromByte(buf[pos + 3]));
        return wordPos;
    }

    public static void putIntintoByte(final int value, final int pos, final byte[] buf) {
        int v = value;
        for (int i = 3; i >= 0; i--) {
            buf[pos + i] = getByteFromInt(v % 256);
            v = v / 256;
        }
    }

    private void AddWord(byte[] buf, int start, int len, int wordPos, int wordSize, int fileOffset) throws UnsupportedEncodingException {
        if (count % bucketSize == 0) {
            wordList.addElement(new starIndexItem(dict, wordList.size(), buf, start, len, wordPos, wordSize, fileOffset));
        } else {
            ((starIndexItem) wordList.lastElement()).addWord(buf, start, len, wordPos, wordSize, fileOffset);
        }
    }

    public static int getIndexFromByte(byte b) {
        if (b < 0) {
            return 256 + b;
        } else {
            return (int) b;
        }
    }

    public static byte getByteFromInt(int i) {
        if (i > 127) {
            return (byte) (i - 256);
        } else {
            return (byte) i;
        }
    }

    private void alignData(byte[] buf, int start, int contentLength) {
        //according to the java doc this will create a temporary array if src and dst array are the same one
        //,so we don't use it
        //System.arraycopy(buf,start,buf,0,contentLength);
        for (int i = start; i < contentLength; i++) {
            buf[i - start] = buf[i];
        }
    }

    int size() {
        return wordList.size();
    }

    protected bucketLet get(int index) {
        return getStarIndexItem(index);
    }

    protected starIndexItem getStarIndexItem(int index) {
        return (starIndexItem) wordList.elementAt(index);
    }

    void remove(int i) {
        wordList.removeElementAt(i);
    }

    private synchronized void saveToIndexOfIndexFile(String fileName) throws IOException {
        FileAccessBase file = FileFactory.newFileAccess(fileName);
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            file.create();
        }
        if (file.isFile() && file.canWrite()) {
            int writeBufLen = 256 + 1 + 16;
            byte[] buf = new byte[bufLen];
            int pos = 0;
            int ret = 0;
            int contentLength = 0;
            starIndex.putIntintoByte(this.bucketSize, 0, buf);
            //System.out.println("write " + wordList.size());
            file.write(buf, 0, 4);
            for (int i = 0; i < wordList.size(); i++) {
                starIndexItem item = (starIndexItem) wordList.elementAt(i);
                if (item.getBytes().length + 19 + pos < bufLen) {
                    ByteArrayString.byteCopy(item.getBytes(), 0, buf, pos, item.getBytes().length);
                    pos += item.getBytes().length;
                    buf[pos] = 0;
                    pos++;
                    starIndex.putIntintoByte(item.getbucket(0).getStart(), pos, buf);
                    pos += 4;
                    starIndex.putIntintoByte(item.getbucket(0).getLength(), pos, buf);
                    pos += 4;
                    starIndex.putIntintoByte(item.getStart(), pos, buf);
                    pos += 4;
                    starIndex.putIntintoByte(item.getLength(), pos, buf);
                    pos += 4;
                } else {
                    file.write(buf, 0, pos);
                    pos = 0;
                    i--;
                }
            }
            if (pos != 0) {
                file.write(buf, 0, pos);
            }
            file.flush();
            file.close();
            buf = null;
        }
    }
    int lastPos = 0;

    protected Vector getIndexes() {
        return wordList;
    }
}
