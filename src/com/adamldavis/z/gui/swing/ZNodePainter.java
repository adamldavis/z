package com.adamldavis.z.gui.swing;

import static java.lang.Math.round;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.gui.ColorManager;
import com.adamldavis.z.gui.ColorSetting;
import com.adamldavis.z.gui.Painter;

public class ZNodePainter extends Graphics2DPainter implements Painter {

	public static Color hsv(float hue, float sat, float value) {
		return Color.getHSBColor(hue, sat, value);
	}

	final float scale;

	final Color color;

	ColorManager colorManager = new ColorManager();

	public ZNodePainter(Graphics2D graphics2d, float scale, Color color) {
		super(graphics2d);
		this.scale = scale;
		this.color = color;
	}

	@Override
	public void paint(Object object) {
		if (object instanceof ZNode) {
			ZNode node = (ZNode) object;
			draw(node, color);
		}
	}

	/**
	 * Draws the node, name, and code.
	 * 
	 * @param node
	 *            The node to draw.
	 * @param color
	 *            Color to draw in.
	 */
	public void draw(ZNode node, Color color) {
		float size = scale(node.getSize());
		final int x = scale(node.getLocation().x - size * 0.5f);
		final int y = scale(node.getLocation().y - size * 0.5f);
		final int isize = Math.round(size);
		Graphics2D g2d = this.graphics2d;

		// g2d.setColor(g2d.getBackground());
		// TODO: keep track of # methods calling this method
		float sat = halfPlusLog(node.getCodeLineSize());
		// TODO: Use ? to get Test-coverage for value
		float value = halfPlusLog(node.getCodeLineSize());
		// TODO: actually keep track of error/warnings
		final Color todoColor = colorManager.getColorFor(ColorSetting.TODO);
		final Color okayColor = colorManager.getColorFor(ColorSetting.OKAY);
		final Color hsvColor = node.hasTodo() ? hsv(
				ColorUtil.findHue(todoColor), sat, value) : hsv(
				ColorUtil.findHue(okayColor), sat, value);
		g2d.setColor(hsvColor);
		g2d.setStroke(new BasicStroke(1.0f));

		switch (node.getNodeType()) {
		case CLASS:
			g2d.fillRect(x, y, isize, isize);
			g2d.setColor(color);
			g2d.drawRect(x, y, isize, isize);
			break;
		case CALLEE:
		case METHOD:
			g2d.fillRect(x, y + isize / 8, isize, (int) (isize * 0.75));
			g2d.setColor(color);
			g2d.drawRect(x, y + isize / 8, isize, (int) (isize * 0.75));
			break;
		case MODULE:
			g2d.fillOval(x + 1, y + 1, isize - 2, isize - 2);
			g2d.setColor(color);
			g2d.drawOval(x, y, isize, isize);
			break;
		case PACKAGE:
			g2d.fillOval(x, y + isize / 8, isize, (int) (isize * 0.75));
			g2d.setColor(color);
			g2d.drawOval(x, y + isize / 8, isize, (int) (isize * 0.75));
			break;
		case CALLER:
		case DEPENDENCY:
			g2d.fillOval(x + 1, y + 1, isize - 2, isize - 2);
			// draw dotted line circle:
			if (isize > 20) {
				g2d.setColor(color);
				for (int i = 0; i < 20; i++) {
					g2d.drawArc(x, y, isize, isize, i * 18, 8);
				}
			} else {
				g2d.setColor(color.darker());
				g2d.drawOval(x, y, isize, isize);
			}
			break;
		}
		final Font oldFont = g2d.getFont();
		if (node.getName() != null && isize > 20) {
			float fontSize = Math.max(size * 16 / 80, 5);
			g2d.setFont(oldFont.deriveFont(fontSize));
			if (node.getNodeType() == ZNodeType.CLASS
					|| node.getNodeType() == ZNodeType.MODULE)
				g2d.drawString(node.getName(), x - 1, y);
			else
				g2d.drawString(node.getName(), x - 1, y + isize / 8);

		}
		if (node.getCodeLines() != null && isize > 20) {
			Point point2 = new Point(x + isize, y + isize / 2);
			final float codeSize = Math.max(size * 1 / 12, 5);
			g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, round(codeSize)));
			g2d.setPaint(new GradientPaint(x, y, hsvColor.darker(), point2.x,
					point2.y, Color.BLACK));
			// g2d.setColor(g2d.getBackground());
			int i = 1;
			for (String line : node.getCodeLines()) {
				g2d.drawString(
						line.substring(
								0,
								Math.min(2 * isize / round(codeSize),
										line.length())), x + 5, y + isize / 4
								+ codeSize * i);
				if (line.length() > 0 && i++ > 5)
					break;
			}
		}
		g2d.setFont(oldFont);
	}

	/** Assumes value range of 0 to about 1100 (logarithmic up to 1100). */
	private float halfPlusLog(int value) {
		if (value <= 1) {
			return 0.5f;
		}
		return Math.min(1.0f, 0.5f + (float) Math.log(value) / 14f);
	}

	protected int scale(float xy) {
		return (int) (xy * scale) + 1;
	}

}
