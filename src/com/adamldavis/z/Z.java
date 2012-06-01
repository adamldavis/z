/** Copyright 2012, Adam L. Davis. */
package com.adamldavis.z;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.swing.Swutil;
import com.adamldavis.z.SmoothAnimator.AnimationType;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.Editor;
import com.adamldavis.z.editor.ZCodeEditor;
import com.adamldavis.z.editor.ZCodeEditorPlus;
import com.adamldavis.z.gui.swing.ZDisplay;
import com.adamldavis.z.gui.swing.ZMenu;

/**
 * Main class of Z program.
 * 
 * @author Adam Davis
 * 
 */
public class Z {

	/** what's happening right now. */
	public enum State {
		NORMAL, SELECTING, ANIMATING, EDITING
	};

	/** organization of nodes. */
	public enum NodeLayout {
		BLOOM, RANDOM, GRID
	}

	/** How to order nodes. */
	public enum SortOrder {
		DEFAULT, ALPHA, TIME, SIZE
	}

	/** Direction from "dependencies" to "sub-modules". */
	public enum Direction {
		LR, RL, UP, DOWN
	}

	private static final Logger log = LoggerFactory.getLogger(Z.class);

	public static void main(String[] args) {
		new Z();
	}

	APIFactory apiFactory;

	/* Mouse points on screen. */
	Point point1, point2;

	State state = State.NORMAL;

	NodeLayout nodeLayout = NodeLayout.BLOOM;

	Direction direction = Direction.LR;

	ZNode draggedNode;

	final List<Editor> editors = new LinkedList<Editor>();

	SortOrder order = SortOrder.DEFAULT;

	UserSettings settings = new UserSettings();

