/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.editor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.io.FileUtils;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.DefaultApiFactory;
import com.adamldavis.z.api.Editor;
import com.adamldavis.z.gui.ColorManager;
import com.adamldavis.z.gui.ColorSetting;

/**
 * Implements text-edit part of Zeta Code.
 * 
 * @author Adam L. Davis
 * 
 */
public class ZEdit {

	final Map<ZNode, Editor> editorMap = new LinkedHashMap<ZNode, Editor>();
	final Map<Editor, ZNode> reverseMap = new LinkedHashMap<Editor, ZNode>();
	final List<Editor> editors = new LinkedList<Editor>();
	final Container pane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	final JScrollPane scrollPane = new JScrollPane(pane,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	ColorManager colorManager = new ColorManager();

	private final KeyListener keyListener = new KeyListener() {

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.isAltDown()) {
				switch (e.getKeyChar()) {
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					int i = (int) (e.getKeyChar() - '1');
					if (i < editors.size()) {
						// TODO get this to work
						editors.get(i).getEditorPanel().requestFocus();
					}
					break;
				case 'o':
					openFile();
					break;
				case 's': // TODO search for open editor

				}
			}
		}
	};

	public ZEdit() {
		pane.requestFocus();
		pane.setBackground(colorManager.getColorFor(ColorSetting.BACKGROUND));
		pane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isControlDown() && e.isAltDown()) {
					openFile();
				}
			}
		});
		pane.addKeyListener(keyListener);
	}

	protected void openFile() {
		JFileChooser chooser = new JFileChooser(new File("."));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.showOpenDialog(pane);
		File f = chooser.getSelectedFile();
		ZNode node = new ZNode(0, 0, f.getName());
		try {
			node.setCode(FileUtils.readLines(f));
			node.setParentFile(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Editor editor = getEditorFor(node, new DefaultApiFactory());
		editor.getEditorPanel().setSize(500, 400);
		updatePaneSize();
	}

	public Editor getEditor(ZNode z) {
		return editorMap.get(z);
	}

	public List<Editor> getEditors() {
		return editors;
	}

	public ZNode getNode(Editor editor) {
		return reverseMap.get(editor);
	}

	public Container getPane() {
		return pane;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public void put(Editor editor, ZNode node) {
		reverseMap.put(editor, node);
	}

	public void put(ZNode node, Editor editor) {
		editorMap.put(node, editor);
	}

	public void remove(Editor editor) {
		editors.remove(editor);
	}

	public void updatePaneSize() {
		int maxW = 0, maxH = 0;
		for (Editor ed : getEditors()) {
			maxW = Math.max(ed.getEditorPanel().getX()
					+ ed.getEditorPanel().getWidth(), maxW);
			maxH = Math.max(ed.getEditorPanel().getY()
					+ ed.getEditorPanel().getHeight() * 2, maxH);
		}
		getPane().setPreferredSize(new Dimension(maxW, maxH + 10));
		getPane().invalidate();
		getScrollPane().validate();
	}

	/**
	 * Creates a new editor or returns existing one.
	 * 
	 * @param z
	 *            Node you want to edit.
	 * @param apiFactory
	 *            API.
	 * @return new or existing Editor.
	 */
	public Editor getEditorFor(final ZNode z, APIFactory apiFactory) {
		final Editor existingEditor = getEditor(z);
		final Editor editor = (existingEditor == null) ? new ZCodeEditor(z,
				apiFactory) : existingEditor;
		if (existingEditor == null) {
			getEditors().add(0, editor);
			put(z, editor);
			put(editor, z);
		}
		getPane().add(editor.getEditorPanel());
		editor.getEditorPanel().addKeyListener(keyListener);
		return editor;
	}

	public static void main(String[] args) {
		ZEdit edit = new ZEdit();
		JFrame frame = new JFrame("ZEDIT");
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(edit.scrollPane);
		frame.setVisible(true);
	}
}
