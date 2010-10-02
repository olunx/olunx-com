/*
 * WrapperFile.java
 * 
 * Created on 2007-10-1, 18:57:15
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.jfile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class WrapperFile extends FileAccessBase{

    FileAccessBase input;
    private long start;
    private long length;
    private String name;
    
    public WrapperFile(FileAccessBase file,String name,long start,long length) throws IOException
    {
        super(file.getAbsolutePath() + name);
        setInputStream(file);
        this.name = name;
        this.start = start;
        this.length = length;
    }
    public Vector listFiles(String name, boolean includeHidden) throws IOException {
        return null;
    }

    public void setInputStream(InputStream input) {
        setInputStream((FileAccessBase)input);
    }
    public void setInputStream(FileAccessBase input) {
        this.input = input;
    }
    

    public void setOutputStream(OutputStream output) {
    }

    public void open() throws IOException {
        input.open();
    }

    public Vector listFiles() throws IOException {
        return null;
    }

    public boolean canRead() throws IOException {
        return true;
    }

    public boolean canWrite() throws IOException {
        return false;
    }

    public void create() throws IOException {
    }

    public boolean exists() throws IOException {
        return true;
    }

    public long fileSize() {
        return this.length;
    }

    public String getAbsolutePath() throws IOException {
        return input.getAbsolutePath() + name;
    }

    public IFileFactory getFileFactory() {
        return input.getFileFactory();
    }

    public InputStream getInputStream() throws IOException {
        return input;
    }

    public String getName() throws IOException {
        return name;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public boolean isDirectory() throws IOException {
        return false;
    }

    public boolean isFile() throws IOException {
        return true;
    }

    public boolean isHidden() throws IOException {
        return true;
    }
    public int read() throws IOException {
        if (getOffset() >=  this.length)
            return -1;

        input.absolute(this.start + this.getOffset());
        int ret = this.getInputStream().read();
        setOffset(getOffset() + 1);
        return ret;
    }
    public int read(byte[] b, int start, int len) throws IOException {
        if (getOffset() >=  this.length)
            return -1;
        if (isDirectory()) {
            return -1;
        }
        if (getInputStream() == null) {
            return -1;
        }
        if (len - start > b.length) {
            len = b.length - start;
        }
        if (len > this.length - getOffset())
        {
            len = (int) (this.length - getOffset());
        }
        if (len == 0) {
            return 0;
        }
        input.absolute(this.start + this.getOffset());
        int ret = getInputStream().read(b, start, len);
        if (ret >=0)
            setOffset(getOffset() + ret);
        return ret;
    }

    public void delete() throws IOException {
        //do nothing
    }
    public boolean isRawFile() {
        return false;
    }

}
