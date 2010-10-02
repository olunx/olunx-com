/*
 * DictFactory.java
 * 
 * Created on Aug 5, 2007, 1:26:25 PM
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
import java.io.IOException;

/**
 *
 * @author wind
 */
public abstract class DictFactory {

    public DictFactory() {
    }
    public Dict CreateDict(FileAccessBase file,String name,Properties properties) throws IOException 
    {
        return null;
    }
    public abstract Dict AcceptDict(FileAccessBase file,Properties properties) throws IOException;
    
    public Properties SupportedProperties()
    {
        return new Properties();
    }
    public boolean supportFileDict()
    {
        return true;
    }
    public boolean supportFolderDict()
    {
        return true;
    }
    public abstract String getFormat();
    
    public String toString()
    {
        return getFormat();
    }
}
