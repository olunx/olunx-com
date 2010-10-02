/*
 * JavaDict.java
 *
 * Created on 2006-9-24, 6:26 PM
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
import com.teesoft.jfile.dz.DictZipFactory;
import com.teesoft.jfile.j2seFileFactory;
import com.teesoft.jfile.sparse.SparseFactory;
import com.teesoft.screentextj.ScreenTextListener;
import com.teesoft.screentextj.ScreenTextMonitor;
import com.teesoft.util.HTMLEncode;
import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Security;
import java.util.Date;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;




/**
 *
 * @author  ly
 */
public class JavaDict extends javax.swing.JFrame implements DictManager.loadingListener, 
        SearchThread.SearchListener,HyperlinkListener,scanWindow.WordListener {

    DictManager dictManager = null;
    JTextField txt = null;
    //Locale l = java.util.Locale.getDefault();
    private boolean isReadyToShow = false;
    private String lastWord;
    private String PLAY_IMAGE_PATH;
    SystemTray tray = SystemTray.getDefaultSystemTray();
    TrayIcon ti;
    long lastTime = 0;
    j2seConfigManager configManager=j2seConfigManager.getJ2seInstance();

    private ScreenTextMonitor monitor;
    private scanWindow scanWind;
    private SingleInstanceService sis;
    private ssListerner sListener;

    private static void addToSecurityProperty(String s, String s1)
    {
        String s2 = Security.getProperty(s);
        if(s2 != null)
            s2 = s2 + "," + s1;
        else
            s2 = s1;
        Security.setProperty(s, s2);
    }
    private Object sii;
    private SearchThread thread=null;
    private int delay;
    private boolean singleInstance=true;
    
    /** Creates new form JavaDict */
    public JavaDict() {
        try {
            
            if (singleInstance)
            {
                sListener = new ssListerner(this);
                try {
                    sis = (SingleInstanceService) ServiceManager.lookup("javax.jnlp.SingleInstanceService");
                    sis.addSingleInstanceListener(sListener);
                } catch (UnavailableServiceException e) {
                    sis = null;
//                    try
//                    {
//                        String jnlpUrlString = "http://www.teesoft.com/teedict/teedict.jnlp";
//                        jarClassLoader classLoader = new jarClassLoader();
//                        classLoader.addJreJar("deploy.jar");
//
//
//                        Object SingleInstanceManager = classLoader.loadClass("com.sun.deploy.si.SingleInstanceManager").newInstance();
//
//                        Class [] paramClass = {java.lang.String.class};
//                        Method isServerRunning = SingleInstanceManager.getClass().getDeclaredMethod("isServerRunning", paramClass);
//                        Method connectToServer = SingleInstanceManager.getClass().getDeclaredMethod("connectToServer", paramClass);
//                        Object [] param = {jnlpUrlString};
//                        Object run = isServerRunning.invoke(SingleInstanceManager, param);
//                        Boolean running = (Boolean) isServerRunning.invoke(SingleInstanceManager, param);
//                        //System.out.println(run);
//                        //System.out.println(running);
//                        if(running.booleanValue())
//                            {
//                                Boolean connected = (Boolean) connectToServer.invoke(SingleInstanceManager,param);
//                                //System.out.println(connected);
//                                if(connected.booleanValue())
//                                    System.exit(0);
//                            }
//                        //sii = new com.sun.deploy.si.SingleInstanceImpl();
//                        sii = classLoader.loadClass("com.sun.deploy.si.SingleInstanceImpl").newInstance();
//                        Class [] addParaClass = {com.sun.deploy.si.DeploySIListener.class,java.lang.String.class};
//                        Method addSingleInstanceListener = sii.getClass().getDeclaredMethod("addSingleInstanceListener", addParaClass);
//                        Object [] addParam = {sListener,jnlpUrlString};
//                        addSingleInstanceListener.invoke(sii,addParam);
//                    }catch(Throwable t)
//                    {
//                        t.printStackTrace();
//
//                    }
                }
            }
            initComponents();
            loadSystray();
            jmnuScanWin.setVisible(false);

//            showURL("http://localhost");
            
            txtExplains.addHyperlinkListener(this);

            initIndex();

            initComboBox();
            lastWord = null;

            URL teesoft = this.getClass().getClassLoader().getResource("com/teesoft/javadict/resources/images/teesoft.png");
            //System.out.println(teesoft);
            Image img = ImageIO.read(teesoft);
            setIconImage(img);
            centerScreen();
            PLAY_IMAGE_PATH = this.getClass().getClassLoader().getResource("com/teesoft/javadict/resources/images/play.jpeg").toString();
            //System.out.println(PLAY_IMAGE_PATH);

            scanWind = new scanWindow (ti);
            scanWind.getTxtPane().addHyperlinkListener(this);
            
            monitor =  ScreenTextMonitor.getInstance();
            listener = new Listener();          
            lastTime = new Date().getTime();
            renewScan();
            
            new Thread(new Runnable() {

                public void run() {
                    try {
                        Thread.sleep(1000);
                        dictManager.search("a", 1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }).start();
            //jmnuSetting.setVisible(false);
            
            this.setVisible(!configManager.getStartMinimize());
            delay = configManager.getDelay();
    } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void showExplains(DictItem item,javax.swing.JTextPane textPane) {

        boolean hasSound = SoundFactory.hasWord(item.getString());
        if (configManager.getPlaySoundDirectly()) {
            InputStream sound = SoundFactory.getSound(item.getString());

            if (sound != null) {
                new soundPlayer(sound).start();
            }
        }
        String soundStr = "";
        if (hasSound) {
            soundStr = "&nbsp;&nbsp;<a href=\"play://" + item.getString() + "\"><img src=\"" + PLAY_IMAGE_PATH + "\"></a>";
        }
        //this.txtExplains.setText(String.valueOf(lstWords.getSelectedIndex()));
        String word = item.getString();
        String exp = "<html><body><h2><a href=\"word://" + word + "\">" + word + "</a>"  + soundStr + "</h2><br>";
        exp +="<b>" + item.getDict().getName() + "</b>\n<br>" + item.getString()  + "\n<br>" + item.getHtmlExplains().getString() + "\n<br>";

        for (int i = 1; i < item.getSubItemCount(); i++) {
            DictItem it = item.getSubItem(i);
            exp += "<br><b>" + it.getDict().getName() + "</b>\n<br>" + it.getString() + "\n<br>" + it.getHtmlExplains().getString() + "\n<br>";
            for (int j = 1; j < it.getSubItemCount(); j++) {
                exp += "<br><b>" + it.getSubItem(i).getDict().getName() + "</b>\n<br>" + it.getSubItem(i).getString() + "\n<br>" + it.getSubItem(i).getHtmlExplains().getString() + "\n<br>";
            }
        }
        exp += "</body></html>";



        //System.out.println(exp);

        textPane.setText(exp);

        textPane.select(0, 0);
    }

    private String formatHtml(String str, boolean html) {
        if (html) {
            return str;
        }
        String ret = HTMLEncode.encode(str);
        //System.out.println(ret);
        return ret;
    }

    private void initComboBox() {

        txt = (JTextField) cboText.getEditor().getEditorComponent();

        txt.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {

                handleTextChanged(e);
            }

            public void removeUpdate(DocumentEvent e) {

                handleTextChanged(e);
            }

            public void changedUpdate(DocumentEvent e) {

                handleTextChanged(e);
            }

            public void handleTextChanged(DocumentEvent e) {

                if (txt.getText().length() > 0 && configManager.getAutoSearch()) {
                    submitSearch(txt.getText());
                }
            }
        });
    }

    public void initIndex() {

        try {

            dictManager = DictManager.getInstance();

            dictManager.setMaxCount(128);

            dictManager.loadDicts(this);

            dictManager.saveDicts();
        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jpopMenu = new javax.swing.JPopupMenu();
        jmnuSetting = new javax.swing.JMenuItem();
        jmnuDictManager = new javax.swing.JMenuItem();
        jmnuAddOnline = new javax.swing.JMenuItem();
        jmnuFile = new javax.swing.JMenu();
        jmnuSplitFile = new javax.swing.JMenuItem();
        jmnuCombineFile = new javax.swing.JMenuItem();
        jmnuUndz = new javax.swing.JMenuItem();
        jmnuScan = new javax.swing.JCheckBoxMenuItem();
        jmnuClipboard = new javax.swing.JCheckBoxMenuItem();
        jmnuKeyCode = new javax.swing.JMenuItem();
        jmnuScanWin = new javax.swing.JMenuItem();
        jmnuExportSound = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jmnuExit = new javax.swing.JMenuItem();
        btnClear = new javax.swing.JButton();
        cboText = new javax.swing.JComboBox();
        btnBack = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnWildSearch = new javax.swing.JButton();
        btnOption = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        tabWordList = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstWords = new javax.swing.JList();
        lstWords.setModel(listModel);
        jScrollPane3 = new javax.swing.JScrollPane();
        lstMisc = new javax.swing.JList();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtExplains = new javax.swing.JTextPane();
        jToolBar1 = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnDictManager = new javax.swing.JButton();
        btnOptions = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N
        jpopMenu.setToolTipText(bundle.getString("JavaDict.jpopMenu.toolTipText")); // NOI18N
        jpopMenu.setLabel(bundle.getString("JavaDict.jpopMenu.label")); // NOI18N

        jmnuSetting.setText(bundle.getString("JavaDict.jmnuSetting.text")); // NOI18N
        jmnuSetting.setToolTipText(bundle.getString("JavaDict.jmnuSetting.toolTipText")); // NOI18N
        jmnuSetting.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuSettingActionPerformed(evt);
            }
        });
        jpopMenu.add(jmnuSetting);

        jmnuDictManager.setText(bundle.getString("JavaDict.jmnuDictManager.text")); // NOI18N
        jmnuDictManager.setToolTipText(bundle.getString("JavaDict.jmnuDictManager.toolTipText")); // NOI18N
        jmnuDictManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuDictManagerActionPerformed(evt);
            }
        });
        jpopMenu.add(jmnuDictManager);

        jmnuAddOnline.setText(bundle.getString("JavaDict.jmnuAddOnline.text")); // NOI18N
        jmnuAddOnline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuAddOnlineActionPerformed(evt);
            }
        });
        jpopMenu.add(jmnuAddOnline);

        jmnuFile.setText(bundle.getString("JavaDict.jmnuFile.text")); // NOI18N
        jmnuFile.setToolTipText(bundle.getString("JavaDict.jmnuFile.toolTipText")); // NOI18N

        jmnuSplitFile.setText(bundle.getString("JavaDict.jmnuSplitFile.text")); // NOI18N
        jmnuSplitFile.setToolTipText(bundle.getString("JavaDict.jmnuSplitFile.toolTipText")); // NOI18N
        jmnuSplitFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuSplitFileActionPerformed(evt);
            }
        });
        jmnuFile.add(jmnuSplitFile);

        jmnuCombineFile.setText(bundle.getString("JavaDict.jmnuCombineFile.text")); // NOI18N
        jmnuCombineFile.setToolTipText(bundle.getString("JavaDict.jmnuCombineFile.toolTipText")); // NOI18N
        jmnuCombineFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuCombineFileActionPerformed(evt);
            }
        });
        jmnuFile.add(jmnuCombineFile);

        jmnuUndz.setText(bundle.getString("JavaDict.jmnuUndz.text")); // NOI18N
        jmnuUndz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuUndzActionPerformed(evt);
            }
        });
        jmnuFile.add(jmnuUndz);

        jpopMenu.add(jmnuFile);

        jmnuScan.setSelected(true);
        jmnuScan.setText(bundle.getString("JavaDict.jmnuScan.text")); // NOI18N
        jmnuScan.setToolTipText(bundle.getString("JavaDict.jmnuScan.toolTipText")); // NOI18N
        jmnuScan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jmnuScanItemStateChanged(evt);
            }
        });
        jpopMenu.add(jmnuScan);

        jmnuClipboard.setSelected(true);
        jmnuClipboard.setText(bundle.getString("JavaDict.jmnuClipboard.text")); // NOI18N
        jmnuClipboard.setToolTipText(bundle.getString("JavaDict.jmnuClipboard.toolTipText")); // NOI18N
        jmnuClipboard.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jmnuClipboardItemStateChanged(evt);
            }
        });
        jpopMenu.add(jmnuClipboard);

        jmnuKeyCode.setText(bundle.getString("JavaDict.jmnuKeyCode.text")); // NOI18N
        jmnuKeyCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuKeyCodeActionPerformed(evt);
            }
        });
        jpopMenu.add(jmnuKeyCode);

        jmnuScanWin.setText(bundle.getString("JavaDict.jmnuScanWin.text")); // NOI18N
        jmnuScanWin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuScanWinActionPerformed(evt);
            }
        });
        jpopMenu.add(jmnuScanWin);

        jmnuExportSound.setText(bundle.getString("JavaDict.jmnuExportSound.text")); // NOI18N
        jmnuExportSound.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuExportSoundActionPerformed(evt);
            }
        });
        jpopMenu.add(jmnuExportSound);
        jpopMenu.add(jSeparator1);

        jmnuExit.setText(bundle.getString("JavaDict.jmnuExit.text")); // NOI18N
        jmnuExit.setToolTipText(bundle.getString("JavaDict.jmnuExit.toolTipText")); // NOI18N
        jmnuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jmnuExitActionPerformed(evt);
            }
        });
        jpopMenu.add(jmnuExit);

        setTitle(bundle.getString("JavaDict.Form.title")); // NOI18N

        btnClear.setText(bundle.getString("JavaDict.btnClear.text")); // NOI18N
        btnClear.setName("btnClear"); // NOI18N
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        cboText.setEditable(true);
        cboText.setName("cboText"); // NOI18N
        cboText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTextActionPerformed(evt);
            }
        });

        btnBack.setText(bundle.getString("JavaDict.btnBack.text")); // NOI18N
        btnBack.setEnabled(false);
        btnBack.setName("btnBack"); // NOI18N

        btnPrevious.setText(bundle.getString("JavaDict.btnPrevious.text")); // NOI18N
        btnPrevious.setEnabled(false);
        btnPrevious.setName("btnPrevious"); // NOI18N

        btnNext.setText(bundle.getString("JavaDict.btnNext.text")); // NOI18N
        btnNext.setEnabled(false);
        btnNext.setName("btnNext"); // NOI18N

        btnWildSearch.setText(bundle.getString("JavaDict.btnWildSearch.text")); // NOI18N
        btnWildSearch.setEnabled(false);
        btnWildSearch.setName("btnWildSearch"); // NOI18N

        btnOption.setText(bundle.getString("JavaDict.btnOption.text")); // NOI18N
        btnOption.setName("btnOption"); // NOI18N
        btnOption.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionActionPerformed(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N

        jSplitPane1.setDividerLocation(180);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        tabWordList.setName("tabWordList"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        lstWords.setName("lstWords"); // NOI18N
        lstWords.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                lstWordsValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(lstWords);

        tabWordList.addTab(bundle.getString("JavaDict.jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        lstMisc.setName("lstMisc"); // NOI18N
        jScrollPane3.setViewportView(lstMisc);

        tabWordList.addTab(bundle.getString("JavaDict.jScrollPane3.TabConstraints.tabTitle"), jScrollPane3); // NOI18N

        jSplitPane1.setLeftComponent(tabWordList);

        txtExplains.setContentType(bundle.getString("JavaDict.txtExplains.contentType")); // NOI18N
        txtExplains.setEditable(false);
        jScrollPane4.setViewportView(txtExplains);

        jSplitPane1.setRightComponent(jScrollPane4);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText(bundle.getString("JavaDict.jLabel1.text")); // NOI18N
        jLabel1.setFocusable(false);
        jLabel1.setMaximumSize(new java.awt.Dimension(1500, 15));
        jToolBar1.add(jLabel1);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 745, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 24, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel2);

        btnDictManager.setText(bundle.getString("JavaDict.btnDictManager.text")); // NOI18N
        btnDictManager.setToolTipText(bundle.getString("JavaDict.btnDictManager.toolTipText")); // NOI18N
        btnDictManager.setBorderPainted(false);
        btnDictManager.setContentAreaFilled(false);
        btnDictManager.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDictManagerActionPerformed(evt);
            }
        });
        jToolBar1.add(btnDictManager);

        btnOptions.setText(bundle.getString("JavaDict.btnOptions.text")); // NOI18N
        btnOptions.setToolTipText(bundle.getString("JavaDict.btnOptions.toolTipText")); // NOI18N
        btnOptions.setBorderPainted(false);
        btnOptions.setContentAreaFilled(false);
        btnOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOptionsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnOptions);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 836, Short.MAX_VALUE)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 836, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jButton1.setText(bundle.getString("JavaDict.jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(btnClear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cboText, 0, 531, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnWildSearch)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnBack)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnPrevious)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnNext)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnOption)
                .addContainerGap())
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnBack)
                    .add(btnPrevious)
                    .add(btnNext)
                    .add(btnWildSearch)
                    .add(btnOption)
                    .add(jButton1)
                    .add(cboText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(btnClear))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDictManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDictManagerActionPerformed
        jDictManager managerUI = new jDictManager(this, true);
        managerUI.setVisible(true);
    }//GEN-LAST:event_btnDictManagerActionPerformed

    private void lstWordsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstWordsValueChanged

        if (evt.getValueIsAdjusting()) {

            return;
        }

        if (isReadyToShow && lstWords.getSelectedIndex() >= 0) {
            String word = lstWords.getModel().getElementAt(lstWords.getSelectedIndex()).toString();

            if (word.equals(lastWord)) {
                return;
            }
            lastWord = word;
            //System.out.println(lstWords.getSelectedIndex());

            int selected = lstWords.getSelectedIndex();

            DictItem item = list.getItem(selected);

            showExplains(item,this.txtExplains);

            if (selected == 0 || selected == lstWords.getModel().getSize() - 1) {
                appendSearch(item.getString(),true);
            }
        }
    }//GEN-LAST:event_lstWordsValueChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        submitSearch(txt.getText());
    }//GEN-LAST:event_jButton1ActionPerformed
    private ItemList list = null;
    private Vector history = new Vector();

    private boolean addHistory(String value) {
        for (int i = 0; i < history.size(); ++i) {
            String s = history.elementAt(i).toString();
            if (s.startsWith(value)) {
                return false;
            } else {
                if (value.startsWith(s)) {
                    history.setElementAt(value, i);
                    try{
                        cboText.setModel(new DefaultComboBoxModel(history));
                    }catch(Throwable ex)
                    {
                        
                    }
                    return true;
                }
            }
        }
        history.insertElementAt(value, 0);
        cboText.setModel(new DefaultComboBoxModel(history));
        return true;
    }

    private void renewScan() {
        monitor.setOptions(configManager.getScanKey(), configManager.getScanDelay());
            
        if (configManager.getScan() || configManager.getMonitoringClipboard()) {
            monitor.RegisterListener(listener);
        } else {
            monitor.UnRegisterListener(listener);
        }
        this.jmnuClipboard.setSelected( configManager.getMonitoringClipboard());
        this.jmnuScan.setSelected( configManager.getScan());        
    }

    private void showURL(String url) {
        try {
            showURL(new URL(url));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    private void submitSearch(String word) {
        appendSearch(word,false);
        if (!this.isVisible())
        {
            this.setVisible(true);
            this.requestFocus();
        }
    }

public SearchThread getSearchThread() {
        
        if (thread == null) {
            
            thread = new SearchThread(dictManager,this);
                                    
        }
        
        return thread;
        
    }
    
    private synchronized void appendSearch(String str, boolean force) {
        
        if (str.length() == 0) {
            
            return;
            
        }
        addHistory(str);
        
        if (!getSearchThread().isBSearching()) {
            //System.out.println("Dosearch " + str);
            doSearch(str, force);
            
        } else {
            synchronized (getSearchThread()) {
                //System.out.println("wait " + str);
                getSearchThread().setNextSearch(str);

                getSearchThread().setForce(force);
            }
        }
        
    }
    
    private void doSearch(final String word, final boolean force) throws IllegalArgumentException {
        
        synchronized (getSearchThread()) {
            
            getSearchThread().setWord(word);
            
            getSearchThread().setForce(force);
            
            getSearchThread().notifyAll();
            
        }
        
    }
    

    private void cboTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTextActionPerformed
    }//GEN-LAST:event_cboTextActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        JTextField txt = (JTextField) cboText.getEditor().getEditorComponent();
        txt.setText("");
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptionsActionPerformed
        if (this.getWidth() + jpopMenu.getWidth() > getToolkit().getScreenSize().getWidth())
            this.jpopMenu.show(btnOptions, -1 * jpopMenu.getWidth() ,-1 * jpopMenu.getHeight());
        else
            this.jpopMenu.show(btnOptions, btnOptions.getWidth(),0);
    }//GEN-LAST:event_btnOptionsActionPerformed

    private void btnOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOptionActionPerformed
        if (this.getWidth() + jpopMenu.getWidth() > getToolkit().getScreenSize().getWidth())
            this.jpopMenu.show(btnOption, -1 * jpopMenu.getWidth() ,-1 * jpopMenu.getHeight());
        else
            this.jpopMenu.show(btnOption, btnOptions.getWidth(),0);
    }//GEN-LAST:event_btnOptionActionPerformed

    private void jmnuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuExitActionPerformed
        
       monitor.UnRegisterListener(listener);
        if (sis!=null)
            sis.removeSingleInstanceListener(sListener);
       
       if (sii != null)
       {
           try{
           Class [] removeParaClass = {com.sun.deploy.si.DeploySIListener.class};
           Method removeSingleInstanceListener = sii.getClass().getDeclaredMethod("removeSingleInstanceListener", removeParaClass);
           Object [] removeParam = {sListener};
                    
           removeSingleInstanceListener.invoke(sii,removeParam);
           }catch(Throwable t)
           {
               t.printStackTrace();
           }
       }
         System.exit(0);
    }//GEN-LAST:event_jmnuExitActionPerformed

    private void jmnuSettingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuSettingActionPerformed
        new settingDialog(this,true,configManager);
        renewScan();
        delay = configManager.getDelay();
    }//GEN-LAST:event_jmnuSettingActionPerformed

    private void jmnuDictManagerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuDictManagerActionPerformed
        jDictManager managerUI = new jDictManager(this, true);
        managerUI.setVisible(true);
    }//GEN-LAST:event_jmnuDictManagerActionPerformed

    private void jmnuScanItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jmnuScanItemStateChanged
        configManager.setScan(jmnuScan.isSelected());
        configManager.saveConfig();
        renewScan();
        ////System.out.println(evt);
    }//GEN-LAST:event_jmnuScanItemStateChanged

    private void jmnuClipboardItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jmnuClipboardItemStateChanged
        configManager.setMonitoringClipboard(jmnuClipboard.isSelected());
        configManager.saveConfig();
        renewScan();
        ////System.out.println(evt);
    }//GEN-LAST:event_jmnuClipboardItemStateChanged
    private JFileChooser chooser=null;
    private JFileChooser getChooser()
    {
        if (chooser == null)
            chooser = new JFileChooser();
        return chooser;
    }
    
    private void jmnuSplitFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuSplitFileActionPerformed
        if (getChooser().showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = getChooser().getSelectedFile();
            java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N
            
            String split = javax.swing.JOptionPane.showInputDialog(this, bundle.getString("valumnPrompt"), "32");
            
            try{
                int splitSize = Integer.parseInt(split)*1024;
                CreateSparseFile instance = new CreateSparseFile(file.getAbsolutePath(),1024,splitSize,"0123456789",false);
                instance.execute();
                javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("SplitDone"),bundle.getString("SplitDone") , javax.swing.JOptionPane.INFORMATION_MESSAGE);
                try {
                    java.awt.Desktop.getDesktop().open(file.getParentFile());
                } catch (Throwable ex) {

                }
                
            }catch (NumberFormatException ex)
            {
                javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("InputValidNumber"), bundle.getString("NumberError"), javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            
        }
    }//GEN-LAST:event_jmnuSplitFileActionPerformed

    public String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    private JFileChooser splitChooser = null;

    private JFileChooser getSplitChooser() {
        if (splitChooser == null) {
            splitChooser = new JFileChooser();
            splitChooser.setAcceptAllFileFilterUsed(false);
            splitChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {


                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }


                    String extension = getExtension(f);
                    if (extension != null) {
                        if (extension.equals("mul")) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    return false;
                }

                public String getDescription() {
                    return "splited file";
                }
            });
        }
        return splitChooser;
    }

    
    private void jmnuCombineFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuCombineFileActionPerformed
        if (getSplitChooser().showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = getSplitChooser().getSelectedFile();
            if (file.getName().toLowerCase().endsWith(".mul"))
            {
                java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N

                String path = file.getAbsolutePath();
                path = path.substring(0,path.length()-4);
                File newFile = new File(path);
                getChooser().setSelectedFile(newFile);
                if (getChooser().showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
                {
                    newFile = getChooser().getSelectedFile();
                    if (newFile.exists())
                    {
                        javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("FileExistError"), bundle.getString("CombinError"), javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try
                    {
                        FileAccessBase combinedFile =  j2seFileFactory.getInstance().newFileAccess(newFile.getAbsolutePath());
                        FileAccessBase splitedFile =  SparseFactory.getInstance().newFileAccess(path);
                        combinedFile.create();
                        splitedFile.copyTo(combinedFile, null);
                        javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("CombinDone"),bundle.getString("CombinDone") , javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        
                        try{
                            java.awt.Desktop.getDesktop().open(newFile.getParentFile());
                        }catch (Throwable ex) {

                        }

                    }catch(Exception ex)
                    {
                        ex.printStackTrace();
                        javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("CombinError"), bundle.getString("CombinError"), javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                    
                    
                }
            }
        }

    }//GEN-LAST:event_jmnuCombineFileActionPerformed

    private void jmnuUndzActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuUndzActionPerformed
        if (getDZChooser().showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File file = getDZChooser().getSelectedFile();
            if (file.getName().toLowerCase().endsWith(".dz"))
            {
                java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N

                String path = file.getAbsolutePath();
                path = path.substring(0,path.length()-3);

                File newFile = new File(path);
                getChooser().setSelectedFile(newFile);
                if (getChooser().showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
                {
                    newFile = getChooser().getSelectedFile();
                    if (newFile.exists())
                    {
                        javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("FileExistError"), bundle.getString("UndzError"), javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try
                    {
                        FileAccessBase combinedFile =  j2seFileFactory.getInstance().newFileAccess(newFile.getAbsolutePath());
                        FileAccessBase splitedFile =  DictZipFactory.getInstance().newFileAccess(path);
                        combinedFile.create();
                        splitedFile.copyTo(combinedFile, combinedFile);
                        javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("UndzDone"),bundle.getString("UndzDone") , javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        try{
                            java.awt.Desktop.getDesktop().open(newFile.getParentFile());
                        }catch (Throwable ex) {

                        }
                        
                    }catch(Exception ex)
                    {
                        ex.printStackTrace();
                        javax.swing.JOptionPane.showMessageDialog(this, bundle.getString("UndzError"), bundle.getString("UndzError"), javax.swing.JOptionPane.ERROR_MESSAGE);
                    }
                    
                    
                }
            }
        }

    }//GEN-LAST:event_jmnuUndzActionPerformed

    private void jmnuScanWinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuScanWinActionPerformed
        scanWind.setVisible(true);
    }//GEN-LAST:event_jmnuScanWinActionPerformed

    private void jmnuKeyCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuKeyCodeActionPerformed
        new KeyCodes(this,false).setVisible(true);
    }//GEN-LAST:event_jmnuKeyCodeActionPerformed

    private File getFolder()
    {
        getChooser().showSaveDialog(this);
        File file = getChooser().getSelectedFile();
        return file;
    }
    private void jmnuExportSoundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuExportSoundActionPerformed
        exportSound.exportAllSound(getFolder(), "wav");
    }//GEN-LAST:event_jmnuExportSoundActionPerformed

