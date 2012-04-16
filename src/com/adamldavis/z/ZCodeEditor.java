package com.adamldavis.z;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import neoe.ne.TopCoderEditorPlugin;

import com.adamldavis.z.api.APIFactory;

public class ZCodeEditor extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(ZCodeEditor.class);

	final JPanel editor;

	private ZNode zNode;

	private final APIFactory apiFactory;
	
	private final TopCoderEditorPlugin plugin = new TopCoderEditorPlugin();

	public ZCodeEditor(ZNode zNode, APIFactory apiFactory) {
		super("Z code editor:" + zNode);
		editor = plugin.getEditorPanel(); 
		plugin.setSource(apiFactory.getCodeFormatter()
				.format(zNode.getCode()));
		JScrollPane scrollPane = new JScrollPane(editor);
		this.getRootPane().setLayout(new BorderLayout());
		this.getRootPane().add(scrollPane);
		this.setVisible(true);
		this.zNode = zNode;
		this.apiFactory = apiFactory;
		editor.requestFocus();
		editor.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					saveLater();
				}
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
		log.info("Saving: {}", zNode);
		zNode.replaceCode(plugin.getSource());
		new ZCodeSaver(apiFactory).save(zNode);
	}

	protected void saveLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100); // 1/10 second
				} catch (InterruptedException e) {
				}
				save();
			}
		});
	}
}
