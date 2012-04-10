package com.adamldavis.z;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.adamldavis.z.api.LanguageParser;
import com.adamldavis.z.java.JavaCodeFormatter;
import com.adamldavis.z.java.JavaLanguageParser;

public class ZCodeEditor extends JFrame {

	private static final long serialVersionUID = 1L;

	final Component editor = new JEditorPane("text", "");

	private ZNode zNode;

	LanguageParser parser = new JavaLanguageParser();

	public ZCodeEditor(ZNode zNode) {
		super("Z code editor:" + zNode);
		JScrollPane scrollPane = new JScrollPane(editor);
		this.getRootPane().setLayout(new BorderLayout());
		this.getRootPane().add(scrollPane);
		this.setAlwaysOnTop(true);
		this.setVisible(true);
		this.zNode = zNode;
		editor.requestFocus();
		editor.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				save();
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
	}

	public void save() {
		System.out.println("Saving: " + zNode);
		// TODO: allow changing of formatter:

		new ZCodeSaver(new JavaCodeFormatter()).save(zNode);
	}
}
