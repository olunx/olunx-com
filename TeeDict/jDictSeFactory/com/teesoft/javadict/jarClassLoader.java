/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.teesoft.javadict;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

/**
 *
 * @author wind
 */
public class jarClassLoader extends URLClassLoader {

    public jarClassLoader() {
        super(new URL[0], jarClassLoader.class.getClassLoader());
    }

    public void addJreJar(String jar) {
        String extPath = System.getProperty("java.ext.dirs");
        if (extPath.indexOf(":") == -1) {
            String newJarPath = extPath.substring(0, extPath.lastIndexOf(File.separator) + 1) + jar;
            //System.out.println(newJarPath);
            addJar(newJarPath);
        } else {
            String[] paths = extPath.split(":");
            for (int i = 0; i < paths.length; ++i) {
                String newJarPath = paths[i].substring(0, paths[i].lastIndexOf(File.separator) + 1) + jar;
                //System.out.println(newJarPath);
                addJar(newJarPath);
            }
        }
        String[] bootClass = System.getProperty("sun.boot.class.path").split(":");
        for (int i = 0; i < bootClass.length; ++i) {
            String newJarPath = bootClass[i].substring(0, bootClass[i].lastIndexOf(File.separator) + 1) + jar;
            //System.out.println(newJarPath);
            addJar(newJarPath);
        }

    }

    private void addJar(String newJarPath) {
        URL url = null;
        try {
            url = new URL(newJarPath);
        } catch (Exception ex) {
            try {
                url = new URL("file://" + newJarPath);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        if (url != null) {
            super.addURL(url);
        }
    }
}
