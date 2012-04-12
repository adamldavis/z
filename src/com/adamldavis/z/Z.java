/** Copyright 2012, Adam L. Davis. */
package com.adamldavis.z;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.adamldavis.swing.Display2d;
import com.adamldavis.z.SmoothAnimator.AnimationType;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.java.JavaFactory;

/**
 * Main Window and Main class of Z program.
 * 
 * @author Adam Davis
 * 
 */
@SuppressWarnings("serial")
public class Z extends Display2d {

	/** what's happening right now. */
	enum State {
		NORMAL, SELECTING, ANIMATING
	};

	/** organization of nodes. */
	enum NodeLayout {
		BLOOM, RANDOM, GRID
	}

	/** How to order nodes. */
	enum SortOrder {
		DEFAULT, ALPHA, TIME, SIZE
	}

	/** Direction from "dependencies" to "sub-modules". */
	enum Direction {
		LR, RL, UP, DOWN
	}

	public static void main(String[] args) {
		new Z();
	}

	final APIFactory apiFactory = new JavaFactory();

	/* Mouse points on screen. */
	Point point1, point2;

	State state = State.NORMAL;

	NodeLayout nodeLayout = NodeLayout.BLOOM;

	Direction direction = Direction.LR;

	SortOrder order = SortOrder.DEFAULT;

	UserSettings settings = new UserSettings();

	ZNode goUp = new ZNode(600, 100, "^");

	ZNode menu = new ZNode(500, 100, "MENU");

