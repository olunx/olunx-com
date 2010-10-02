/*
 * NewJFrame.java
 *
 * Created on 2007-10-02, 12:32 PM
teedict , to be the best dictionary application for java me enabled devices.
Copyright (C) 2006,2007  Yong Li. All rights reserved.
 
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package com.teesoft.javadict;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Method;
import java.util.Date;
import org.jdesktop.jdic.tray.TrayIcon;

/**
 *
 * @author  wind
 */
public class scanWindow extends javax.swing.JFrame implements Runnable {
    int time=5000;
    private boolean stick=false;
    TrayIcon ti;
    private Thread displayThread;
    private int sleepStep;
    long lastTime = 0;
    private boolean running=true;
    private boolean enabledChangeDecorated=true;
    private boolean disableResize=false;

    /** Creates new form NewJFrame */
    public scanWindow(TrayIcon ti) {
        initComponents();
        this.ti = ti;
        centerScreen();
        displayThread = new Thread(this);
          
    }

    public void addMouseEventHandler(Component comp) {
        MouseMove mm = new MouseMove();
        comp.addMouseListener(mm);
        comp.addMouseMotionListener(mm);
    }
    public void centerScreen() {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2,
            (dim.height - abounds.height) / 2);
        requestFocus();
        
        addMouseEventHandler(btnStick);
        addMouseEventHandler(jPanel1);
        addMouseEventHandler(jPanel2);
        //addMouseEventHandler(jScrollPane1);
        addMouseEventHandler(lblWord);
        
    }
    public void moveIncrease(int xMove,int yMove)
    {
        setLocation( (int) this.getLocation().getX() + xMove, (int)this.getLocation().getY()+yMove);
    }
    
    public void setVisible(boolean b) {
        
        if ( b) {
            sleepStep = 200;
            time = j2seConfigManager.getJ2seInstance().getScanWinStickTime();
            setStick(j2seConfigManager.getJ2seInstance().getScanWinStick());
            if (!this.isVisible() && !disableResize)
            {
                int preferWidth = 300;
                int preferHeight = 300;
                
                Dimension tcSize =this.jToolBar1.getSize();
                Dimension pcSize =this.jPanel1.getSize();
                if (tcSize.width - pcSize.width + 20 > preferWidth)
                    preferWidth = tcSize.width - pcSize.width + 20;
                
                
                Dimension size =this.getSize();
                Dimension pSize =txtPane.getPreferredSize();
                Dimension cSize =txtPane.getSize();
                if (size.width - ( cSize.width - pSize.width) > preferWidth)
                    preferWidth = size.width - ( cSize.width - pSize.width);
                
                if (size.height - (cSize.height - pSize.height) > preferHeight)
                    preferHeight = size.height - (cSize.height - pSize.height);
                setSize(preferWidth,preferHeight );
            }
            running = true;
            if (!displayThread.isAlive())
                displayThread.start();      
        }
        else
        {
            sleepStep = 2000;
        }
        lastTime = new Date().getTime();
        super.setVisible(b);
        //if (b)
        //    this.pack();
    }
    boolean undecorated = false;

    public void setUndecorated(boolean undecorated) {
        if (this.undecorated != undecorated)
        {
            disableResize = true;
            boolean visable = this.isVisible();
            this.dispose();
            super.setUndecorated(undecorated);
            this.setVisible(visable);
            disableResize = false;
        }
                                
        this.undecorated = undecorated;
        
    }
    Point locationOnScreen()
    {
        try {

            Method lOnScreen = getClass().getMethod("getLocationOnScreen", new Class[0]);
            return (Point) lOnScreen.invoke(this, null);
        } catch (Throwable ex) {
        }
        return null;
    }
    
    public void run() {
        while(running)
        {
            try {
                Thread.sleep(sleepStep);
                if (isVisible() && !stick) {
                    Point p = this.getMousePosition(true);
                    boolean holding = (p!=null && (p.x<this.getWidth()) && (p.y < this.getHeight()) && p.x >0 && p.y >0);
                    if (p==null)
                    {
                        PointerInfo pi= MouseInfo.getPointerInfo();
                        p = pi.getLocation();
                        Point thisLocaltion = this.locationOnScreen();
                        holding = (p!=null && thisLocaltion!= null && (p.x<thisLocaltion.x+  this.getWidth()+20) 
                                && (p.y < thisLocaltion.y + this.getHeight()+20) &&  p.x >thisLocaltion.x -20 && p.y > thisLocaltion.y -20);
                    }
                    if (holding)
                    {
                        if (enabledChangeDecorated)
                            this.setUndecorated(false);
                        lastTime = new Date().getTime();
                    }
                    else if (time < new Date().getTime() - lastTime)
                    {
                        this.setVisible(false);                       
                    }
                    else
                    {
                        if (enabledChangeDecorated)
                            this.setUndecorated(true);
                    }
                }
            } catch (InterruptedException ex) {
                
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlPL = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtPane = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        lblWord = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        btnStick = new javax.swing.JToggleButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("scan window"); // NOI18N
        setUndecorated(true);

        pnlPL.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N
        txtPane.setContentType(bundle.getString("scanWindow.txtPane.contentType")); // NOI18N
        txtPane.setEditable(false);
        txtPane.setText(bundle.getString("scanWindow.txtPane.text")); // NOI18N
        jScrollPane1.setViewportView(txtPane);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jToolBar1MousePressed(evt);
            }
        });
        jToolBar1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                jToolBar1MouseDragged(evt);
            }
        });

        lblWord.setText(bundle.getString("scanWindow.lblWord.text")); // NOI18N
        jToolBar1.add(lblWord);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 435, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 23, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel1);

        jButton1.setText(bundle.getString("scanWindow.jButton1.text")); // NOI18N
        jButton1.setActionCommand(bundle.getString("scanWindow.jButton1.actionCommand")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        btnStick.setText(bundle.getString("scanWindow.btnStick.text")); // NOI18N
        btnStick.setFocusable(false);
        btnStick.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnStick.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStickActionPerformed(evt);
            }
        });
        jToolBar1.add(btnStick);

        jButton3.setText(bundle.getString("scanWindow.jButton3.text")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 621, Short.MAX_VALUE)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 29, Short.MAX_VALUE)
            .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout pnlPLLayout = new org.jdesktop.layout.GroupLayout(pnlPL);
        pnlPL.setLayout(pnlPLLayout);
        pnlPLLayout.setHorizontalGroup(
            pnlPLLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE)
        );
        pnlPLLayout.setVerticalGroup(
            pnlPLLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlPLLayout.createSequentialGroup()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlPL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlPL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void btnStickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStickActionPerformed
        stick = btnStick.isSelected();
        j2seConfigManager.getJ2seInstance().setScanWinStick(stick);
        j2seConfigManager.getJ2seInstance().saveConfig();
        //jToggleButton1.setSelected(stick);
}//GEN-LAST:event_btnStickActionPerformed

    private void jToolBar1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToolBar1MouseDragged
    
        
    }//GEN-LAST:event_jToolBar1MouseDragged

    private void jToolBar1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToolBar1MousePressed
        // TODO add your handling code here:
        //System.out.println(evt);
        
    }//GEN-LAST:event_jToolBar1MousePressed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (listener!=null)
        {
            listener.OnText(lblWord.getText());
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnStick;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblWord;
    private javax.swing.JPanel pnlPL;
    public javax.swing.JTextPane txtPane;
    // End of variables declaration//GEN-END:variables

    WordListener listener=null;

    public WordListener getListener() {
        return listener;
    }

    public void setListener(WordListener listener) {
        this.listener = listener;
    }
    
    public javax.swing.JTextPane getTxtPane()
    {
        return txtPane;
    }
    public void setWord(String word,String fullWord)
    {
        lblWord.setText(word);
    }

    public boolean isEnabledChangeDecorated() {
        return enabledChangeDecorated;
    }

    public void setEnabledChangeDecorated(boolean enabledChangeDecorated) {
        this.enabledChangeDecorated = enabledChangeDecorated;
    }

    public boolean isStick() {
        return stick;
    }

    public void setStick(boolean stick) {
        this.stick = stick;
        btnStick.setSelected(stick);
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
    
    
    public static interface WordListener
    {
        void OnText(String word);
    }

    public class MouseMove implements MouseMotionListener,MouseListener
    {
    private int xOnScreen;
    private int yOnScreen;

        public void mouseDragged(MouseEvent e) {
            moveIncrease(e.getX() - xOnScreen,e.getY() - yOnScreen);
        }

        public void mouseMoved(MouseEvent e) {
            
        }

        public void mouseClicked(MouseEvent e) {
            
        }

        public void mousePressed(MouseEvent e) {
            xOnScreen = e.getX();
            yOnScreen = e.getY();           
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
            
        }

        public void mouseExited(MouseEvent e) {
            
        }
        
    }
}
