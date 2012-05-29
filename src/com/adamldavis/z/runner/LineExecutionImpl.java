package com.adamldavis.z.runner;

import com.adamldavis.z.api.LineExecution;

/**
 * All fields are final except lineNumber.
 * 
 * @author Adam L. Davis
 * 
 */
public class LineExecutionImpl implements LineExecution {

	protected int lineNumber;
	protected final String message;

	public LineExecutionImpl(int lineNumber, String message) {
		super();
		this.lineNumber = lineNumber;
		this.message = message;
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public String toString() {
		return message;
	}

}