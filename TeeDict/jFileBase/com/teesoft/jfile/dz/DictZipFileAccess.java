/*
 * DictZipFileAccess.java
 *
 * Created on September 28, 2006, 7:37 PM
Copyright (C) 2006,2007  Yong Li. All rights reserved.
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





package com.teesoft.jfile.dz;

import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.jfile.IFileFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;



/**
 *
 * @author wind
 */

public class DictZipFileAccess extends FileAccessBase {

    private FileAccessBase connection = null;
    protected InputStream input = null;
    protected OutputStream output = null;

    public DictZipFileAccess(FileAccessBase file) throws IOException {
        super(file.getAbsolutePath());
        connection = file;
    }

    public DictZipFileAccess(String s) throws IOException {

        super(s);
    }

    public InputStream getInputStream() throws IOException {

        if (input == null) {
            
            input = new DictZipInputStream(getConnection());
        }

        return input;
    }

    public boolean canRead() throws IOException {
        return getConnection().canRead();
    }

    public boolean canWrite() throws IOException {
        return getConnection().canWrite();
    }

    public boolean exists() throws IOException {
        return getConnection().exists();
    }

    public long fileSize() {
        try {
            return getConnection().fileSize();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public OutputStream getOutputStream() throws IOException {
        return getConnection().getOutputStream();
    }

    public boolean isDirectory() throws IOException {
        return false;
    }

    public boolean isFile() throws IOException {
        return true;
    }

    public boolean isHidden() throws IOException {
        return getConnection().isHidden();
    }

    public Vector listFiles() throws IOException {
        return null;
    }

    public Vector listFiles(String name, boolean includeHidden) throws IOException {
        return null;
    }

    public void open() throws IOException {
        if (connection == null) {
            setConnection((FileAccessBase) FileFactory.openFileAccess(getRealFileURI(getLocation()),true));
        }
    }

    protected String getRealFileURI(String s) {
        String name = s;
        if (name.startsWith("dz://")) {
            name =  name.substring("dz://".length());
        }
        if (!name.endsWith(".dz"))
            name += ".dz";
        return name;
    }

    public void setInputStream(InputStream input) {
        this.input = input;
    }

    public void setOutputStream(OutputStream output) {
    }

    public FileAccessBase child(String dir) throws IOException {
        return null;
    }

    public String getName() throws IOException {
        String name =  getConnection().getName();
        return name.substring(0,name.length()-".dz".length());
    }
    
    public String getAbsolutePath() throws IOException {
        String path = getConnection().getAbsolutePath();
        return path.substring(0,path.length()-".dz".length());
    }

    public void create() throws IOException {
    }

    public IFileFactory getFileFactory() {
        return DictZipFactory.getInstance();
    }
    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
        if (output != null) {
            output.close();
        }
        if (connection != null) {
            getConnection().close();
        }
        setInputStream(null);
        setConnection((null));
        setOffset(0);
    }

    public boolean copyTo(FileAccessBase dest,Object para) throws IOException {
        if (para!=null)
        {
            //copy the uncompressed file
            return super.copyTo(dest, para);
        }
        else    
        {
            //copy the orginal file
            return getConnection().copyTo(dest,para);
        }            
    }

    public FileAccessBase getConnection() throws IOException {
        if (connection == null) {
                open();
            }
        return connection;
    }

    public void setConnection(FileAccessBase connection) {
        this.connection = connection;
    }

    public void delete() throws IOException {
        getConnection().delete();
    }

    public boolean isCompressed() {
        return true;
    }

    public boolean isRawFile() {
        try {
            return getConnection().isRawFile();
        } catch (IOException ex) {
            return false;
        }
    }

}
