/** Copyright 2012, Adam L. Davis. */
package com.adamldavis.z;

import static java.util.Arrays.asList;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.swing.Swutil;
import com.adamldavis.z.SmoothAnimator.AnimationType;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.ZNodeLink.LinkType;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.Editor;
import com.adamldavis.z.editor.ZCodeEditor;
import com.adamldavis.z.editor.ZEdit;
import com.adamldavis.z.git.GitLogDiffsMap;
import com.adamldavis.z.gui.ZMenu;
import com.adamldavis.z.gui.swing.ZDisplay;
import com.adamldavis.z.tasks.ZTask;
import com.adamldavis.z.tasks.ZTaskList;
import com.adamldavis.z.util.ThreadingUtil;

/**
 * Main class of Z program.
 * 
 * @author Adam Davis
 * 
 */
public class Z implements MouseListener, MouseWheelListener,
		MouseMotionListener, KeyListener, Runnable {

	/** Direction from "dependencies" to "sub-modules". */
	public enum Direction {
		LR, RL, UP, DOWN
	};

	/** organization of nodes. */
	public enum NodeLayout {
		BLOOM, RANDOM, GRID
	}

	/** How to order nodes. */
	public enum SortOrder {
		DEFAULT, ALPHA, TIME, SIZE
	}

	/** what's happening right now. */
	public enum State {
		NORMAL, SELECTING, ANIMATING, EDITING, TIME_TRAVEL
	}

	private static final Logger log = LoggerFactory.getLogger(Z.class);

	public static float logSize(int size) {
		return (float) (size > 2 ? Math.log(size) : 0);
	}

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

	SortOrder order = SortOrder.DEFAULT;

	UserSettings settings = new UserSettings();

	final ZMenu zMenu = new ZMenu(Z.this, new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedNode != null) {
				clicked(selectedNode);
			}
			zMenu.setVisible(false);
			display.removeMouseMotionListener(zMenu);
			display.removeMouseListener(zMenu);
			saveSettings();
			addListeners();
		}
	});

	ZDisplay display = new ZDisplay(this);

	Timer timer = new Timer("Z timer", true);

	private ZNode hoveredNode;

	private String hoverText;

	private Point mouseLocation;

	private final List<ZNodeLink> links = new ArrayList<ZNodeLink>();

	private final ZTaskList taskList = new ZTaskList();

	AtomicInteger count = new AtomicInteger(0);

	AtomicInteger aniCount = new AtomicInteger(0);

	final Map<ZNode, Point2D> pointMap = new HashMap<ZNode, Point2D>();

	final Map<ZNode, Float> sizeMap = new HashMap<ZNode, Float>();

	ZNodePositioner nodePositioner;

	SmoothAnimator animator = new SmoothAnimator();

	ZNode selectedNode;

	final List<ZNode> zNodes = new ArrayList<ZNode>();

	float scale = 1.0f;

	public ZFactory zfactory;

	public GitLogDiffsMap diffsMap;

	private ZEdit edit = new ZEdit();

	public Z() {
		addListeners();
		zfactory = new ZFactory(Z.class.getResourceAsStream("z.properties"));
		loadSettings();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Z.this.run();
			}
		}, 33, 33);
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private void addListeners() {
		display.addMouseWheelListener(Z.this);
		display.addMouseListener(Z.this);
		display.addMouseMotionListener(Z.this);
		display.addKeyListener(Z.this);
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

	protected void activateMenu(MouseEvent e) {
		removeListeners();
		zMenu.setLocation(e.getPoint());
		display.addMouseListener(zMenu);
		display.addMouseMotionListener(zMenu);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				zMenu.setVisible(true);
			}
		});
	}

	private void removeListeners() {
		display.removeMouseWheelListener(Z.this);
		display.removeMouseListener(Z.this);
		display.removeMouseMotionListener(Z.this);
		display.removeKeyListener(Z.this);
	}

	protected void addFieldLinks() {
		log.info("addFieldLinks()");
		final Map<String, ZNode> nodeMap = new HashMap<String, ZNode>();
		final Pattern patt = Pattern
				.compile("(private |protected |public |final |static )+\\s*(\\w+)");

		// TODO redo this to actually include packages
		for (ZNode node : selectedNode.getSubmodules()) {
			if (node.getNodeType() == ZNodeType.CLASS) {
				nodeMap.put(node.getName(), node);
			}
		}
		log.info("nodeMap={}", nodeMap);
		for (ZNode node : selectedNode.getSubmodules()) {
			for (String line : node.getCodeLines()) {
				final Matcher matcher = patt.matcher(line);
				if (matcher.find()) {
					log.info("group2={}", matcher.group(2));
					if (nodeMap.containsKey(matcher.group(2))) {
						links.add(new ZNodeLink(node, nodeMap.get(matcher
								.group(2)), LinkType.HAS_A));
					}
				}
			}
		}
		// just add 1 so there's at least 1
		links.add(new ZNodeLink(selectedNode, selectedNode, LinkType.HAS_A));
	}

	private void addMethodLinks() {
		log.info("addMethodLinks()");
		final Map<String, ZNode> nodeMap = new HashMap<String, ZNode>();
		final Pattern patt = Pattern.compile("(\\w+)\\(");

		for (ZNode method : selectedNode.getSubmodules()) {
			final Matcher matcher = patt.matcher(method.getName());
			if (matcher.find()) {
				final String name = matcher.group(1);
				nodeMap.put(name, method);
			}
		}
		log.info("nodeMap={}", nodeMap);
		for (ZNode method : selectedNode.getSubmodules()) {
			for (ZNode call : method.getSubmodules()) {
				// TODO fix
				if (nodeMap.containsKey(call.getName())) {
					links.add(new ZNodeLink(method,
							nodeMap.get(call.getName()), LinkType.METHOD_CALL));
				}
			}
		}
		// just add 1 so there's at least 1
		links.add(new ZNodeLink(selectedNode, selectedNode, LinkType.HAS_A));
	}

	protected void clicked(ZNode node) {
		log.info("selected: " + node);
		selectedNode = new ZCodeLoader(apiFactory).load(node);
		synchronized (zNodes) {
			zNodes.clear();
			zNodes.add(selectedNode);
			zNodes.addAll(selectedNode.getDependencies());
			zNodes.addAll(selectedNode.getSubmodules());
		}
		count.set(0);

		state = State.ANIMATING;
		aniCount.set(0);
		final Dimension dim = display.getDimension();
		sortNodes();
		float selSize = (float) Math.min(dim.getWidth(), dim.getHeight()) / 2.2f;
		sizeMap.put(selectedNode, selSize);
		float shrinkFactor = 0.2f;
		float size = (float) (selSize * shrinkFactor);

		for (ZNode dep : selectedNode.getDependencies()) {
			sizeMap.put(dep, size);
		}
		for (ZNode sub : selectedNode.getSubmodules()) {
			sub = new ZCodeLoader(apiFactory).load(sub);
			sizeMap.put(sub, size + logSize(sub.getSubmodules().size()));
			synchronized (zNodes) {
				zNodes.addAll(sub.getSubmodules());
			}
			for (ZNode sub2 : sub.getSubmodules()) {
				sizeMap.put(sub2,
						size * shrinkFactor + logSize(sub2.getCodeLineSize()));
			}
		}
		Point2D.Float center = new Point2D.Float(selSize * 1.25f, selSize);
		pointMap.put(node, center);
		updateSubLocations(selectedNode, false, center);
	}

	ZNode createNewZ(final Point point, final ZNodeType type) {
		String name = null;

		if (type == ZNodeType.DEPENDENCY) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					ZNode dep = createNewZNode(point, type,
							new FakeDependencySelector().get());
					if (dep != null)
						selectedNode.getDependencies().add(dep);
				}
			}).start();
		} else {
			name = display.showInputDialog("Name for new " + type.name(), "Z");
		}
		return createNewZNode(point, type, name);
	}

	private ZNode createNewZNode(Point point, ZNodeType type, String name) {
		if (name == null) {
			return null;
		}
		final Point2D.Float zp = translateToZNodePoint(point);
		final ZNode zNode = new ZNode(zp.x, zp.y, name.trim());
		zNode.setNodeType(type);
		zNode.setParentFile(selectedNode.getParentFile());
		if (type == ZNodeType.DEPENDENCY) {
			zNode.setCode(asList("artifactId=" + name, "groupId=" + name));
		}
		synchronized (zNodes) {
			zNodes.add(zNode);
		}
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

	protected void dragged(ZNode z) {
		log.info("dragged: " + z);
		// create dependency on z?
		if (z.getNodeType() == ZNodeType.DEPENDENCY)
			selectedNode.getDependencies().add(z);
	}

	ZNode findZNodeAt(Point p) {
		ZNode found = null;
		synchronized (zNodes) {
			for (ZNode z : zNodes) {
				final double hs = z.getSize() * scale * 0.5;
				if (translateToDisplayPoint(z.getLocation()).distance(p.x, p.y) < hs) {
					found = z;
				}
			}
		}
		return found;
	}

	public AtomicInteger getAniCount() {
		return aniCount;
	}

	public SmoothAnimator getAnimator() {
		return animator;
	}

	public APIFactory getApiFactory() {
		return apiFactory;
	}

	public AtomicInteger getCount() {
		return count;
	}

	public Direction getDirection() {
		return direction;
	}

	public ZDisplay getDisplay() {
		return display;
	}

	public ZNode getDraggedNode() {
		return draggedNode;
	}

	public ZNode getHoveredNode() {
		return hoveredNode;
	}

	public String getHoverText() {
		return hoverText;
	}

	public List<ZNodeLink> getLinks() {
		return links;
	}

	public ZMenu getMenu() {
		return this.zMenu;
	}

	public Point getMouseLocation() {
		return mouseLocation;
	}

	public NodeLayout getNodeLayout() {
		return nodeLayout;
	}

	public ZNodePositioner getNodePositioner() {
		return nodePositioner;
	}

	public SortOrder getOrder() {
		return order;
	}

	public Point getPoint1() {
		return point1;
	}

	public Point getPoint2() {
		return point2;
	}

	public Map<ZNode, Point2D> getPointMap() {
		return pointMap;
	}

	public float getScale() {
		return scale;
	}

	public ZNode getSelectedNode() {
		return selectedNode;
	}

	public UserSettings getSettings() {
		return settings;
	}

	public State getState() {
		return state;
	}

	public ZTaskList getTaskList() {
		return taskList;
	}

	public List<ZNode> getZNodes() {
		return this.zNodes;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!links.isEmpty())
			return;
		switch (e.getKeyChar()) {
		case 's':
			// TODO: Search!!!!
			String name = display.showInputDialog("Search", "Z");
			ZTask activeTask = taskList.getActiveTask();
			for (ZNode node : getZNodes()) {
				if (node.getName().startsWith(name)) {
					log.info("found {}", node.getName());
					if (activeTask != null) {
						activeTask.add(node);
					}
				}
			}
			break;
		case 'm':
			if (selectedNode.getNodeType() == ZNodeType.CLASS) {
				addMethodLinks();
			}
			break;
		case 'p':
			// TODO: add polymorphic links
		case 'i':
		case 'r':
			// TODO: add import/require links
		case 'h':
		case 'f':
			addFieldLinks();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		links.clear();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	public ZNode load(File file) {
		apiFactory = zfactory.getApiFactory(file);
		log.info("api=" + apiFactory);
		final ZNode node = new ZCodeLoader(apiFactory).load(file);
		selectedNode = node;
		return node;
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

	@Override
	public void mouseClicked(MouseEvent e) {
		final Point p = e.getPoint();
		ZNode z = findZNodeAt(p);

		if (z == null) {
			zMenu.setVisible(false);
			if (e.getButton() == MouseEvent.BUTTON3) {
				activateMenu(e);
			} else if (p.y >= display.getHeight() - 40) {
				final ZTask task = taskList.getTaskAt(p.x, 20);
				if (task == null)
					showNewEditor(selectedNode);
				else
					selectTask(task);
			} else if (selectedNode == null) {
				selectedNode = createNewZ(p, ZNodeType.MODULE);
			} else {
				createNewZ(p, ZNodeType.DEPENDENCY);
			}
		} else if (e.getButton() == MouseEvent.BUTTON1 && !e.isControlDown()) {
			clicked(z);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON3) {
			point2 = e.getPoint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}

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
			} else if (e.getPoint().y >= display.getHeight() - 40) {
				final ZTask task = taskList.getTaskAt(e.getPoint().x, 20);
				if (task == null) {
					// TODO: show confirmation?
					synchronized (zNodes) {
						zNodes.remove(draggedNode);
					}
				} else
					task.add(draggedNode);
				draggedNode = null;
			} else {
				draggedNode.getLocation().setLocation(
						translateToZNodePoint(point2));
				updateSubLocations(draggedNode, true, draggedNode.getLocation());
				draggedNode = null;
			}
			point1 = point2 = null;
		}
	}

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
			if (!edit.getEditors().isEmpty()) {
				for (Editor editor : edit.getEditors()) {
					editor.setScale(scale);
				}
			}
		}
	}

	@Override
	public void run() {
		if ((state == State.ANIMATING && aniCount.incrementAndGet() >= 100)
				|| (state == State.TIME_TRAVEL && aniCount.addAndGet(1) >= 999)
				|| (state == State.SELECTING && aniCount.addAndGet(2) >= 100)) {
			if (state == State.ANIMATING || state == State.TIME_TRAVEL) {
				state = State.NORMAL;
				links.clear();
			} else if (state == State.SELECTING) {
				if (edit.getEditors().isEmpty()) {
					state = State.NORMAL;
				} else {
					state = State.EDITING;
					updateEditorSize(edit.getEditors().get(0));
					edit.updatePaneSize();
				}
			}
		}
		count.incrementAndGet();
		if (count.get() >= 20) {
			count.set(0);
		}
		final float time = aniCount.get() / 100f;

		if (getState() == State.SELECTING) {
			Editor ed = edit.getEditors().get(0);
			ZNode editorNode = edit.getNode(ed);
			Editor previousEd = edit.getEditors().size() == 1 ? null : edit
					.getEditors().get(1);
			int y = 8;
			if (previousEd != null) {
				JPanel previousPanel = previousEd.getEditorPanel();
				y += previousPanel.getY() + previousPanel.getHeight();
			}
			ed.setScale(0.25f + 0.75f * time);
			final Point2D point = animator.animate(editorNode.getLocation(),
					new Point2D.Float(8, y), time, AnimationType.COSINE);
			ed.getEditorPanel().setLocation((int) point.getX(),
					(int) point.getY());
		} else if (getState() == State.ANIMATING)
			synchronized (zNodes) {
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
		else if (getState() == State.TIME_TRAVEL
				&& aniCount.get() * diffsMap.getLogSize() / 1000 > (aniCount
						.get() - 1) * diffsMap.getLogSize() / 1000)
			synchronized (zNodes) {
				final Collection<ZNodeLink> nodeLinks = diffsMap.getNodeLinks(
						aniCount.get() * diffsMap.getLogSize() / 1000, zNodes);
				if (!nodeLinks.isEmpty()) {
					links.clear();
					links.addAll(nodeLinks);
				}
			}
		else if (getState() == State.TIME_TRAVEL) {
			if (diffsMap.author != null) {
				ZNode node = diffsMap.author;
				float t = aniCount.get() % 100 / 100f;

				node.getLocation().setLocation(
						animator.animate(node.getLocation(),
								diffsMap.authorLocation, t,
								AnimationType.COSINE));
			}
		}
	}

	private void updateEditorSize(Editor editor) {
		ZNode editorNode = edit.getNode(editor);
		int width = display.getWidth() - 50;
		int height = 50 + editorNode.getCodeLineSize() * 14;
		editor.getEditorPanel().setPreferredSize(new Dimension(width, height));
	}

	public void saveSettings() {
		settings.setProperty(UserSettings.DIRECTION, direction.toString());
		settings.setProperty(UserSettings.LAYOUT, nodeLayout.toString());
		settings.setProperty(UserSettings.ORDER, order.toString());
		settings.save();
	}

	private void selectTask(final ZTask task) {
		if (task == taskList.getActiveTask()) {
			taskList.setActiveTask(null);
			clicked(selectedNode);
			return;
		} else {
			taskList.setActiveTask(task);
		}
		if (task.getNodes().isEmpty()) {
			clicked(selectedNode);
		} else {
			synchronized (zNodes) {
				zNodes.clear();
				zNodes.addAll(task.getNodes());
			}
		}
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public void setHoverText(String hoverText) {
		this.hoverText = hoverText;
	}

	public void setNodeLayout(NodeLayout nodeLayout) {
		this.nodeLayout = nodeLayout;
	}

	public void setOrder(SortOrder order) {
		this.order = order;
	}

	public void setState(State state) {
		this.state = state;
	}

	public void showNewEditor(final ZNode z) {
		removeListeners();
		final Editor editor = edit.getEditorFor(z, apiFactory);
		display.getContentPane().add(edit.getScrollPane());
		edit.getPane().setMaximumSize(
				new Dimension(display.getWidth(), Integer.MAX_VALUE));

		final KeyListener keyAdapter = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					endEditing();
				} else if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
					((ZCodeEditor) editor).save();
				} else if (e.getKeyCode() == KeyEvent.VK_W && e.isControlDown()) {
					// close just this editor.
					edit.remove(editor);
					edit.getPane().remove(editor.getEditorPanel());
					display.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					if (edit.getEditors().isEmpty()) {
						endEditing();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_F1) {
					try {
						display.showEditorHelp();
					} catch (IOException e1) {
						log.error(e1.getMessage());
					}
					display.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

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
							((ZCodeEditor) editor).save();
							Swutil.flashMessage(display, "Saved " + z.getName());
						}
					});
				}
			}
		};
		((ZCodeEditor) editor).addKeyListener(keyAdapter);
		final int size = (int) z.getSize();
		editor.getEditorPanel().setSize(new Dimension(2 * size, size));
		editor.getEditorPanel().setLocation((int) z.getLocation().x - size / 2,
				(int) z.getLocation().y - size / 2);
		editor.setScale(0.25f);
		state = State.SELECTING;
		aniCount.set(10);
		edit.updatePaneSize();
		ThreadingUtil.runAsThread(new Runnable() {
			@Override
			public void run() {
				// display.stop();
				display.doLayout();
				display.getContentPane().doLayout();
				edit.getScrollPane().doLayout();
				// edit.getPane().doLayout();
			}
		});
	}

	private void endEditing() {
		setState(State.NORMAL);
		display.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		addListeners();
		// display.start();
		display.getContentPane().remove(edit.getScrollPane());
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

	/** Translates from location as stored for ZNodes to GUI point. */
	public Point2D.Float translateToDisplayPoint(Point2D.Float point) {
		return new Point2D.Float(point.x * scale, point.y * scale);
	}

	/** Translates from GUI point to location as stored for ZNodes. */
	public Point2D.Float translateToZNodePoint(Point point) {
		return new Point2D.Float(point.x / scale, point.y / scale);
	}

	private void updateSubLocations(ZNode node, boolean immediate, Point2D loc) {
		final Point center = new Point((int) Math.round(loc.getX()),
				(int) Math.round(loc.getY()));
		// make bigger when more nodes
		final float factor = selectedNode == node ? (1.3f + logSize(node
				.getSubmodules().size()) / 2.2f) : 1.2f;
		int size = Math.round(sizeMap.get(node) * factor);
		final float xRatio = display.getWidth() / display.getHeight();
		Map<ZNode, Point2D> map = new PixelZNodePositioner(center,
				new Dimension((int) (size * xRatio), size),
				new DirectionZNodePositioner(direction, makeNodePositioner()))
				.getNewPositions(node);

		for (ZNode sub : node.getSubmodules()) {
			pointMap.put(sub, map.get(sub));
			if (immediate)
				sub.setLocation((java.awt.geom.Point2D.Float) map.get(sub));

			updateSubLocations(sub, immediate, map.get(sub));
		}
		if (node == selectedNode)
			for (ZNode dep : node.getDependencies()) {
				pointMap.put(dep, map.get(dep));
				if (immediate)
					dep.setLocation((java.awt.geom.Point2D.Float) map.get(dep));
			}
	}
}
