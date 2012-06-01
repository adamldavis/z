/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.editor;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FilenameFilter;
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
import com.adamldavis.z.api.DependencyManager;
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
		for (ZNode sub : node.getSubmodules()) {
			log.info("sub=" + sub.getName());
			if (sub.getName().trim().equals("two()")) {
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
		super(node, apiFactory);
		this.compilePath = getPaths(apiFactory, node.getParentFile());
		this.location = getClassName(node, compilePath[0]);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		init(node);
	}

	private static String getClassName(final ZNode node, final File src) {
		final String path = node.getParentFile().getAbsolutePath();
		return path.substring(src.getAbsolutePath().length() + 1,
				path.length() - ".java".length()).replaceAll("[/\\\\]", ".");
	}

	/** Source and classes. */
	public static File[] getPaths(final APIFactory apiFactory,
			final File parentFile) {
		File src = new File("src");
		File bin = new File("bin");
		// find the pom file, get src
		FILES: for (File parent = parentFile.isDirectory() ? parentFile
				: parentFile.getParentFile(); parent != null; parent = parent
				.getParentFile()) {
			final DependencyManager dependencyManager = apiFactory
					.getDependencyManager();
			for (File pom : parent.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals(dependencyManager.getStandardFileName());
				}
			})) {
				src = dependencyManager.getSourceFolder(pom);
				bin = dependencyManager.getCompiledFolder(pom);
				break FILES;
			}
		}
		return new File[] { src, bin };
	}

	public ZCodeEditorPlus(final ZNode node, final APIFactory apiFactory,
			String fileName, File[] compilePath) {
		super(node, apiFactory);
		this.location = fileName;
		this.compilePath = compilePath;
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		init(node);
	}

	private void init(final ZNode node) {
		try {
			resultsEditor = new BetterEditPanel("");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		panel.remove(editor);
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
					runMethod(node.getName().substring(0,
							node.getName().indexOf('(')));
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
