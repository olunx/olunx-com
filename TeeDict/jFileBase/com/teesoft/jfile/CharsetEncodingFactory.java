/*
 * CharsetEncodingFactory.java
 *
 * Created on 2007-9-9, 14:54:06
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

import java.io.UnsupportedEncodingException;

/**
 *
 * @author wind
 */
public abstract class CharsetEncodingFactory {

    static CharsetEncodingFactory instance = null;

    static String pathRoot="";

    public static String getPathRoot() {
        return pathRoot;
    }

    public static void setPathRoot(String pathRoot) {
        CharsetEncodingFactory.pathRoot = pathRoot;
    }
    public static CharsetEncodingFactory getInstance() {
        if (instance == null) {
            try {
                instance = (com.teesoft.jfile.CharsetEncodingFactory) Class.forName("com.teesoft.icu.CharsetEncoding").newInstance();
                if (pathRoot.length()!=0)
                    instance.doSetPathRoot(pathRoot);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return instance;
    }
    public abstract String doNewString(byte[] data, int offset, int count, String encoding) throws UnsupportedEncodingException;

    public abstract byte[] doGetBytes(String value, int offset, int count, String enc) throws UnsupportedEncodingException;
    public abstract void doSetPathRoot(String pathRoot) ;
    public static String newString(byte[] data, String encoding) throws UnsupportedEncodingException
    {
        try
        {
            return newString(data,0,data.length,encoding);
        }catch(UnsupportedEncodingException e)
        {
            throw e;
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public static String newString(byte[] data, int offset, int count, String encoding) throws UnsupportedEncodingException
    {
        try{
            String str = new String(data,offset,count,encoding);
            return str;
        }catch(UnsupportedEncodingException ex)
        {
            if (getInstance()!=null)
                return getInstance().doNewString(data, offset, count, encoding);
        }catch(Throwable e)
        {
            String str = new String(data,offset,count);
            return str;
        }
        throw new UnsupportedEncodingException();
    }

    public static byte[] getBytes(String value, String enc) throws UnsupportedEncodingException
    {
        return getBytes(value,0,value.length(),enc);
    }
    public static byte[] getBytes(String value, int offset, int count, String enc) throws UnsupportedEncodingException
    {
        String str = value;
        if (offset!=0 || count != value.length())
        {
            str = value.substring(offset, count+offset);
        }
        try{
            return str.getBytes(enc);
        }catch(UnsupportedEncodingException ex)
        {
            if (getInstance()!=null)
                return getInstance().doGetBytes(value, offset, count, enc);
        }
        throw new UnsupportedEncodingException();        
    }

    
}
