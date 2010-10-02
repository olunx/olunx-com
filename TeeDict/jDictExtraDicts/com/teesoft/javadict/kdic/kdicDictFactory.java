/*
 * startDictFactory.java
 *
 * Created on Aug 5, 2007, 4:58:28 PM
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

package com.teesoft.javadict.kdic;

import com.teesoft.javadict.Dict;
import com.teesoft.javadict.DictFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.javadict.Properties;
import java.io.IOException;


/**
 *
 * @author wind
 */
public class kdicDictFactory extends DictFactory {

    public Dict AcceptDict(FileAccessBase f, Properties properties) throws IOException {
        if (!f.isFile()) {
            return null;
        }
        String encoding = "";
        if (properties != null)
            encoding = (String) properties.getProperty("charset");
        
        kdic.ValidateResult validate = kdic.validate(f,encoding);
        if (validate == null || validate.dictName.length()==0)
            return null;
        if (properties != null && validate.charset.length()>0)
             properties.setProperty("charset",validate.charset);
        
        Dict dict = new kdicDict(f,validate.dictName, properties);
        if (dict != null) {
            dict.setFactoryClass(this.getClass().getName());
            dict.setFormat(this.getFormat());
        }
        return dict;
    }



    public Dict CreateDict(FileAccessBase file, String name, Properties properties) throws IOException {
        Dict dict = new kdicDict(file, name, properties);
        String encoding=null;
        if (dict != null) {
            dict.setFactoryClass(this.getClass().getName());
                   
            dict.setFormat(this.getFormat());
        }
        return dict;
    }

    public String getFormat() {
        return "kdic";
    }

    public Properties SupportedProperties() {
        return super.SupportedProperties();
    }

    public boolean supportFileDict() {
        return true;
    }

    public boolean supportFolderDict() {
        return false;
    }
}
