package com.adamldavis.z.gui.swing;

import java.awt.Graphics2D;

import com.adamldavis.z.gui.Painter;

public abstract class Graphics2DPainter implements Painter {

	protected final Graphics2D graphics2d;

	public Graphics2DPainter(Graphics2D graphics2d) {
		super();
		this.graphics2d = graphics2d;
	}

}