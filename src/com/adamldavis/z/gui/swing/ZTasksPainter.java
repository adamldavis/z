/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Collection;

import com.adamldavis.z.tasks.ZTask;
import com.adamldavis.z.tasks.ZTaskList;

/**
 * @author Adam L. Davis
 * 
 */
public class ZTasksPainter extends Graphics2DPainter {

	final int y;

	final int height;

	final ZTask activeTask;

	public ZTasksPainter(Graphics2D graphics2d, int y, int height,
			ZTask activeTask) {
		super(graphics2d);
		this.y = y;
		this.height = height;
		this.activeTask = activeTask;
	}

	/**
	 * Paints the given Collection of Tasks.
	 * 
	 * @see com.adamldavis.z.gui.Painter#paint(java.lang.Object)
	 */
	@Override
	public void paint(Object object) {
		int x = 10;

		if (object instanceof ZTaskList) {
			ZTaskList taskList = (ZTaskList) object;
			Collection<ZTask> tasks = taskList.getTasks();

			for (ZTask task : tasks) {
				paintTask(x, task);
				x += task.getName().length() * height / 2 + 2;
			}
		}
	}

	public void paintTask(int x, final ZTask task) {
		final String name = task.getName();
		final int width = name.length() * height / 2 + 1;
		final Color pearl = new Color(245, 250, 250);

		if (task.equals(activeTask)) {
			graphics2d.setColor(Color.GREEN);
			graphics2d.setStroke(new BasicStroke(3f));
		} else {
			graphics2d.setColor(task.isComplete() ? Color.GRAY : pearl);
			graphics2d.setStroke(new BasicStroke(2f));
		}
		graphics2d.drawRect(x, y - height, width, height);
		graphics2d.setFont(graphics2d.getFont().deriveFont((float) height));
		graphics2d.drawString(name, x, y);
	}

}
