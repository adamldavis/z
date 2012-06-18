/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui.swing;

import java.awt.Color;

/**
 * Utility functions for the java.awt.Color class.
 * 
 * @author Adam L. Davis
 * 
 */
public class ColorUtil {

	/**
	 * Finds the closest hue possible to given color.
	 * 
	 * @param color
	 *            The color to match.
	 * @return Hue float value between 0 and 1.
	 */
	public static float findHue(Color color) {
		float[] hsbvals = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(),
				hsbvals);
		return hsbvals[0];
	}
}
