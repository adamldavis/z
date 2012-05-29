/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.runner;

import java.util.List;

import com.adamldavis.z.api.MethodExecution;
import com.adamldavis.z.api.Param;

/**
 * All fields are final.
 * 
 * @author Adam L. Davis
 * 
 */
public class MethodExecutionImpl extends LineExecutionImpl implements
		MethodExecution {

	private final String location;
	private final String methodName;
	private final List<Param> parameters;

	public MethodExecutionImpl(int lineNumber, String message, String location,
			String methodName, List<Param> parameters) {
		super(lineNumber, message);
		this.location = location;
		this.methodName = methodName;
		this.parameters = parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.MethodExecution#getParameters()
	 */
	@Override
	public List<Param> getParameters() {
		return parameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.MethodExecution#getMethodName()
	 */
	@Override
	public String getMethodName() {
		return methodName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.MethodExecution#getLocation()
	 */
	@Override
	public String getLocation() {
		return location;
	}

}
