/** Copyright 2012, Adam L. Davis, all rights reserved. */
package neoe.ne;

import java.io.File;

import javax.swing.JPanel;

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
public class EditorAdapter implements Editor {

	private static final long serialVersionUID = 1042L;

	private static final Logger log = LoggerFactory
			.getLogger(EditorAdapter.class);

	EditPanel editor;

	public EditorAdapter() {
		try {
			editor = new EditPanel("");
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
		editor = new EditPanel(f);
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
		case BLACK:
			ui.applyColorMode(1);
		default:
			ui.applyColorMode(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.Editor#getEditorPanel()
	 */
	@Override
	public JPanel getEditorPanel() {
		return editor;
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
			editor.page.ptEdit.setText(s);
			editor.revalidate();
			editor.repaint();
		}
	}

}
