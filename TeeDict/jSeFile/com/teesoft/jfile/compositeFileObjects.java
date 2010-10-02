/*
 * compositeFileObjects.java
 *
 * Created on 2007-9-1, 3:06 PM
 *
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

import com.teesoft.jfile.resource.FileObject;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author wind
 */
public abstract class compositeFileObjects {

    private String name;

    private FileObject rootFile;

    private Object refObject;
    
    /** Creates a new instance of compositeFileObjects */
    public compositeFileObjects(String name,Object refObject) {
        this.setName(name);
        this.setRefObject(refObject);
        cache.put(name,this);
        rootFile = new FileObject("","",true,null);
        listContent(name,refObject);
    }
    public abstract FileObject listContent(String name,Object refObject);
    public abstract void doClose();
    public void close()
    {
        this.doClose();
        cache.remove(name);
    }
    
    public void addFolder(String[] list,Object entry) {
        addFolder(list, 0, list.length,entry);
    }
    
    public FileObject addFile(long size, String[] list,Object entry) {
        return addFile(size, list, 0, list.length,entry);
    }
    
    public FileObject addFile(long size, String[] list, int i, int length,Object entry) {
        FileObject folder = addFolder(list, i, length - 1,entry);
        return folder.addFile(size, list[i + length - 1],entry);
    }
    
    public FileObject addFolder(String[] list, int i, int length,Object entry) {
        FileObject file = getRootFile();
        for (int l = 0; l < length; ++l) {
            String name = list[l + i];
            if (name.length() > 0) {
                FileObject newFile = file.getFolder(name);
                if (newFile == null) {
                    newFile = file.addFolder(name,entry);
                }
                file = newFile;
            }
        }
        return file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileObject getRootFile() {
        return rootFile;
    }

    public void setRootFile(FileObject rootFile) {
        this.rootFile = rootFile;
    }

    public Object getRefObject() {
        return refObject;
    }

    public void setRefObject(Object refObject) {
        this.refObject = refObject;
    }


    public static File getTempDir()
    {
            String tmpFilePath = System.getProperty("java.io.tmpdir");
            File tmp = new File(tmpFilePath );
            File teePath = new File(tmp.getAbsolutePath() + File.separator + "teedict");
            teePath = new File(teePath.getAbsolutePath()  + File.separator + String.valueOf( java.lang.Math.random())+".tmp");
            teePath.deleteOnExit();
            teePath.mkdirs();
        return teePath;
    }

    static HashMap cache = new HashMap();
    public static compositeFileObjects getCompositeFileObjectsFromCache(String filePath) {
        return (compositeFileObjects) cache.get(filePath);
    }
}
