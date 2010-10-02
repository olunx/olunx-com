/*
 * jDictManager.java
 *
 * Created on 2007-8-23, 8:22 PM
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

import com.teesoft.ant.CreateSparseFile;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author  wind
 */
public class jDictManager extends javax.swing.JDialog
{

    /** Creates new form jDictManager */
    public jDictManager(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
        dictManager = DictManager.getInstance();
        dictManager.loadDicts();
        model = new DictTableModel(dictManager);
        tblList.setModel(model);

        tblList.setDefaultEditor(DictFactory.class, new DefaultCellEditor(getDictFormats()));
        centerScreen();
    }

    public static JComboBox getDictFormats()
    {

        JComboBox formats = new javax.swing.JComboBox();
        String[] allFormats = new String[DictManager.getDictFactoryCount()];
        for (int i = 0; i < allFormats.length; i++)
        {
            allFormats[i] = DictManager.getDictFactory(i).getFormat();
        }
        formats.setModel(new DefaultComboBoxModel(allFormats));
        return formats;
    }
    static java.util.ResourceBundle theBundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N
    static String[] columns = {theBundle.getString("Enabled"), theBundle.getString("Scan"), theBundle.getString("Name"), theBundle.getString("Path"), theBundle.getString("Type"), theBundle.getString("Encoding")};
    private DictTableModel model;

    public static class DictTableModel extends AbstractTableModel
    {

        private DictManager dictManager;

        DictTableModel(DictManager dictManager)
        {
            this.dictManager = dictManager;
        }

        public int getRowCount()
        {
            return dictManager.size();
        }

        public int getColumnCount()
        {
            return 6;
        }

        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (rowIndex >= 0 && rowIndex < dictManager.size())
            {
                Dict dict = dictManager.getDict(rowIndex);
                if (columnIndex == 0)
                {
                    return Boolean.valueOf(dict.isEnabled());
                } else if (columnIndex == 1)
                {
                    return Boolean.valueOf(dict.isScan());
                } else if (columnIndex == 2)
                {
                    return dict.getName();
                } else if (columnIndex == 3)
                {
                    try
                    {
                        if (dict.getFile() == null)
                        {
                            return "no such file";
                        }
                        return dict.getFile().getAbsolutePath();
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                } else if (columnIndex == 4)
                {
                    return dict.getFormat();
                } else if (columnIndex == 5)
                {
                    return dict.getEncoding();
                }
            }
            return "";
        }

        public String getColumnName(int column)
        {
            String retValue = columns[column];

            return retValue;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            boolean retValue = true;
            return retValue;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            String value = aValue.toString();
            if (rowIndex >= 0 && rowIndex < dictManager.size())
            {
                Dict dict = dictManager.getDict(rowIndex);
                if (columnIndex == 0)
                {
                    if (aValue instanceof Boolean)
                    {
                        dict.setEnabled(((Boolean) aValue).booleanValue());
                    } else
                    {
                        dict.setEnabled(Boolean.getBoolean(value));
                    }
                } else if (columnIndex == 1)
                {
                    if (aValue instanceof Boolean)
                    {
                        dict.setScan(((Boolean) aValue).booleanValue());
                    } else
                    {
                        dict.setScan(Boolean.getBoolean(value));
                    }
                } else if (columnIndex == 2)
                {
                    dict.setName(value);
                } else if (columnIndex == 3)
                {
                    dict.setFile(value);
                } else if (columnIndex == 4)
                {
                    dict.setFormat(value);
                } else if (columnIndex == 5)
                {
                    dict.setEncoding(value);
                }
            }

        }

        public Class getColumnClass(int columnIndex)
        {
            Class retValue = String.class;
            if (columnIndex == 0 || columnIndex == 1)
            {
                return Boolean.class;
            } else if (columnIndex == 4)
            {
                return DictFactory.class;
            }
            return retValue;
        }
    }

