package com.adamldavis.z.api;

/**
 * Represents a parameter, field, or variable.
 * 
 * @author Adam L. Davis
 */
public interface Param {

	/** Type such as int in Java. */
	String getType();

	/** A String representation of the value. */
	String getValue();

	/** Name of the parameter, field, or variable. */
	String getName();
}