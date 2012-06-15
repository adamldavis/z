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

	/** Updates my positions to current node positions. */
	public void updatePositions() {
		for (ZNode node : nodes)
			positions.put(node, node.getLocation());
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	/** sets node positions to saved positions. */
	public void resetPositions() {
		for (ZNode node : nodes)
			node.setLocation(positions.get(node));
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public long getCreated() {
		return created;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ZTask) {
			ZTask task = (ZTask) obj;
			return name.equals(task.name);
		}
		return super.equals(obj);
	}

	public boolean contains(ZNode node) {
		return nodes.contains(node);
	}

	public ZTask(String name) {
		super();
		this.name = name;
	}

	public ZTask() {
		this("Task" + idNum.getAndIncrement());
	}

	public ZTask remove(ZNode node) {
		nodes.remove(node);
		positions.remove(node);
		return this;
	}

	public ZTask add(ZNode node) {
		nodes.add(node);
		positions.put(node,
				new Point2D.Float(node.getLocation().x, node.getLocation().y));
		return this;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Collection<ZNode> getNodes() {
		return Collections.unmodifiableCollection(nodes);
	}
}
