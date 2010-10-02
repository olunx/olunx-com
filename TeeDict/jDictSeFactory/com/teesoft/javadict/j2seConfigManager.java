/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.javadict;

/**
 *
 * @author wind
 */
public class j2seConfigManager extends ConfigManager {
    private static j2seConfigManager j2seInstance=null;

    public static j2seConfigManager getJ2seInstance() {
        if (j2seInstance == null) {
            j2seInstance = new j2seConfigManager();
        }
        return j2seInstance;
    }
    
    public boolean getStartMinimize() {
        return getBooleanValue("StartMinimize", true);
    }

    public void setStartMinimize(boolean value) {
        setGeneralValue("StartMinimize", String.valueOf(value));
    }  
    
    public boolean getAutoSearch() {
        return getBooleanValue("AutoSearch", true);
    }

    public void setAutoSearch(boolean value) {
        setGeneralValue("AutoSearch", String.valueOf(value));
    }  
    public boolean getScan() {
        return getBooleanValue("Scan", true);
    }

    public void setScan(boolean value) {
        setGeneralValue("Scan", String.valueOf(value));
    }  
    
    public int getScanDelay() {
        return getGeneralValue("ScanDelay", 300);
    }

    public void setScanDelay(int value) {
        setGeneralValue("ScanDelay", String.valueOf(value));
    }
    public int getScanWinStickTime() {
        return getGeneralValue("ScanWinStickTime", 5000);
    }

    public void setScanWinStickTime(int value) {
        setGeneralValue("ScanWinStickTime", String.valueOf(value));
    }
    public boolean getScanWinStick() {
        return getBooleanValue("ScanWinStick", true);
    }

    public void setScanWinStick(boolean value) {
        setGeneralValue("ScanWinStick", String.valueOf(value));
    }  

    
    public int getScanKey() {
        return getGeneralValue("ScanKey", 1);
    }

    public void setScanKey(int value) {
        setGeneralValue("ScanKey", String.valueOf(value));
    }
    
    public boolean getMonitoringClipboard() {
        return getBooleanValue("MonitoringClipboard", true);
    }

    public void setMonitoringClipboard(boolean value) {
        setGeneralValue("MonitoringClipboard", String.valueOf(value));
    }  

    
}
