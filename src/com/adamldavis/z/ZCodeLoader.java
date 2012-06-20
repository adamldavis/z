package com.adamldavis.z;

import static java.util.Arrays.asList;

import java.awt.geom.Point2D.Float;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

public class ZCodeLoader {

	private static final Logger log = LoggerFactory
			.getLogger(ZCodeLoader.class);

	private static final Set<String> ignore = new HashSet<String>(asList(
			".git", ".svn", ".project"));

	CodeFormatter codeFormatter;

	DependencyManager dependencyManager;

	LanguageParser languageParser;

	public ZCodeLoader(APIFactory apiFactory) {
		this(apiFactory.getCodeFormatter(), apiFactory.getDependencyManager(),
				apiFactory.getLanguageParser());
	}

	public ZCodeLoader(CodeFormatter codeFormatter,
			DependencyManager dependencyManager, LanguageParser languageParser) {
		super();
		this.codeFormatter = codeFormatter;
		this.dependencyManager = dependencyManager;
		this.languageParser = languageParser;
	}

	public ZNode load(File file) {
		if (file.isFile()) {
			if (file.getName().equals(dependencyManager.getStandardFileName())) {
				List<ZNode> deps = dependencyManager.getDependencies(file);

				ZNode node = new ZNode(ZNodeType.MODULE,
						dependencyManager.getProjectName(file), "", "xml", file);
				node.getDependencies().addAll(deps);
				node.setParentFile(file.getParentFile());
				node.replaceCode(dependencyManager.loadCode(file));
				final File src = dependencyManager.getSourceFolder(file);

				if (src == null || !src.isDirectory()) {
					for (File f : file.getParentFile().listFiles()) {
						if (ignore.contains(f.getName()))
							continue;
						if (f.isDirectory()) {
							node.getSubmodules().add(load(f));
						} else if (f.isFile() && !f.getName().startsWith(".")) {
							node.getSubmodules().add(loadPlainFile(f, false));
						}
					}
				} else {
					node.getSubmodules().addAll(loadPackages(src));
				}
				for (ZNode pack : node.getSubmodules()) {
					pack.getDependencies().addAll(deps);
				}
				return node;
			}
			final String name = file.getName();
			final String ext = name.substring(name.lastIndexOf(".") + 1);

			if (languageParser.getValidFileExtensions().contains(
					ext.toLowerCase())
					&& name.contains(".")) {
				return loadClassFile(file);
			}
			return loadPlainFile(file, true);
		} else if (file.isDirectory()) {

			for (File f : file.listFiles()) {
				if (f.isFile()
						&& f.getName().equals(
								dependencyManager.getStandardFileName())) {
					return load(f);
				}
			}

			return loadDir(file);
		}
		throw new IllegalArgumentException("Unknown file type: " + file);
	}

