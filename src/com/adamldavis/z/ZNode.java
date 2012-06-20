package com.adamldavis.z;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.Serializable;
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
public class ZNode implements Serializable {

	private static final long serialVersionUID = 10101L;

	public enum ZNodeType {
		MODULE, PACKAGE, CLASS, METHOD, DEPENDENCY, CALLEE, CALLER
	};

	public enum NodeStatus {
		OKAY, FAIL, WARNING, TODO;
	}

	private Point2D.Float location;
	private float size = 80f; // size in 1:1 pixels
	private ZNodeType nodeType = ZNodeType.MODULE;

	private String name = "";
	private final List<String> code = new LinkedList<String>();

	private String extension = "";
	private File parentFile = new File("./");

	private final List<ZNode> dependencies = new ArrayList<ZNode>();
	private final List<ZNode> submodules = new ArrayList<ZNode>();
	private long lastModified = System.currentTimeMillis();

	private int lineNumber = 0; // only for things in files, like functions
	private ZNode parentNode; // null if no parent
	private int originalSize = 0;

	public ZNode() {
		setLocation(new Point2D.Float(0, 0));
	}

	public ZNode(float x, float y) {
		this(x, y, "Z");
	}

	public ZNode(float x, float y, String name) {
		setLocation(new Point2D.Float(x, y));
		this.setName(name);
	}

	public ZNode(ZNodeType zNodeType, String name, String code,
			String extension, File parentFile) {
		this();
		this.setNodeType(zNodeType);
		this.setName(name);
		setCode(code);
		this.originalSize = this.code.size();
		this.setExtension(extension);
		this.lastModified = parentFile.lastModified();
		this.setParentFile(parentFile);
	}

	public ZNode(ZNodeType zNodeType, String name, String code, int lineNumber,
			File parentFile) {
		this(zNodeType, name, code, "", parentFile);
		this.lineNumber = lineNumber;
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
		return getName();
	}

	public float getSize() {
		return size;
	}

	public void setSize(float size) {
		this.size = size;
	}

	public void setParentFile(File parentFile) {
		this.parentFile = parentFile;
	}

	public File getParentFile() {
		return parentFile;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setLocation(Point2D.Float location) {
		this.location = location;
	}

	public Point2D.Float getLocation() {
		return location;
	}

	public void setNodeType(ZNodeType zNodeType) {
		this.nodeType = zNodeType;
	}

	public ZNodeType getNodeType() {
		return nodeType;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public List<ZNode> getDependencies() {
		return dependencies;
	}

	public List<ZNode> getSubmodules() {
		return submodules;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ZNode) {
			ZNode node = (ZNode) o;
			return node.name.equals(name) && node.parentFile.equals(parentFile);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (name == null ? 0 : name.hashCode() * 13)
				+ (parentFile == null ? 0 : parentFile.hashCode());
	}

	Boolean hasTodo = null;

	/** If the code contains the keyword TODO. */
	public boolean hasTodo() {
		if (hasTodo == null) {
			hasTodo = false;
			for (String line : this.code) {
				if (line.contains("TODO")) {
					hasTodo = true;
					break;
				}
			}
		}
		return hasTodo;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public ZNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(ZNode parentNode) {
		this.parentNode = parentNode;
	}

	public int getOriginalSize() {
		return originalSize;
	}

}
