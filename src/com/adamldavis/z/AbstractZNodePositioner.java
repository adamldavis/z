package com.adamldavis.z;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractZNodePositioner implements ZNodePositioner {

	@Override
	public Map<ZNode, Point2D> getNewPositions(ZNode selected) {
		final Map<ZNode, Point2D> map = new HashMap<ZNode, Point2D>();
		int i = 0;

		map.put(selected, new Point2D.Float(0, 0));

		for (ZNode node : selected.getDependencies()) {
			map.put(node,
					getDependencyPosition(node, i++,
							selected.getDependencies().size()));
		}
		i = 0;
		for (ZNode node : selected.getSubmodules()) {
			map.put(node,
					getSubmodulePosition(node, i++, selected.getSubmodules().size()));
		}
		return map;
	}

	protected abstract Point2D getDependencyPosition(ZNode node, int index, int size);

	protected abstract Point2D getSubmodulePosition(ZNode node, int index, int size);

}
