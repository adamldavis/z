package com.adamldavis.z;

import java.awt.geom.Point2D;

// Assume left-to-right.

public class BloomZNodePositioner extends AbstractZNodePositioner {

	@Override
	protected Point2D getDependencyPosition(ZNode node, int index, int size) {
		final double angle = Math.PI
				* (1.5 - (1.0 / (size + 1.0)) - (index / (size + 1.0)));
		return getPoint(angle);
	}

	private Point2D getPoint(final double angle) {
		float x = (float) (Math.cos(angle) * 0.95);
		float y = (float) (Math.sin(angle) * 0.95);

		return new Point2D.Float(x, y);
	}

	@Override
	protected Point2D getSubmodulePosition(ZNode node, int index, int size) {
		final double angle = Math.PI
				* (0.5 - (1.0 / (size + 1.0)) - (index / (size + 1.0)));
		return getPoint(angle);
	}

}
