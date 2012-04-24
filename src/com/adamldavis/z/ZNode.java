package com.adamldavis.z;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;
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
			String extension, File parentFile) {
		this();
		this.zNodeType = zNodeType;
		this.name = name;
		setCode(code);
		this.extension = extension;
		this.lastModified = parentFile.lastModified();
		this.parentFile = parentFile;
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
		final int x = (int) (location.x - size * 0.5);
		final int y = (int) (location.y - size * 0.5);
		final int isize = (int) size;

		g2d.setColor(g2d.getBackground());
		switch (zNodeType) {
		case CLASS:
			g2d.fillRect(x, y, isize, isize);
			g2d.setColor(color);
			g2d.drawRect(x, y, isize, isize);
			break;
		case METHOD:
			g2d.fillRect(x, y, isize, (int) (isize * 0.75));
			g2d.setColor(color);
			g2d.drawRect(x, y, isize, (int) (isize * 0.75));
			break;
		case MODULE:
			g2d.fillOval(x + 1, y + 1, isize - 2, isize - 2);
			g2d.setColor(color);
			g2d.drawOval(x, y, isize, isize);
			break;
		case PACKAGE:
			g2d.fillOval(x, y, isize, (int) (isize * 0.75));
			g2d.setColor(color);
			g2d.drawOval(x, y, isize, (int) (isize * 0.75));
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
		if (name != null) {
			g2d.setFont(g2d.getFont().deriveFont(Math.max(size * 16 / 80, 5)));
			g2d.drawString(name, x - 1, y - 1);
		}
		if (code != null) {
			Point point2 = new Point(x + isize + 1, y + isize / 2);
			final float codeSize = Math.max(size * 1 / 8, 5);
			g2d.setFont(g2d.getFont().deriveFont(codeSize));
			g2d.setPaint(new GradientPaint(location, color.darker(), point2,
					Color.BLACK));
			int i = 1;
			for (String line : code) {
				g2d.drawString(line.substring(0, Math.min(20, line.length())),
						x + 5, y + codeSize * i);
				if (i++ > 3)
					break;
			}
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

	// code related methods

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
		this.code.addAll(Arrays.asList(code.split("(\n\r)|\n")));
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
		if (code.isEmpty()) {
			return 0;
		}

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
