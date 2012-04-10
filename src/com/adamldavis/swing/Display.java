package com.adamldavis.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * @author Adam Davis
 * 
 */
@SuppressWarnings("serial")
public abstract class Display extends JFrame {

	public int height = 800;

	public int width = 1200;

	public static void println(String s) {
		System.out.println(s);
	}

	protected Timer timer;

	public Display() {
		this(true, 2, 31);
	}

	public Display(boolean alwaysOnTop, int buffers, int delay) {
		this(alwaysOnTop, buffers, delay, Toolkit.getDefaultToolkit()
				.getScreenSize());
	}

	public Display(boolean alwaysOnTop, int buffers, int delay, Dimension dim) {
		width = dim.width;
		height = dim.height;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setAlwaysOnTop(alwaysOnTop);
		println("w,h=" + width + "," + height);
		setSize(width, height);
		super.addComponentListener(new ComponentAdapter() {
			// This method is called after the component's size changes
			public void componentResized(ComponentEvent evt) {
				Component c = (Component) evt.getSource();
				Dimension newSize = c.getSize();
				width = newSize.width;
				height = newSize.height;
			}
		});
		setVisible(true);

		createBufferStrategy(buffers);
		timer = new Timer(delay, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					Display.this.repaint();
				} catch (Exception e1) {
					e1.printStackTrace();
					System.exit(1);
				}
			}
		});
		timer.start();
	}

	@Override
	public void paint(Graphics g) {
		if (this.getBufferStrategy() == null) {
			return;
		}
		final Graphics2D g2 = (Graphics2D) this.getBufferStrategy()
				.getDrawGraphics();
		paintBuffered(g2);
		g2.dispose();
		getBufferStrategy().show();
	}

	protected abstract void paintBuffered(Graphics2D g2);


}
