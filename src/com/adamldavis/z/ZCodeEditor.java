package com.adamldavis.z;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import neoe.ne.EditorAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.swing.Swutil;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.ColorMode;
import com.adamldavis.z.api.Editor;

public class ZCodeEditor extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory
			.getLogger(ZCodeEditor.class);

	final JPanel panel;

	private ZNode zNode;

	private final APIFactory apiFactory;

	private final Editor editor = new EditorAdapter();

	public ZCodeEditor(ZNode zNode, APIFactory apiFactory) {
		super("Z code editor:" + zNode);
		panel = editor.getEditorPanel();
		editor.applyColorMode(ColorMode.WHITE);
		editor.setText(apiFactory.getCodeFormatter().format(zNode.getCode()));
		JScrollPane scrollPane = new JScrollPane(panel);
		this.getRootPane().setLayout(new BorderLayout());
		this.getRootPane().add(scrollPane);
		this.setVisible(true);
		this.zNode = zNode;
		this.apiFactory = apiFactory;
		panel.requestFocus();
		panel.addKeyListener(new KeyListener() {

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
				if (e.getKeyCode() == KeyEvent.VK_F1) {
					try {
						showHelp();
					} catch (IOException e1) {
						log.error(e1.getMessage());
					}
				}
			}
		});
	}

	public void save() {
		log.info("Saving: {}", zNode);
		zNode.replaceCode(editor.getText());
		new ZCodeSaver(apiFactory).save(zNode);
		Swutil.flashMessage(this, "Saved " + zNode.name);
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

	protected void showHelp() throws IOException {
		log.info("showHelp called.");
		try {
			Swutil.showStaticPage(Thread.currentThread()
					.getContextClassLoader().getResource("./neoedit.html")
					.toURI(), this.getSize(), this.getLocation());
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		}
	}
}
