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
package com.teesoft.jfile.resource;

import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.IFileFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class resourceFileAccess extends FileAccessBase {

    protected FileObject connection = null;
    protected InputStream input = null;
    protected OutputStream output = null;
    private static FileObject rootFile = new FileObject("", "", true,null);
    private static String fileList = "/resource.list";
    private static String languageList = "/language.list";
    
    static {
        
        try {
            readResourceList(fileList);
            readResourceList(languageList);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void readResourceList(final String  file) throws IOException, NumberFormatException {
        InputStream input = rootFile.getClass().getResourceAsStream(file);
        if (input==null)
            return;
        String content = readFull(input).toString();
        String[] lines = split(content, '\n');
        for (int i = 0; i < lines.length; ++i) {
            String line = trim(lines[i]);
            if (line.length() > 2) {
                if (line.substring(0, 2).equals("d ")) {
                    addFolder(split(line.substring(2), '/'));
                } else if (line.substring(0, 2).equals("f ")) {
                    String fileDesc = line.substring(2);
                    int pos = fileDesc.indexOf(' ');
                    long size = Long.parseLong(fileDesc.substring(0, pos));
                    addFile(size, split(fileDesc.substring(pos + 1), '/'));
                }
            }
        }
        input.close();
    }

    public static FileObject getFileObject(String name) {
        String[] path = split(name, '/');
        FileObject folder = getFolder(path, 0, path.length - 1);
        if (folder == null) {
            return null;
        }
        if (path[path.length - 1].length() == 0) {
            return folder;
        }
        FileObject file = folder.getFolder(path[path.length - 1]);
        if (file != null) {
            return file;
        }
        return folder.getFile(path[path.length - 1]);
    }


    public resourceFileAccess(String s) throws IOException {
        super(s);
        setOffset(0);
    }

    public void open() throws IOException {
        this.getConnection();
    }

    private String getResourcenName(String string) {
        if (string.startsWith("res://")) {
            return string.substring("res://".length());
        } else {
            return string;
        }
    }
    static byte[] skipBuf = new byte[512];

    public long fileSize() {
        try {
            if (this.getConnection().getFilesize() != -1) {
                return this.getConnection().getFilesize();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            long pos = this.getOffset();
            long size = pos;
            long skipped = skip(512);
            while (skipped > 0) {
                size += skipped;
                skipped = skip(512);
            }
            absolute(pos);
            getConnection().setFilesize(size);
            return size;
        } catch (IOException ex) {
            return -1;
        }
    }

    public boolean exists() throws IOException {
        if (getLocation().endsWith("/")) {
            return true;
        }
        return getConnection() != null;
    }

    public boolean isDirectory() throws IOException {
        try {
            return getConnection() != null && getConnection().isDirectory();
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean isFile() throws IOException {
        try {
            return getConnection() != null && getConnection().isFile();
        } catch (IOException ex) {
            return false;
        }
    }

    public boolean isHidden() throws IOException {
        return false;
    }

    public boolean canRead() throws IOException {
        return isFile();
    }

    public boolean canWrite() throws IOException {
        return false;
    }

    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
        setInputStream(null);
        setConnection(null);
        setOffset(0);
    }

    public static Vector listRoots() throws IOException {
        return listRoots("");
    }

    public static Vector listRoots(String regex) throws IOException {
        Vector files = new Vector();
        files.addElement(new resourceFileAccess("res:///"));
        return files; //return addAll(File.listRoots());
    }

    protected static Vector addAll(Vector v, Vector newV) throws IOException {
        for (int i = 0; i < newV.size(); i++) {
            FileObject file = (FileObject) newV.elementAt(i);
            v.addElement(new resourceFileAccess(file.getPath()));
        }
        return v;
    }

    public Vector listFiles() throws IOException {
        return listFiles("", true);
    }

    public Vector listFiles(String name, boolean includeHidden) throws IOException {
        //return addAll(getConnection().listFiles(new filter(name,includeHidden)));
        FileObject file = getConnection();
        Vector v = new Vector();
        if (file!=null)
        {
            addAll(v, file.getSubFolders());
            addAll(v, file.getSubFiles());
        }
        return v;
    }

    protected FileObject getConnection() throws IOException {
        if (connection == null) {
            connection = getFileObject(getResourcenName(getLocation()));
        }
        return connection;
    }

    protected void setConnection(FileObject connection) {
        this.connection = connection;
    }

    public InputStream getInputStream() throws IOException {
        if (input == null) {
            input = this.getClass().getResourceAsStream(getResourcenName(getLocation()));
        }
        return input;
    }

    public void setInputStream(InputStream input) {
        this.input = input;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public void setOutputStream(OutputStream output) {
        this.output = output;
    }

    public String getPath() throws IOException {
        if (getLocation().startsWith("res://")) {
            return getLocation();
        }
        return "res://" + getLocation();
    }

    public String getAbsolutePath() throws IOException {
        return getPath();
    }

    public String getName() throws IOException {
        return getConnection().getName();
    }

    public FileAccessBase child1(String dir) throws IOException {
        String absolutePath = getAbsolutePath();
        if (absolutePath.substring(absolutePath.length() - getSeparator().length()).equals(getSeparator())) {
            return new resourceFileAccess(getAbsolutePath() + dir);
        }
        return new resourceFileAccess(getAbsolutePath() + getSeparator() + dir);
    }

    public void create() throws IOException {
    }

    public IFileFactory getFileFactory() {
        return ResourceFactory.getInstance();
    }

    private static String trim(String str) {
        if (str.length() == 0) {
            return str;
        }
        if (str.charAt(str.length() - 1) == '\r') {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static String[] split(String content, char match) {
        int count = 0;
        for (int i = 0; i < content.length(); ++i) {
            if (content.charAt(i) == match) {
                count++;
            }
        }
        String[] lists = new String[count + 1];
        int start = 0;
        int index = 0;
        for (int i = 0; i <= content.length(); ++i) {
            if (i == content.length() || content.charAt(i) == match) {
                lists[index] = content.substring(start, i);
                index++;
                start = i + 1;
            }
        }
        return lists;
    }

    private static void addFolder(String[] list) {
        addFolder(list, 0, list.length);
    }

    private static FileObject addFile(long size, String[] list) {
        return addFile(size, list, 0, list.length);
    }

    private static FileObject addFile(long size, String[] list, int i, int length) {
        FileObject folder = addFolder(list, i, length - 1);
        return folder.addFile(size, list[i + length - 1]);
    }

    private static FileObject addFolder(String[] list, int i, int length) {
        FileObject file = rootFile;
        for (int l = 0; l < length; ++l) {
            String name = list[l + i];
            if (name.length() > 0) {
                FileObject newFile = file.getFolder(name);
                if (newFile == null) {
                    newFile = file.addFolder(name);
                }
                file = newFile;
            }
        }
        return file;
    }

    private static StringBuffer readFull(InputStream input) throws IOException {
        StringBuffer buf = new StringBuffer();
        byte[] b = new byte[512];
        int len = input.read(b);
        while (len > 0) {
            buf.append(new String(b, 0, len));
            len = input.read(b);
        }
        return buf;
    }

    private static FileObject getFolder(String[] list, int i, int length) {
        FileObject file = rootFile;
        for (int l = 0; l < length; ++l) {
            String name = list[l + i];
            if (name.length() > 0) {
                FileObject newFile = file.getFolder(name);
                if (newFile == null) {
                    return null;
                }
                file = newFile;
            }
        }
        return file;
    }

    public void delete() throws IOException {
        //do nothing
    }
}
