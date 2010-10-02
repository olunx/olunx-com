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

import java.util.Vector;
public class FileObject {
    
    String name;
    private String path;
    private Vector subFiles;
    private Vector subFolders;
    private long filesize = -1;
    
    private Object refObject;
    
    public FileObject(String path, String name, boolean folder,Object refObject) {
        this.setPath(path);
        this.name = name;
        this.setRefObject(refObject);
        if (folder) {
            setSubFolders(new Vector());
            setSubFiles(new Vector());
        } else {
            setSubFolders(null);
            setSubFiles(null);
        }
    }
    
    public boolean isDirectory() {
        return getSubFolders() != null;
    }
    
    public boolean isFile() {
        return getSubFolders() == null;
    }
    
    public FileObject getFolder(String name) {
        if (getSubFolders() == null) {
            return null;
        }
        for (int i = 0; i < getSubFolders().size(); ++i) {
            FileObject file = (FileObject) getSubFolders().elementAt(i);
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }
    
    public String getName() {
        return name;
    }
    
    public FileObject addFolder(String name) {
        return addFolder(name,null);
    }
    public FileObject addFolder(String name,Object obj) {
        FileObject file = getFolder(name);
        if (file != null) {
            return file;
        }
        file = new FileObject(getPath() + "/" + name, name, true,obj);
        getSubFolders().addElement(file);
        return file;
    }
    public FileObject addFile(long size, String name) {
        return addFile(size,name,null);
    }
    public FileObject addFile(long size, String name,Object obj) {
        FileObject file = new FileObject(getPath() + "/" + name, name, false,obj);
        file.filesize = size;
        getSubFiles().addElement(file);
        return file;
    }
    
    public FileObject getFile(String string) {
        if (getSubFiles() == null) {
            return null;
        }
        for (int i = 0; i < getSubFiles().size(); ++i) {
            FileObject file = (FileObject) getSubFiles().elementAt(i);
            if (string.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }
    
    public long getFilesize() {
        return filesize;
    }
    
    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public Vector getSubFiles() {
        return subFiles;
    }

    public void setSubFiles(Vector subFiles) {
        this.subFiles = subFiles;
    }

    public Vector getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(Vector subFolders) {
        this.subFolders = subFolders;
    }

    public Object getRefObject() {
        return refObject;
    }

    public void setRefObject(Object refObject) {
        this.refObject = refObject;
    }
    public FileObject getSubObject(String subPath)
    {
        String [] paths = split(subPath,'/');
        FileObject folder = this;
        for(int i=0;folder!=null && i<paths.length;++i)
        {
            if (paths[i].length()>0)
                folder = folder.getChildObject(paths[i]);
        }
        return folder;
    }
    private FileObject getChildObject(String string) {
        FileObject obj = this.getFolder(string);
        if (obj == null)
            obj = this.getFile(string);
        return obj;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }



}

