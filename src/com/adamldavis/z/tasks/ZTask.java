/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.tasks;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.adamldavis.z.ZNode;

/**
 * A group of ZNodes being worked with.
 * 
 * @author Adam L. Davis
 * 
 */
public class ZTask implements Serializable {

	private static final long serialVersionUID = 6424781902318063687L;

	private static final AtomicInteger idNum = new AtomicInteger(1);

	private String name;

	private String description;

	private long created = System.currentTimeMillis();

	boolean complete = false;

	private final Collection<ZNode> nodes = new LinkedHashSet<ZNode>();

	private final Map<ZNode, Point2D.Float> positions = new HashMap<ZNode, Point2D.Float>();

	public ZTask() {
		this("Task" + idNum.getAndIncrement());
	}

	public ZTask(String name) {
		super();
		this.name = name;
	}

	public ZTask add(ZNode node) {
		nodes.add(node);
		positions.put(node,
				new Point2D.Float(node.getLocation().x, node.getLocation().y));
		return this;
	}

	/** Updates my positions to current node positions. */
	public void updatePositions() {
		for (ZNode node : nodes)
			positions.put(node, node.getLocation());
	}

	/** sets node positions to saved positions. */
	public void resetPositions() {
		for (ZNode node : nodes)
			node.setLocation(positions.get(node));
	}

	public boolean contains(ZNode node) {
		return nodes.contains(node);
	}

	public ZTask remove(ZNode node) {
		nodes.remove(node);
		positions.remove(node);
		return this;
	}

	public Collection<ZNode> getNodes() {
		return Collections.unmodifiableCollection(nodes);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public long getCreated() {
		return created;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ZTask) {
			ZTask task = (ZTask) obj;
			return name.equals(task.name);
		}
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return name;
	}

}
