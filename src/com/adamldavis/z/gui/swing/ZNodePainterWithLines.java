/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.gui.ColorManager;
import com.adamldavis.z.gui.ColorSetting;

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

	ColorManager colorManager = new ColorManager();

	void drawLines(ZNode node, Graphics2D g2d) {
		for (ZNode dep : node.getDependencies()) {
			g2d.setColor(colorManager.getColorFor(ColorSetting.LINE));
			g2d.setStroke(new BasicStroke(1f));
			g2d.drawLine(scale(node.getLocation().x),
					scale(node.getLocation().y), scale(dep.getLocation().x),
					scale(dep.getLocation().y));
		}
		for (ZNode sub : node.getSubmodules()) {
			g2d.setColor(colorManager.getColorFor(ColorSetting.LINE));
			g2d.setStroke(new BasicStroke(2f));
			g2d.drawLine(scale(node.getLocation().x),
					scale(node.getLocation().y), scale(sub.getLocation().x),
					scale(sub.getLocation().y));
		}
	}

}
