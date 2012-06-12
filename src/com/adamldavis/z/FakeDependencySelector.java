/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;

/**
 * @author Adam L. Davis
 * 
 */
public class FakeDependencySelector extends JFrame {

	static final String[] options = new String[] { "commons-beanutils",
			"commons-cli", "commons-codec", "commons-collections",
			"commons-digester", "commons-discovery", "commons-email",
			"commons-fileupload", "commons-httpclient", "commons-io",
			"commons-lang", "commons-logging", "commons-pool",
			"commons-validator" };

	private String name;

	private JComboBox box = new JComboBox();

	private JTextField field = new JTextField(15);

	private JButton ok = new JButton("OK");

	private JButton cancel = new JButton("Cancel");

	public FakeDependencySelector() {
		super("New dependency");
		setSize(210, 150);
		getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT));
		getContentPane().add(field);
		getContentPane().add(box);
		getContentPane().add(ok);
		getContentPane().add(cancel);
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				name = (String) box.getSelectedItem();
				dispose();
			}
		});
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		field.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String t = field.getText() + e.getKeyChar();
				if (t.length() > 5) {
					List<String> list = new LinkedList<String>();
					for (String opt : options) {
						if (opt.startsWith(t)) {
							list.add(opt);
						}
					}
					box.setModel(new DefaultComboBoxModel(list
							.toArray(new String[0])));
					repaint();
				}
			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocation(200, 200);
		setVisible(true);
	}

	public String get() {
		// name = (String) JOptionPane.showInputDialog(null,
		// "New maven dependency", ZNodeType.DEPENDENCY.name(),
		// JOptionPane.QUESTION_MESSAGE, null, options, "commons-io");

		while (name == null && isVisible()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return name;
	}
}
