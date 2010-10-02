/*
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

package com.teesoft.jfile;



import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Vector;


public class j2seURLAccess extends FileAccessBase {

    protected URLConnection connection = null;
    protected InputStream input = null;
    protected OutputStream output = null;


    public j2seURLAccess(String s) throws IOException {

        super(s);

        setOffset(0);
    }

    public j2seURLAccess(URLConnection c) throws IOException {
        super(c.getURL().toExternalForm());
        setConnection(c);

        setOffset(0);
    }

    public void open() throws IOException {
        if (connection == null) {
            String s = getFileName(getLocation());
            setConnection(new URL(s).openConnection());
        }
    }

    private String getFileName(String string) {
        if (string.indexOf("://")!=-1) {
            return string;
        } else {
            return "file://" + string;
        }
    }


    public boolean exists() throws IOException {
        return getConnection().getContentLength()>0;
    }


    public boolean isDirectory() throws IOException {
        return false;
    }

    public boolean isFile() throws IOException {
        return true;
    }

    public boolean isHidden() throws IOException {
        return false;
    }

    public boolean canRead() throws IOException {
        return exists();
    }

    public boolean canWrite() throws IOException {
        return false;
    }

    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
        if (connection != null) {
            connection = null;
        }
        setInputStream(null);
        setConnection(null);
        setOffset(0);
    }

    public static Vector listRoots() throws IOException {
        return listRoots("");
    }

    public static Vector listRoots(String regex) throws IOException {
        Vector list = new Vector();
        return list;
    }

    public Vector listFiles() throws IOException {
        return new Vector();
    }


    public Vector listFiles(String name, boolean includeHidden) throws IOException {
        return new Vector();
    }

    protected URLConnection getConnection() throws IOException {
        if (connection == null) {
            open();
        }
        if (connection == null) {
            throw new IOException("Unable to open the file:" + getLocation());
        }
        return connection;
    }

    protected void setConnection(URLConnection connection) {
        this.connection = connection;
    }

    public InputStream getInputStream() throws IOException {
        if (input == null) {
            input = getConnection().getInputStream();
        }
        return input;
    }

    public void setInputStream(InputStream input) {
        this.input = input;
    }

    public OutputStream getOutputStream() throws IOException {
        if (output == null) {
            //
        }
        return output;
    }

    public void setOutputStream(OutputStream output) {
        this.output = output;
    }

    public String getSeparator() {
        return "/";
    }

    String getPath() throws IOException {
        return getConnection().getURL().toExternalForm();
    }

    public String getAbsolutePath() throws IOException {
        return getConnection().getURL().toExternalForm();
    }

    public String getName() throws IOException {
        String name = getConnection().getURL().getFile();
        if (name.length() == 0) {
            return name;
        }
        if (name.substring(name.length() - 1).equals(this.getSeparator())) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }


    public void create() throws IOException {
        
    }


    static final int highestPower = 256 * 256 * 256;
    static final int midPower = 256 * 256;
    static final int smallPower = 256;

    public static int getIntFromByte(final int pos, final byte[] buf) {
        int wordPos;
        wordPos = getIndexFromByte(buf[pos]) * highestPower + getIndexFromByte(buf[pos + 1]) * midPower + getIndexFromByte(buf[pos + 2]) * smallPower + getIndexFromByte(buf[pos + 3]);
        return wordPos;
    }

    public static int getIndexFromByte(byte b) {
        if (b < 0) {
            return 256 + b;
        } else {
            return (int) b;
        }
    }


    public IFileFactory getFileFactory() {
        return j2seURLFactory.getInstance();
    }
    public void mkdir() throws IOException {
        //
    }    
    
    public void delete() throws IOException {
        //
    }

    public long fileSize() {
        //to do
        long length=0;
        try {
            length = getConnection().getContentLength();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return length;
    }
    public boolean isCompressed() {
        return false;
    }

    public boolean isRawFile() {
        return true;
    }
}
