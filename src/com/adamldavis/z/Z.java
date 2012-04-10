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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

	ZNode menu = new ZNode(100, 100, "MENU");
	{
		menu.zNodeType = ZNodeType.METHOD;
	}

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

		this.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) {
					point2 = e.getLocationOnScreen();
				}
			}
		});
		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				System.out.println("pressed: " + e);
				if (e.getButton() != MouseEvent.BUTTON3) {
					point1 = e.getLocationOnScreen();
				}
				ZNode z = findZNodeAt(e.getLocationOnScreen());
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
					ZNode z = findZNodeAt(e.getLocationOnScreen());
					if (z == null) {
						if (selectedNode != null) {
							// create sub-module
							ZNode sub = createNewZ(e.getLocationOnScreen());
							selectedNode.submodules.add(sub);
						}
					} else {
						dragged(z);
					}
					point1 = point2 = null;
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				final Point p = (new Point(e.getLocationOnScreen()));

				if (menu.location.distance(p.x, p.y) < size) {
					timer.stop();
					zMenu.setLocation(e.getLocationOnScreen());
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							zMenu.setVisible(true);
							zMenu.requestFocus();
						}
					});
					return;
				}
				ZNode z = findZNodeAt(p);
				if (z == null) {
					createNewZ(p);
				} else
					clicked(z);
			}
		});
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

	Map<ZNode, Point2D> pointMap;

	ZNodePositioner nodePositioner;

	protected void clicked(ZNode z) {
		System.out.println("selected: " + z);
		selectedNode = new ZCodeLoader(apiFactory).load(z);
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
		pointMap = nodePositioner.getNewPositions(selectedNode);
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
				node.draw(g2d, size + (count.get() / 10) % 10, Color.YELLOW);
			} else {
				node.draw(g2d, size, Color.WHITE);
			}
		}
		if (point1 != null && point2 != null) {
			drawLine(g2d, point1.x, point1.y, point2.x, point2.y);
		}
		menu.draw(g2d, 100, Color.GREEN);
	}

	void updateCount() {
		if (count.get() == 99) {
			count.set(199);
		} else if (count.get() == 101) {
			count.set(0);
		} else if (count.get() >= 100) {
			count.decrementAndGet();
		} else {
			count.incrementAndGet();
		}
	}

	ZNode findZNodeAt(Point p) {
		ZNode found = null;

		for (ZNode z : zNodes) {
			if (z.location.distance(p.x, p.y) < size) {
				found = z;
			}
		}
		return found;
	}

	ZNode createNewZ(Point p) {
		final String name = JOptionPane.showInputDialog(this,
				"Name for new module", "Z");
		if (name == null) {
			return null;
		}
		final ZNode zNode = new ZNode(p.x, p.y, name);
		zNodes.add(zNode);
		return zNode;
	}

}
