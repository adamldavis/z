/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * Swing util.
 * 
 * @author Adam L. Davis
 */
public final class Swutil {

	/** Creates a frame with a JEditorPane with given URI. */
	public static void showStaticPage(final URI uri, final Dimension size,
			final Point location) throws IOException {
		final JFrame frame = new JFrame();
		frame.getRootPane().setLayout(new BorderLayout());
		JEditorPane editor = new JEditorPane(uri.toString());
		editor.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(editor);
		frame.getRootPane().add(scrollPane);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(size);
		frame.setLocation(location);
		frame.setVisible(true);
	}

	public static void flashMessage(final Window parent, String string) {
		flashMessage(parent, string, Color.WHITE, Color.BLACK, 1000);
	}

	public static void flashMessage(final Window parent, String string,
			Color background, Color foreground, final long howLong) {
		final int fontSize = 20;
		final JLabel label = new JLabel(string);
		final JDialog dialog = new JDialog(parent);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
		label.setFont(label.getFont().deriveFont(fontSize));
		label.setBackground(background);
		label.setForeground(foreground);
		label.setBorder(new BevelBorder(BevelBorder.RAISED));
		dialog.getRootPane().setLayout(new BorderLayout());
		dialog.getRootPane().add(label);
		final Point point = parent.getLocation();
		dialog.setLocation(point.x + parent.getHeight() / 2,
				point.y + parent.getWidth() / 2);
		dialog.setSize(fontSize * string.length() * 6 / 10, fontSize);
		dialog.setUndecorated(true);
		dialog.setVisible(true);
		dialog.setAlwaysOnTop(true);
		parent.requestFocus();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(howLong);
				} catch (InterruptedException e) {
				}
				dialog.dispose();
			}
		});
	}

}