    public void doAddDicts(newDicts newdicts, DictManager manager)
    {
        Cursor cur = this.getCursor();
        ;
        boolean ok = newdicts.isOK();
        int copyType = newdicts.getCopyType();
        Object copyOption = null;

        showLoading();
        if (ok)
        {
            String globalDict = System.getProperty("user.dir") + File.separator + "dict";

            String userDict = FileFactory.getApplicationFolder() + File.separator + "dict";
            boolean needReload = false;
            for (int i = 0; i < manager.size(); ++i)
            {
                Dict dict = manager.getDict(i);
                //System.out.println(dict.getName());
                cur = this.getCursor();
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                if (copyType == newDicts.COPY_TO_ALL)
                {
                    dict.copyTo(globalDict, null);
                    needReload = true;
                } else if (copyType == newDicts.COPY_TO_CURRENT)
                {
                    dict.copyTo(userDict, null);
                    needReload = true;
                } else
                {
                    dictManager.addDict(dict, true);
                }
                this.setCursor(cur);
            }
            if (needReload)
            {
                dictManager.discoverDicts();
            }
            model.fireTableDataChanged();
        }

        hideLoading();
    }

    @Override
    public void setVisible(boolean b)
    {
        hideLoading();
        super.setVisible(b);
    }

    private void hideLoading()
    {
        //lblLoading.setLocation(-100, -100);
        lblLoading.setVisible(false);
        LoadingFrame.hideLoading();
    //pnlAll.setEnabled(true);
    }

    private void showLoading()
    {
        //lblLoading.setLocation((this.getWidth() -lblLoading.getWidth())/2 , (this.getHeight() -lblLoading.getHeight())/2);
        lblLoading.setVisible(true);
        LoadingFrame.showLoading();
    //pnlAll.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlAll = new javax.swing.JPanel();
        lblLoading = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnAddDict = new javax.swing.JButton();
        btnDeleteDict = new javax.swing.JButton();
        btnEnable = new javax.swing.JButton();
        btnDisable = new javax.swing.JButton();
        cmdInstallOnline = new javax.swing.JButton();
        panList = new javax.swing.JScrollPane();
        tblList = new javax.swing.JTable();
        btnPackage = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        lblLoading.setForeground(new java.awt.Color(243, 9, 29));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N
        lblLoading.setText(bundle.getString("jDictManager.lblLoading.text")); // NOI18N

        btnAddDict.setText(bundle.getString("jDictManager.btnAddDict.text")); // NOI18N
        btnAddDict.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddDictActionPerformed(evt);
            }
        });

        btnDeleteDict.setText(bundle.getString("jDictManager.btnDeleteDict.text")); // NOI18N
        btnDeleteDict.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteDictActionPerformed(evt);
            }
        });

        btnEnable.setText(bundle.getString("jDictManager.btnEnable.text")); // NOI18N
        btnEnable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnableActionPerformed(evt);
            }
        });

        btnDisable.setText(bundle.getString("jDictManager.btnDisable.text")); // NOI18N
        btnDisable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisableActionPerformed(evt);
            }
        });

        cmdInstallOnline.setText(bundle.getString("jDictManager.cmdInstallOnline.text")); // NOI18N
        cmdInstallOnline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmdInstallOnlineActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(btnAddDict, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
            .add(btnDeleteDict, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, btnEnable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, btnDisable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
            .add(cmdInstallOnline, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(btnAddDict)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmdInstallOnline)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnDeleteDict)
                .add(37, 37, 37)
                .add(btnEnable)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(btnDisable)
                .addContainerGap())
        );

        tblList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        panList.setViewportView(tblList);

        btnPackage.setText(bundle.getString("jDictManager.btnPackage.text")); // NOI18N
        btnPackage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPackageActionPerformed(evt);
            }
        });

        jButton2.setText(bundle.getString("jDictManager.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout pnlAllLayout = new org.jdesktop.layout.GroupLayout(pnlAll);
        pnlAll.setLayout(pnlAllLayout);
        pnlAllLayout.setHorizontalGroup(
            pnlAllLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlAllLayout.createSequentialGroup()
                .addContainerGap(740, Short.MAX_VALUE)
                .add(lblLoading, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(23, 23, 23))
            .add(pnlAllLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnlAllLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(pnlAllLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pnlAllLayout.createSequentialGroup()
                            .add(panList, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 713, Short.MAX_VALUE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(pnlAllLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(btnPackage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .add(jButton2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap()))
        );
        pnlAllLayout.setVerticalGroup(
            pnlAllLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlAllLayout.createSequentialGroup()
                .addContainerGap(273, Short.MAX_VALUE)
                .add(lblLoading, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 36, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(120, 120, 120)
                .add(jButton2)
                .addContainerGap())
            .add(pnlAllLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(pnlAllLayout.createSequentialGroup()
                    .addContainerGap()
                    .add(pnlAllLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, panList, 0, 0, Short.MAX_VALUE)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, pnlAllLayout.createSequentialGroup()
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(137, 137, 137)
                            .add(btnPackage)))
                    .add(55, 55, 55)))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlAll, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pnlAll, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void btnDeleteDictActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteDictActionPerformed
        int index = tblList.getSelectedRow();
        if (index >= 0)
        {
            dictManager.delete(index);
            model.fireTableDataChanged();
        }
    }//GEN-LAST:event_btnDeleteDictActionPerformed
    private JFileChooser chooser;

    private void btnAddDictActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDictActionPerformed
        if (chooser == null)
        {
            chooser = new javax.swing.JFileChooser();
        }
        chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();

        if (file == null)
        {
            return;
        }
        showNewDictDialog(file);

        
    }//GEN-LAST:event_btnAddDictActionPerformed

    private void newMethod() throws HeadlessException
    {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();
        CreateSparseFile instance = new CreateSparseFile(file.getAbsolutePath(), 1024 * 1024, 64 * 1024, "0123456789", false);
        instance.execute();
    }

    private void btnPackageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPackageActionPerformed
        new ExportDict(this, dictManager, true).setVisible(true);
    }//GEN-LAST:event_btnPackageActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dictManager.saveDicts();
        this.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btnDisableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisableActionPerformed
        int index = tblList.getSelectedRow();
        if (index >= 0)
        {
            Dict dict = dictManager.getDict(index);
            dict.setEnabled(false);
            model.fireTableCellUpdated(index, 0);
        }
        
    }//GEN-LAST:event_btnDisableActionPerformed

    private void btnEnableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnableActionPerformed
        int index = tblList.getSelectedRow();
        if (index >= 0)
        {
            Dict dict = dictManager.getDict(index);
            dict.setEnabled(true);
            model.fireTableCellUpdated(index, 0);
        }
    }//GEN-LAST:event_btnEnableActionPerformed

