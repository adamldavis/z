package com.adamldavis.z;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.adamldavis.z.api.LanguageParser;

/**
 * Module, Package, Class or Method.
 * 
 * @author Adam Davis
 * 
 */
public class ZNode {

	public enum ZNodeType {
		MODULE, PACKAGE, CLASS, METHOD, DEPENDENCY
	};

	public Point2D.Float location;
	public ZNodeType zNodeType = ZNodeType.MODULE;

	public String name = "";
	private final List<String> code = new LinkedList<String>();

	public String extension = "";
	public File parentFile = new File("./");

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
		setCode(code);
		this.extension = extension;
		this.lastModified = lastModified;
	}

	/**
	 * Draws the node, name, and code.
	 * 
	 * @param g2d
	 *            2D graphics to draw on.
	 * @param size
	 *            Size in pixels.
	 * @param color
	 *            Color to draw in.
	 */
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
		case DEPENDENCY:
			// draw dotted line circle:
			for (int i = 0; i < 36; i++) {
				g2d.drawArc(x, y, isize, isize, i * 10, 5);
			}
			break;
		}
		if (name != null) {
			g2d.setFont(g2d.getFont().deriveFont(14f));
			g2d.drawString(name, x - 1, y - 1);
		}
		if (code != null) {
			g2d.setFont(g2d.getFont().deriveFont(10f));
			g2d.drawString(code.isEmpty() ? "" : code.get(0), x + 10, y + 10);
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

	// code related method
	/** Is code empty? */
	public boolean isCodeEmpty() {
		return code.isEmpty();
	}

	public void addCodeLine(String code) {
		this.code.add(code);
	}

	public void replaceCode(String code) {
		setCode(code);
	}

	private void setCode(String code) {
		this.code.clear();
		this.code.addAll(Arrays.asList(code.split("[\n\r]{1,2}")));
	}

	public void setCode(Collection<String> code) {
		this.code.clear();
		this.code.addAll(code);
	}

	public String getCode() {
		StringBuilder buffer = new StringBuilder();
		for (String line : code) {
			buffer.append(line).append('\n');
		}
		return buffer.toString();
	}

	public int getCodeLineSize() {
		return code.size();
	}

	public List<String> getCodeLines() {
		return Collections.unmodifiableList(this.code);
	}

	public int getEndLineNumber(LanguageParser languageParser) {
		int i = code.size() - 1;

		if (languageParser.usesBraces()) {
			for (; code.get(i).indexOf("}") < 0 && i > 0; i--)
				;
		}
		return i;
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
