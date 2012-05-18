/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.adamldavis.z.ZNode;

/**
 * @author Adam L. Davis
 * 
 */
public class ZNodePainterWithLines extends ZNodePainter {

	public ZNodePainterWithLines(Graphics2D graphics2d, float size, Color color) {
		super(graphics2d, size, color);
	}

	@Override
	public void paint(Object object) {
		if (object instanceof ZNode) {
			ZNode node = (ZNode) object;
			drawLines(node, graphics2d);
		}
		super.paint(object);
	}

	void drawLines(ZNode node, Graphics2D g2d) {
		for (ZNode dep : node.dependencies) {
			g2d.setColor(Color.WHITE);
			g2d.setStroke(new BasicStroke(1f));
			g2d.drawLine(scale(node.location.x), scale(node.location.y),
					scale(dep.location.x), scale(dep.location.y));
		}
		for (ZNode sub : node.submodules) {
			g2d.setColor(Color.blue);
			g2d.setStroke(new BasicStroke(2f));
			g2d.drawLine(scale(node.location.x), scale(node.location.y),
					scale(sub.location.x), scale(sub.location.y));
		}
	}

}
