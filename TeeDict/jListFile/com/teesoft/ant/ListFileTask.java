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
import org.apache.tools.ant.Task;

/**
 *
 * @author wind
 */
public class ListFileTask extends Task {
    
    private String folder;
    private String dest;
    
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
    public void execute() throws BuildException {
        
        ListFile file = new ListFile(this.getFolder(),this.getDest(),this.getExclude(),this.overwrite,getProject().getBaseDir().getAbsolutePath());
        file.execute();
    }
    
}
