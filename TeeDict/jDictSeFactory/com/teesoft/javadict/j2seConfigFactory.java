/*
 * j2seConfigFactory.java
 *
 * Created on Aug 5, 2007, 8:14:40 PM
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

import com.teesoft.jfile.CharsetEncodingFactory;
import com.teesoft.jfile.FileFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Hashtable;

/**
 *
 * @author wind
 */
public class j2seConfigFactory extends ConfigStore {

    public j2seConfigFactory() {
    }

    public ConfigGroup doLoadConfigGroup(String appName, String groupName) {
        ConfigGroup group = new ConfigGroup(appName, groupName, null);

        try {
            File folder = new File(FileFactory.getApplicationFolder());
            if (folder == null || !folder.exists() || !folder.isDirectory()) {
                return group;
            }
            File configFolder = new File(folder.getAbsolutePath() + File.separator + "config" + File.separator + appName + File.separator + groupName);
            File[] configs = configFolder.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".properties");
                }
            });

            if (configs == null) {
                return group;
            }
            byte[] nextRec;

            for (int i = 0; i < configs.length; ++i) {
                try {

                    FileInputStream input = new FileInputStream(configs[i]);
                    String name = configs[i].getName();
                    nextRec = new byte[(int) configs[i].length()];
                    input.read(nextRec);
                    tabParser paser = new tabParser(nextRec, "utf-8",'=');
                    ConfigItem item = new ConfigItem(getInstanceName(name), name);
                    for (int j = 0; j < paser.size(); ++j) {
                        item.setProperty(paser.getKey(j), paser.getValue(j));
                    }
                    group.addInstance(item);
                    input.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return group;
    }

    private String getInstanceName(String name) {
        int pos = name.indexOf(".");
        if (pos >= 0) {
            return name.substring(0, pos);
        }
        return name;
    }

    public void doSaveConfigGroup(String appName, ConfigGroup group) {
        try {

            File folder = new File(FileFactory.getApplicationFolder());
            if (folder == null || !folder.exists() || !folder.isDirectory()) {
                folder.mkdirs();
            }
            File configFolder = new File(folder.getAbsolutePath() + File.separator + "config" + File.separator + appName + File.separator + group.getGroup());
            if (configFolder == null || !configFolder.exists() || !configFolder.isDirectory()) {
                configFolder.mkdirs();
            }
            
            //delete not used files
            File[] configs = configFolder.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.endsWith(".properties");
                }
            });
            if (configs != null) {
                for (int i = 0; i < configs.length; ++i) {
                    boolean found=false;
                    for(int k=0;k<group.size();++k)
                    {
                        ConfigItem item = group.getInstance(k);
                        if (configs[i].getName().equalsIgnoreCase(item.getInstance() + ".properties"))
                        {
                            found = true;
                            break;
                        }
                    }
                    if(!found)
                    {
                        configs[i].delete();
                    }
                }
            }
            
            for (int i = 0; i < group.size(); ++i) {
                try {
                    String value = "";
                    ConfigItem item = group.getInstance(i);
                    for (int k = 0; k < item.size(); ++k) {
                        value += item.getKey(k) + "=" + item.getProperty(k) + "\n";
                    }
                    byte[] buf = CharsetEncodingFactory.getBytes(value,"utf-8");
                    confirmAndSave(buf, configFolder.getAbsolutePath() + File.separator + item.getInstance() + ".properties");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void confirmAndSave(byte[] buf, String filename) throws FileNotFoundException, IOException {
        File file = new File(filename);
        boolean needOutput = false;
        if (file.length() != buf.length) {
            needOutput = true;
        } else {
            FileInputStream input = new FileInputStream(file);
            byte[] newbuf = new byte[buf.length];
            input.read(newbuf);
            input.close();
            needOutput = (ByteArrayString.compareToIgnoreCase(buf, newbuf) != 0);
        }
        if (needOutput) {
            FileOutputStream output = new FileOutputStream(new File(filename));
            output.write(buf, 0, buf.length);
            output.close();
        }
    }

    public Hashtable parseTab(byte[] buf) {
        Hashtable table = new Hashtable();
        tabParser paser = new tabParser(buf, "utf-8", '=');
        for (int i = 0; i < paser.size(); ++i) {
            table.put(paser.getKey(i), paser.getValue(i));
        }
        return table;
    }
}