package com.adamldavis.z;

import java.awt.geom.Point2D;
import java.util.Random;

public class RandomZNodePositioner extends AbstractZNodePositioner {

	final Random rnd = new Random(20120412);

	@Override
	protected Point2D getDependencyPosition(ZNode node, int index, int size) {
		return getPosition(index, size, -1f);
	}

	private Point2D getPosition(int index, int size, float x) {
		float y = 1.8f * rnd.nextFloat() - 0.8f;

		return new Point2D.Float(x * rnd.nextFloat(), y);
	}

	@Override
	protected Point2D getSubmodulePosition(ZNode node, int index, int size) {
		return getPosition(index, size, 1f);
	}

}
