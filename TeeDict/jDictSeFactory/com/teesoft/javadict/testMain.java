/*
 * testMain.java
 *
 * Created on 2007-9-1, 7:11 PM
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

package com.teesoft.javadict;

import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.IFileFactory;
import com.teesoft.jfile.compressedFileFactory;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class testMain {
    
    /** Creates a new instance of testMain */
    public testMain() {
    }
    public static void main(String [] args) throws IOException
    {
        String string = "javatar-2.5.tar.gz";
        
        IFileFactory instance = compressedFileFactory.getInstance();
        
        FileAccessBase expResult = null;
        FileAccessBase result ;//= instance.newFileAccess(string);
        //dumpFile(result);
        string = "stardict-cdict-gb-2.4.2.tar.bz2";
        result = instance.newFileAccess(string);
        dumpFile(result);
    }

    private static void dumpFile(final FileAccessBase result) throws IOException {
        Vector v = result.listFiles();
        byte [] buf = new byte[1024];
        for(int i=0;v!=null && i<v.size();++i)
        {
            FileAccessBase file = (FileAccessBase) v.elementAt(i);
            System.out.println(file.isDirectory() + " " +  file.getAbsolutePath());
            if (file.isFile() && file.getName().endsWith(".java"))
            {
                int ret = file.read(buf);
                System.out.println(new String(buf,0,ret));
            }
            dumpFile(file);
            file.close();
        }
    }
}
