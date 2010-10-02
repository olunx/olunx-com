/*
 * Dict.java
 *
 * Created on Aug 5, 2007, 1:23:51 PM
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
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.jfile.sparse.SparseFileAccess;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public abstract class Dict {

    private String factoryClass;
    private String format;
    private int order = 100;
    private boolean scan;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }

    public Dict(String file, String name, Properties properties) throws IOException {
        this(openFile(file), name, properties);
    }

    public static FileAccessBase openFile(String fileName) throws IOException {
        FileAccessBase file = com.teesoft.jfile.FileFactory.openFileAccess(fileName, true);
        if (file != null) {
            return file;
        } else {
            return com.teesoft.jfile.FileFactory.openFileAccess(fileName, false);
        }
    }

    public Dict(FileAccessBase file, String name, Properties properties) {
        this.file = file;
        this.name = name;
        this.properties = properties;
        encoding = "utf-8";
        setEnabled(true);
        this.html = false;
    }

    public Dict() throws IOException {
        this("", "", null);
    }

    public boolean open() throws IOException {
        opened = true;
        return isOpened();
    }

    public boolean isOpened() {
        return opened;
    }

    public void close() {
        opened = false;
    }

    public ItemList search(String word, int maxCount) {
        try {
            try {
                return search(CharsetEncodingFactory.getBytes(word, this.getEncoding()), maxCount);
            } catch (UnsupportedEncodingException ex) {
                return search(word.getBytes(), maxCount);
            }
        } catch (Exception ex) {
            return new ItemList();
        }
    }

    public ItemList search(byte[] word, int maxCount) {
        return null;
    }

    public FileAccessBase getFile() {
        return file;
    }

    public void setFile(FileAccessBase file) throws IOException {
        if (this.file != file) {
            boolean oldOpened = this.isOpened();
            this.file = file;
            close();
            if (oldOpened) {
                open();
            }
        }
    }

    void setFile(String filename) {
        try {
            setFile(FileFactory.openFileAccess(filename, true));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding.toLowerCase();
    }
    private boolean opened;
    private FileAccessBase file;
    private String name;
    private Properties properties;
    private String encoding;
    private boolean enabled;

    public boolean isDeleted() {
        try {
            return file == null || !file.exists();
        } catch (IOException ex) {
            ex.printStackTrace();
            return true;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public static boolean isMicroedition() {
        return FileFactory.isMicroedition();
    }

    public Vector getFiles() {
        Vector v = new Vector();
        v.addElement(this.getFile());
        return v;
    }

    public void copyTo(String dir, Object para) {
        FileAccessBase dest;
        try {
            dest = FileFactory.newFileAccess(dir);
            try {
                dest.mkdir();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            Vector lists = this.getFiles();
            for (int i = 0; lists != null && i < lists.size(); ++i) {
                FileAccessBase file = (FileAccessBase) lists.elementAt(i);
                file.copyTo(dest, para);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void Tuning(boolean uncompress, boolean splitFile, int minSplitSize) {
        //switchDisplayable(null,getSearchForm());
        boolean changed = false;
        Vector lists = this.getFiles();
        for (int i = 0; lists != null && i < lists.size(); ++i) {
            FileAccessBase f = (FileAccessBase) lists.elementAt(i);
            //        file.copyTo(dest,para);
            if (this.isNeedTuning(f, uncompress, splitFile, minSplitSize)) {
                String path;

                FileAccessBase targetFile = null;
                try {
                    path = f.getAbsolutePath();
                    if (f.isCompressed() && !uncompress)
                    {
                        path = path + ".dz";
                    }
                    if (splitFile) {
                        targetFile = new SparseFileAccess(path);
                    } else {
                        targetFile = FileFactory.newFileAccess(path);
                    }
                    targetFile.create();
                    Object copyOption = null;
                    if (uncompress) {
                        copyOption = new Object();
                    }
                    if (f.copyTo(targetFile, copyOption)) {
                        targetFile.close();
                        f.delete();

                    }
                    changed = true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }
        if (changed) {
            reloadDict();
        }
    }

    public boolean isNeedTuning(boolean uncompress, boolean splitFile, int minSplitSize) {
        //switchDisplayable(null,getSearchForm());
        boolean needTuning = false;
        Vector lists = this.getFiles();
        for (int i = 0; lists != null && i < lists.size(); ++i) {
            FileAccessBase f = (FileAccessBase) lists.elementAt(i);
            //        file.copyTo(dest,para);
            if (this.isNeedTuning(f, uncompress, splitFile, minSplitSize))
            {
                needTuning = true;
                break;
            }
        }
        return needTuning;
    }
    public boolean isNeedTuning(FileAccessBase f,boolean uncompress, boolean splitFile, int minSplitSize) 
    {
        return (uncompress && f.isCompressed()) || (splitFile &&  f.isRawFile() && f.fileSize() > minSplitSize);
    }
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isHtml() {
        return html;
    }
    private boolean html;

    public void setHtml(boolean html) {
        this.html = html;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setWordCount(int aInt) {
        wordCount = aInt;
    }
    private int wordCount = -1;
    private String author;
    private String description;
    private String date;

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getWordCount() {
        return wordCount;
    }

    public boolean isScan() {
        return scan;
    }

    public void setScan(boolean scan) {
        this.scan = scan;
    }

    public void reloadDict() {
    }
}