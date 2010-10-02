/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.deploy.si;

/**
 *
 * @author wind
 */
public interface DeploySIListener {
        public void newActivation(String[] arg0);
        public Object getSingleInstanceListener();
}
