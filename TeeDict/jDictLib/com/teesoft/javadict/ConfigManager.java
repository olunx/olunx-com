/*
 * ConfigManager.java
 *
 * Created on Aug 5, 2007, 9:30:31 PM
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

import com.teesoft.jfile.FileFactory;

/**
 *
 * @author wind
 */
public class ConfigManager {

    private String appName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
    public static ConfigManager instance = null;

    private int maxCount;

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    protected ConfigManager() {
        //ConfigGroup general = new ConfigGroup("defaults","general");
        appName = "defaults";
        general = null;
        bookmarks = null;
        if (FileFactory.isMicroedition()) {
            this.maxCount = 5;
        } else {
            this.maxCount = 20;
        }
    }

    public void loadConfig() {
        loadConfig(appName);
    }

    public void loadConfig(String appName) {
        this.appName = appName;
        general = ConfigStore.loadConfigGroup(appName, "general");
        if (general == null) {
            general = new ConfigGroup(appName, "general", null);
        }
        if (general.size() == 0) {
            general.addInstance(new ConfigItem("general", null));
        }
        bookmarks = ConfigStore.loadConfigGroup(appName, "bookmarks");
        if (bookmarks == null) {
            general = new ConfigGroup(appName, "bookmarks", null);
        }
    }

    public void saveConfig() {
        saveConfig(appName);
    }

    public void saveConfig(String appName) {
        ConfigStore.saveConfigGroup(appName, general);
        ConfigStore.saveConfigGroup(appName, bookmarks);
    }

    public int getMaxCount() {
        return getGeneralValue("MaxCount", maxCount);
    }

    public void setMaxCount(int maxCount) {
        setGeneralValue("MaxCount", String.valueOf(maxCount));
    }
    public int getVolumn() {
        return getGeneralValue("Volumn", 7);
    }

    public void setVolumn(int Volumn) {
        setGeneralValue("Volumn", String.valueOf(Volumn));
    }

    void setMaxCount(String maxCount) {
        setGeneralValue("MaxCount", maxCount);
    }

    
    public boolean getPlaySoundDirectly() {
        return getBooleanValue("PlaySoundDirectly", !FileFactory.isMicroedition());
    }

    public void setPlaySoundDirectly(boolean value) {
        setGeneralValue("PlaySoundDirectly", String.valueOf(value));
    }  
    
    public void setGeneralValue(String name, String value) {
        if (general == null || general.size() == 0) {
            loadConfig();
        }
        ConfigItem item = general.getInstance(0);
        item.setProperty(name, value);
    }

    public int getMaxLenthForCtrl() {
        return getGeneralValue("MaxLenthForCtrl", 20480);
    }

    public void setMaxLenthForCtrl(int value) {
        setGeneralValue("MaxLenthForCtrl", String.valueOf(value));
    }

    
    public boolean getBooleanValue(String name, boolean defaultValue) {
        String value = getGeneralValue(name,String.valueOf(defaultValue));
        value = value.toLowerCase();
        return value.equals("true") || value.equals("yes") || value.equals("1");
    }

    public int getGeneralValue(String name, int defaultValue) {
        int value = defaultValue;
        if (general == null || general.size() == 0) {
            loadConfig();
        }
        ConfigItem item = general.getInstance(0);
        value = item.getInt(name, value);
        item.setProperty(name, String.valueOf(value));
        return value;
    }

    public String getGeneralValue(String name, String defaultValue) {
        String value = defaultValue;
        if (general == null || general.size() == 0) {
            loadConfig();
        }
        ConfigItem item = general.getInstance(0);
        value = item.getString(name, value);
        item.setProperty(name, value);
        return value;
    }

    public String getLang() {
        String lang = "default";
        if (general == null || general.size() == 0) {
            loadConfig();
        }
        ConfigItem item = general.getInstance(0);
        lang = item.getString("language", lang);
        item.setProperty("language", lang);
        return lang;
    }

    public void setLang(String lang) {
        if (general == null || general.size() == 0) {
            loadConfig();
        }
        ConfigItem item = general.getInstance(0);
        item.setProperty("language", lang);
    }


    private ConfigGroup general;
    private ConfigGroup bookmarks;

    private void assertBook() {
        if (bookmarks == null) {
            loadConfig();
        }
    }

    public int getBookmarkCount() {
        assertBook();
        return bookmarks.size();
    }

    public BookItem getBookmark(int index) {
        assertBook();
        if (index < 0 || index >= bookmarks.size()) {
            return null;
        }
        ConfigItem item = bookmarks.getInstance(index);
        String word = (String) item.getProperty("word");
        return new BookItem(word);
    }

    public void addBookmark(String word) {
        assertBook();
        for (int i = 0; i < bookmarks.size(); ++i) {
            String oldWord = (String) bookmarks.getInstance(i).getProperty("word");
            oldWord = oldWord.toLowerCase();

            if (oldWord != null && oldWord.equals(word.toLowerCase())) {
                return;
            }
        }
        ConfigItem newItem = new ConfigItem(null, null);
        newItem.setProperty("word", word);
        bookmarks.addInstance(newItem);
    }

    public void removeBookmark(String word) {
        assertBook();
        for (int i = 0; i < bookmarks.size(); ++i) {
            String oldWord = (String) bookmarks.getInstance(i).getProperty("word");

            if (oldWord != null && oldWord.toLowerCase().equals(word.toLowerCase())) {
                bookmarks.removeInstance(i);
                return;
            }
        }
    }
    public static class BookItem {

        String word;

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        private BookItem(String word) {
            this.word = word;
        }
    }

    private static int delayDefault = 800;
    public int getDelay() {
        return getGeneralValue("delay", delayDefault);
    }

    public void setDelay(int delay) {
        setGeneralValue("delay", String.valueOf(delay));
    }

    //disable autodiscover by default for phone
    public boolean getNoAutoDiscovery()
    {
        return getBooleanValue("NoAutoDiscovery", FileFactory.isMicroedition());
    }
    public void setNoAutoDiscovery(boolean noAutoDiscovery)
    {
        setGeneralValue("NoAutoDiscovery", String.valueOf(noAutoDiscovery));
    }    
    
    public boolean getTuningCompressed()
    {
        return getBooleanValue("TuningCompressed", true);
    }
    public void setTuningCompressed(boolean TuningCompressed)
    {
        setGeneralValue("TuningCompressed", String.valueOf(TuningCompressed));
    }    
    public boolean getSplitFile()
    {
        return getBooleanValue("SplitFile", true);
    }
    public void setSplitFile(boolean SplitFile)
    {
        setGeneralValue("SplitFile", String.valueOf(SplitFile));
    }    
}
