package com.olunx;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.LayoutStyle;
import javax.swing.border.TitledBorder;

import test.AXMLPrinter;

import com.android.signapk.SignApk;

public class Main {

	private JFrame frame;
	private JLabel xmlFileLabel, dexFileLabel, apkFileLabel, statusLabel;
	private String xmlFilePath, dexFilePath, apkFilePath;

	/**
	 * Launch the application
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application
	 */
	public Main() {
		createContents();
	}

	/**
	 * Initialize the contents of the frame
	 */
	private void createContents() {
		frame = new JFrame();
		frame.setTitle("Apk反编译工具");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		final JMenu helpMenu = new JMenu();
		helpMenu.setText("帮助");
		menuBar.add(helpMenu);

		final JMenuItem aboutItem = new JMenuItem();
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				JOptionPane.showMessageDialog(frame, Utils.getAboutText());
			}
		});
		aboutItem.setText("关于");
		helpMenu.add(aboutItem);

		JLabel xmlLabel;
		xmlLabel = new JLabel();
		xmlLabel.setText("反编译xml文件");

		JButton xmlFileChoose;
		xmlFileChoose = new JButton();
		xmlFileChoose.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				JFileChooser choose = new JFileChooser();
				choose.setFileFilter(new FileFilter(".xml"));
				int resultVal = choose.showDialog(frame, "选择");
				if (resultVal == JFileChooser.APPROVE_OPTION) {
					xmlFilePath = choose.getSelectedFile().getAbsolutePath();
					xmlFileLabel.setText(xmlFilePath);
				}
			}
		});
		xmlFileChoose.setText("选择文件");

		JSeparator separator;
		separator = new JSeparator();

		JLabel dexLabel;
		dexLabel = new JLabel();
		dexLabel.setText("反编译dex为jar");

		JButton dexFileChoose;
		dexFileChoose = new JButton();
		dexFileChoose.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				JFileChooser choose = new JFileChooser();
				choose.setFileFilter(new FileFilter(".dex"));
				int resultVal = choose.showDialog(frame, "选择");
				if (resultVal == JFileChooser.APPROVE_OPTION) {
					dexFilePath = choose.getSelectedFile().getAbsolutePath();
					dexFileLabel.setText(dexFilePath);
				}
			}
		});
		dexFileChoose.setText("选择文件");

		JLabel apkLabel;
		apkLabel = new JLabel();
		apkLabel.setText("给apk文件签名");

		JButton apkFileChoose;
		apkFileChoose = new JButton();
		apkFileChoose.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				JFileChooser choose = new JFileChooser();
				choose.setFileFilter(new FileFilter(".apk"));
				int resultVal = choose.showDialog(frame, "选择");
				if (resultVal == JFileChooser.APPROVE_OPTION) {
					apkFilePath = choose.getSelectedFile().getAbsolutePath();
					apkFileLabel.setText(apkFilePath);
				}
			}
		});
		apkFileChoose.setText("选择文件");

		xmlFileLabel = new JLabel();
		xmlFileLabel.setText("xml文件路径");

		dexFileLabel = new JLabel();
		dexFileLabel.setText("dex文件路径");

		apkFileLabel = new JLabel();
		apkFileLabel.setText("apk文件路径");

		statusLabel = new JLabel();
		statusLabel.setBorder(new TitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
		statusLabel.setText("状态栏");

		final JButton converXmlBtn;
		converXmlBtn = new JButton();
		converXmlBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (xmlFilePath == null) {
					statusLabel.setText("没有选择文件！");
					return;
				}

				new Thread() {
					public void run() {
						converXmlBtn.setEnabled(false);
						statusLabel.setText("正在反编译xml文件，请稍等...");
						AXMLPrinter.main(new String[] { xmlFilePath });
						statusLabel.setText("反编译成功！已在xml所在目录生成新的xml文件。文件名[*_new.xml]");
						converXmlBtn.setEnabled(true);
					}
				}.start();
			}
		});
		converXmlBtn.setText("开始");

		final JButton converDexBtn;
		converDexBtn = new JButton();
		converDexBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if (dexFilePath == null) {
					statusLabel.setText("没有选择文件！");
					return;
				}

				new Thread() {
					public void run() {
						converDexBtn.setEnabled(false);
						statusLabel.setText("正在反编译dex文件，请稍等...");
						pxb.android.dex2jar.v3.Main.main(new String[] { dexFilePath });
						statusLabel.setText("反编译成功！已在dex所在目录生成jar文件。文件名[classes.dex.dex2jar.jar]");
						converDexBtn.setEnabled(true);
					}
				}.start();
			}
		});
		converDexBtn.setText("开始");

		final JButton converApkBtn;
		converApkBtn = new JButton();
		converApkBtn.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {

				if (apkFilePath == null) {
					statusLabel.setText("没有选择文件！");
					return;
				}

				new Thread() {
					public void run() {
						converApkBtn.setEnabled(false);
						statusLabel.setText("正在处理签名，请稍等...");

						String newApkFilePath = apkFilePath.replace(".apk", "_new.apk");

						// 检查public key
						File publicKey = new File(Utils.getCurrentDir() + File.separator + Utils.publicKey);
						System.out.println("public key: " + publicKey.getAbsolutePath());
						if (!publicKey.exists()) {
							// 如果key不存在，则先创建一份。
							System.out.println("key does noe exists");
							Utils.createFile(Utils.publicKey, Utils.publicKeySize);
						}

						// 检查private key
						File privateKey = new File(Utils.getCurrentDir() + File.separator + Utils.privateKey);
						System.out.println("private key: " + privateKey.getAbsolutePath());
						if (!privateKey.exists()) {
							// 如果key不存在，则先创建一份。
							Utils.createFile(Utils.privateKey, Utils.privateKeySize);
						}

						SignApk
								.main(new String[] { publicKey.getAbsolutePath(), privateKey.getAbsolutePath(), apkFilePath, newApkFilePath });

						statusLabel.setText("签名成功！已在包所在目录生成签名后的apk文件。文件名[*_new.apk]");
						converApkBtn.setEnabled(true);
					}
				}.start();

			}
		});
		converApkBtn.setText("开始");

		JSeparator separator_1;
		separator_1 = new JSeparator();

		JSeparator separator_2;
		separator_2 = new JSeparator();

		final GroupLayout groupLayout = new GroupLayout((JComponent) frame.getContentPane());
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				groupLayout.createSequentialGroup().addContainerGap().addGroup(
						groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(statusLabel,
								GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE).addComponent(separator_2,
								GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE).addComponent(apkLabel,
								GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE).addComponent(separator_1,
								GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE).addComponent(dexLabel,
								GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE).addComponent(separator,
								GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE).addComponent(xmlLabel,
								GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
								.addGroup(
										GroupLayout.Alignment.LEADING,
										groupLayout.createSequentialGroup().addComponent(xmlFileChoose).addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED).addComponent(xmlFileLabel,
												GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE).addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED).addComponent(converXmlBtn)).addGroup(
										GroupLayout.Alignment.LEADING,
										groupLayout.createSequentialGroup().addComponent(dexFileChoose).addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED).addComponent(dexFileLabel,
												GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE).addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED).addComponent(converDexBtn)).addGroup(
										GroupLayout.Alignment.LEADING,
										groupLayout.createSequentialGroup().addComponent(apkFileChoose).addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED).addComponent(apkFileLabel,
												GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE).addPreferredGap(
												LayoutStyle.ComponentPlacement.RELATED).addComponent(converApkBtn))).addContainerGap()));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				groupLayout.createSequentialGroup().addContainerGap().addComponent(xmlLabel).addPreferredGap(
						LayoutStyle.ComponentPlacement.RELATED).addGroup(
						groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false).addGroup(
								groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(converXmlBtn).addComponent(
										xmlFileLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addComponent(
								xmlFileChoose, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addGap(8, 8, 8)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(dexLabel).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED).addGroup(
								groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(dexFileChoose).addComponent(
										converDexBtn).addComponent(dexFileLabel)).addGap(8, 8, 8).addComponent(separator_1,
								GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED).addComponent(apkLabel).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED).addGroup(
								groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(apkFileChoose).addComponent(
										converApkBtn).addComponent(apkFileLabel)).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(separator_2, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE).addPreferredGap(
								LayoutStyle.ComponentPlacement.RELATED).addComponent(statusLabel).addContainerGap(GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)));
		frame.getContentPane().setLayout(groupLayout);
		frame.pack();
	}
	
}