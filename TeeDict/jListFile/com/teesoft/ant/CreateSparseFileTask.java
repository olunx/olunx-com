/*
 * CreateSparseFileTask.java
 *
 * Created on 2007-8-31, 8:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.teesoft.ant;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author wind
 */
public class CreateSparseFileTask extends Task{
    
    private String folder;
    
    private long minSize=256*1024;
    
    private int sparseSize=64*1024;
    
    private String digit="0123456789";
    
    private boolean removeOrginal=true;
    
    /** Creates a new instance of CreateSparseFileTask */
    public CreateSparseFileTask() {
    }
    
    public void execute() throws BuildException {
        File file = new File(getFolder());
        if (!file.isAbsolute()) {
            file = new File( getProject().getBaseDir().getAbsolutePath() + File.separator + this.getFolder());
        }
        
        CreateSparseFile createSparse = new CreateSparseFile( file.getAbsolutePath(), getMinSize(), getSparseSize(), getDigit(), isRemoveOrginal());
        createSparse.execute();
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public long getMinSize() {
        return minSize;
    }

    public void setMinSize(long minSize) {
        this.minSize = minSize;
    }

    public int getSparseSize() {
        return sparseSize;
    }

    public void setSparseSize(int sparseSize) {
        this.sparseSize = sparseSize;
    }

    public String getDigit() {
        return digit;
    }

    public void setDigit(String digit) {
        this.digit = digit;
    }

    public boolean isRemoveOrginal() {
        return removeOrginal;
    }

    public void setRemoveOrginal(boolean removeOrginal) {
        this.removeOrginal = removeOrginal;
    }
    
}