	final ZMenu zMenu = new ZMenu(Z.this, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedNode != null) {
				clicked(selectedNode);
			}
			zMenu.setVisible(false);
			saveSettings();
		}
	});

	ZDisplay display = new ZDisplay(this);

	Timer timer = new Timer("Z timer", true);

	private ZNode hoveredNode;

	private String hoverText;

	private Point mouseLocation;

	public Z() {
		display.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					log.debug("zoom:" + e.getWheelRotation());
					if (e.getWheelRotation() > 0 && scale > 0.125f) {
						scale /= 2f;
					} else if (e.getWheelRotation() < 0 && scale < 32) {
						scale *= 2f;
					}
					log.debug("Scale:" + scale);
					if (!editors.isEmpty()) {
						for (Editor editor : editors) {
							editor.setScale(scale);
						}
					}
				}
			}
		});
		display.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON3) {
					point2 = e.getPoint();
				}
			}
		});
		display.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				log.info("pressed: " + e);
				if (e.getButton() != MouseEvent.BUTTON3) {
					point1 = e.getPoint();
				}
				ZNode z = findZNodeAt(e.getPoint());
				if (z != null) {
					if (e.isControlDown()) {
						// new code editor
						showNewEditor(z);
					} else if (e.getButton() == MouseEvent.BUTTON1) {
						draggedNode = z;
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				log.info("released: " + e);
				if (point1 != null && point2 != null) {
					if (draggedNode == null) {
						ZNode z = findZNodeAt(e.getPoint());
						if (z == null) {
							createSubNode(e.getPoint());
						} else
							dragged(z);
					} else {
						draggedNode.getLocation().setLocation(
								translateToZNodePoint(point2));
						draggedNode = null;
					}
					point1 = point2 = null;
				}
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				final Point p = e.getPoint();
				ZNode z = findZNodeAt(p);

				if (z == null) {
					zMenu.setVisible(false);
					if (e.getButton() == MouseEvent.BUTTON3) {
						activateMenu(e);
					} else if (selectedNode == null) {
						selectedNode = createNewZ(p, ZNodeType.MODULE);
					} else {
						final ZNode dep = createNewZ(p, ZNodeType.DEPENDENCY);
						if (dep != null)
							selectedNode.getDependencies().add(dep);
					}
				} else if (e.getButton() == MouseEvent.BUTTON1
						&& !e.isControlDown()) {
					clicked(z);
				}
			}
		});
		display.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				final ZNode node = findZNodeAt(e.getPoint());
				if (node == selectedNode) {
					return;
				}
				if (hoveredNode != node && hoveredNode != null) {
					hoveredNode.setSize(hoveredNode.getSize() * 1f / 1.1f);
				}
				if (node != null && hoveredNode != node) {
					node.setSize(node.getSize() * 1.1f);
				}
				hoveredNode = node;
				hoverText = node == null ? null : node.getName();
				mouseLocation = e.getPoint();
			}
		});
		zfactory = new ZFactory(Z.class.getResourceAsStream("z.properties"));
		loadSettings();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				Z.this.run();
			}
		}, 33, 33);
	}

	protected void activateMenu(MouseEvent e) {
		zMenu.setLocation(e.getLocationOnScreen());
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				zMenu.setVisible(true);
				zMenu.requestFocus();
			}
		});
	}

	public void activateGoUp() {
		File pFile = selectedNode.getParentFile();

		if (selectedNode.getNodeType() == ZNodeType.PACKAGE) {
			for (int i = 0; i < selectedNode.getName().split("\\.").length; i++) {
				pFile = pFile.getParentFile();
			}
		} else if (selectedNode.getNodeType() == ZNodeType.MODULE) {
			pFile = pFile.getParentFile();
		}
		selectedNode = new ZCodeLoader(apiFactory).load(pFile);
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
			final File file = settings.getFile(UserSettings.LAST_LOCATION);
			clicked(load(file));
		}
	}

	public void saveSettings() {
		settings.setProperty(UserSettings.DIRECTION, direction.toString());
		settings.setProperty(UserSettings.LAYOUT, nodeLayout.toString());
		settings.setProperty(UserSettings.ORDER, order.toString());
		settings.save();
	}

	protected void dragged(ZNode z) {
		log.info("dragged: " + z);
		// create dependency on z?
		if (z.getNodeType() == ZNodeType.DEPENDENCY)
			selectedNode.getDependencies().add(z);
	}

	AtomicInteger count = new AtomicInteger(0);

	AtomicInteger aniCount = new AtomicInteger(0);

	final Map<ZNode, Point2D> pointMap = new HashMap<ZNode, Point2D>();

	final Map<ZNode, Float> sizeMap = new HashMap<ZNode, Float>();

	ZNodePositioner nodePositioner;

	protected void clicked(ZNode node) {
		log.info("selected: " + node);
		selectedNode = new ZCodeLoader(apiFactory).load(node);
		zNodes.clear();
		zNodes.add(selectedNode);
		zNodes.addAll(selectedNode.getDependencies());
		zNodes.addAll(selectedNode.getSubmodules());
		count.set(0);

		state = State.ANIMATING;
		aniCount.set(0);
		final Dimension dim = display.getDimension();
		nodePositioner = new PixelZNodePositioner(dim,
				new DirectionZNodePositioner(direction, makeNodePositioner()));
		sortNodes();
		pointMap.putAll(nodePositioner.getNewPositions(selectedNode));
		sizeMap.put(selectedNode,
				(float) Math.min(dim.getWidth(), dim.getHeight()) / 2);
		Map<ZNode, Point2D> depMap = new HashMap<ZNode, Point2D>();

		for (ZNode dep : selectedNode.getDependencies()) {
			depMap.put(dep, pointMap.get(dep));
		}
		for (ZNode sub : selectedNode.getSubmodules()) {
			Point2D loc = pointMap.get(sub);
			final Point center = new Point((int) Math.round(loc.getX()),
					(int) Math.round(loc.getY()));
			sub = new ZCodeLoader(apiFactory).load(sub);
			float size = (float) (dim.getHeight() * 0.10416666666666);
			sizeMap.put(sub, size + logSize(sub.getSubmodules().size()));
			zNodes.addAll(sub.getSubmodules());
			for (ZNode sub2 : sub.getSubmodules()) {
				sizeMap.put(sub2, size / 8f + logSize(sub2.getCodeLineSize()));
			}
			pointMap.putAll(new PixelZNodePositioner(center, new Dimension(
					dim.width / 10, dim.height / 10),
					new DirectionZNodePositioner(direction,
							makeNodePositioner())).getNewPositions(sub));
		}
		pointMap.putAll(depMap);
	}

	public static float logSize(int size) {
		return (float) (size > 2 ? Math.log(size) : 0);
	}

	private void sortNodes() {
		Collections.sort(selectedNode.getSubmodules(), new Comparator<ZNode>() {

			@Override
			public int compare(ZNode node1, ZNode node2) {
				switch (order) {
				case ALPHA:
					return node1.getName().compareTo(node2.getName());
				case SIZE:
					return node1.getCodeLineSize() - node2.getCodeLineSize();
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

	float scale = 1.0f;

	public ZFactory zfactory;

	public void run() {
		if (aniCount.incrementAndGet() >= 100) {
			if (state == State.ANIMATING) {
				state = State.NORMAL;
			} else if (state == State.SELECTING) {
				if (editors.isEmpty()) {
					state = State.NORMAL;
				} else {
					state = State.EDITING;
				}
			}
		}
		count.incrementAndGet();
		if (count.get() >= 20) {
			count.set(0);
		}
		final float time = aniCount.get() / 100f;

		if (getState() == State.SELECTING) {
			Editor ed = getEditors().get(0);
			int y = 8 + (editors.size() == 1 ? 0 : editors.get(1)
					.getEditorPanel().getY()
					+ editors.get(1).getEditorPanel().getHeight());
			ed.setScale(0.25f + 0.75f * time);
			final Point2D point = animator.animate(getSelectedNode()
					.getLocation(), new Point2D.Float(8, y), time,
					AnimationType.COSINE);
			ed.getEditorPanel().setLocation((int) point.getX(),
					(int) point.getY());
		} else if (getState() == State.ANIMATING)
			for (ZNode node : zNodes) {
				if (pointMap.containsKey(node)) {
					node.getLocation().setLocation(
							animator.animate(node.getLocation(),
									pointMap.get(node), time,
									AnimationType.COSINE));
				}
				if (sizeMap.containsKey(node)) {
					final Float size = sizeMap.get(node);
					final Float currentSize = node.getSize();
					node.setSize((float) animator.animate(
							new Point2D.Float(currentSize, 0),
							new Point2D.Float(size, 0), time,
							AnimationType.COSINE).getX());
				}
			}
	}

	ZNode findZNodeAt(Point p) {
		ZNode found = null;

		for (ZNode z : zNodes) {
			final double hs = z.getSize() * scale * 0.5;
			if (translateToDisplayPoint(z.getLocation()).distance(p.x, p.y) < hs) {
				found = z;
			}
		}
		return found;
	}

	public void createSubNode(Point point) {
		if (selectedNode != null) {
			// create sub-module
			final ZNodeType subtype;
			switch (selectedNode.getNodeType()) {
			case METHOD:
				subtype = ZNodeType.CALLEE;
				break;
			case CLASS:
				subtype = ZNodeType.METHOD;
				break;
			case PACKAGE:
				subtype = ZNodeType.CLASS;
				break;
			case CALLEE:
			case CALLER:
			case DEPENDENCY:
				return;
			default:
				subtype = ZNodeType.PACKAGE;
			}
			ZNode sub = createNewZ(point, subtype);
			if (sub != null)
				selectedNode.getSubmodules().add(sub);
		}
	}

	ZNode createNewZ(Point point, ZNodeType type) {
		final String name = display.showInputDialog(
				"Name for new " + type.name(), "Z");
		if (name == null) {
			return null;
		}
		final Point2D.Float zp = translateToZNodePoint(point);
		final ZNode zNode = new ZNode(zp.x, zp.y, name.trim());
		zNode.setNodeType(type);
		zNode.setParentFile(selectedNode.getParentFile());
		zNodes.add(zNode);
		if (type == ZNodeType.METHOD) {
			zNode.setParentFile(new File(selectedNode.getParentFile(),
					selectedNode.getName() + "." + selectedNode.getExtension()));
			int end = selectedNode.getEndLineNumber(apiFactory
					.getLanguageParser());
			for (ZNode method : selectedNode.getSubmodules()) {
				end += method.getCodeLineSize();
			}
			zNode.setExtension(String.valueOf(end));
			System.err.println("ext=" + zNode.getExtension());
		} else if (type == ZNodeType.CALLEE) {
			selectedNode.addCodeLine(zNode.getName());
		}
		new ZCodeSaver(apiFactory).save(zNode);
		return zNode;
	}

	public ZNode load(File file) {
		apiFactory = zfactory.getApiFactory(file);
		log.info("api=" + apiFactory);
		final ZNode node = new ZCodeLoader(apiFactory).load(file);
		selectedNode = node;
		return node;
	}

	/** Translates from location as stored for ZNodes to GUI point. */
	public Point2D.Float translateToDisplayPoint(Point2D.Float point) {
		return new Point2D.Float(point.x * scale, point.y * scale);
	}

	/** Translates from GUI point to location as stored for ZNodes. */
	public Point2D.Float translateToZNodePoint(Point point) {
		return new Point2D.Float(point.x / scale, point.y / scale);
	}

	public List<ZNode> getZNodes() {
		return this.zNodes;
	}

	public APIFactory getApiFactory() {
		return apiFactory;
	}

	public Point getPoint1() {
		return point1;
	}

	public Point getPoint2() {
		return point2;
	}

	public State getState() {
		return state;
	}

	public NodeLayout getNodeLayout() {
		return nodeLayout;
	}

	public Direction getDirection() {
		return direction;
	}

	public ZNode getDraggedNode() {
		return draggedNode;
	}

	public List<Editor> getEditors() {
		return editors;
	}

	public SortOrder getOrder() {
		return order;
	}

	public UserSettings getSettings() {
		return settings;
	}

	public ZDisplay getDisplay() {
		return display;
	}

	public AtomicInteger getCount() {
		return count;
	}

	public AtomicInteger getAniCount() {
		return aniCount;
	}

	public Map<ZNode, Point2D> getPointMap() {
		return pointMap;
	}

	public ZNodePositioner getNodePositioner() {
		return nodePositioner;
	}

	public SmoothAnimator getAnimator() {
		return animator;
	}

	public ZNode getSelectedNode() {
		return selectedNode;
	}

	public float getScale() {
		return scale;
	}

	public void setNodeLayout(NodeLayout nodeLayout) {
		this.nodeLayout = nodeLayout;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public void setOrder(SortOrder order) {
		this.order = order;
	}

	public void showNewEditor(final ZNode z) {
		selectedNode = z;
		final ZCodeEditor editor = z.getNodeType() == ZNodeType.METHOD ? new ZCodeEditorPlus(
				z, apiFactory) : new ZCodeEditor(z, apiFactory);
		final KeyListener keyAdapter = new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.sleep(100); // 1/10 second
							} catch (InterruptedException e) {
							}
							editor.save();
							Swutil.flashMessage(display, "Saved " + z.getName());
						}
					});
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					for (Editor ed : editors)
						display.getContentPane().remove(ed.getEditorPanel());

					state = State.NORMAL;
					display.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					clicked(selectedNode);
				} else if (e.getKeyCode() == KeyEvent.VK_F1) {
					try {
						display.showEditorHelp();
					} catch (IOException e1) {
						log.error(e1.getMessage());
					}
					display.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		};
		editor.addKeyListener(keyAdapter);
		editors.add(0, editor);
		final int size = (int) z.getSize();
		editor.getEditorPanel().setSize(new Dimension(size, size));
		for (Editor ed : editors)
			display.getContentPane().add(ed.getEditorPanel());

		editor.getEditorPanel().setLocation((int) z.getLocation().x - size / 2,
				(int) z.getLocation().y - size / 2);
		editor.setScale(0.25f);
		state = State.SELECTING;
		aniCount.set(10);
	}

	public ZNode getHoveredNode() {
		return hoveredNode;
	}

	public String getHoverText() {
		return hoverText;
	}

	public void setHoverText(String hoverText) {
		this.hoverText = hoverText;
	}

	public Point getMouseLocation() {
		return mouseLocation;
	}

}
