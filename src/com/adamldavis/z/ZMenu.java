package com.adamldavis.z;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.swing.Swutil;
import com.adamldavis.z.Z.Direction;
import com.adamldavis.z.Z.NodeLayout;
import com.adamldavis.z.Z.SortOrder;

@SuppressWarnings("serial")
public class ZMenu extends JFrame {

	public static final String ABOUT_MSG = "Copyright 2012, Adam L. Davis, All rights reserved.";

	private static final Logger log = LoggerFactory.getLogger(ZMenu.class);

	public ZMenu(final Z z, final ActionListener actionListener) {
		super("Menu");

		final JMenu fileMenu = makeFileMenu(z, actionListener);
		final JMenu sorting = makeSortingMenu(z, actionListener);
		final JMenu layout = makeLayoutMenu(z, actionListener);
		final JMenu direction = makeDirectionMenu(z, actionListener);
		final JMenuItem up = makeItemGoUp(z, actionListener);
		final JMenu aboutMenu = makeAboutMenu(z, actionListener);
		JMenuBar bar = new JMenuBar();

		bar.add(fileMenu);
		bar.add(sorting);
		bar.add(layout);
		bar.add(direction);
		bar.add(up);
		bar.add(aboutMenu);
		super.setJMenuBar(bar);
		super.setAlwaysOnTop(true);
		super.setSize(300, 25);
		super.setUndecorated(true);
	}

	private JMenu makeAboutMenu(final Z z, final ActionListener listener) {
		final JMenu about = new JMenu("About");
		about.add("About").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(z, ABOUT_MSG);
				listener.actionPerformed(e);
			}
		});
		about.add("Help").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showHelp(z);
				listener.actionPerformed(e);
			}
		});
		return about;
	}

	private JMenu makeDirectionMenu(final Z z,
			final ActionListener actionListener) {
		final JMenu direction = new JMenu("Direction");
		direction.add("Left-to-Right").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.direction = Direction.LR;
				actionListener.actionPerformed(e);
			}
		});
		direction.add("Right-to-Left").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.direction = Direction.RL;
				actionListener.actionPerformed(e);
			}
		});
		direction.add("Down").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.direction = Direction.DOWN;
				actionListener.actionPerformed(e);
			}
		});
		direction.add("Up").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.direction = Direction.UP;
				actionListener.actionPerformed(e);
			}
		});
		return direction;
	}

	private JMenu makeLayoutMenu(final Z z, final ActionListener actionListener) {
		final JMenu layout = new JMenu("Layout");
		layout.add("Bloom").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.nodeLayout = NodeLayout.BLOOM;
				actionListener.actionPerformed(e);
			}
		});
		layout.add("Grid").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.nodeLayout = NodeLayout.GRID;
				actionListener.actionPerformed(e);
			}
		});
		layout.add("Random").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.nodeLayout = NodeLayout.RANDOM;
				actionListener.actionPerformed(e);
			}
		});
		return layout;
	}

	private JMenu makeSortingMenu(final Z z, final ActionListener actionListener) {
		final JMenu sorting = new JMenu("Sorting");
		sorting.add("Default").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.order = SortOrder.DEFAULT;
				actionListener.actionPerformed(e);
			}
		});
		sorting.add("Alphabetical").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.order = SortOrder.ALPHA;
				actionListener.actionPerformed(e);
			}
		});
		sorting.add("Time").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.order = SortOrder.TIME;
				actionListener.actionPerformed(e);
			}
		});
		sorting.add("Size").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.order = SortOrder.SIZE;
				actionListener.actionPerformed(e);
			}
		});
		return sorting;
	}

	private JMenu makeFileMenu(final Z z, final ActionListener actionListener) {
		final JMenu fileMenu = new JMenu("File");
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
					z.selectedNode = z.load(file);
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
		return fileMenu;
	}

	private JMenuItem makeItemGoUp(final Z z,
			final ActionListener actionListener) {
		final JMenuItem up = new JMenuItem("Go Up");
		up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.activateGoUp();
				actionListener.actionPerformed(e);
			}
		});
		return up;
	}

	public void showHelp(JFrame frame) {
		try {
			Swutil.showStaticPage(
					Thread.currentThread().getContextClassLoader()
							.getResource("./help.html").toURI(),
					frame.getSize(), frame.getLocation());
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
