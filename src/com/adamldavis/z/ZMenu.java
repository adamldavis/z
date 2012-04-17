package com.adamldavis.z;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.adamldavis.z.Z.Direction;
import com.adamldavis.z.Z.NodeLayout;
import com.adamldavis.z.Z.SortOrder;

@SuppressWarnings("serial")
public class ZMenu extends JFrame {

	public ZMenu(final Z z, final ActionListener actionListener) {
		super("Menu");

		final JMenu fileMenu = new JMenu("File");
		final JMenu sorting = new JMenu("Sorting");
		final JMenu layout = new JMenu("Layout");
		final JMenu direction = new JMenu("Direction");
		
		fileMenu.add("Open...").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Open file or project folder");
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				chooser.showOpenDialog(z);

				File file = chooser.getSelectedFile();
				if (file != null) {
					z.selectedNode = new ZCodeLoader(z.apiFactory).load(file);
					z.settings.setProperty(UserSettings.LAST_LOCATION,
							file.getAbsolutePath());
				}
				actionListener.actionPerformed(e);
			}
		});
		fileMenu.add("Save").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ZCodeSaver(z.apiFactory).save(z.selectedNode);
				actionListener.actionPerformed(e);
			}
		});

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
		
		direction.add("Left-to-Right").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.direction = Direction.LR;
						actionListener.actionPerformed(e);
					}
				});
		direction.add("Right-to-Left").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.direction = Direction.RL;
						actionListener.actionPerformed(e);
					}
				});
		direction.add("Down").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.direction = Direction.DOWN;
						actionListener.actionPerformed(e);
					}
				});
		direction.add("Up").addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						z.direction = Direction.UP;
						actionListener.actionPerformed(e);
					}
				});

		JMenuBar bar = new JMenuBar();
		bar.add(fileMenu);
		bar.add(sorting);
		bar.add(layout);
		bar.add(direction);
		super.setJMenuBar(bar);
		super.setAlwaysOnTop(true);
		super.setSize(200, 25);
		super.setUndecorated(true);
	}

}
