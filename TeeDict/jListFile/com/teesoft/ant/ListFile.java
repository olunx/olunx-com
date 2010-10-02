/*
 * FileListTask.java
 *
 * Created on Aug 20, 2007, 3:01:09 PM
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;

/**
 *
 * @author wind
 */
public class ListFile {
    
    private String folder;
    private String dest;
    
    private String baseDir;
    public ListFile(String folder,String dest,String exclude,boolean overwrite,String baseDir) {
        this.folder = folder;
        this.dest = dest;
        this.exclude = exclude;
        this.overwrite = overwrite;
        this.baseDir = baseDir;
    }
    public String getExclude() {
        return exclude;
    }
    
    public void setExclude(String exclude) {
        this.exclude = exclude;
    }
    private String exclude="CVS|.*~";
    private boolean overwrite = false;
    
    public String getDest() {
        return dest;
    }
    
    public void setDest(String dest) {
        this.dest = dest;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    public boolean isOverwrite() {
        return overwrite;
    }
    
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
    
    java.io.FileOutputStream output = null;
        /*
    @Override
         * */
    public void execute() {
        
        try {
            java.io.File outfile = new java.io.File(this.getDest());
            
            if (!outfile.isAbsolute())
            {
                outfile = new java.io.File(baseDir + File.separator + this.getDest());
            }
            
            output = new java.io.FileOutputStream(outfile,!isOverwrite());
            java.io.File file = new java.io.File(this.getFolder());
            if (!file.isAbsolute()) {
                file = new File( baseDir + File.separator + this.getFolder());
            }
            
            if (this.getFolder().endsWith("/")) {
                listFiles(file);
            } else {
                listFiles(file, "");
            }
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
    
    private boolean shouldInclude(File file) {
        if (file.getName().matches(this.getExclude()))
            return false;
        return true;
    }
    private void listFiles(File file) {
        if (file == null)
            return;
        if (!shouldInclude(file))
            return;
        String path = "/" + file.getName();
        if (file.isFile()) {
            writeOut("f " + file.length() + " " + path);
            
        } else {
            File[] f = file.listFiles();
            if (f != null) {
                for (int i = 0; i < f.length; ++i) {
                    listFiles(f[i], "");
                }
            }
        }
    }
    private void writeOut(String str) {
        try {
            //java.lang.System.out.println(str);
            output.write((str+"\n").getBytes("utf-8"));
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }
    private void listFiles(File file,String path) {
        if (file == null)
            return;
        if (!shouldInclude(file))
            return;
        path = path + "/" + file.getName();
        if (file.isFile()) {
            writeOut("f " + file.length() + " " + path);
        } else {
            writeOut("d " +path);
            File[] f = file.listFiles();
            if (f != null) {
                for (int i = 0; i < f.length; ++i) {
                    listFiles(f[i], path);
                }
            }
        }
    }
}
