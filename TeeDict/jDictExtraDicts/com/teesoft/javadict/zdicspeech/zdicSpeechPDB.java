/*
 * zdicSpeechPDB.java
 *
 * Created on 2007-10-1, 17:26:05
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.javadict.zdicspeech;

import com.teesoft.javadict.ByteArrayString;
import com.teesoft.javadict.bucketLet;
import com.teesoft.javadict.byteArray;
import com.teesoft.jfile.CharsetEncodingFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.WrapperFile;
import com.teesoft.jpdb.jpdb;
import com.teesoft.jpdb.lzss;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class zdicSpeechPDB implements bucketLet {

    private FileAccessBase file;
    private jpdb pdb;
    String charset = "utf-8";
    int step = 40;
    private String extName=".ogg";

    public zdicSpeechPDB(FileAccessBase file) {
        try {
            this.file = file;
            pdb = new jpdb(file, charset);
            for (int i = 0; i <= pdb.size() / step; ++i) {
                int index = i * step;
                if (index < pdb.size()) {
                    indexs.addElement(new zdicBucket(index));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
     public FileAccessBase getSound(byte[] b) throws IOException {
        lastPos = ByteArrayString.search(indexs, b, lastPos, true);
        if (lastPos<0)
            return null;
        zdicBucket bucket = getZdicBucket(lastPos);
                
        Vector bucketVec = bucket.getBucket();
        int bucketIndex = ByteArrayString.search(bucketVec, b, 0, false);
        
//        System.out.println( getZdicBucket(lastPos) );
//        System.out.println(bucket.getItem(bucketIndex).getString() );
        if ( ByteArrayString.compareToIgnoreCase(bucket.getItem(bucketIndex).getBytes(),b) == 0)
        {
            int wordIndex =  lastPos * step + bucketIndex;

            int len = bucket.getItem(bucketIndex).getBytes().length;

            WrapperFile soundFle = new WrapperFile(file, bucket.getItem(bucketIndex).getString() + extName , pdb.getStart(wordIndex) + 2 + len,pdb.getLength(wordIndex) -2 - len );
            return soundFle;
        }
        return null;
     }
     public boolean hasSound(byte[] b) {
        
        lastPos = ByteArrayString.search(indexs, b, lastPos, true);
        System.out.println( getZdicBucket(lastPos) );
        return true;
     }
    Vector indexs = new Vector();
    int lastPos = 0;

    public Vector getBucket() {
        return indexs;
    }
    public void clearBucket() {
    }
        
    public zdicBucket getZdicBucket(int index)
    {
        return (zdicSpeechPDB.zdicBucket) indexs.elementAt(index);
    }

    public class zdicItem implements byteArray {

        private byte[] bytes;

        private zdicItem(byte[] chars) {
            bytes = chars;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getString() {
            String word;
            try {
                word = CharsetEncodingFactory.newString(getBytes(), charset);
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
                word = new String(getBytes());
            }
            return word;
        }

        public String toString() {
            return getString();
        }

    }

    public class zdicBucket implements bucketLet, byteArray {

        Vector subIndexs;
        private byte[] bytes;
        boolean othersLoaded = false;
        int index;

        zdicBucket(int index) throws IOException {
            subIndexs = new Vector();
            this.index = index;
        }

        public Vector getBucket() {

            loadOthers();
            return subIndexs;
        }
        public void clearBucket() {
            othersLoaded = false;
            subIndexs.removeAllElements();
        }

        public zdicItem getItem(int index) {
            if (subIndexs.size() == 0)
            {
                try {
                    byte[] lenBytes = pdb.getBytes(this.index, 0, 2);
                    int len = lzss.bytesToInt(lenBytes, 0, 2);
                    byte[] chars = pdb.getBytes(this.index, 2, len);
                    subIndexs.addElement(new zdicItem(chars));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
            }
            if (index >= 0 && index < getBucket().size()) {
                return (zdicSpeechPDB.zdicItem) getBucket().elementAt(index);
            }
            return null;
        }

        public byte[] getBytes() {
            return getItem(0).getBytes();
        }

        public String getString() {
            return getItem(0).getString();
        }

        public String toString() {
            return getString();
        }

        private synchronized  void loadOthers() {
            if (!othersLoaded) {
                pdb.loadIndex(index+1,index+step);
                for (int i = index + 1; i < index + step && i < pdb.size(); ++i) {
                    try {
                        byte[] lenBytes = pdb.getBytes(i, 0, 2);
                        int len = lzss.bytesToInt(lenBytes, 0, 2);
                        byte[] chars = pdb.getBytes(i, 2, len);
                        subIndexs.addElement(new zdicItem(chars));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                othersLoaded = true;
            }
        }
    }
}