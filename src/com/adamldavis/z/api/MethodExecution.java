/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.api;

import java.util.List;

/**
 * @author Adam L. Davis
 * 
 */
public interface MethodExecution extends LineExecution {

	/** Parameters passed to the method. */
	List<Param> getParameters();

	/** Name of the method called. */
	String getMethodName();

	/** Class or file containing the method. */
	String getLocation();

}
