/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.editor;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import neoe.ne.BetterEditPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.ZCodeLoader;
import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNodeCompiler;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeRunner;
import com.adamldavis.z.api.LineExecution;
import com.adamldavis.z.api.Param;
import com.adamldavis.z.java.JavaCodeRunner;
import com.adamldavis.z.java.JavaFactory;
import com.adamldavis.z.runner.MethodExecutionImpl;

/**
 * @author Adam L. Davis
 * 
 */
/**
 * @author Adam L. Davis
 * 
 */
public class ZCodeEditorPlus extends ZCodeEditor {

	private static final Logger log = LoggerFactory
			.getLogger(ZCodeEditorPlus.class);

	public static void main(String[] args) {
		APIFactory apiFactory = new JavaFactory();
		final String name = "com.adamldavis.z.java.JavaObject";
		final ZCodeLoader loader = new ZCodeLoader(apiFactory);
		ZNode node = loader.load(new File("test", name.replace('.', '/')
				+ ".java"));
		node = loader.load(node);
		for (ZNode sub : node.submodules) {
			log.info("sub=" + sub.name);
			if (sub.name.trim().equals("two()")) {
				node = sub;
			}
		}
		ZCodeEditorPlus runner = new ZCodeEditorPlus(node, apiFactory, name,
				new File[] { new File("target/classes"), new File("test") });
		runner.show();
	}

	private BetterEditPanel resultsEditor;

	private final CodeRunner codeRunner = new JavaCodeRunner();

	private final List<Param> parameters = new ArrayList<Param>();

	private String location;

	private File[] compilePath;

	private final JSplitPane splitPane;

	// TODO make this general
	public ZCodeEditorPlus(final ZNode node, final APIFactory apiFactory) {
		this(node, apiFactory, getClassName(node), new File[] {
				new File("src/"), new File("target/classes") });
	}

	private static String getClassName(final ZNode node) {
		return node.parentFile
				.getAbsolutePath()
				.substring(
						new File("src").getAbsolutePath().length() + 1,
						node.parentFile.getAbsolutePath().length()
								- ".java".length()).replaceAll("[/\\\\]", ".");
	}

	public ZCodeEditorPlus(final ZNode node, final APIFactory apiFactory,
			String fileName, File[] compilePath) {
		super(node, apiFactory);
		this.location = fileName;
		this.compilePath = compilePath;
		try {
			resultsEditor = new BetterEditPanel("");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		panel.remove(editor);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(editor);
		splitPane.setRightComponent(resultsEditor);
		splitPane.setDividerLocation(100);
		panel.add(splitPane);
		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_F5) {
					splitPane.setDividerLocation(panel.getWidth() / 2);
				} else if (e.getKeyCode() == KeyEvent.VK_F6) {
					splitPane.setDividerLocation(panel.getWidth() / 2);
					save();
					compile();
					// TODO actually figure out the name
					runMethod(node.name.substring(0, node.name.indexOf('(')));
				}
			}
		});
	}

	protected void compile() {
		log.info("Saving: {}", zNode);
		new ZNodeCompiler(apiFactory).compile(zNode);
	}

	void setParams(List<Param> parameters) {
		this.parameters.clear();
		this.parameters.addAll(parameters);
	}

	public void runMethod(String methodName) {
		List<LineExecution> results = codeRunner.run(new MethodExecutionImpl(0,
				methodName, location, methodName, parameters), compilePath);
		StringBuilder sb = new StringBuilder();
		if (results != null)
			for (LineExecution exe : results)
				sb.append(exe.getMessage()).append('\n');

		resultsEditor.setText(sb.toString());
	}

	public void show() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(splitPane);
		frame.setSize(800, 600);
		splitPane.setDividerLocation(400);
		frame.setVisible(true);
	}

}
