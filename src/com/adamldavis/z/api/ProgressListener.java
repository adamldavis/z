/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.api;

/**
 * @author Adam L. Davis
 * 
 */
public interface ProgressListener {

	/** Set a number between 0 and 100. */
	void update(int progress);

}