	ZMenu zMenu = new ZMenu(Z.this, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			timer.start();
			if (selectedNode != null) {
				clicked(selectedNode);
			}
			zMenu.setVisible(false);
			saveSettings();
		}
	});

	public Z() {
		super(false, 2, 33);

		loadSettings();
		menu.zNodeType = ZNodeType.METHOD;
		goUp.zNodeType = ZNodeType.METHOD;
		goUp.code = "go up";
		menu.location.x = width / 2 - size;
		goUp.location.x = menu.location.x + size;

		this.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) {
					point2 = e.getPoint();
				}
			}
		});
		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				System.out.println("pressed: " + e);
				if (e.getButton() != MouseEvent.BUTTON3) {
					point1 = e.getPoint();
				}
				ZNode z = findZNodeAt(e.getPoint());
				if (z != null) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						clicked(z);
					} else {
						// new code editor
						ZCodeEditor editor = new ZCodeEditor(selectedNode,
								apiFactory);
						editor.setSize(width / 2, height / 2);
						editor.setLocation(width / 4, height / 4);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("released: " + e);
				if (point1 != null && point2 != null) {
					ZNode z = findZNodeAt(e.getPoint());
					if (z == null) {
						createSubNode(e);
					} else {
						dragged(z);
					}
					point1 = point2 = null;
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				final Point p = e.getPoint();

				if (menu.location.distance(p.x, p.y) < size * 0.5) {
					activateMenu(e);
					return;
				} else if (goUp.location.distance(p.x, p.y) < size * 0.5) {
					activateGoUp();
					return;
				}
				ZNode z = findZNodeAt(p);
				if (z == null) {
					if (selectedNode == null) {
						selectedNode = createNewZ(p, ZNodeType.MODULE);
					} else {
						final ZNode dep = createNewZ(p, ZNodeType.DEPENDENCY);
						if (dep != null)
							selectedNode.dependencies.add(dep);
					}
				} else
					clicked(z);
			}
		});
	}

	protected void activateMenu(MouseEvent e) {
		timer.stop();
		zMenu.setLocation(e.getLocationOnScreen());
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				zMenu.setVisible(true);
				zMenu.requestFocus();
			}
		});
	}

	protected void activateGoUp() {
		final File pFile = selectedNode.parentFile;
		selectedNode = new ZCodeLoader(apiFactory)
				.load((selectedNode.zNodeType == ZNodeType.PACKAGE) ? pFile
						.getParentFile() : pFile);
		clicked(selectedNode);
	}

	private void loadSettings() {
		if (settings.getProperty(UserSettings.DIRECTION) != null) {
			direction = Direction.valueOf(settings
					.getProperty(UserSettings.DIRECTION));
			nodeLayout = NodeLayout.valueOf(settings
					.getProperty(UserSettings.LAYOUT));
			order = SortOrder.valueOf(settings.getProperty(UserSettings.ORDER));
		}
		if (settings.getProperty(UserSettings.LAST_LOCATION) != null) {
			selectedNode = new ZCodeLoader(apiFactory).load(settings
					.getFile(UserSettings.LAST_LOCATION));
			clicked(selectedNode);
		}
	}

	public void saveSettings() {
		settings.setProperty(UserSettings.DIRECTION, direction.toString());
		settings.setProperty(UserSettings.LAYOUT, nodeLayout.toString());
		settings.setProperty(UserSettings.ORDER, order.toString());
		settings.save();
	}

	protected void dragged(ZNode z) {
		System.out.println("dragged: " + z);
		// create dependency on z?
		selectedNode.dependencies.add(z);
	}

	AtomicInteger count = new AtomicInteger(0);

	AtomicInteger aniCount = new AtomicInteger(0);

	final Map<ZNode, Point2D> pointMap = new HashMap<ZNode, Point2D>();

	ZNodePositioner nodePositioner;

	protected void clicked(ZNode node) {
		System.out.println("selected: " + node);
		selectedNode = new ZCodeLoader(apiFactory).load(node);
		zNodes.clear();
		zNodes.add(selectedNode);
		zNodes.addAll(selectedNode.dependencies);
		zNodes.addAll(selectedNode.submodules);
		count.set(0);

		state = State.ANIMATING;
		aniCount.set(0);
		nodePositioner = new PixelZNodePositioner(new Dimension(width, height),
				new DirectionZNodePositioner(direction, makeNodePositioner()));
		sortNodes();
		pointMap.putAll(nodePositioner.getNewPositions(selectedNode));
	}

	private void sortNodes() {
		Collections.sort(selectedNode.submodules, new Comparator<ZNode>() {

			@Override
			public int compare(ZNode node1, ZNode node2) {
				switch (order) {
				case ALPHA:
					return node1.name.compareTo(node2.name);
				case SIZE:
					return node1.code.length() - node2.code.length();
				case TIME:
					return (int) (node1.getLastModified() - node2
							.getLastModified());
				default:
					return 0;
				}
			}

		});
	}

	private ZNodePositioner makeNodePositioner() {
		switch (nodeLayout) {
		case BLOOM:
			return new BloomZNodePositioner();
		case RANDOM:
			return new RandomZNodePositioner();
		default:
			return new GridZNodePositioner();
		}
	}

	SmoothAnimator animator = new SmoothAnimator();

	ZNode selectedNode;

	final List<ZNode> zNodes = new ArrayList<ZNode>();

	float size = 80;

	@Override
	protected void paintBuffered(Graphics2D g2d) {
		if (state == State.ANIMATING && aniCount.incrementAndGet() > 100) {
			state = State.NORMAL;
		}
		g2d.addRenderingHints(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
		g2d.setColor(Color.black);
		g2d.fillRect(0, 0, width, height);
		if (zNodes == null) {
			return;
		}
		final float time = aniCount.get() / 100f;

		for (ZNode node : zNodes) {
			if (state == State.ANIMATING) {
				if (pointMap.containsKey(node)) {
					node.location.setLocation(animator.animate(node.location,
							pointMap.get(node), time, AnimationType.COSINE));
				}
			}
			if (node == selectedNode) {
				updateCount();
				node.draw(g2d, size + (count.get() / 10) * 2, Color.YELLOW);
			} else {
				node.draw(g2d, size, Color.WHITE);
			}
		}
		if (point1 != null && point2 != null) {
			drawLine(g2d, point1.x, point1.y, point2.x, point2.y);
		}
		menu.draw(g2d, size, Color.GREEN);
		goUp.draw(g2d, size, Color.GREEN);
	}

	private void updateCount() {
		count.incrementAndGet();
		if (count.get() >= 20) {
			count.set(0);
		}
	}

	ZNode findZNodeAt(Point p) {
		ZNode found = null;

		for (ZNode z : zNodes) {
			if (z.location.distance(p.x, p.y) < size * 0.5) {
				found = z;
			}
		}
		return found;
	}

	protected void createSubNode(MouseEvent e) {
		if (selectedNode != null && selectedNode.zNodeType != ZNodeType.METHOD
				&& selectedNode.zNodeType != ZNodeType.DEPENDENCY) {
			// create sub-module
			final ZNodeType subtype;
			switch (selectedNode.zNodeType) {
			case CLASS:
				subtype = ZNodeType.METHOD;
				break;
			case PACKAGE:
				subtype = ZNodeType.CLASS;
				break;
			default:
				subtype = ZNodeType.PACKAGE;
			}
			ZNode sub = createNewZ(e.getPoint(), subtype);
			if (sub != null)
				selectedNode.submodules.add(sub);
		}
	}

	ZNode createNewZ(Point p, ZNodeType type) {
		final String name = JOptionPane.showInputDialog(this, "Name for new "
				+ type.name(), "Z");
		if (name == null) {
			return null;
		}
		final ZNode zNode = new ZNode(p.x, p.y, name.trim());
		zNode.zNodeType = type;
		zNode.parentFile = selectedNode.parentFile;
		zNodes.add(zNode);
		if (type == ZNodeType.METHOD) {
			zNode.parentFile = new File(selectedNode.parentFile,
					selectedNode.name + "." + selectedNode.extension);
			String[] split = selectedNode.code.split("[\n\r]{1,2}");
			zNode.extension = String.valueOf(split.length - 1);
			System.err.println("ext=" + zNode.extension);
		}
		new ZCodeSaver(apiFactory).save(zNode);
		return zNode;
	}

}
