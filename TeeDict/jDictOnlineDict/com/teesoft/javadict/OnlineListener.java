/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.javadict;

/**
 *
 * @author wind
 */
public interface OnlineListener {
    boolean startLoading(String file);
    boolean endLoading(String file);
    boolean onException(String file,Exception ex);
    boolean loading(String file,long loaded,long size);
}