private void jmnuAddOnlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jmnuAddOnlineActionPerformed
    new JOnlineInstaller(this,true).setVisible(true);
}//GEN-LAST:event_jmnuAddOnlineActionPerformed
    private JFileChooser dzChooser = null;

    private JFileChooser getDZChooser() {
        if (dzChooser == null) {
            dzChooser = new JFileChooser();
            dzChooser.setAcceptAllFileFilterUsed(false);
            dzChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {


                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }


                    String extension = getExtension(f);
                    if (extension != null) {
                        if (extension.equals("dz")) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    return false;
                }

                public String getDescription() {
                    return "dz file";
                }
            });
        }
        return dzChooser;
    }


    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        new JavaDict();
       
        boolean modelBug= true;
        if (modelBug)
        {
        //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6497929
Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
	public void eventDispatched(AWTEvent event) {
		if (event.getID() == WindowEvent.WINDOW_CLOSED) {
			Object source = event.getSource();
			if (source instanceof Dialog) {
				Dialog dialog = (Dialog) source;
				try {
					Field f = java.awt.Dialog.class
							.getDeclaredField("modalDialogs");
					f.setAccessible(true);
					Vector modalDialogs = (Vector) f.get(dialog);
					if (modalDialogs != null) {
						while (modalDialogs.contains(dialog)) {
							modalDialogs.remove(dialog);
						}
					}
				} catch (Exception e1) {
					//System.out.println("Error fixing memory leak in Dialog!");
					e1.printStackTrace();
				}
			}
		}
	}
}, AWTEvent.WINDOW_EVENT_MASK);        
        }
    }

    public boolean NotifyStartingLoadDict(String dictName, boolean canceled) {

        return canceled;
    }

    public void NotifyEndingLoadDict(String dictName, boolean ignored) {
    }

    public void NotifyAllDone() {
    }

    public boolean NotifyNewDict(String dictName, boolean disabled) {
        return disabled;
    }

    public void centerScreen() {
        Dimension dim = getToolkit().getScreenSize();
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
        requestFocus();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnDictManager;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOption;
    private javax.swing.JButton btnOptions;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnWildSearch;
    private javax.swing.JComboBox cboText;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem jmnuAddOnline;
    private javax.swing.JCheckBoxMenuItem jmnuClipboard;
    private javax.swing.JMenuItem jmnuCombineFile;
    private javax.swing.JMenuItem jmnuDictManager;
    private javax.swing.JMenuItem jmnuExit;
    private javax.swing.JMenuItem jmnuExportSound;
    private javax.swing.JMenu jmnuFile;
    private javax.swing.JMenuItem jmnuKeyCode;
    private javax.swing.JCheckBoxMenuItem jmnuScan;
    private javax.swing.JMenuItem jmnuScanWin;
    private javax.swing.JMenuItem jmnuSetting;
    private javax.swing.JMenuItem jmnuSplitFile;
    private javax.swing.JMenuItem jmnuUndz;
    private javax.swing.JPopupMenu jpopMenu;
    private javax.swing.JList lstMisc;
    private javax.swing.JList lstWords;
    private javax.swing.JTabbedPane tabWordList;
    private javax.swing.JTextPane txtExplains;
    // End of variables declaration//GEN-END:variables

    public void hyperlinkUpdate(HyperlinkEvent e) {
        //System.out.println(e.getEventType());
        //System.out.println(e.getURL());
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String url = e.getDescription();
            //System.out.println(url);
            if (url.startsWith("play://")) {
                try {
                    String word = url.substring("play://".length());
                    InputStream sound = SoundFactory.getSound(word);

                    if (sound != null) {
                        new soundPlayer(sound).start();
                    } else {
                        sound.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            else if (url.startsWith("bword://")) {
                    String word = url.substring("bword://".length());
                    submitSearch(word);
            }
            else if (url.startsWith("word://")) {
                    String word = url.substring("word://".length());
                    submitSearch(word);
            }
 
        }
    }
    public void loadSystray()
    {
        // ImageIcon i = new ImageIcon("duke.gif");
        ImageIcon img = new ImageIcon(this.getClass().getResource("/com/teesoft/systray/images/tray.gif"));

        ti = new TrayIcon(img, "jStarDict, the best java dict application.",this.jpopMenu);
        
        ti.setIconAutoSize(true);
        ti.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	setVisible(!isVisible());
                requestFocus();
            }
        });
        ti.addBalloonActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setVisible(!isVisible());
                requestFocus();
                //JOptionPane.showMessageDialog(null, "Balloon Message been clicked - TrayIcon", "Message", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        tray.addTrayIcon(ti);        
    }


    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getNewState() == Frame.ICONIFIED || e.paramString().startsWith("WINDOW_ICONIFIED"))
        {
            this.setVisible(false);
        }
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b && this.getState() == Frame.ICONIFIED)
            this.setState(Frame.NORMAL);
    }
    private static Listener listener;
    public class Listener implements ScreenTextListener
    {

        public boolean OnText( String text, final int providerID, final int nXStart, final int nYStart, final int BeginPos) {
            if (ScreenTextMonitor.prvDummy == providerID)
                return false;
            final String theText = text;
            try{
                new Thread(new Runnable(){
                    public void run() {
                        try {
                            DoOnText(theText, providerID, nXStart, nYStart, BeginPos);
                            if (!scanWind.isVisible())
                                scanWind.setVisible(true);
                            
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }catch(Throwable e)
            {
                
            }
            return true;
        }
        boolean searching=false;
        public synchronized boolean DoOnText(String text, int providerID, int nXStart, int nYStart, int BeginPos) {
            //System.out.println(text);
            if (!configManager.getScan() && providerID == ScreenTextMonitor.prvMousePickup)
            {
                return false;
            }
            if (!configManager.getMonitoringClipboard() && providerID == ScreenTextMonitor.prvClipboardMonitor)
            {
                return false;
            }
            if(searching && new Date().getTime() - lastTime < 10000)
                return false;
            lastTime = new Date().getTime();
            searching = true;
            
            String word = getWord(text,BeginPos);
            
            if (word.length() == 0) {
                return false;
            }
            scanWind.setWord(word, text);
            ItemList list = dictManager.searchScanWord(word,2);
            if (list!=null && list.size() >0 && list.getItem(list.getSelected()).getString().equalsIgnoreCase(word))
            {
                DictItem item = list.getItem(list.getSelected());
                showExplains(item,scanWind.getTxtPane());
            }
            else
            {
                java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/teesoft/javadict/resources/JavaDict"); // NOI18N
                String exp="<html><body>" ;
                exp += "<b>" + bundle.getString("UnableToGetWord") +":<a href=\"word://" + com.teesoft.util.HTMLEncode.escape(word) + "\">" 
                        + com.teesoft.util.HTMLEncode.escape(word) + "</a></b><br>";
                exp += "<b>" + bundle.getString("PleaseTry") +":</b><br>";
                text = text.replaceAll("[\t]", " ");
                text = text.replaceAll("[\n]", " ");
                text = text.replaceAll("[.]", " ");
                
                String [] words = text.split(" ");
                if (words==null || words.length==0)
                {
                     exp += "<b><a href=\"word://" + com.teesoft.util.HTMLEncode.escape(text) + "\">" 
                            + com.teesoft.util.HTMLEncode.escape(text) + "</a></b>";
                }
                else
                {
                    for(int i=0;i<words.length;++i)
                    {
                        if (words[i]!=null && words[i].length()>0)
                        {
                            exp += "<b><a href=\"word://" + com.teesoft.util.HTMLEncode.escape(words[i]) + "\">" 
                                    + com.teesoft.util.HTMLEncode.escape(words[i]) + "</a></b>";
                            if (i!=words.length-1)
                                exp += ",";
                        }
                    }
                }
                exp +="</body></html>";
                //System.out.println(exp);
                scanWind.getTxtPane().setText(exp);
            }
            scanWind.setAlwaysOnTop(true);
//            if (!scanWind.isVisible())
//                scanWind.setVisible(true);
//            scanWind.requestFocus();
            searching = false;
            return true;
        }
    String delimiters = "\t\n\f\r !\"#$%&'()*+,-./0123456789:;<=>?@[\\]^_`{|}~";
    private boolean isDelimiters(char c) {
        for (int i = 0; i < delimiters.length(); ++i) {
            ////System.out.println(delimiters.charAt(i));
            if (c == delimiters.charAt(i)) {
                return true;
            }
            if (c < delimiters.charAt(i)) {
                return false;
            }
        }
        return false;
    }

        private String getWord(String text, int BeginPos) {
            int selectStart=BeginPos;
            if (selectStart>0)
                selectStart--;
            char [] content = text.toCharArray();
            if (isDelimiters(content[selectStart]))
            {
                if(selectStart>0)
                    selectStart--;
                else
                    selectStart++;
            }
                
            while (selectStart >= 1 && !isDelimiters(content[selectStart - 1])) {
                selectStart--;
            }
                int selectEnd=BeginPos;
            while (selectEnd < content.length - 1 && !isDelimiters(content[selectEnd + 1])) {
                selectEnd++;
                ////System.out.println("selectStart " + selectStart + " selectEnd " + selectEnd );
            }
            
            return text.substring(selectStart,selectEnd+1);
        }
        
    }
    public void dispose() {
   }

    public void OnText(String word) {
        this.submitSearch(word);
    }

    public static class ssListerner implements SingleInstanceListener,com.sun.deploy.si.DeploySIListener
    {
        private JavaDict dict;
        ssListerner(JavaDict dict)
        {
            this.dict = dict;
        }
        public void newActivation(String[] arg0) {
            dict.setVisible(true);
        }

        public Object getSingleInstanceListener() {
            return this;
        }
    }
// Method to show a URL
   boolean showURL(URL url) {
       try {
           // Lookup the javax.jnlp.BasicService object
           BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
           // Invoke the showDocument method
           return bs.showDocument(url);
       } catch(UnavailableServiceException ue) {
           // Service is not supported
           return false;
       }
    }

    public boolean onStartSearch(String word) {
        //throw new UnsupportedOperationException("Not supported yet.");
        return true;
    }

    public class wordListModel extends DefaultListModel
    {
            public int getSize() {

                if (list == null) {

                    return 0;
                }
                return list.size();
            }

            public Object getElementAt(int index) {

                if (index > -1 && index < getSize()) {

                    return list.getItem(index);
                }
                return null;
            }

        protected void fireContentsChanged(Object source, int index0, int index1) {
            super.fireContentsChanged(source, index0, index1);
            
        }
        public void fireContentsChanged(Object source) {
            fireContentsChanged(source,0,getSize()-1);
        }

        protected void fireIntervalRemoved(Object source, int index0, int index1) {
            super.fireIntervalRemoved(source, index0, index1);
        }
        
    }
    wordListModel listModel = new wordListModel();
    
    public void onSearchResult(String word, ItemList searchResult) {
     if (searchResult == null) {
            return;
        }
        int oldSize = lstWords.getModel().getSize();
        list = searchResult;//dictManager.search(word,20);

        isReadyToShow = false;

        if (lstWords.getModel().getSize()<oldSize)
        {
            listModel.fireIntervalRemoved(this,lstWords.getModel().getSize() , oldSize-1);
        }
        int selectedNum = list.getSelected();

        if (selectedNum < 0) {
            selectedNum = 0;
        }
        
        
        listModel.fireContentsChanged(this); 
        if (selectedNum!=0)
            lstWords.setSelectedIndex(0);
        else
            if (lstWords.getModel().getSize()>0)
                lstWords.setSelectedIndex(1);
        isReadyToShow = true;

        lstWords.setSelectedIndex(selectedNum);

        lstWords.ensureIndexIsVisible(lstWords.getSelectedIndex());
    }

    public void onSearchError(Throwable ex) {
        //throw new UnsupportedOperationException("Not supported yet.");
        ex.printStackTrace();
    }

    public String getSearchDictName() {
        return "ALL";
    }

    public int getDelay() {
        return delay;
        
    }
    
}