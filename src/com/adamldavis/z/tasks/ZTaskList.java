/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.tasks;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Adam L. Davis
 * 
 */
public class ZTaskList implements Serializable {

	private static final long serialVersionUID = 42L;

	private ZTask task1, task2;

	private final List<ZTask> tasks = new LinkedList<ZTask>();

	private boolean equal;

	public boolean testEquals() {
		task1 = new ZTask("Task A");
		task2 = new ZTask("Task B");
		equal = task1.equals(task2);
		task2.setName("Task A");
		equal = task1.equals(task2);
		return equal;
	}

	public ZTask addTask(final ZTask task) {
		tasks.add(task);
		return task;
	}

	public ZTask newTask() {
		final ZTask task = new ZTask();
		tasks.add(task);
		return task;
	}

	public List<ZTask> getTasks() {
		return tasks;
	}

}
