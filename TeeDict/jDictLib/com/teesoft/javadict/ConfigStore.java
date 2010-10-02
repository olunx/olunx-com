/*
 * DictConfig.java
 * 
 * Created on Aug 5, 2007, 8:03:39 PM
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

import com.teesoft.jfile.FileFactory;
import java.io.InputStream;
import java.util.Hashtable;

/**
 *
 * @author wind
 */
public abstract class ConfigStore {

    protected ConfigStore() {
    }
    public abstract ConfigGroup doLoadConfigGroup(String appName,String groupName);
    public abstract void doSaveConfigGroup(String appName,ConfigGroup group);
    
 private static ConfigStore theFactory=null;
    static {
        try{
        if (FileFactory.isMicroedition())
            theFactory = (ConfigStore) java.lang.Class.forName("com.teesoft.javadict.j2meConfigFactory").newInstance()  ;
        else
            theFactory = (ConfigStore) java.lang.Class.forName("com.teesoft.javadict.j2seConfigFactory").newInstance()  ;
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }    
    public static  ConfigGroup loadConfigGroup(String appName,String groupName)
    {
            return theFactory.doLoadConfigGroup(appName,groupName);
    }
    public static void saveConfigGroup(String appName,ConfigGroup group)
    {
            theFactory.doSaveConfigGroup(appName,group);
    }

    public static ConfigStore getTheFactory() {
        return theFactory;
    }

    public static void setTheFactory(ConfigStore theFactory) {
        ConfigStore.theFactory = theFactory;
    }
    
    
    public abstract Hashtable parseTab(byte [] buf);
        
}