	private ZNode loadPlainFile(File file, boolean read) {
		final ZNode node = new ZNode(0, 0, file.getName());
		try {
			node.setParentFile(file.getParentFile());
			node.setNodeType(ZNodeType.CLASS);
			if (file.getName().contains(".")) {
				final int index = file.getName().lastIndexOf('.');
				node.setName(file.getName().substring(0, index));
				node.setExtension(file.getName().substring(index + 1));
			}
			if (read)
				node.setCode(FileUtils.readLines(file));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return node;
	}

	private Collection<? extends ZNode> loadPackages(File srcDir) {
		return loadPackages(srcDir, new ArrayList<ZNode>(), srcDir);
	}

	private Collection<? extends ZNode> loadPackages(File curr,
			final ArrayList<ZNode> nodes, final File srcDir) {
		if (curr == null || !curr.isDirectory()) {
			return nodes;
		}
		for (File file : curr.listFiles()) {
			String name = file.getName();

			if (file.isFile() && name.contains(".")) {
				String ext = name.substring(name.lastIndexOf(".") + 1);
				if (languageParser.getValidFileExtensions().contains(
						ext.toLowerCase())) {
					nodes.add(loadPackage(file, srcDir));
					break;
				}
			}
		}
		for (File file : curr.listFiles()) {
			if (file.isDirectory()) {
				loadPackages(file, nodes, srcDir);
			}
		}
		return nodes;
	}

	private ZNode loadPackage(File file, File srcDir) {
		final ZNode node = loadPackageFromFile(file, srcDir);

		for (File packageInfo : file.getParentFile().listFiles(
				new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.equals(languageParser.getPackageFilename());
					}
				})) {
			try {
				node.setLastModified(packageInfo.lastModified());
				node.setCode(FileUtils.readLines(packageInfo));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		return node;
	}

	/** load Directory as a Module. */
	private ZNode loadDir(File dir) {
		ZNode node = new ZNode();
		node.setParentFile(dir);
		node.setName(dir.getName());
		node.setExtension("");
		node.setNodeType(ZNodeType.MODULE);
		node.getSubmodules().addAll(loadPackages(dir));
		return node;
	}

	/** Load a module, package or class. */
	public ZNode load(ZNode node) {

		log.info("load: " + node);
		log.info("parentFile=" + node.getParentFile());
		if (!node.getSubmodules().isEmpty()) {
			return node;
		}

		switch (node.getNodeType()) {

		case CLASS:
			final String extension = node.getExtension();
			final String filename = node.getName()
					+ (extension.length() > 0 ? ("." + extension) : "");
			final File file = new File(node.getParentFile(), filename);

			if (!languageParser.getValidFileExtensions().contains(
					extension.toLowerCase())) {
				return loadPlainFile(file, true);
			}
			final List<ZNode> methods = languageParser.getMethods(file);

			for (ZNode method : methods) {
				method.setParentFile(file);
				method.setParentNode(node);
			}
			node.getSubmodules().addAll(methods);
			node.getDependencies().clear();
			node.getDependencies().addAll(languageParser.loadImports(file));
			break;
		case MODULE:
			File depFile = new File(node.getParentFile(),
					dependencyManager.getStandardFileName());
			if (depFile.isFile()) {
				return load(depFile);
			} else {
				return load(node.getParentFile());
			}
		case PACKAGE:
			node.getSubmodules().addAll(loadClassFiles(node.getParentFile()));
			break;
		case METHOD:
			languageParser.loadMethodHierarchy(node);
			break;
		default: // do nothing
		}
		for (ZNode sub : node.getSubmodules()) {
			sub.setLocation((Float) node.getLocation().clone());
			sub.setSize(1.0f);
		}
		return node;
	}

	private Collection<? extends ZNode> loadClassFiles(File directory) {
		final List<ZNode> nodes = new LinkedList<ZNode>();
		final File[] classFiles = directory.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				final String name = file.getName();

				if (file.isFile() && name.contains(".")) {
					String ext = name.substring(name.lastIndexOf('.') + 1);

					if (languageParser.getValidFileExtensions().contains(ext)
							&& !languageParser.getPackageFilename()
									.equals(name)) {
						return true;
					}
				}
				return false;
			}
		});
		for (File file : classFiles) {
			nodes.add(loadClassFile(file));
		}

		return nodes;
	}

	/** Loads a class file or gets the package-name from it. */
	public ZNode loadClassFile(File file) {
		final String name = file.getName();
		final ZNode node = new ZNode(ZNodeType.CLASS, name, "", "", file);
		node.setParentFile(file.getParentFile());

		if (name.contains(".")) {
			node.setExtension(name.substring(name.lastIndexOf('.') + 1));
			node.setName(name.substring(0, name.lastIndexOf('.')));
		}
		node.replaceCode(languageParser.getNonMethodPart(file));
		node.replaceCode(codeFormatter.format(node.getCode()));
		return node;
	}

	/**
	 * Gets the package-name from given class-file and source-dir.
	 * 
	 * @param file
	 *            Code file.
	 * @param srcDir
	 *            Source directory above current file.
	 * @return Node named after the absolute path of file's dir minus srcDir.
	 */
	public ZNode loadPackageFromFile(File file, File srcDir) {
		final String path = file.getParentFile().getAbsolutePath();
		final String srcPath = srcDir.getAbsolutePath();
		final String name;

		if (path.length() > srcPath.length()) {
			name = path.substring(srcPath.length() + 1);
		} else {
			name = "*"; // no "package"
		}
		return new ZNode(ZNodeType.PACKAGE, name, "", "", file.getParentFile());
	}

}
