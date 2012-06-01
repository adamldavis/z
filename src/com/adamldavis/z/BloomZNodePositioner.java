package com.adamldavis.z;

import java.awt.geom.Point2D;

// Assume left-to-right.

public class BloomZNodePositioner extends AbstractZNodePositioner {

	// max to show in one bloom
	private static final int MAX = 15;

	@Override
	protected Point2D getDependencyPosition(ZNode node, int index, int size) {
		return getXPoint(-1.5, index, size);
	}

	@Override
	protected Point2D getSubmodulePosition(ZNode node, int index, int size) {
		return getXPoint(-0.5, index, size);
	}

	private Point2D getXPoint(final double start, int index, int size) {
		int halfSz = Math.max(1, size / 2);
		final int ring = size <= MAX ? 0 : (int) (index / halfSz);

		return getXPoint(getYAngle(start, index, size), ring);
	}

	// two rings 0 = outside, 1 = inside
	private Point2D getXPoint(final double angle, int ring) {
		float x = (float) (Math.cos(angle) * (0.8 - ring * 0.4));
		float y = (float) (Math.sin(angle) * (0.8 - ring * 0.4));

		return new Point2D.Float(x, y);
	}

	private double getYAngle(final double start, int index, int size) {
		assert size >= 0;
		int halfSz = Math.max(1, size / 2);
		final double hfsp1 = size <= MAX ? (size + 1) : (halfSz + 1);
		final int mod = size <= MAX ? index : index % halfSz;

		return Math.PI * (start + (1.0 / hfsp1) + (mod / hfsp1));
	}

}
