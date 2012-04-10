/** Copyright 2012, The Solution Design Group, Inc. */
package com.adamldavis.z;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Map;

/**
 * @author Adam Davis
 * 
 */
public class PixelZNodePositioner implements ZNodePositioner {

	final ZNodePositioner positioner;

	final Dimension dimension;

	public PixelZNodePositioner(Dimension dimension, ZNodePositioner positioner) {
		super();
		this.positioner = positioner;
		this.dimension = dimension;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adamldavis.z.ZNodePositioner#getNewPositions(com.adamldavis.z.ZNode)
	 */
	@Override
	public Map<ZNode, Point2D> getNewPositions(ZNode selected) {
		Map<ZNode, Point2D> map = positioner.getNewPositions(selected);

		for (ZNode node : map.keySet()) {
			Point2D p = map.get(node);
			double halfH = dimension.height * 0.5;
			double halfW = dimension.width * 0.5;

			p.setLocation(halfW * (1 + p.getX()), halfH * (1 + p.getY()));
		}

		return map;
	}

}
