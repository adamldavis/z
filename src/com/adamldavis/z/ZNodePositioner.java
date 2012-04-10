package com.adamldavis.z;

import java.awt.geom.Point2D;
import java.util.Map;

public interface ZNodePositioner {

	/** Get new positions of all nodes with range -1 to 1. */
	Map<ZNode, Point2D> getNewPositions(ZNode selected);
	
	
}
