/*
 * tab.java
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

package com.teesoft.javadict.tab;

import com.teesoft.javadict.ByteArrayString;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.javadict.tabParser;
import java.io.IOException;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 *
 * @author wind
 */
public class tab {
    
    private static int COUNT_BITS = 3;
    private static int minLength;
    static{
        if (FileFactory.isMicroedition())
            minLength = 128;
        else
            minLength = 256;
    }
    /** Creates a new instance of Main */
    private tab() {
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
    
    public static ValidateResult validate(FileAccessBase file, String charset) {
        ValidateResult result = new ValidateResult();
        try {
            String fileName = file.getName();
            int lastDashPos = getRevIndexOf(fileName, '.');
            if (lastDashPos == -1) {
                return null;
            }
            String extName = fileName.substring(lastDashPos + 1).toLowerCase();
            
            if (!extName.equals("txt") && !extName.equals("tab")) {
                return null;
            }
            String dictName = fileName.substring(0, lastDashPos);
            if (dictName.length() == 0) {
                return null;
            }
            
            if (!file.exists()) {
                return null;
            }
            
            //for a file great then 128k, it must be sorted
            if (file.fileSize()>1024*minLength) {
                file.absolute(0);
                byte[] b = new byte[1024*8];
                int len = file.read(b,0,2);
                if(len>=2 && b[0] == -1 && b[1]==-2)//FFFE
                {
                    len = file.read(b,0,b.length);
                } else {
                    len = file.read(b,2,b.length - 2);
                }
                tabParser parser = new tabParser(b,0,len, "utf-8",'\t');
                //the first and last line is ignored
                for(int i=1;i<parser.size()-1;++i) {
                    //each word should greater then previous word
                    if(ByteArrayString.compareToIgnoreCase(parser.getKeyByte(i),parser.getKeyByte(i-1))<0) {
                        return null;
                    }
                }
            }
            
            String chset = charset;
            if (chset == null || chset.length() == 0) {
                chset = getCharset(file);
            }
            result.charset = chset;
            result.dictName = dictName;
            
            //System.out.println(CharsetEncodingFactory.newString(indexBuf,"utf-8"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        //System.out.println(result.dictName);
        return result;
    }
    
    
    public static String getCharset(FileAccessBase file) throws IOException {
        FileAccessBase input = file;
        if (!input.exists()) {
            throw new IOException("file " + file + " doesn't exist!");
        }
        try{
            
            
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
            
            
            boolean done = false;
            file.absolute(0);
            byte[] b = new byte[1024*8];
            int len = file.read(b,0,2);
            if(len>=2 && b[0] == -1 && b[1]==-2)//FFFE
            {
                len = file.read(b,0,b.length );
            } else {
                len = file.read(b,2,b.length -2)+2;
            }
            //get the list \n
            while(len >0 && b[len-1]!='\n')
                len--;
            done = det.DoIt(b, len, false);
            
            if (!done) {
                tabParser parser = new tabParser(b,0,len, "utf-8",'\t');
                int i = 0;
                while (!done && i < parser.size()-1) {
                    byte [] value = parser.getValueByte(i);
                    done = det.DoIt(value, value.length, false);
                    
                    if (done && observer.charset.length() == 0) {
                        det.Reset();
                        for (int k = 0; k < parser.size()-1; ++i) {
                            byte[] val = parser.getValueByte(k);
                            done = det.DoIt(val, val.length, false);
                            
                        }
                        if (done) {
                            break;
                        }
                    }
                    i++;
                }
            }
            det.DataEnd();
            String[] prob = det.getProbableCharsets();
            if (prob!=null) {
                for (int i = 0; i < prob.length; i++) {
                    //System.out.println("Probable Charset = " + prob[i]);
                }
            }
            if (done) {
                //if (prob.length>0)
                //    return prob[0];
                //else
                return observer.charset;
            }
            det = null;
            System.gc();
            return prob[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "utf-8";
    }
    public static void detdicSmallFile(tabDict dict,int start) throws IOException {
        FileAccessBase input = dict.getFile();
        input.absolute(start);
        byte [] buf = new byte[(int)input.fileSize()-start];
        int len = input.read(buf);
        
        tabParser p = new tabParser(buf,0,len,dict.getEncoding(),'\t');
        p.setDealEscapse(true);
        p.sort();
        
        tabIndex kIndex = new tabIndex(dict, 1);
        if(p.size()>0) {
            tabIndexItem item = new tabIndexItem(dict, p.getKeyByte(0), start, len);
            item.loadItems(p);
            kIndex.addIndexItem(item);
        }
        
        dict.setIndex(kIndex);
        input.close();
        System.gc();
        
    }
    public static void detdic(tabDict dict) throws IOException {
        FileAccessBase input = dict.getFile();
        if (!input.exists()) {
            throw new IOException("file " + dict.getFile().getAbsolutePath() + " doesn't exist!");
        }
        input.absolute(0);
        
        try {
            
            int index = 0;
            //System.out.println("Index size:" + wordListLength);
            tabIndex kIndex = new tabIndex(dict, 1024);
            
            int filePos=0;
            int sectionLength =0;
            byte[] startValue=null;
            
            input.absolute(0);
            byte [] buf = new byte[1024*4];
            int len = input.read(buf, 0, 2);
            int start =0;
            if (len>=2 && buf[0]==-1 && buf[1]==-2) //ignore FFFE
            {
                start =2;
            }
            if(input.fileSize()<1024*minLength) {
                detdicSmallFile(dict,start);
                return;
            }
            if(len>=2 && buf[0] == -1 && buf[1]==-2)//FFFE
            {
                len = input.read(buf,0,512 );
            } else {
                len = input.read(buf,2,510);
            }
            tabParser p = new tabParser(buf,start,len-start,dict.getEncoding(),1,'\t');
            if(p.size()>0) {
                startValue = p.getKeyByte(0);
                filePos = start;
                sectionLength = len;
            } else {
                return;
            }
            
            
            int skipLen = 1024*8;
            while(len>0) {
                if (skipLen + input.getOffset()>input.fileSize()) {
                    sectionLength += input.fileSize() - input.getOffset();
                    break;
                } else {
                    input.skip(skipLen);
                    sectionLength+= skipLen;
                    len = input.read(buf, 0, 1024);
                    p = new tabParser(buf,0,len,dict.getEncoding(),2,'t');
                    if(p.size()>=2 && p.getValueByte(1).length>0) {
                        sectionLength+= p.getKeyStart(1);
                        kIndex.addIndexItem(new tabIndexItem(dict, startValue, filePos, sectionLength));
                        filePos += sectionLength;
                        sectionLength=len - p.getKeyStart(1);
                        skipLen = 1024*8;
                        startValue = p.getKeyByte(1);
                    } else {
                        skipLen = 0;
                    }
                }
            }
            
            //System.out.println(dict.getEncoding());
            
            kIndex.addIndexItem(new tabIndexItem(dict, startValue, filePos, sectionLength));
            
            dict.setIndex(kIndex);
            input.close();
            System.gc();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
