/*
 * Properties.java
 *
 * Created on Aug 5, 2007, 12:55:29 AM
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

import java.util.Vector;

/**
 *
 * @author wind
 */

public class Properties {

    public Properties() {
    }

    Vector keys = new Vector();
    Vector values = new Vector();

    public int size() {
        return keys.size();
    }

    public void setProperty(String key, Object value) {
        int index = keys.indexOf(key);
        if (index == -1) {
            keys.addElement(key);
            values.addElement(value);
        } else {
            values.setElementAt(value, index);
        }
    }

    public Object getProperty(String key) {
        try{
            return values.elementAt(getKeyIndex(key));
        }catch(Exception ex)
        {
            return null;
        }
    }

    public Object getProperty(int index) {
        return values.elementAt(index);
    }

    public Object getKey(int index) {
        return keys.elementAt(index);
    }

    public int getKeyIndex(String key)
    {
        key = key.toLowerCase();
        for(int i=0;i<keys.size();++i)
            if(key.equals(keys.elementAt(i).toString().toLowerCase()))
                return i;
        return -1;
    }
    public Object getKeyOf(Object value) {
        return keys.elementAt(values.indexOf(value));
    }
}
