/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.history;

import java.io.File;
import java.io.Serializable;
import java.util.Stack;

import com.adamldavis.z.ZNode;

/**
 * Saves a stack for going back/forward through History much like a browser.
 * 
 * @author Adam L. Davis
 * 
 */
public class History implements Serializable {

	private static final long serialVersionUID = 12345L;

	public static class LocationNode {

		/** Location. */
		float x, y;

		float scale;

	}

	public static class HistoryNode implements Serializable {

		private static final long serialVersionUID = 1111L;

		/** lineNumber in the file if any. */
		int lineNumber = -1;

		/** file opened, if any. */
		File file;

		/** method name if one, otherwise module, package or class name. */
		String name;

		/** Not serialized, used during runtime. */
		transient ZNode node;

		public HistoryNode(int lineNumber, File file, String name) {
			super();
			this.lineNumber = lineNumber;
			this.file = file;
			this.name = name;
		}

		public HistoryNode(File file, String name) {
			super();
			this.file = file;
			this.name = name;
		}

		public HistoryNode(File file) {
			this(file, file.getName());
		}

	}

	Stack<HistoryNode> history = new Stack<History.HistoryNode>();

	public HistoryNode back() {
		return history.pop();
	}

	public HistoryNode peek() {
		return history.peek();
	}

	public HistoryNode add(HistoryNode node) {
		history.push(node);
		return node;
	}

	public HistoryNode add(File file) {
		return add(new HistoryNode(file));
	}

}
