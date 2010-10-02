/*
 * ConfigItem.java
 *
 * Created on Aug 5, 2007, 7:49:08 PM
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
public class ConfigGroup {

    public ConfigGroup(String appName, String group, Object storeId) {
        this.appName = appName;
        Group = group;
        instances = new Vector();
        this.storeId = storeId;
    }

    public void addInstance(ConfigItem instance) {
        instances.addElement(instance);
    }

    public void removeInstance(int index) {
        instances.removeElementAt(index);
    }

    public int size() {
        return instances.size();
    }

    public ConfigItem getInstance(int index) {
        return (ConfigItem) instances.elementAt(index);
    }

    public String getGroup() {
        return Group;
    }

    public void setGroup(String Group) {
        this.Group = Group;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Object getStoreId() {
        return storeId;
    }

    public void setStoreId(Object storeId) {
        this.storeId = storeId;
    }

    public void save() {
    }

    public boolean load() {
        return false;
    }
    private String appName;
    private String Group;
    private Vector instances;
    private Object storeId;
}