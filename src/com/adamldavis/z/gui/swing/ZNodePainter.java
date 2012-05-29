package com.adamldavis.z.gui.swing;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.gui.Painter;

public class ZNodePainter extends Graphics2DPainter implements Painter {

	final float scale;

	final Color color;

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
		final int x = scale(node.location.x - size * 0.5f);
		final int y = scale(node.location.y - size * 0.5f);
		final int isize = Math.round(size);
		Graphics2D g2d = this.graphics2d;

		g2d.setColor(g2d.getBackground());
		switch (node.zNodeType) {
		case CLASS:
			g2d.fillRect(x, y, isize, isize);
			g2d.setColor(color);
			g2d.drawRect(x, y, isize, isize);
			break;
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
		if (node.name != null) {
			g2d.setFont(g2d.getFont().deriveFont(Math.max(size * 16 / 80, 5)));
			g2d.drawString(node.name, x - 1, y + isize / 8);
		}
		if (node.getCodeLines() != null && isize > 20) {
			Point point2 = new Point(x + isize + 1, y + isize / 2);
			final float codeSize = Math.max(size * 1 / 8, 5);
			g2d.setFont(g2d.getFont().deriveFont(codeSize));
			g2d.setPaint(new GradientPaint(node.location, color.darker(),
					point2, Color.BLACK));
			int i = 1;
			for (String line : node.getCodeLines()) {
				g2d.drawString(line.substring(0, Math.min(20, line.length())),
						x + 5, y + isize / 8 + codeSize * i);
				if (i++ > 3)
					break;
			}
		}
	}


	protected int scale(float xy) {
		return (int) (xy * scale) + 1;
		
	}

}
