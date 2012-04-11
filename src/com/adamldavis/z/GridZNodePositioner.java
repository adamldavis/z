package com.adamldavis.z;

import java.awt.geom.Point2D;

public class GridZNodePositioner extends AbstractZNodePositioner {

	@Override
	protected Point2D getDependencyPosition(ZNode node, int index, int size) {
		float x = -0.8f;
		return getPosition(index, size, x);
	}

	private Point2D getPosition(int index, int size, float x) {
		int maxSize = 6;
		int smin = Math.min(maxSize, size), imod = index % maxSize;
		float y = 1 / (smin + 1.0f) + imod / (smin + 1.0f);

		return new Point2D.Float(x * (1f - (index / maxSize) / 4f), 2f * y - 1f);
	}

	@Override
	protected Point2D getSubmodulePosition(ZNode node, int index, int size) {
		float x = 0.8f;
		return getPosition(index, size, x);
	}

}
