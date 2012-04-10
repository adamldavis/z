package com.adamldavis.z;

import java.awt.geom.Point2D;

public class GridZNodePositioner extends AbstractZNodePositioner {

	@Override
	protected Point2D getDependencyPosition(ZNode node, int index, int size) {
		float x = 1 / (size + 1.0f) + index / ((float) size);
		float y = 0.1f;

		return new Point2D.Float(x, y);
	}

	@Override
	protected Point2D getSubmodulePosition(ZNode node, int index, int size) {
		float x = 1 / (size + 1.0f) + index / ((float) size);
		float y = 0.9f;

		return new Point2D.Float(x, y);
	}

}
