/** Copyright 2012, The Solution Design Group, Inc. */
package com.adamldavis.z;

import java.awt.geom.Point2D;
import java.util.Map;

import com.adamldavis.z.Z.Direction;

/**
 * @author Adam Davis
 * 
 */
public class DirectionZNodePositioner implements ZNodePositioner {

	final ZNodePositioner positioner;

	final Direction direction;

	public DirectionZNodePositioner(Direction direction,
			ZNodePositioner positioner) {
		super();
		this.positioner = positioner;
		this.direction = direction;
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

		switch (direction) {
		case RL:
			for (ZNode node : map.keySet()) {
				Point2D p = map.get(node);
				p.setLocation(-1.0 * p.getX(), p.getY());
			}
			break;
		case DOWN:
			for (ZNode node : map.keySet()) {
				Point2D p = map.get(node);
				p.setLocation(p.getY(), p.getX());
			}
			break;
		case UP:
			for (ZNode node : map.keySet()) {
				Point2D p = map.get(node);
				p.setLocation(-1 * p.getY(), p.getX());
			}
			break;
		default:
		}

		return map;
	}

}
