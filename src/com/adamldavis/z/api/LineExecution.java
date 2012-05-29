/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.api;


/**
 * The LineExecution will have a lineNumer corresponding to lines in the test
 * method starting at 0.
 * 
 * @author Adam L. Davis
 * 
 */
public interface LineExecution {

	int getLineNumber();

	String getMessage();

}
