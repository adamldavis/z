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

	private final List<ZTask> tasks = new LinkedList<ZTask>();

	private ZTask activeTask;

	public ZTaskList() {
		tasks.add(new ZTask("Task"));
	}

	public void clear() {
		tasks.clear();
	}

	public ZTask addTask(final ZTask task) {
		tasks.add(task);
		return task;
	}

	/**
	 * Gets ZTask at given x-position if any.
	 * 
	 * @param xPostn
	 *            X-position of mouse assuming y is within task area.
	 * @param height
	 *            Height of task area.
	 * @return ZTask clicked on.
	 */
	public ZTask getTaskAt(int xPostn, int height) {
		int x = 10;

		for (ZTask task : tasks) {
			int prevX = x;
			x += task.getName().length() * height / 2 + 2;
			if (xPostn >= prevX && xPostn <= x) {
				return task;
			}
		}
		return null;
	}

	public ZTask newTask() {
		final ZTask task = new ZTask();
		tasks.add(task);
		return task;
	}

	public List<ZTask> getTasks() {
		return tasks;
	}

	public ZTask getActiveTask() {
		return activeTask;
	}

	public void setActiveTask(ZTask activeTask) {
		this.activeTask = activeTask;
		if (activeTask != null) {
			activeTask.resetPositions();
		}
	}

}
