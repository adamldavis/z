package com.adamldavis.z.gui.swing;

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
import javax.swing.ProgressMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.swing.Swutil;
import com.adamldavis.z.UserSettings;
import com.adamldavis.z.Z;
import com.adamldavis.z.Z.Direction;
import com.adamldavis.z.Z.NodeLayout;
import com.adamldavis.z.Z.SortOrder;
import com.adamldavis.z.ZCodeSaver;
import com.adamldavis.z.api.ProgressListener;
import com.adamldavis.z.git.GitLogDiffsMap;

@SuppressWarnings("serial")
public class ZMenu extends JFrame {

	public static final String ABOUT_MSG = "Copyright 2012, Adam L. Davis, All rights reserved.";

	private static final Logger log = LoggerFactory.getLogger(ZMenu.class);

	private ProgressMonitor mon = new ProgressMonitor(null, "Gitting...",
			"Running git commands.", 0, 100);

	public ZMenu(final Z z, final ActionListener actionListener) {
		super("Menu");

		final JMenu fileMenu = makeFileMenu(z, actionListener);
		final JMenu sorting = makeSortingMenu(z, actionListener);
		final JMenu layout = makeLayoutMenu(z, actionListener);
		final JMenu direction = makeDirectionMenu(z, actionListener);
		final JMenu actionMenu = makeActionMenu(z, actionListener);
		final JMenu aboutMenu = makeAboutMenu(z, actionListener);
		JMenuBar bar = new JMenuBar();

		bar.add(fileMenu);
		bar.add(sorting);
		bar.add(layout);
		bar.add(direction);
		bar.add(actionMenu);
		bar.add(aboutMenu);
		super.setJMenuBar(bar);
		super.setAlwaysOnTop(true);
		super.setSize(300, 25);
		super.setUndecorated(true);
	}

	private JMenu makeActionMenu(Z z, ActionListener actionListener) {
		final JMenu actions = new JMenu("Actions");
		final JMenuItem up = makeItemGoUp(z, actionListener);
		final JMenuItem time = makeItemTimeTravel(z, actionListener);
		actions.add(up);
		actions.add(time);
		return actions;
	}

	private JMenuItem makeItemTimeTravel(final Z z,
			final ActionListener actionListener) {
		final JMenuItem time = new JMenuItem("Time-Travel");
		time.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionListener.actionPerformed(e);
				new Thread() {
					@Override
					public void run() {
						z.getAniCount().set(0);
						z.diffsMap = new GitLogDiffsMap(z.getSelectedNode()
								.getParentFile());
						z.diffsMap.runDiff(new ProgressListener() {
							@Override
							public void update(int progress) {
								mon.setProgress(progress);
							}
						});
						log.info("size={}", z.diffsMap.getLogSize());
						synchronized (z.getZNodes()) {
							z.diffsMap.removeDiffNodesFrom(z.getZNodes());
							mon.setProgress(100);
							z.setState(Z.State.TIME_TRAVEL);
						}
					}
				}.start();
			}
		});
		return time;
	}

	private JMenu makeAboutMenu(final Z z, final ActionListener listener) {
		final JMenu about = new JMenu("About");
		about.add("About").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(z.getDisplay(), ABOUT_MSG);
				listener.actionPerformed(e);
			}
		});
		about.add("Help").addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showHelp(z.getDisplay());
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
				z.setDirection(Direction.LR);
				actionListener.actionPerformed(e);
			}
		});
		direction.add("Right-to-Left").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setDirection(Direction.RL);
				actionListener.actionPerformed(e);
			}
		});
		direction.add("Down").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setDirection(Direction.DOWN);
				actionListener.actionPerformed(e);
			}
		});
		direction.add("Up").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setDirection(Direction.UP);
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
				z.setNodeLayout(NodeLayout.BLOOM);
				actionListener.actionPerformed(e);
			}
		});
		layout.add("Grid").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setNodeLayout(NodeLayout.GRID);
				actionListener.actionPerformed(e);
			}
		});
		layout.add("Random").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setNodeLayout(NodeLayout.RANDOM);
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
				z.setOrder(SortOrder.DEFAULT);
				actionListener.actionPerformed(e);
			}
		});
		sorting.add("Alphabetical").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setOrder(SortOrder.ALPHA);
				actionListener.actionPerformed(e);
			}
		});
		sorting.add("Time").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setOrder(SortOrder.TIME);
				actionListener.actionPerformed(e);
			}
		});
		sorting.add("Size").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				z.setOrder(SortOrder.SIZE);
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
				chooser.showOpenDialog(z.getDisplay());

				File file = chooser.getSelectedFile();
				if (file != null) {
					z.load(file);
					z.getSettings().setProperty(UserSettings.LAST_LOCATION,
							file.getAbsolutePath());
				}
				actionListener.actionPerformed(e);
			}
		});
		fileMenu.add("Save").addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ZCodeSaver(z.getApiFactory()).save(z.getSelectedNode());
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
			Swutil.showStaticPage(Z.class.getResource("help.html").toURI(),
					frame.getSize(), frame.getLocation());
		} catch (URISyntaxException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
