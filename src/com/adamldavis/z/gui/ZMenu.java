/** Copyright 2012, Adam L. Davis. */
package com.adamldavis.z.gui;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
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

/**
 * 
 * @author Adam L. Davis
 */
public class ZMenu extends MouseAdapter implements MouseMotionListener,
		MouseListener {

	public static final String ABOUT_MSG = "Copyright 2012, Adam L. Davis, All rights reserved.";

	public static final int FONT_SIZE = 14;

	private static final Logger log = LoggerFactory.getLogger(ZMenu.class);

	private ProgressMonitor mon = new ProgressMonitor(null, "Gitting...",
			"Running git commands.", 0, 100);

	private boolean visible;

	private final ZMenuBar bar = new ZMenuBar("");

	ZMenu.Menu hoveredMenu;

	private int width;

	private int height;

	public static class NameNode {
		public NameNode(String name) {
			super();
			this.name = name;
		}

		private Point location = new Point(0, 0);
		final String name;

		public Point getLocation() {
			return location;
		}

		public void setLocation(Point location) {
			this.location = location;
		}
	}

	/** Represents a menu group. */
	public static class ZMenuBar extends NameNode {
		final List<Menu> menus = new ArrayList<ZMenu.Menu>();

		ZMenuBar(String name) {
			super(name);
		}

		public void add(Menu menu) {
			menus.add(menu);
		}

		public List<Menu> getMenus() {
			return menus;
		}
	}

	/** Represents a menu group. */
	public static class Menu extends NameNode {

		final List<MenuItem> items = new ArrayList<ZMenu.MenuItem>();

		public Menu(String name) {
			super(name);
		}

		MenuItem add(String menuTitle) {
			final MenuItem item = new MenuItem(menuTitle);
			items.add(item);
			return item;
		}

		public void add(MenuItem menu) {
			items.add(menu);
		}

		public List<MenuItem> getItems() {
			return items;
		}

		public String getName() {
			return name;
		}
	}

	/** Represents a menu item. */
	public static class MenuItem extends NameNode {
		final List<ActionListener> listeners = new LinkedList<ActionListener>();

		MenuItem(String name) {
			super(name);
		}

		public void addActionListener(ActionListener listener) {
			listeners.add(listener);
		}

		public String getName() {
			return name;
		}

		public List<ActionListener> getListeners() {
			return listeners;
		}

		public void click() {
			for (ActionListener al : listeners) {
				al.actionPerformed(null);
			}
		}
	}

	final ActionListener endingAction;

	public ZMenu(final Z z, final ActionListener actionListener) {
		endingAction = actionListener;
		final Menu fileMenu = makeFileMenu(z, actionListener);
		final Menu sorting = makeSortingMenu(z, actionListener);
		final Menu layout = makeLayoutMenu(z, actionListener);
		final Menu direction = makeDirectionMenu(z, actionListener);
		final Menu actionMenu = makeActionMenu(z, actionListener);
		final Menu aboutMenu = makeAboutMenu(z, actionListener);
		bar.add(fileMenu);
		bar.add(sorting);
		bar.add(layout);
		bar.add(direction);
		bar.add(actionMenu);
		bar.add(aboutMenu);
	}

	private Menu makeActionMenu(Z z, ActionListener actionListener) {
		final Menu actions = new Menu("Actions");
		final MenuItem up = makeItemGoUp(z, actionListener);
		final MenuItem time = makeItemTimeTravel(z, actionListener);
		actions.add(up);
		actions.add(time);
		return actions;
	}

	private MenuItem makeItemTimeTravel(final Z z,
			final ActionListener actionListener) {
		final MenuItem time = new MenuItem("Time-Travel");
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

	private Menu makeAboutMenu(final Z z, final ActionListener listener) {
		final Menu about = new Menu("About");
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

	private Menu makeDirectionMenu(final Z z,
			final ActionListener actionListener) {
		final Menu direction = new Menu("Direction");
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

	private Menu makeLayoutMenu(final Z z, final ActionListener actionListener) {
		final Menu layout = new Menu("Layout");
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

	private Menu makeSortingMenu(final Z z, final ActionListener actionListener) {
		final Menu sorting = new Menu("Sorting");
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

	private Menu makeFileMenu(final Z z, final ActionListener actionListener) {
		final Menu fileMenu = new Menu("File");
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

	private MenuItem makeItemGoUp(final Z z, final ActionListener actionListener) {
		final MenuItem up = new MenuItem("Go Up");
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

	public void setLocation(Point location) {
		int x = location.x, y = location.y;

		width = 10;
		height = getBar().getMenus().size() * FONT_SIZE + 2;
		for (ZMenu.Menu menu : getBar().getMenus()) {
			if (menu.getName().length() * FONT_SIZE / 2 > width)
				width = menu.getName().length() * FONT_SIZE / 2;
		}
		for (ZMenu.Menu menu : getBar().getMenus()) {
			menu.setLocation(new Point(x, y += FONT_SIZE));
		}
		this.bar.setLocation(location);
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public Point getLocation() {
		return bar.getLocation();
	}

	public ZMenuBar getBar() {
		return bar;
	}

	public ZMenu.Menu getHoveredMenu() {
		return hoveredMenu;
	}

	public void setHoveredMenu(ZMenu.Menu hoveredMenu) {
		this.hoveredMenu = hoveredMenu;
		if (hoveredMenu != null) {
			int x = hoveredMenu.getLocation().x;
			int y = hoveredMenu.getLocation().y - FONT_SIZE;
			for (ZMenu.MenuItem item : hoveredMenu.getItems()) {
				item.setLocation(new Point(x + width, y += FONT_SIZE));
			}
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// do nothing
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		final Point point = e.getPoint();
		if (!visible)
			return;

		if (hoveredMenu != null) {
			int x = hoveredMenu.getLocation().x + width;
			int y = hoveredMenu.getLocation().y - FONT_SIZE;
			if (point.x > x && point.y > y && point.x < x + width
					&& point.y < y + FONT_SIZE * hoveredMenu.getItems().size())
				return;
		}
		hoveredMenu = null;
		for (ZMenu.Menu menu : getBar().getMenus()) {
			int x = menu.getLocation().x;
			int y = menu.getLocation().y - FONT_SIZE;
			if (point.x > x && point.y > y && point.x < x + width
					&& point.y < y + FONT_SIZE)
				setHoveredMenu(menu);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (hoveredMenu != null) {
			for (ZMenu.MenuItem item : hoveredMenu.getItems()) {
				int x = item.getLocation().x;
				int y = item.getLocation().y - FONT_SIZE;
				int w = width;
				Point point = e.getPoint();
				if (point.x > x && point.y > y && point.x < x + w
						&& point.y < y + FONT_SIZE) {
					item.click();
					return;
				}
			}
		}
		endingAction.actionPerformed(null);
	}

}
