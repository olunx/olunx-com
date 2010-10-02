/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teesoft.javadict;

import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.jfile.dz.DictZipFileAccess;
import com.teesoft.jfile.resource.resourceFileAccess;
import com.teesoft.jfile.sparse.SparseFileAccess;
import java.io.IOException;
import java.util.Hashtable;

/**
 *
 * @author wind
 */
public class DictSetInfos {

    static Hashtable dictSets = null;
    static OnlineListener listner = null;

    static{
        SplitDictFileFactory.getInstance();
    }
    public static Hashtable getSets() {
        if (dictSets != null && dictSets.size() > 0) {
            return dictSets;
        }
        dictSets = new Hashtable();
        String url = OnlineInfo.setsUrl;

        FileAccessBase file = null;
        try {
            file = FileFactory.openFileAccess(url, true);
            byte[] buf = getBytes(file);
            int size = 0;
            if (buf != null) {
                size = buf.length;
            }
            if (size > 0) {
                Hashtable info = ConfigStore.getTheFactory().parseTab(buf);
                String dictsets = info.get("dictsets").toString();
                String[] sets = resourceFileAccess.split(dictsets, ',');
                for (int i = 0; sets != null && i < sets.length; i++) {
                    String name = info.get(sets[i] + "-name").toString();
                    if (name.toLowerCase().indexOf("special") < 0) {
                        dictSets.put(name, new DictSetInfo(sets[i], name));
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (getListner() != null) {
                getListner().onException(url, ex);
            }
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                file = null;
            }
        }
        return dictSets;
    }

    public static Hashtable getDicts(String setName) {
        Hashtable table = new Hashtable();
        String url = OnlineInfo.dictsUrl + setName;

        FileAccessBase file = null;
        try {
            //System.out.println(url);
            file = FileFactory.openFileAccess(url, true);
            byte[] buf = getBytes(file);
            int size = 0;
            if (buf != null) {
                size = buf.length;
            }
            if (size > 0) {

                Hashtable info = ConfigStore.getTheFactory().parseTab(buf);
                buf = null;
                if (Runtime.getRuntime().freeMemory() < 64 * 1024) {
                    System.gc();
                //System.out.println(info);
                }
                String dicts = info.get("dicts").toString();
                if (dicts.length() > 0) {
                    String[] ds = resourceFileAccess.split(dicts, ',');
                    for (int i = 0; ds != null && i < ds.length; i++) {
                        //System.out.println(ds[i]);
                        if (info.containsKey(ds[i])) {
                            String name = (String) info.get(ds[i]);
                            if (name.toLowerCase().indexOf("special") < 0) {
                                //System.out.println(name);
                                String desc = (String) info.get(ds[i] + "-detail");
                                String href = (String) info.get(ds[i] + "-href");

                                table.put(name, new DictInfo(name, ds[i], href, desc));
                                if (Runtime.getRuntime().freeMemory() < 64 * 1024) {
                                    System.gc();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (getListner() != null) {
                getListner().onException(url, ex);
            }
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                file = null;
            }
        }
        return table;
    }
    static String[] exts = new String[]{
        ".tar.bz2", ".tar.gz", ".tar", ".zip"
    };

    public static void copyWithOption(FileAccessBase f, String target, boolean uncompress, boolean splitFile, int minSplitSize) {
        FileAccessBase targetFile = null;
        try {

            if (splitFile && f.fileSize() > minSplitSize) {
                targetFile = new SparseFileAccess(target);
            } else {
                targetFile = FileFactory.newFileAccess(target);
            }
            if (!targetFile.exists()) {
                targetFile.create();
            }
            Object copyOption = null;
            if (uncompress) {
                copyOption = new Object();
            }
            if (f.copyTo(targetFile, copyOption)) {
                targetFile.close();
                f.delete();

            }
        } catch (IOException ex) {
            ex.printStackTrace();
            if (getListner() != null) {
                try {
                    getListner().onException(f.getAbsolutePath(), ex);
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            }
        }

    }
    public static String getDictFiles(String folder, String dictName) throws IOException {
        return getDictFiles(folder, dictName, true, true);
    }
    public static String getDictFiles(String folder, String dictName,boolean uncompress,boolean splitFile) throws IOException {
        String url =  OnlineInfo.dictfilesUrl + dictName;
        String subFolderName = getFolderName(dictName);
        if (!folder.endsWith("/"))
            folder = folder +"/";
        FileAccessBase subFolder = FileFactory.newFileAccess(folder + subFolderName + "/");
        FileAccessBase parent = null;
        try {
            parent = FileFactory.newFileAccess(folder);
            parent.mkdir();
        } catch (IOException ex) {
            //ex.printStackTrace();
        } finally {
            if (parent != null) {
                parent.close();
            }
        }
        try {
            subFolder.mkdir();
        } catch (IOException ex) {
            System.out.println("Failed to create folder:" + subFolder.getAbsolutePath());
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }

        int minSplitSize = 512 * 1024;

        FileAccessBase indexFile = null;
        try {
            indexFile = FileFactory.openFileAccess(url, true);
            byte[] buf = getBytes(indexFile);
            int size = 0;
            if (buf != null) {
                size = buf.length;
            }

            if (size > 0) {
                String fileNames = new String(buf, "utf-8");
                String[] files = resourceFileAccess.split(fileNames, '\n');
                for (int i = 0; i < files.length; i++) {
                    if (Runtime.getRuntime().freeMemory() < 64 * 1024) {
                        System.gc();
                    }
                    String fileName = files[i];

                    if (fileName.length() > 0) {
                        if (getListner() != null && !getListner().startLoading(fileName)) {
                            return subFolder.getAbsolutePath();
                        }
                        FileAccessBase file = null;
if (FileFactory.isMicroedition())
{
//#if RIM                        
//#                         String fileUrl = "splitdict://" +dictName + "/" + files[i]; // OnlineInfo.dictfileUrl + dictName + "/" + files[i];
//#                         file = SplitDictFileFactory.getInstance().newFileAccess(fileUrl);
//#else                        
                         String fileUrl = "splitdict://" +dictName + "/" + files[i]; // OnlineInfo.dictfileUrl + dictName + "/" + files[i];
                         file = SplitDictFileFactory.getInstance().newFileAccess(fileUrl);
//                       String fileUrl = OnlineInfo.dictfileUrl + dictName + "/" + files[i];
//                       file = FileFactory.openFileAccess(fileUrl,true);
//#endif
}
else
{
                       String fileUrl = OnlineInfo.dictfileUrl + dictName + "/" + files[i];
                       file = FileFactory.openFileAccess(fileUrl,true);                            
}
                        try {
                            if (file.exists()) {
                                if (files[i].endsWith(".dz")) {
                                    file = new DictZipFileAccess(file);
                                    if (uncompress)
                                        fileName = files[i].substring(0, files[i].length() - ".dz".length());
                                }
                            }
                            if (getListner() != null && !getListner().loading(fileName, 0, file.fileSize())) {
                                return subFolder.getAbsolutePath();
                            }
                            copyWithOption(file, subFolder.getAbsolutePath() + subFolder.getSeparator() + fileName, uncompress, splitFile, minSplitSize);
                        } finally {
                            if (file != null) {
                                file.close();
                                file = null;
                            }
                        }
                        if (getListner() != null && !getListner().endLoading(fileName)) {
                            return subFolder.getAbsolutePath();
                        }
                    }

                }
            }
        } finally {
            if (indexFile != null) {
                try {
                    indexFile.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                indexFile = null;
            }
            if (subFolder != null) {
                try {
                    subFolder.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
        return subFolder.getAbsolutePath();
    }

    public static byte[] getBytes(FileAccessBase file) throws IOException {
        if (getListner() != null && !getListner().startLoading(file.getAbsolutePath())) {
            return new byte[0];
        }
        int size = (int) file.fileSize();
        byte[] buf = null;
        if (size > 0) {
            buf = new byte[size];
            //file.read(buf);
            int ret = file.read(buf);
            int currPos = 0;
            while (ret > 0 && currPos < size) {
                currPos += ret;
                try {
                    ret = file.read(buf, currPos, size - currPos);
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                    ret = -1;
                }
            }
        } else {
            byte[] tmp = new byte[32 * 1024];
            size = 0;
            int ret = file.read(tmp);
            if (ret > 0) {
                buf = new byte[ret];
                System.arraycopy(tmp, 0, buf, 0, ret);
                size = ret;
            }
        }
        if (getListner() != null && !getListner().endLoading(file.getAbsolutePath())) {
            return new byte[0];
        }
        return buf;
    }

    protected static String getFolderName(String dictName) {
        String subFolderName = null;
        for (int i = 0; i < exts.length; i++) {
            if (dictName.endsWith(exts[i])) {
                subFolderName = dictName.substring(0, dictName.length() - exts[i].length());
                break;
            }
        }
        if (subFolderName == null) {
            subFolderName = dictName;
        }
        return subFolderName;
    }

    public static OnlineListener getListner() {
        return listner;
    }

    public static void setListner(OnlineListener listner) {
        DictSetInfos.listner = listner;
    }
}
