package com.adamldavis.z;

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
	private float size = 80f; // size in 1:1 pixels
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

	public float getSize() {
		return size;
	}

}
