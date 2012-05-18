/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui;

/**
 * Painter interface with one method to ease the development of other GUIs
 * regardless of implementation.
 * 
 * @author Adam L. Davis
 * 
 */
public interface Painter {

	/**
	 * Renders the given object.
	 * 
	 * @param object
	 *            Object to paint.
	 */
	void paint(Object object);

}
