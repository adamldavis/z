package com.adamldavis.z;

import java.awt.geom.Point2D;

/** Determines the path along a line for a smooth animation. */
public class SmoothAnimator {

	enum AnimationType {
		EXP, COSINE, LINEAR
	};

	/**
	 * 
	 * @param p1
	 *            First point.
	 * @param p2
	 *            Second point.
	 * @param time
	 *            0 to 1.
	 * @param type
	 *            Type of animation.
	 * @return point in between p1 and p2.
	 */
	public Point2D animate(Point2D p1, Point2D p2, float time,
			AnimationType type) {
		Point2D.Double p = new Point2D.Double(0, 0);

		switch (type) {
		case COSINE:
			final double theta = Math.PI * 0.5 * time;
			final double cosine = Math.cos(theta);
			p.x = p1.getX() * cosine + p2.getX() * (1f - cosine);
			p.y = p1.getY() * cosine + p2.getY() * (1f - cosine);
			break;
		case EXP:
			p.x = p1.getX() * (1f - time * time) + p2.getX() * (time * time);
			p.y = p1.getY() * (1f - time * time) + p2.getY() * (time * time);
			break;
		case LINEAR:
			p.x = p1.getX() * (1f - time) + p2.getX() * (time);
			p.y = p1.getY() * (1f - time) + p2.getY() * (time);
			break;
		}
		return p;
	}

}
