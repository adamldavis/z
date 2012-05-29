/** Copyright 2012, Adam L. Davis, all rights reserved. */
package neoe.ne;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import neoe.ne.PlainPage.Paint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.api.ColorMode;
import com.adamldavis.z.api.Editor;

/**
 * Adapter for the neo-edit code editor.
 * 
 * @author Adam L. Davis
 * 
 */
public class EditorAdapter implements Editor, MouseListener,
		MouseMotionListener, KeyListener {

	private static final int EDGE_SIZE = 8;

	private static final long serialVersionUID = 1042L;

	private static final Logger log = LoggerFactory
			.getLogger(EditorAdapter.class);

	protected BetterEditPanel editor;

	protected JPanel panel = new JPanel(new BorderLayout());

	public EditorAdapter() {
		try {
			panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			editor = new BetterEditPanel("");
			panel.add(editor);
			panel.addMouseListener(this);
			panel.addMouseMotionListener(this);
			editor.addKeyListener(this);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	/**
	 * Opens a new window with given file.
	 * 
	 * @see com.adamldavis.z.api.Editor#open(java.io.File)
	 */
	@Override
	public JPanel open(File f) throws Exception {
		editor = new BetterEditPanel(f);
		editor.openWindow();
		return editor;
	}

	/*
	 * Applies the given color mode.
	 * 
	 * @see
	 * com.adamldavis.z.api.Editor#applyColorMode(com.adamldavis.z.api.ColorMode
	 * )
	 */
	@Override
	public void applyColorMode(ColorMode mode) {
		final Paint ui = editor.page.ui;
		switch (mode) {
		case BLUE:
			ui.applyColorMode(2);
			break;
		case BLACK:
			ui.applyColorMode(1);
			break;
		default:
			ui.applyColorMode(0);
		}
	}

	public void setShowNumbers(boolean show) {
		editor.page.ui.showLineNumbers = show;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.Editor#getEditorPanel()
	 */
	@Override
	public JPanel getEditorPanel() {
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.Editor#getSource()
	 */
	@Override
	public String getText() {
		if (editor != null) {
			return U.getText(editor.page);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.Editor#setSource(java.lang.String)
	 */
	@Override
	public void setText(String s) {
		if (editor != null) {
			editor.setText(s);
			editor.revalidate();
			editor.repaint();
		}
	}

	@Override
	public void setScale(float scale) {
		editor.rescale(scale);
		panel.setSize((int) editor.getSize().getWidth() + 5, (int) editor
				.getSize().getHeight() + 5);
		panel.doLayout();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	int offsetX, offsetY;

	@Override
	public void mousePressed(MouseEvent e) {
		Point loc = e.getLocationOnScreen();
		Point pan = panel.getLocationOnScreen();
		offsetX = pan.x - loc.x;
		offsetY = pan.y - loc.y;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		offsetX = offsetY = 0;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		panel.setBackground(Color.RED.darker());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		panel.setBackground(Color.GRAY.darker());
		panel.getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		log.info("e=" + e);
		Point loc = e.getLocationOnScreen();
		SwingUtilities.convertPointFromScreen(loc, panel.getParent());

		if (e.isShiftDown()) {
			panel.getParent().setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
			editor.setSize(loc.x - panel.getX(), loc.y - panel.getY());
			final Dimension size = editor.getSize();
			panel.setSize((int) size.getWidth() + EDGE_SIZE,
					(int) size.getHeight() + EDGE_SIZE);
			panel.doLayout();
			panel.getParent().repaint();
		} else {
			panel.getParent().setCursor(new Cursor(Cursor.MOVE_CURSOR));
			loc.x += offsetX;
			loc.y += offsetY;
			panel.setLocation(loc);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		log.info("e=" + e);
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			panel.getParent().setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			panel.getParent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

}
