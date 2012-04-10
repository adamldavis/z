package com.adamldavis.z;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ZNode {

	public enum ZNodeType {
		MODULE, PACKAGE, CLASS, METHOD
	};

	public Point2D.Float location;
	public ZNodeType zNodeType = ZNodeType.MODULE;

	public String name = "";
	public String code = "";

	String extension = "java";
	public File directory = new File("./");

	public final List<ZNode> dependencies = new ArrayList<ZNode>();
	public final List<ZNode> submodules = new ArrayList<ZNode>();
	private long lastModified = System.currentTimeMillis();

	public ZNode() {
		location = new Point2D.Float(0, 0);
	}

	public ZNode(float x, float y) {
		this(x, y, "Z");
	}

	public ZNode(float x, float y, String name) {
		location = new Point2D.Float(x, y);
		this.name = name;
	}

	public ZNode(ZNodeType zNodeType, String name, String code,
			String extension, long lastModified) {
		this();
		this.zNodeType = zNodeType;
		this.name = name;
		this.code = code;
		this.extension = extension;
		this.lastModified = lastModified;
	}

	public void draw(Graphics2D g2d, float size, Color color) {
		drawLines(g2d);
		final int x = (int) (location.x - size * 0.5);
		final int y = (int) (location.y - size * 0.5);
		final int isize = (int) size;
		g2d.setColor(color);
		switch (zNodeType) {
		case CLASS:
			g2d.drawRect(x, y, isize, isize);
			break;
		case METHOD:
			g2d.drawRect(x, y, isize, (int) (isize * 0.75));
			break;
		case MODULE:
			g2d.drawOval(x, y, isize, isize);
			break;
		case PACKAGE:
			g2d.drawOval(x, y, isize, (int) (isize * 0.75));
			break;
		}
		if (name != null) {
			g2d.setFont(g2d.getFont().deriveFont(14f));
			g2d.drawString(name, x - 1, y - 1);
		}
		if (code != null) {
			g2d.setFont(g2d.getFont().deriveFont(10f));
			g2d.drawString(code.substring(0, Math.min(code.length(), 30)),
					x + 10, y + 10);
		}
	}

	void drawLines(Graphics2D g2d) {
		for (ZNode dep : dependencies) {
			g2d.setColor(Color.WHITE);
			g2d.setStroke(new BasicStroke(1f));
			g2d.drawLine((int) location.x, (int) location.y,
					(int) dep.location.x, (int) dep.location.y);
		}
		for (ZNode sub : submodules) {
			g2d.setColor(Color.blue);
			g2d.setStroke(new BasicStroke(2f));
			g2d.drawLine((int) location.x, (int) location.y,
					(int) sub.location.x, (int) sub.location.y);
		}
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return name;
	}

}
