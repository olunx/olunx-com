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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wind
 */
public class CreateSparseFile {
    
    private String folder;
    
    private String baseDir;
    
    private long minSize;
    
    private int sparseSize;
    
    private String digit;
    
    private boolean removeOrginal;
    
    private int digitCount;
    public CreateSparseFile(String folder, long minSize,int sparseSize,String digit,boolean removeOrginal) {
        this.folder = folder;
        this.minSize = minSize;
        this.sparseSize = sparseSize;
        this.digit = digit;
        this.removeOrginal = removeOrginal;
    }
    
    public String getFolder() {
        return folder;
    }
    
    public void setFolder(String folder) {
        this.folder = folder;
    }
    
    
    java.io.FileOutputStream output = null;
        /*
    @Override
         * */
    public void execute() {
        
        try {
            java.io.File file = new java.io.File(this.getFolder());
//            if (!file.isAbsolute()) {
//                file = new File( baseDir + File.separator + this.getFolder());
//            }
            System.out.println(file.getAbsolutePath());
            handleFiles(file);
        } catch (Exception ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        } finally {
        }
    }
    
    private boolean shouldInclude(File file) {
        String name = file.getName().toLowerCase();
        return (file.isFile() && !name.endsWith(".mul") && !name.endsWith(".split") &&  file.length()>this.minSize);
    }
    private void handleFiles(File file) {
        if (file == null)
            return;
        if(file.isFile()) {
            if (shouldInclude(file)) {
                splitFile(file);
                }
        }else if (file.isDirectory()) {
            File[] f = file.listFiles();
            if (f != null) {
                for (int i = 0; i < f.length; ++i) {
                    handleFiles(f[i]);
                }
            }
        }
    }
    private boolean isSplitOf(String name,String basename)
    {
        if (name.equals(basename+".mul" ))
            return true;
        if (!( name.startsWith(basename) &&  name.endsWith(".split")))
            return false;
        String secondName = name.substring(basename.length()+1);
        int pos = secondName.indexOf(".");
        if(pos != this.digitCount)
            return false;
        String digitName = secondName.substring(0, pos);
        for(int i=0;i<pos;++i)
        {
            if( this.digit.indexOf(digitName.charAt(i))==-1)
                return false;
        }
        return true;
    }
    private void deleteOldFiles(File dir,final String filename) {
        if (dir == null)
            return;
        File [] files = dir.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return isSplitOf(name,filename);
            }});
        for(int i=0;files!=null && i<files.length;++i)
            files[i].delete();
        
    }
    
    private void splitFile(File file)   {
        long len = file.length();

        long count = len / this.sparseSize;
        digitCount = (int)(java.lang.Math.log(count+1) /java.lang.Math.log(this.digit.length())) +1;
        deleteOldFiles(file.getParentFile(),file.getName());
        byte [] buf = new byte[this.sparseSize];
        try{
            FileInputStream input = new FileInputStream(file);
            String name = file.getAbsolutePath();
            for(int i=0;i<count;++i) {
                input.read(buf);
                
                FileOutputStream output = new FileOutputStream(new File(name + "." + getDigit(i)+".split"));
                output.write(buf);
                output.close();
            }
            if (len % this.sparseSize>0) {
                int ret = input.read(buf,0,(int)( len - count * this.sparseSize));
                
                FileOutputStream output = new FileOutputStream(new File(name + "." + getDigit((int)count)+".split"));
                output.write(buf,0,ret);
                output.close();
                count ++;
            }
            input.close();
            input = null;
            String desc = "sparseft" + "01" + getString(len,10) + getString(sparseSize,8) + getString(digitCount,1) + digit;
            FileOutputStream output = new FileOutputStream(new File(name + ".mul"));
                output.write(desc.getBytes("utf-8"));
                output.close();
                
            if(this.removeOrginal)
            {
                if (!file.delete())
                {
                    String filename = file.getAbsolutePath();
                    file = null;
                    file = new File(filename);
                    file.delete();
                    //failed to delete the file
                    file.deleteOnExit();
                }
                    
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String getDigit(int num) {
        int digitLen = this.digit.length();
        int tmp =num;
        String result="";
        for(int i=0;i<digitCount;++i) {
            result =  digit.charAt(tmp % digitLen) + result;
            tmp = tmp / digitLen;
        }
        return result;
    }

    private String getString(long len, int i) {
        String str = String.valueOf(len);
        while(str.length()<i)
            str = " " + str;
        return str.substring(str.length() -i);
    }
}
