/** Copyright 2012, The Solution Design Group, Inc. */
package com.adamldavis.z;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;

/**
 * Converts positions to pixels using given center and dimensions.
 * 
 * @author Adam Davis
 * 
 */
public class PixelZNodePositioner implements ZNodePositioner {

	final ZNodePositioner positioner;

	final Dimension dimension;

	final Point center;

	public PixelZNodePositioner(Point center, Dimension dimension,
			ZNodePositioner positioner) {
		super();
		this.positioner = positioner;
		this.dimension = dimension;
		this.center = center;
	}

	public PixelZNodePositioner(Dimension dimension, ZNodePositioner positioner) {
		this(new Point(dimension.width / 2, dimension.height / 2), dimension,
				positioner);
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

			p.setLocation(center.x + halfW * p.getX(),
					center.y + halfH * p.getY());
		}

		return map;
	}

}
