/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui.swing;

import static java.awt.Color.BLACK;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import static java.awt.Color.YELLOW;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.swing.Display2d;
import com.adamldavis.swing.Swutil;
import com.adamldavis.z.Z;
import com.adamldavis.z.Z.State;
import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNodeLink;

/**
 * @author Adam L. Davis
 * 
 */
@SuppressWarnings("serial")
public class ZDisplay extends Display2d {

	private static final Logger log = LoggerFactory.getLogger(ZDisplay.class);

	Z z;

	public ZDisplay(Z z) {
		super(false, 2, 35, new Dimension(800, 600));
		setTitle("Z");
		this.z = z;
		this.getContentPane().setBackground(Color.BLACK.brighter());
		this.getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.swing.Display#paintBuffered(java.awt.Graphics2D)
	 */
	@Override
	protected void paintBuffered(Graphics2D g2d) {
		final List<ZNode> zNodes = Collections.unmodifiableList(z.getZNodes());
		final Point point1 = z.getPoint1();
		final Point point2 = z.getPoint2();

		g2d.addRenderingHints(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
		g2d.setBackground(BLACK);
		g2d.setColor(g2d.getBackground());
		// g2d.fillRect(0, 0, width, height);
		if (zNodes == null || z.getState() == State.EDITING
				|| z.getState() == State.SELECTING) {
			return;
		}
		final ZNodePainter nodePainter = new ZNodePainter(g2d, z.getScale(),
				LIGHT_GRAY);
		final ZNodePainter hoverPainter = new ZNodePainter(g2d, z.getScale(),
				Color.WHITE);
		final ZNodePainter selNodePainter = new ZNodePainterWithLines(g2d,
				z.getScale(), YELLOW.darker());

		for (ZNode node : zNodes) {
			if (node == z.getSelectedNode() && z.getLinks().isEmpty()) {
				selNodePainter.paint(node);
			} else if (node == z.getHoveredNode()) {
				hoverPainter.paint(node);
			} else {
				nodePainter.paint(node);
			}
		}
		if (point1 != null && point2 != null) {
			g2d.setColor(YELLOW.darker());
			drawLine(g2d, point1.x, point1.y, point2.x, point2.y);
			final int s = (int) z.getScale() * 80;
			g2d.drawOval(point2.x - s / 2, point2.y - s / 2, s, s);
		}
		g2d.setFont(g2d.getFont().deriveFont(20f));
		if (z.getHoverText() != null) {
			final Point point = z.getMouseLocation();
			final int length = z.getHoverText().length();
			g2d.setColor(WHITE);
			g2d.drawString(z.getHoverText(), point.x
					- (length * 10 * point.x / width), point.y);
		}
		g2d.setColor(LIGHT_GRAY);
		g2d.drawRect(8, 30, 40, 20);
		g2d.drawString("Back", 8, 42);
		g2d.setStroke(new BasicStroke(2.0f));
		for (ZNodeLink link : z.getLinks()) {
			g2d.setPaint(new GradientPaint(link.getNode1().getLocation(),
					YELLOW.darker(), link.getNode2().getLocation(), Color.BLUE
							.darker()));
			g2d.drawLine((int) link.getNode1().getLocation().getX(), (int) link
					.getNode1().getLocation().getY(), (int) link.getNode2()
					.getLocation().getX(), (int) link.getNode2().getLocation()
					.getY());
		}
	}

	public Dimension getDimension() {
		return new Dimension(width, height);
	}

	public String showInputDialog(String message, String initialValue) {
		return JOptionPane.showInputDialog(this, message, initialValue);
	}

	public void showEditorHelp() throws IOException {
		log.info("showEditorHelp called.");
		try {
			Swutil.showStaticPage(ZNode.class.getResource("/neoedit.html")
					.toURI(), getSize(), getLocation());
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		}
	}

}
