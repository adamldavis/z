/** Copyright 2012, Adam L. Davis, all rights reserved. */
package neoe.ne;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;

/**
 * Extends EditPanel to add rescaling and preferred size.
 * 
 * @author Adam L. Davis
 * 
 */
@SuppressWarnings("serial")
public class BetterEditPanel extends EditPanel {

	private static final int DEFAULT_HEIGHT = 200;
	private static final int DEFAULT_WIDTH = 500;

	/** Original width and height. */
	int width, height;

	public BetterEditPanel(File f) throws Exception {
		this(f, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public BetterEditPanel(String text) throws Exception {
		this(text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	public BetterEditPanel(File f, int width, int height) throws Exception {
		super(f);
		setPreferredSize(new Dimension(width, height));
		this.width = width;
		this.height = height;
	}

	public BetterEditPanel(String text, int width, int height) throws Exception {
		super(text);
		setPreferredSize(new Dimension(width, height));
		this.width = width;
		this.height = height;
	}

	@Override
	public void keyPressed(KeyEvent env) {
		super.keyPressed(env);
		if (env.isControlDown() && env.getKeyChar() == '1') {
			rescale(1f); // reset zoom
		} else if (env.isControlDown() && env.getKeyChar() == '2') {
			rescale(2f); // zoom 200%
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent env) {
		super.mouseWheelMoved(env);
		if (env.isControlDown()) {// scale
			rescale(page.ui.scalev);
		}
	}

	/** Sets the scale for this editor, then tells parent to doLayout. */
	public void rescale(final float scale) {
		page.ui.scalev = scale;
		setSize(new Dimension(Math.round(width * scale), Math.round(height
				* scale)));
		// getParent().doLayout();
	}

}
