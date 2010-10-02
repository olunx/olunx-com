/*
 * ConfigItem.java
 *
 * Created on Aug 5, 2007, 7:49:08 PM
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

package com.teesoft.javadict;

import com.teesoft.jfile.FileAccessBase;
import java.io.IOException;

/**
 *
 * @author wind
 */
public class ConfigItem {

    public ConfigItem(String instance, Object storeId) {
        Instance = instance;
        properties = new Properties();
        this.storeId = storeId;
    }

    public String getInstance() {
        return Instance;
    }

    public void setInstance(String Instance) {
        this.Instance = Instance;
    }

    public int size() {
        return properties.size();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperty(String key, Object value) {
        properties.setProperty(key, value);
    }

    public Object getProperty(String key) {
        return properties.getProperty(key);
    }

    public int getInt(String key, int defaultValue) {
        String value = (String) this.getProperty(key);
        int intValue = defaultValue;
        try {
            intValue = Integer.parseInt(value);
        } catch (Exception e) {
        }
        return intValue;
    }

    public boolean getBoolProperty(String key, boolean defaultValue) {
        String value = (String) this.getProperty(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        value = value.toLowerCase();
        return value.equals("true") || value.equals("1") || value.equals("yes");
    }

    public Object getProperty(int index) {
        return properties.getProperty(index);
    }

    public Object getKey(int index) {
        return properties.getKey(index);
    }

    public void save() {
    }

    public boolean load() {
        return false;
    }

    public boolean load(FileAccessBase file) {
        try {
            byte[] content = new byte[(int) file.fileSize()];
            file.read(content);
            file.close();
            com.teesoft.javadict.tabParser paser = new com.teesoft.javadict.tabParser(content, "utf-8", '=');
            for (int j = 0; j < paser.size(); ++j) {
                String key = paser.getKey(j).trim();
                if (!key.startsWith("#"))
                    this.setProperty(key, paser.getValue(j).trim());
            }

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    public String getString(String key, String defaultValue) {
        Object result = this.getProperty(key);
        if (result == null) {
            return defaultValue;
        }
        return result.toString();
    }
    private Properties properties;
    private String Instance;
    private Object storeId;
}