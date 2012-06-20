package com.adamldavis.z.api;

import java.io.File;

import javax.swing.JPanel;

import com.adamldavis.z.gui.ColorManager;

public interface Editor {

	/** Opens a new window with given file. */
	JPanel open(File f) throws Exception;

	/** Applies the given color manager. */
	void applyColor(ColorManager colorManager);

	/** Gets the panel containing the editor. */
	JPanel getEditorPanel();

	/** Gets the current text from the editor. */
	String getText();

	/** Set the current text in the editor. */
	void setText(String s);

	/** sets the scale of the editor. */
	void setScale(float scale);

}
