/*
 * Dictmanager.java
 *
 * Created on Aug 5, 2007, 2:41:57 PM
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
import com.teesoft.jfile.FileFactory;
import com.teesoft.javadict.DictManager.loadingListener;
import com.teesoft.javadict.mdict.mdictFactory;
import com.teesoft.javadict.stardict.startDictFactory;
import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class DictManager {

    public static Vector dictFactories;
    

    static {
        CharsetEncodingFactory.setPathRoot("dict/config/icu/");
        dictFactories = new Vector();
        RegisterFactory(startDictFactory.getInstance());
        RegisterFactory(new mdictFactory());

        try {
            RegisterFactory((DictFactory) Class.forName("com.teesoft.javadict.kdic.kdicDictFactory").newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            RegisterFactory((DictFactory) Class.forName("com.teesoft.javadict.tab.tabDictFactory").newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            RegisterFactory((DictFactory) Class.forName("com.teesoft.javadict.dicf.dicfDictFactory").newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public static DictManager instance = null;

    public static DictManager getInstance() {
        if (instance == null) {
            instance = new DictManager();
        }
        return instance;
    }

    public static int getDictFactoryCount() {
        return dictFactories.size();
    }

    public static DictFactory getDictFactory(int index) {
        if (index >= 0 && index < getDictFactoryCount()) {
            return (DictFactory) dictFactories.elementAt(index);
        }
        return null;
    }

    public static DictFactory getDictFactory(String format) {
        for (int i = 0; i < dictFactories.size(); ++i) {
            DictFactory dictFactory = (DictFactory) dictFactories.elementAt(i);
            if (dictFactory.getFormat().equals(format)) {
                return dictFactory;
            }
        }
        return null;
    }

    public static String getDictFactoryClass(String format) {
        DictFactory dictFactory = getDictFactory(format);
        if (dictFactory != null) {
            return dictFactory.getClass().getName();
        }
        return "";
    }

    public static void RegisterFactory(DictFactory factory) {
        dictFactories.addElement(factory);
    }

    public static DictFactory getDictFactory(String name, String className) {
        for (int i = 0; i < dictFactories.size(); ++i) {
            DictFactory factory = (DictFactory) dictFactories.elementAt(i);
            if (factory.getFormat().equals(name)) {
                return factory;
            }
        }

        try {
            return (com.teesoft.javadict.DictFactory) Class.forName(className).newInstance();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    Vector dictList = new /*<Dict>*/ Vector /*<Dict>*/();
    private int maxCount;

    public DictManager() {
        if (FileFactory.isMicroedition()) {
            this.maxCount = 5;
        } else {
            this.maxCount = 20;
        }

    }

    public ItemList search(String word, String dictName) {
        //System.gc();
        Dict dict = getDictByName(dictName);
        ItemList list = null;
        if (dict == null) {
            list = search(word, maxCount);
        } else {
            if (!dict.isOpened()) {
                openDict(dict);
            }
            if (!dict.isOpened()) {
                list = new ItemList();
            } else {
                list = dict.search(word, maxCount);
                list.select(word);
            }
        }
        notifySearchDone();

        return list;
    }

    public ItemList search(String word, int max) {
        ItemList list = null;

        if (dictList.size() > 0) {
            int first = getFirstEnable();
            if (first >= dictList.size() || first < 0) {
                return new ItemList();
            }
            com.teesoft.javadict.Dict dict = (com.teesoft.javadict.Dict) dictList.elementAt(first);
            if (!dict.isOpened()) {
                openDict(dict);
            }
            if (dict.isOpened()) {
                list = dict.search(word, max);
            } else {
                list = new ItemList();
            }
            for (int i = first + 1; i < dictList.size(); ++i) {
                dict = (com.teesoft.javadict.Dict) dictList.elementAt(i);
                if (dict.isEnabled()) {
                    if (!dict.isOpened()) {
                        openDict(dict);
                    }
                    this.notifyPartResult(list);
                    com.teesoft.javadict.ItemList newList = dict.search(word, max);
                    list.addItems(newList);
                }
            }
            int index = list.select(word);

            ItemList newList = new ItemList();
            int minIndex = index - max / 2;
            if (minIndex < 0) {
                minIndex = 0;
            }
            int maxIndex = minIndex + max - 1;
            if (maxIndex >= list.size()) {
                maxIndex = list.size() - 1;
                minIndex = list.size() - max > 0 ? list.size() - max : 0;
            }
            for (int i = minIndex; i <= maxIndex; ++i) {
                newList.appendItem(list.getItem(i));
            }
            newList.setSelected(index - minIndex);
            list.clear();
            list = newList;
        }
        if (list == null) {
            list = new ItemList();
        }
        return list;
    }

    public int loadDicts() {
        loadDicts((DictManager.loadingListener) null);
        return size();
    }

    public static interface loadingListener {

        boolean NotifyStartingLoadDict(String dictName, boolean canceled);

        void NotifyEndingLoadDict(String dictName, boolean ignored);

        void NotifyAllDone();

        boolean NotifyNewDict(String dictName, boolean disabled);
    }

    public static interface searchListener {

        boolean NotifyStartingLoadDict(Dict dict);

        void NotifyEndingLoadDict(Dict dict, boolean failed);

        void NotifyPartResult(ItemList list);

        void NotifySearchDone();

        void NotifyPromptStart(String prompt);

        void NotifyPromptEnd(String prompt);
    }
    Vector searchLisners = new Vector();

    public void addSearchListener(searchListener listener) {
        searchLisners.addElement(listener);
    }

    public void removeSearchListener(searchListener listener) {
        searchLisners.removeElement(listener);
    }

    public void notifyStartRealLoadDict(Dict dict) {
        for (int i = 0; i < searchLisners.size(); ++i) {
            searchListener listener = (searchListener) searchLisners.elementAt(i);
            listener.NotifyStartingLoadDict(dict);
        }
    }

    public void notifyEndRealLoadDict(Dict dict, boolean failed) {
        for (int i = 0; i < searchLisners.size(); ++i) {
            searchListener listener = (searchListener) searchLisners.elementAt(i);
            listener.NotifyEndingLoadDict(dict, failed);
        }
    }

    public void notifyPartResult(ItemList list) {
        for (int i = 0; i < searchLisners.size(); ++i) {
            searchListener listener = (searchListener) searchLisners.elementAt(i);
            listener.NotifyPartResult(list);
        }
    }

    public void notifySearchDone() {
        for (int i = 0; i < searchLisners.size(); ++i) {
            searchListener listener = (searchListener) searchLisners.elementAt(i);
            listener.NotifySearchDone();
        }
    }

    public void NotifyPromptStart(String prompt) {
        for (int i = 0; i < searchLisners.size(); ++i) {
            searchListener listener = (searchListener) searchLisners.elementAt(i);
            listener.NotifyPromptStart(prompt);
        }
    }

    public void NotifyPromptEnd(String prompt) {
        for (int i = 0; i < searchLisners.size(); ++i) {
            searchListener listener = (searchListener) searchLisners.elementAt(i);
            listener.NotifyPromptEnd(prompt);
        }
    }

    public void saveDicts() {
        configGroup = new ConfigGroup("default", "dicts", null);
        for (int i = 0; i < this.dictList.size(); ++i) {
            try {
                com.teesoft.javadict.ConfigItem item = new com.teesoft.javadict.ConfigItem(java.lang.String.valueOf(i), java.lang.String.valueOf(i));
                item.setProperty("name", getDict(i).getName());
                if (getDict(i).getFile() != null) {
                    item.setProperty("path", getDict(i).getFile().getAbsolutePath());
                }
                item.setProperty("class", getDict(i).getFactoryClass());
                item.setProperty("format", getDict(i).getFormat());
                item.setProperty("charset", getDict(i).getEncoding());
                item.setProperty("enabled", String.valueOf(getDict(i).isEnabled()));
                item.setProperty("scan", String.valueOf(getDict(i).isScan()));
                item.setProperty("html", String.valueOf(getDict(i).isHtml()));
                item.setProperty("order", String.valueOf(i));
                configGroup.addInstance(item);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        ConfigStore.saveConfigGroup("defaults", configGroup);
    }

    public Dict getDict(int index) {
        return (Dict) dictList.elementAt(index);
    }

    public Dict getDictByName(String name) {
        for (int i = 0; i < this.dictList.size(); ++i) {
            if (getDict(i).getName().equals(name)) {
                return getDict(i);
            }
        }
        return null;
    }

    public int size() {
        return dictList.size();
    }

    public void closeDicts() {
        for (int i = 0; i < this.dictList.size(); ++i) {
            getDict(i).close();
        }
    }

    public void loadDicts(loadingListener listener) {
        loadDicts(listener, false);
    }

    public void close() {
        closeDicts();
        this.dictList.removeAllElements();
    }

    public void loadDicts(loadingListener theListener, boolean noDiscover) {
        this.listener = theListener;

        try {
            close();
            try{
                loadSaveSettings();
            }catch(Throwable e)
            {
                e.printStackTrace();
            }
            /*            if (dictList == null) {
            dictList =  FileFactory.newDictConfig("dict");
            dictList.loadConfig();
            }
             */
            if (this.size()==0 || !noDiscover) {
                discoverDicts();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //for(int i=0;i<dictList.size();i++) {
        //    doLoadDict(dictList.get(i));
        //}
        if (theListener != null) {
            theListener.NotifyAllDone();
        }

    }

    public void discoverDicts() {
        loadDicts("dict/");
    }

    void loadDicts(String dir) {
        try {

//            Vector resourceRoots = ResourceFactory.getInstance().listRoots();
//            for (int i = 0; i < resourceRoots.size(); i++) {
//                try{
//                    discoverDicts(((FileAccessBase) resourceRoots.elementAt(i)).child(dir));
//                } catch (IOException ex) {
//                    ex.printStackTrace();
//                }
//            }
            //if (FileFactory.isMicroedition()) {
            Vector roots = FileFactory.listRoots();
            for (int i = 0; i < roots.size(); i++) {
                try {
                    FileAccessBase file = (FileAccessBase) roots.elementAt(i);

                    discoverDicts(file.child(dir));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        //}
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Dict addDict(String dictName, String path, String format, String encoding) {
        DictFactory factory = getDictFactory(format);
        Properties p = new Properties();
        p.setProperty("charset", encoding);
        try {
            Dict dict = factory.AcceptDict(FileFactory.openFileAccess(path, true), p);
            if (dict != null) {
                dict.setName(dictName);
                if (addDict(dict, false)) {
                    return dict;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean addDict(Dict dict, boolean notify) {
        if (dict == null) {
            return false;
        }
        for (int i = 0; i < this.dictList.size(); ++i) {
            try {
                com.teesoft.javadict.Dict dic = (com.teesoft.javadict.Dict) dictList.elementAt(i);
                if (dic != null && dic.getFile() != null && dict.getFile() != null && dic.getFile().getAbsolutePath().equals(dict.getFile().getAbsolutePath())) {
                    //dic.close();
                    //dictList.setElementAt(dict, i);
                    return false;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (this.listener != null) {
            //System.out.println(dict.getName());
            this.listener.NotifyStartingLoadDict(dict.getName(), !dict.isEnabled());
        }
        int i = size() - 1;
        {
            while (i >= 0) {
                if (getDict(i).getOrder() <= dict.getOrder()) {
                    break;
                }
                i--;
            }
            dictList.insertElementAt(dict, i + 1);
        }
        if (this.listener != null && notify) {
            this.listener.NotifyNewDict(dict.getName(), !dict.isEnabled());
        }
        if (this.listener != null) {
            this.listener.NotifyEndingLoadDict(dict.getName(), !dict.isEnabled());
        }
        return true;
    }

    public void discoverDictsRes(FileAccessBase file) {
        try {
            Dict dict = discoverFile(file, null);
            if (dict != null) {
                this.addDict(dict, true);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        discoverDicts(file);
        Vector v;
        try {
            v = file.listFiles();
            if (v != null) {
                for (int i = 0; i < v.size(); ++i) {
                    FileAccessBase child = (FileAccessBase) v.elementAt(i);
                    discoverDictsRes(child);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void discoverDicts(FileAccessBase child) {
        try {
            Vector dicts = DiscoverDicts(child, null);
            for (int i = 0; i < dicts.size(); ++i) {
                addDict((Dict) dicts.elementAt(i), true);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public java.util.Vector DiscoverDicts(FileAccessBase folder, Properties properties) throws IOException {
        Vector dicts = new /*<Dict>*/ Vector /*<Dict>*/();
        if (folder == null) {
            return dicts;
        }
        try {
            if (folder.exists() && folder.isDirectory()) {
                Vector folders = folder.listFiles();

                for (int i = 0; i < folders.size(); i++) {
                    FileAccessBase f = (FileAccessBase) folders.elementAt(i);
                    if (findDictByFile(f) != null) {
                        continue;
                    }
                    Dict dict = discoverFile(f, properties);

                    if (dict != null) {
                        Dict oldDict = this.getDictByName(dict.getName());
                        if (oldDict != null && oldDict.isEnabled()) {
                            dict.setEnabled(false);
                        }
                        dicts.addElement(dict);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return dicts;
    }

    ItemList searchScanWord(String word, int max) {
        ItemList list = null;

        if (dictList.size() > 0) {
            int first = getFirstScan();
            if (first >= dictList.size() || first < 0) {
                return new ItemList();
            }
            com.teesoft.javadict.Dict dict = (com.teesoft.javadict.Dict) dictList.elementAt(first);
            if (!dict.isOpened()) {
                openDict(dict);
            }
            if (dict.isOpened()) {
                list = dict.search(word, max);
            } else {
                list = new ItemList();
            }
            for (int i = first + 1; i < dictList.size(); ++i) {
                dict = (com.teesoft.javadict.Dict) dictList.elementAt(i);
                if (dict.isScan()) {
                    if (!dict.isOpened()) {
                        openDict(dict);
                    }
                    this.notifyPartResult(list);
                    com.teesoft.javadict.ItemList newList = dict.search(word, max);
                    list.addItems(newList);
                }
            }
            int index = list.select(word);

            ItemList newList = new ItemList();
            int minIndex = index - max / 2;
            if (minIndex < 0) {
                minIndex = 0;
            }
            int maxIndex = minIndex + max - 1;
            if (maxIndex >= list.size()) {
                maxIndex = list.size() - 1;
                minIndex = list.size() - max > 0 ? list.size() - max : 0;
            }
            for (int i = minIndex; i <= maxIndex; ++i) {
                newList.appendItem(list.getItem(i));
            }
            newList.setSelected(index - minIndex);
            list.clear();
            list = newList;
        }
        return list;
    }

    public Dict discoverFile(final FileAccessBase f, final Properties properties) throws IOException {
        for (int index = 0; index < dictFactories.size(); ++index) {
            DictFactory factory = (DictFactory) dictFactories.elementAt(index);

            if (!f.isDirectory() && !factory.supportFileDict()) {
                continue;
            }
            if (f.isDirectory() && !factory.supportFolderDict()) {
                continue;
            }
            try {
                Dict dict = factory.AcceptDict(f, properties);
                if (dict != null) {
                    dict.setFactoryClass(factory.getClass().getName());
                    dict.setFormat(factory.getFormat());
                    return dict;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
    private loadingListener listener;
    private ConfigGroup configGroup;

    private int getFirstEnable() {
        int first = 0;
        while (first < dictList.size() && !this.getDict(first).isEnabled()) {
            first++;
        }
        return first;
    }

    private int getFirstScan() {
        int first = 0;
        while (first < dictList.size() && !this.getDict(first).isScan()) {
            first++;
        }
        return first;
    }

    private void loadSaveSettings() {
        try {
            configGroup = ConfigStore.loadConfigGroup("defaults", "dicts");
        } catch (Exception ex) {
            return;
        }
        FileAccessBase file;
        for (int i = 0; i < configGroup.size(); ++i) {
            ConfigItem item = configGroup.getInstance(i);
            String name = (String) item.getProperty("name");
            String path = item.getString("path", "");
            String format = (String) item.getProperty("format");
            String charset = (String) item.getProperty("charset");
            String dictClass = (String) item.getProperty("class");
            int order = item.getInt("order", i);
            boolean enabled = item.getBoolProperty("enabled", true);
            boolean html = item.getBoolProperty("html", false);
            boolean scan = item.getBoolProperty("scan", enabled);
            //System.out.println(dictClass + " " + format);
            if (name.length() > 0 && path.length() > 0 && dictClass.length() > 0) {
                try {
                    com.teesoft.javadict.DictFactory factory = getDictFactory(format, dictClass);
                    file = FileFactory.openFileAccess(path, true);
                    if (file!=null)
                    {
                        Dict dict = factory.CreateDict(file, name, item.getProperties());
                        if (dict != null) {
                            dict.setEnabled(enabled);
                            dict.setEncoding(charset);
                            dict.setOrder(order);
                            dict.setHtml(html);
                            dict.setScan(scan);
                            addDict(dict, false);
                        }
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (java.lang.NullPointerException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    private boolean openDict(Dict dict) {
        try {
            notifyStartRealLoadDict(dict);
            dict.open();
            notifyEndRealLoadDict(dict, dict.isOpened());
            return dict.isOpened();
        } catch (Exception ex) {
            notifyEndRealLoadDict(dict, dict.isOpened());
            return false;
        }
    }

    void delete(int i) {
        if (i < 0 || i > dictList.size() - 1) {
            return;
        }
        this.getDict(i).close();
        dictList.removeElementAt(i);
    }

    String getDefaultEncoding() {
        return "utf-8";
    }

    private Dict findDictByFile(FileAccessBase f) {
        if (f == null) {
            return null;
        }
        for (int i = 0; i < this.size(); ++i) {
            Dict dict = this.getDict(i);
            FileAccessBase file = dict.getFile();
            try {
                if (file != null && file.getAbsolutePath().equals(f.getAbsolutePath())) {
                    return dict;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    void moveDown(int selectIndex) {
        if (selectIndex >= 0 && selectIndex < size() - 1) {
            this.dictList.insertElementAt(this.dictList.elementAt(selectIndex), selectIndex + 2);
            this.dictList.removeElementAt(selectIndex);
        }
    }

    void moveUp(int selectIndex) {
        if (selectIndex > 0 && selectIndex <= size() - 1) {
            this.dictList.insertElementAt(this.dictList.elementAt(selectIndex), selectIndex - 1);
            this.dictList.removeElementAt(selectIndex + 1);
        }
    }
}