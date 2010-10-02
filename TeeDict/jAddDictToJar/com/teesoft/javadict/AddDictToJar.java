/*
 * AddDictToJar.java
 *
 * Created on 2007-8-23, 11:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.teesoft.javadict;

import com.teesoft.ant.CreateSparseFile;
import com.teesoft.ant.ListFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author wind
 */
public class AddDictToJar {

    private static void deleteAll(File dictPath) {
        if (dictPath.isDirectory())
        {
            File [] subfiles = dictPath.listFiles();
            for(int i=0;subfiles!=null && i<subfiles.length;++i)
            {
                deleteAll(subfiles[i]);
            }
        }
        dictPath.delete();
            
    }
    
    /** Creates a new instance of AddDictToJar */
    private AddDictToJar() {
    }
    public static interface Listener{
        void afterCopy(String path);
    }
    public static boolean AddFolder(Vector dicts,File folder,Object copyOption, File jar,File target,
            boolean splitFile,long minSize,int splitSize,boolean deleteAfterAdd) {
        return AddFolder(dicts, folder, copyOption, jar, target, splitFile, minSize, splitSize, deleteAfterAdd, null);
    }
    public static boolean AddFolder(Vector dicts,File folder,Object copyOption, File jar,File target,
            boolean splitFile,long minSize,int splitSize,boolean deleteAfterAdd,Listener listener) {
        
        File dictPath ;
        File teePath = folder;
        if (teePath == null) {
            teePath = getTmpDir();
        } else
            deleteAfterAdd = false;
        
        teePath = new File(teePath.getAbsolutePath() + File.separator + "exported");
        dictPath = new File(teePath.getAbsolutePath() + File.separator + "dict");
        deleteAll(dictPath);
        dictPath.mkdirs();
        
        for(int i=0;i<dicts.size();++i) {
            Dict dict = (Dict) dicts.elementAt(i);
            if(dict!=null)
                dict.copyTo(dictPath.getAbsolutePath(),copyOption);
        }
        if (listener!=null)
            listener.afterCopy(dictPath.getAbsolutePath());
        if (splitFile)
        {
            CreateSparseFile instance = new CreateSparseFile(dictPath.getAbsolutePath(),minSize,splitSize,"0123456789",true);
            instance.execute();
        }
        
        
        
        if (jar!=null) {
            prepareJarFolder(dictPath,target, deleteAfterAdd, teePath, jar);
        }
        if (deleteAfterAdd) {
            dictPath.delete();
            //teePath.delete();
        }
        return true;
    }

    public static File getTmpDir() {
        File teePath;
        String tmpFilePath = System.getProperty("java.io.tmpdir");
        File tmp = new File(tmpFilePath );
        teePath = new File(tmp.getAbsolutePath() + File.separator + "teedict");
        
        teePath.mkdirs();
        return teePath;
    }
    
    private static void prepareJarFolder(final File dictPath,final File target, final boolean deleteAfterAdd, final File teePath, final File jar) {
        ZipOutputStream output = null;
        try {
            ZipFile file = new ZipFile(jar);
            Enumeration entries = file.entries();
            output = new ZipOutputStream(new java.io.FileOutputStream(target));
            output.setLevel(Deflater.BEST_COMPRESSION);
            while(entries.hasMoreElements()) {
                ZipEntry  entry = (ZipEntry) entries.nextElement();
                //System.out.println(entry.getName());
                if(entry.getName().equals("resource.list") || entry.getName().startsWith("dict/")) {
                    if (entry.getName().endsWith("/")) {
                        File f = new File(teePath.getAbsolutePath()+File.separator+entry.getName());
                        f.mkdirs();
                    } else {
                        byte [] buf = new byte[ 512];
                        InputStream input = file.getInputStream(entry);
                        int len = input.read(buf);
                        FileOutputStream outputFile = new FileOutputStream(teePath.getAbsolutePath()+File.separator+entry.getName());
                        while(len>0) {
                            outputFile.write(buf, 0, len);
                            len = input.read(buf);
                        }
                        outputFile.flush();
                        outputFile.close();
                    }
                } else {
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    newEntry.setExtra(entry.getExtra());
                    output.putNextEntry(newEntry);
                    
                    
                    if (entry.getSize()>0) {
                        byte [] buf = new byte[ 512];
                        InputStream input = file.getInputStream(entry);
                        int len = input.read(buf);
                        
                        while(len>0) {
                            output.write(buf, 0, len);
                            len = input.read(buf);
                        }
                    }
                }
            }
            
            ListFile listFile = new ListFile(dictPath.getAbsolutePath()
            ,teePath.getAbsolutePath() + File.separator + "resource.list"
                ,"CVS|.*~",true,dictPath.getAbsolutePath());
            listFile.execute();            
            AddFolderToZip(output,teePath,deleteAfterAdd);
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        } finally {
            try {
                output.flush();
                output.close();
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static void AddFolderToZip(ZipOutputStream output, File teePath,boolean deleteAfterAdd) {
        String prefix = teePath.getAbsolutePath() + File.separator;
        AddFolderToZip(output,prefix,teePath,deleteAfterAdd);
    }
    private static void AddFolderToZip(ZipOutputStream output, String prefix,File path,boolean deleteAfterAdd) {
        File [] files = path.listFiles();
        for(int i=0;i<files.length;++i) {
            File file = files[i];
            String name = file.getAbsolutePath().substring(prefix.length());
            if (name==null)
                name="";
            name = name.replace('\\','/');
            if (file.isFile()) {
                try {
                    output.putNextEntry(new ZipEntry(name));
                    byte [] buf = new byte[ 512];
                    InputStream input = new FileInputStream(file);
                    int len = input.read(buf);
                    while(len>0) {
                        output.write(buf, 0, len);
                        len = input.read(buf);
                    }
                    input.close();
                    if (deleteAfterAdd)
                        file.delete();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                if (name.length()>0) {
                    try {
                        output.putNextEntry(new ZipEntry(name+"/"));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                AddFolderToZip(output,prefix,file,deleteAfterAdd);
                if (deleteAfterAdd)
                    file.delete();
            }
        }
    }
    
}
