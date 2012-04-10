package com.adamldavis.z;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.adamldavis.z.api.APIFactory;

public class ZCodeEditor extends JFrame {

	private static final long serialVersionUID = 1L;

	final JEditorPane editor;

	private ZNode zNode;

	private final APIFactory apiFactory;

	public ZCodeEditor(ZNode zNode, APIFactory apiFactory) {
		super("Z code editor:" + zNode);
		editor = new JEditorPane("text/plain", apiFactory.getCodeFormatter()
				.format(zNode.code));
		JScrollPane scrollPane = new JScrollPane(editor);
		this.getRootPane().setLayout(new BorderLayout());
		this.getRootPane().add(scrollPane);
		this.setAlwaysOnTop(true);
		this.setVisible(true);
		this.zNode = zNode;
		this.apiFactory = apiFactory;
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
		zNode.code = editor.getText();
		new ZCodeSaver(apiFactory).save(zNode);
	}
}
