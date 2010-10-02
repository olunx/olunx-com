/*
 * starDict.java
 *
 * Created on Aug 5, 2007, 3:19:20 PM
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

package com.teesoft.javadict.stardict;

import com.teesoft.javadict.ConfigItem;
import com.teesoft.javadict.Dict;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.javadict.ItemList;
import com.teesoft.javadict.Properties;
import com.teesoft.javadict.tabParser;
import com.teesoft.util.StringUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class starDict extends Dict {

    String dictName;
    private startDictFactory factory;

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }
    public boolean isDictHtmlFormat()
    {
        if (typesequence == null)
            return false;
        return (typesequence.toLowerCase().indexOf('h')!=-1) || (typesequence.toLowerCase().indexOf('k')!=-1);
    }
    public starDict(startDictFactory factory, FileAccessBase file, String name, Properties properties) {
        super(file, name, properties);
        this.factory = factory;
        index = null;
        try {
            setDictName(parserDictName(file.getName()));
            FileAccessBase ifo = factory.openInfo(this.getFile(),this.getDictName());
            if (ifo!=null)
            {
                ConfigItem item = new ConfigItem(getDictName(), getDictName());
                item.load(ifo);
                this.setWordCount(item.getInt("wordcount",-1) );
                if (name.equals(this.getDictName()))
                    this.setName(item.getString("bookname", name));
                this.setAuthor(item.getString("author", dictName));
                this.setDescription(item.getString("description", dictName));
                this.setDate(item.getString("date", (new Date()).toString()));
                this.setSametypesequence(item.getString("sametypesequence", "m"));
            }else
            {
                this.setSametypesequence("m");
                this.setWordCount(-1);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            setDictName(name);
        }

    }
    public String parserDictName(String name)
    {
        int firstDashPos = name.indexOf('-');
        if (firstDashPos == -1) {
            return null;
        }
        int lastDashPos = startDictFactory.getRevIndexOf(name, '-');
        if (lastDashPos == -1 || lastDashPos <= firstDashPos) {
            return name;
        }
        FileAccessBase idx=null;
        if (name.substring(0, firstDashPos).toLowerCase().equals("stardict"))
        {
            return name.substring(firstDashPos + 1, lastDashPos);
        } else
        {
            return name;
        }
        
    }
    public void close() {
        if (index!=null)
            index.close();
        try {
            if (dictStream != null) {
                dictStream.close();
            }
            dictStream = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
            super.close();
    }

    public boolean open() throws IOException {
        if (!this.isOpened()) {
            index = new starIndex(factory.openIndex(this.getFile(),this.getDictName()), this);
            super.open();
        }
        
        return isOpened();
    }

    public ItemList search(byte[] word,int maxCount) {
        return index.search(word,maxCount);
    }

    FileAccessBase getIndexStream() throws IOException {
        this.open();
        return index.getIndexFile(); //factory.openIndex(this.getFile(),this.getDictName());        
    }

    public FileAccessBase getDictStream() throws IOException {
        if (dictStream == null) {
            dictStream = factory.openDict(this.getFile(),this.getDictName());
        }
        return dictStream;
    }
    public FileAccessBase reloadDictStream() throws IOException {
        if (dictStream != null) {
            dictStream.close();
        }
        dictStream = factory.openDict(this.getFile(),this.getDictName());
        return dictStream;
    }

    public synchronized byte[] getTextAt(int start, int length) {
        
        byte[] b = null;
        try {
            if (getDictStream() == null) {
                return null;
            }
            if (FileFactory.isMicroedition()) {
                explainGetCount++;
                //if this is running on microedtion then we reload the stream every 10 times
                if (explainGetCount % 2 == 0) {
                    getDictStream().close();
                    dictStream = null;
                }
            }
            getDictStream().absolute(start);
            b = new byte[length];
            getDictStream().read(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return b;
    }
    //private FileAccessBase indexStream;
    private FileAccessBase dictStream;

    private starIndex index;
    private int explainGetCount = 0;
    
    public starIndexItem getIndex(int i) {
        if (this.index!=null)
        {
            if (i>=0 && this.index.size()>i)
                return index.getStarIndexItem(i);
        }
        return null;
    }
    
    public void copyTo(String dir,Object para) {
        FileAccessBase dest;
        try {
            dest = FileFactory.newFileAccess(dir);
            dest = dest.child(this.getFile().getName());
            try {
                dest.mkdir();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Vector lists = this.getFiles();
            for (int i = 0; lists != null && i < lists.size(); ++i) {
                FileAccessBase file = (FileAccessBase) lists.elementAt(i);
                file.copyTo(dest,para);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }    
    public Vector getFiles() {
            Vector v = new Vector();
        try {
            FileAccessBase indexFile = this.getIndexStream();
            v.addElement(indexFile);
            FileAccessBase file = FileFactory.openFileAccess(indexFile.getAbsolutePath() + ".idx",true);
            if (file != null)
                v.addElement(file);
            
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            v.addElement(this.getDictStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            FileAccessBase ifo = factory.openInfo(this.getFile(),this.getDictName());
            if (ifo != null)
                v.addElement(ifo);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return v;
        
    }

    public void setHtml(boolean html) {
        super.setHtml(html || isDictHtmlFormat());
    }

   

    private void setSametypesequence(String typesequence) {
        this.typesequence = typesequence;
        if (isDictHtmlFormat())
            this.setHtml(true);
        
    }
    private String typesequence;
    public boolean isXDict()
    {
        return (typesequence!=null && typesequence.toLowerCase().indexOf('k')!=-1);            
    }
    public String convertXDictToHtml(byte[] text)
    {
        java.lang.String src="";
        try {
            src = new java.lang.String(text, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            src = new java.lang.String(text);
        }
        for(int i=0;i<ENTITIES.length/3;i++)
        {
            src = StringUtil.replace(src, ENTITIES[3*i],ENTITIES[3*i+1]);
        }
        return src;
    }
    public String convertXDictToText(byte[] text)
    {
        java.lang.String src="";
        try {
            src = new java.lang.String(text, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            src = new java.lang.String(text);
        }
        for(int i=0;i<ENTITIES.length/3-1;i++)
        {
            src = StringUtil.replace(src, ENTITIES[3*i],ENTITIES[3*i+2]);
        }
//        System.out.println(src);
        return src;
    }
    public void reloadDict() {
        index.close();        
        if (dictStream != null) {
            try {
                dictStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            dictStream = null;
        }
        
    }

    public void Tuning(boolean uncompress, boolean splitFile, int minSplitSize) {
        //TODO: tuning index file to index of index
        super.Tuning(uncompress, splitFile, minSplitSize);
    }
    
    private static final String[] ENTITIES = {
    "\n", "","", 
    "<单词解释块>", "","",
    "</单词解释块>", "","", 
    "<基本词义>", "","",
    "</基本词义>", "","",
    "<单词项>", "","",
    "</单词项>", "", "",
    "<单词音标>", "","",
    "</单词音标>", "", "",
    "<用法>", "","",
    "</用法>", "", "",   
    "<视听文本><![CDATA[", "视听文本:","视听文本:",
    "]]></视听文本>", "<br>", "\n",
    "<视听文本>", "视听文本:","视听文本:",
    "</视听文本>", "<br>", "\n",
    "<单词原型><![CDATA[", "单词原型:","单词原型:",
    "]]></单词原型>", "<br>","\n",
    "<单词原型>", "单词原型:","单词原型:",
    "</单词原型>", "<br>","\n",
    "<音节分段><![CDATA[", "音节分段:","音节分段:",
    "]]></音节分段>", "<br>", "\n",
    "<音节分段>", "音节分段:","音节分段:",
    "</音节分段>", "<br>", "\n",
    "<国际音标><![CDATA[", "国际音标:","国际音标:",
    "]]></国际音标>", "<br>", "\n",
    "<国际音标>", "国际音标:","国际音标:",
    "</国际音标>", "<br>", "\n",
    "<美国音标><![CDATA[", "美国音标:","美国音标:",
    "]]></美国音标>", "<br>", "\n",
    "<美国音标>", "美国音标:","美国音标:",
    "</美国音标>", "<br>", "\n",
    "<AHD音标><![CDATA[", "AHD音标:","AHD音标:",
    "]]></AHD音标>", "<br>", "\n",
    "<AHD音标>", "AHD音标:","AHD音标:",
    "</AHD音标>", "<br>", "\n",
    "<单词词性><![CDATA[", "单词词性:","单词词性:",
    "]]></单词词性>", "<br>", "\n",
    "<单词词性>", "单词词性:","单词词性:",
    "</单词词性>", "<br>", "\n",
    "<解释项><![CDATA[","<pre>","",
    "]]></解释项>","</pre>","\n",
    "<解释项>","","",
    "</解释项>","","",
    "<跟随注释><![CDATA[","<pre>","",
    "]]></跟随注释>","</pre>","\n",
    "<跟随注释>","","",
    "</跟随注释>","","",    
    "<例句原型><![CDATA[","<pre>","",
    "]]></例句原型>","</pre>","\n",
    "<例句原型>","","",
    "</例句原型>","","",
    "<例句解释><![CDATA[","<pre>","",
    "]]></例句解释>","</pre>","\n",
    "<例句解释>","","",
    "</例句解释>","","",
    "<预解释><![CDATA[","<pre>","",
    "]]></预解释>","</pre>","\n",
    "<预解释>","","",
    "</预解释>","","",
    "<子解释项><![CDATA[","<pre>","",
    "]]></子解释项>","</pre>","\n",
    "<子解释项>","","",
    "</子解释项>","","",
    "<语源>", "语源:<br>","语源\n",
    "</语源>", "", "",    
    "<例句>", "","",
    "</例句>", "", "",    
    "<![CDATA[", "<pre>","",
    "]]>", "</pre>","\n"};
        
}
