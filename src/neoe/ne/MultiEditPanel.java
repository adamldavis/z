/** Copyright 2012, Adam L. Davis, all rights reserved. */
package neoe.ne;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Allows multiple editors to be opened and movable in one frame.
 * 
 * @author Adam L. Davis
 * 
 */
public class MultiEditPanel extends JFrame implements MouseMotionListener,
		MouseListener {

	public static void main(String[] args) {
		new MultiEditPanel();
	}

	List<EditPanel> panels = new ArrayList<EditPanel>();

	EditPanel hoveredPanel;

	EditPanel selectedPanel;

	public MultiEditPanel() {
		super("test");
		try {
			setIconImage(ImageIO.read(EditPanel.class
					.getResourceAsStream("/Alien.png")));

			final Container pane = getContentPane();
			// pane.setLayout(new OverlayLayout(pane));
			// pane.setLayout(new GridLayout(2, 2));
			pane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
			final EditPanel center = new EditPanel(new File("pom.xml"));
			center.setPreferredSize(new Dimension(200, 200));

			panels.add(center);
			panels.add(new BetterEditPanel(new File("zip.xml")));
			panels.add(new BetterEditPanel(new File("pom.xml")));
			panels.add(new BetterEditPanel(new File("README")));
			for (EditPanel p : panels) {
				pane.add(p);
			}

			pane.setBackground(Color.black);

			U.setFrameSize(this, 800, 600);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		addMouseListener(this);
		addMouseMotionListener(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		for (EditPanel p : panels) {
			Point loc = p.getLocationOnScreen();
			SwingUtilities.convertPointFromScreen(loc, this);
			g.setColor(p == hoveredPanel ? Color.RED : Color.BLUE);
			g.drawRect(loc.x, loc.y, p.getWidth() + 1, p.getHeight() + 1);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	int offsetX, offsetY;

	@Override
	public void mousePressed(MouseEvent e) {
		final EditPanel panelAt = getPanelAt(e.getLocationOnScreen());
		if (null == panelAt) {
			selectedPanel = null;
			repaint();
		} else {
			System.out.println("selected=" + panelAt);
			offsetX = panelAt.getX() - e.getX();
			offsetY = panelAt.getY() - e.getY();
			selectedPanel = panelAt;
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

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
	public void mouseDragged(MouseEvent e) {
		if (selectedPanel != null) {
			Point loc = e.getLocationOnScreen();
			SwingUtilities.convertPointFromScreen(loc, this);
			loc.x += offsetX;
			loc.y += offsetY;
			selectedPanel.setLocation(loc);
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		final EditPanel panelAt = getPanelAt(e.getLocationOnScreen());
		if (null == panelAt) {
			hoveredPanel = null;
			repaint();
		} else {
			hoveredPanel = panelAt;
			repaint();
		}
	}

	/** p is point on screen. */
	EditPanel getPanelAt(Point p) {
		for (EditPanel panel : panels) {
			Point loc = panel.getLocationOnScreen();
			if (p.x + 2 >= loc.x && p.x - 2 <= loc.x + panel.getWidth()
					&& p.y + 2 >= loc.y && p.y - 2 <= loc.y + panel.getHeight()) {
				return panel;
			}
		}
		return null;
	}

}
