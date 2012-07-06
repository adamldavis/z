/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.java.GroovyPlay;
import com.adamldavis.z.java.JavaFactory;
import com.adamldavis.z.util.ThreadingUtil;

/**
 * Implements a left-side script and right-side output similar to Chris
 * Granger's Light-Table playground. Currently for Java/Groovy.
 * 
 * @author Adam L. Davis
 */
public class Playground {

	final Container content;

	final ZNode leftNode = new ZNode();
	final ZNode rightNode = new ZNode();

	final ZCodeEditor left;
	final ZCodeEditor right;

	public static PlaygroundBuilder builder(APIFactory apiFactory) {
		return new PlaygroundBuilder(apiFactory);
	}

	public static class PlaygroundBuilder {
		final APIFactory apiFactory;

		PlaygroundBuilder(APIFactory apiFactory) {
			super();
			this.apiFactory = apiFactory;
		}

		public Playground build(Container panel) {
			return new Playground(apiFactory, panel);
		}
	}

	public static void main(String[] args) {
		JFrame f = new JFrame("ZetaCode - Groovy Play");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		Playground p = builder(new JavaFactory()).build(f.getContentPane());
		f.setJMenuBar(p.makeMenuBar());
		f.setVisible(true);
		try {
			p.left.setText(IOUtils.toString(Playground.class
					.getResourceAsStream("/strings.groovy")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private JMenuBar makeMenuBar() {
		JMenuBar bar = new JMenuBar();
		// JMenuItem go = new JMenuItem("Go!");
		// bar.add(go);
		return bar;
	}

	public void go() {
		File script = new File("script.groovy");
		leftNode.replaceCode(left.getText());
		try {
			// TODO keep everything in memeory
			FileUtils.writeLines(script, leftNode.getCodeLines());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		final GroovyPlay play = new GroovyPlay(script); // TODO make API
		final Thread playThread = ThreadingUtil.runAsThread(play);
		ThreadingUtil.runAsThread(new Runnable() {

			@Override
			public void run() {
				// keep updating the right side
				while (playThread.isAlive()) {
					updateOutput(play);
					try {
						Thread.sleep(500);// half second
					} catch (InterruptedException e) {
					}
				}
				updateOutput(play);
			}
		});
	}

	protected String join(List<String> lines) {
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	private Playground(APIFactory apiFactory, Container content) {
		this.content = content;
		left = new ZCodeEditor(leftNode, apiFactory);
		right = new ZCodeEditor(rightNode, apiFactory);
		left.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				go();
			}
		});
		content.setLayout(new BorderLayout());
		content.add(left.getEditorPanel(), BorderLayout.WEST);
		content.add(right.getEditorPanel());
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		left.getEditorPanel().setPreferredSize(
				new Dimension(size.width / 2, size.height - 50));
	}

	// converts play output to line up with code by line number
	// there could be multiple tabbed columns due to loops
	// TODO: this is hack-ish, I know, it's only a demo
	public List<String> convertFromPlayOutput(List<String> out, int loc) {
		String[] result = new String[loc];
		Arrays.fill(result, "");

		for (String line : out) {
			String[] split = new String[2];
			int i = line.indexOf(':');
			split[0] = line.substring(0, i);
			split[1] = line.substring(i + 1);
			int num = Integer.parseInt(split[0]);
			if ("".equals(result[num])) {
				result[num] = split[1];
			} else {
				result[num] += "\t" + split[1];
			}
		}

		return Arrays.asList(result);
	}

	protected void updateOutput(final GroovyPlay play) {
		synchronized (play.getOut()) {
			synchronized (right) {
				right.setText(join(convertFromPlayOutput(play.getOut(),
						leftNode.getCodeLineSize())));
			}
		}
	}

	public ZCodeEditor getLeft() {
		return left;
	}

	public ZCodeEditor getRight() {
		return right;
	}

}
