package com.adamldavis.z;

import java.awt.geom.Point2D;

// Assume left-to-right.

public class BloomZNodePositioner extends AbstractZNodePositioner {

	static final int maxSize = 15;

	@Override
	protected Point2D getDependencyPosition(ZNode node, int index, int size) {
		return getXPoint(-1.5, index, size);
	}

	@Override
	protected Point2D getSubmodulePosition(ZNode node, int index, int size) {
		return getXPoint(-0.5, index, size);
	}

	private Point2D getXPoint(final double start, int index, int size) {
		return getXPoint(getYAngle(start, index, size), index / maxSize);
	}

	private Point2D getXPoint(final double angle, int pow) {
		float x = (float) (Math.cos(angle) * 0.8 * Math.pow(0.5, pow));
		float y = (float) (Math.sin(angle) * 0.8 * Math.pow(0.5, pow));

		return new Point2D.Float(x, y);
	}

	private double getYAngle(final double start, int index, int size) {
		int smin = Math.min(maxSize, size);
		final double smin1 = smin + 1.0;

		return Math.PI * (start + (1.0 / smin1) + (index % maxSize / smin1));
	}

}
