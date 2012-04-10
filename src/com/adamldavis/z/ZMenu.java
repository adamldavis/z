package com.adamldavis.z;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.adamldavis.z.Z.NodeLayout;
import com.adamldavis.z.Z.SortOrder;

@SuppressWarnings("serial")
public class ZMenu extends JFrame {

	public ZMenu(final Z z, final ActionListener actionListener) {
		super("Menu");
		final JMenu sorting = new JMenu("Sorting");
		final JMenu layout = new JMenu("Layout");
		
		sorting.add("Default").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.order = SortOrder.DEFAULT;
						actionListener.actionPerformed(e);
					}
				});
		sorting.add("Alphabetical").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.order = SortOrder.ALPHA;
						actionListener.actionPerformed(e);
					}
				});
		sorting.add("Time").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.order = SortOrder.TIME;
						actionListener.actionPerformed(e);
					}
				});
		sorting.add("Size").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.order = SortOrder.SIZE;
						actionListener.actionPerformed(e);
					}
				});

		layout.add("Bloom").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.nodeLayout = NodeLayout.BLOOM;
						actionListener.actionPerformed(e);
					}
				});
		layout.add("Grid").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.nodeLayout = NodeLayout.GRID;
						actionListener.actionPerformed(e);
					}
				});
		layout.add("Random").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.nodeLayout = NodeLayout.RANDOM;
						actionListener.actionPerformed(e);
					}
				});

		JMenuBar bar = new JMenuBar();
		bar.add(sorting);
		bar.add(layout);
		super.setJMenuBar(bar);
		super.setAlwaysOnTop(true);
		super.setSize(200, 25);
		super.setUndecorated(true);
	}

}