private void cmdInstallOnlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdInstallOnlineActionPerformed
        new JOnlineInstaller(null,true).setVisible(true);
        model.fireTableDataChanged();
}//GEN-LAST:event_cmdInstallOnlineActionPerformed

    public void centerScreen()
    {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2,
                (dim.height - abounds.height) / 2);
        requestFocus();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        new jDictManager(null, true).setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDict;
    private javax.swing.JButton btnDeleteDict;
    private javax.swing.JButton btnDisable;
    private javax.swing.JButton btnEnable;
    private javax.swing.JButton btnPackage;
    private javax.swing.JButton cmdInstallOnline;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblLoading;
    private javax.swing.JScrollPane panList;
    private javax.swing.JPanel pnlAll;
    private javax.swing.JTable tblList;
    // End of variables declaration//GEN-END:variables
    private DictManager dictManager;

    private void showNewDictDialog(final File file)
    {
        showLoading();

        doDiscoverDict(file);
        hideLoading();
    }

    private void doDiscoverDict(File file)
    {

        try
        {

            FileAccessBase fileAccess = FileFactory.openFileAccess(file.getAbsolutePath(), true);
            DictManager manager = new DictManager();
            Cursor cur = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            manager.discoverDictsRes(fileAccess);
            hideLoading();
            setCursor(cur);
            if (manager.size() > 0)
            {
                newDicts newdicts = new newDicts(jDictManager.this, true, manager);
                newdicts.setVisible(true);

            //doAddDicts(newdicts, manager);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
