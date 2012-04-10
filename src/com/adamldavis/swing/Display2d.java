package com.adamldavis.swing;

import java.awt.Dimension;
import java.awt.Graphics2D;

@SuppressWarnings("serial")
public abstract class Display2d extends Display {

	public Display2d() {
		super();
	}

	public Display2d(boolean alwaysOnTop, int buffers, int delay) {
		super(alwaysOnTop, buffers, delay);
	}

	public Display2d(boolean alwaysOnTop, int buffers, int delay, Dimension dim) {
		super(alwaysOnTop, buffers, delay, dim);
	}

	public Display2d(boolean alwaysOnTop, int buffers, int delay,
			Dimension dim, boolean undecorated) {
		super(alwaysOnTop, buffers, delay, dim);
		setUndecorated(undecorated);
	}

	public void drawLine(Graphics2D g2d, float x1, float y1, float x2, float y2) {
		g2d.drawLine((int) (x1 * width), (int) (y1 * height),
				(int) (x2 * width), (int) (y2 * height));
	}

	public void drawLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
		g2d.drawLine(x1, y1, x2, y2);
	}

	public void drawRect(Graphics2D g2d, float x1, float y1, float x2, float y2) {
		g2d.drawRect((int) (x1 * width), (int) (y1 * height),
				(int) ((x2 - x1) * width), (int) ((y2 - y1) * height));
	}

	public void drawRect(Graphics2D g2d, int x1, int y1, int x2, int y2) {
		g2d.drawRect(x1, y1, (x2 - x1), (y2 - y1));
	}

	/** Draws the string centered on screen. */
	public void stringWrite(Graphics2D g2d, String str, float size) {
		stringWrite(g2d, str, size, height / 2 - size);
	}

	/** Draws the string centered on x-axis with given y. */
	public void stringWrite(Graphics2D g2d, String str, float size, float y) {
		stringWrite(g2d, str, size, width / 2 - str.length() * size / 4, y);
	}

	public void stringWrite(Graphics2D g2d, String str, float size, float x,
			float y) {
		g2d.setFont(getFont().deriveFont(size));
		g2d.drawString(str, x, y);
	}

}
