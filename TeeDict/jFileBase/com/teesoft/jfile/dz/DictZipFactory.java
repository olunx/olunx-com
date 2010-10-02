/*
 * DictZipFactory.java
 *
 * Created on August 19, 2007, 12:12 PM
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

package com.teesoft.jfile.dz;

import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.jfile.IFileFactory;

import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class DictZipFactory implements IFileFactory {
    
    private static IFileFactory instance =null;
    public static IFileFactory getInstance() {
        if (instance == null) {
            instance = new DictZipFactory();
        }
        return instance;
    }
    /** Creates a new instance of ResourceFactory */
    private DictZipFactory() {
    }
    
    public String getSeparator() {
        return "/";
    }
    
    public Vector listRoots() throws IOException {
        return new Vector();
    }
    
    
    public FileAccessBase newFileAccess(String string) throws IOException {
        if (string.startsWith("dz://"))
            return new DictZipFileAccess(string);
        else {
            if (string.endsWith(".dict")) {
                FileAccessBase file = openDictDzFile(string + ".dz");
                if (file!=null && file.exists() && file.isFile())
                    return new DictZipFileAccess(string);
            }
        }
        return null;
    }
    protected FileAccessBase openDictDzFile(String fileName) {
        for (int i = 0; i < FileFactory.getFactories().size(); ++i) {
            
            IFileFactory factory = (IFileFactory) FileFactory.getFactories().elementAt(i);
            FileAccessBase file;
            try {
                if (!(factory instanceof DictZipFactory)) {
                    file = factory.newFileAccess(fileName);
                    if (file != null && (file.exists())) {
                        return file;
                    }
                    
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            
        }
        
        return null;
    }
    
    
    
    
}
