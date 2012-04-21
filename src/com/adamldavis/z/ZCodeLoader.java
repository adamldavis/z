package com.adamldavis.z;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
						dependencyManager.getProjectName(file), "", "xml",
						file.lastModified());
				node.dependencies.addAll(deps);
				node.parentFile = file.getParentFile();
				node.replaceCode(dependencyManager.loadCode(file));
				final File src = dependencyManager.getSourceFolder(file);

				if (src != null) {
					node.submodules.addAll(loadPackages(src));
				}
				for (ZNode pack : node.submodules) {
					pack.dependencies.addAll(deps);
				}
				return node;
			}
			return loadFile(file, false);
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

	private Collection<? extends ZNode> loadPackages(File sourceFolder) {
		return loadPackages(sourceFolder, new ArrayList<ZNode>());
	}

	private Collection<? extends ZNode> loadPackages(File curr,
			final ArrayList<ZNode> nodes) {
		if (curr == null || !curr.isDirectory()) {
			return nodes;
		}
		for (File file : curr.listFiles()) {
			String name = file.getName();

			if (file.isFile() && name.contains(".")) {
				String ext = name.substring(name.lastIndexOf(".") + 1);
				if (languageParser.getValidFileExtensions().contains(
						ext.toLowerCase())) {
					nodes.add(loadPackage(file));
					break;
				}
			}
		}
		for (File file : curr.listFiles()) {
			if (file.isDirectory()) {
				loadPackages(file, nodes);
			}
		}
		return nodes;
	}

	private ZNode loadPackage(File file) {
		final ZNode node = loadFile(file, true);

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
		node.parentFile = dir;
		node.name = dir.getName();
		node.extension = "";
		node.zNodeType = ZNodeType.MODULE;
		node.submodules.addAll(loadPackages(dir));
		return node;
	}

	/** Load a module, package or class. */
	public ZNode load(ZNode node) {

		log.info("load: " + node);
		log.info("parentFile=" + node.parentFile);
		node.submodules.clear();

		switch (node.zNodeType) {

		case CLASS:
			final String filename = node.name + "." + node.extension;
			final File file = new File(node.parentFile, filename);
			final List<ZNode> methods = languageParser.getMethods(file);

			for (ZNode method : methods) {
				method.parentFile = file;
			}
			node.submodules.addAll(methods);
			node.dependencies.clear();
			node.dependencies.addAll(languageParser.loadImports(file));
			break;
		case MODULE:
			node.submodules.addAll(loadPackages(node.parentFile));
			break;
		case PACKAGE:
			node.submodules.addAll(loadClassFiles(node.parentFile));
			break;
		default: // do nothing
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
			nodes.add(loadFile(file, false));
		}

		return nodes;
	}

	/** Loads a class file or gets the package-name from it. */
	public ZNode loadFile(File file, boolean getPackage) {
		final String name = file.getName();
		final ZNode node = new ZNode(ZNodeType.CLASS, name, "", "",
				file.lastModified());
		node.parentFile = file.getParentFile();

		if (name.contains(".")) {
			node.extension = name.substring(name.lastIndexOf('.') + 1);
			node.name = name.substring(0, name.lastIndexOf('.'));
		}
		if (!getPackage) {
			node.replaceCode(languageParser.getNonMethodPart(file));
			node.replaceCode(codeFormatter.format(node.getCode()));
			return node;
		}
		FileReader reader = null;

		try {
			reader = new FileReader(file);
			final BufferedReader br = new BufferedReader(reader);

			while (true) {
				final String line = br.readLine();
				if (line == null) {
					break;
				}
				if (getPackage
						&& line.startsWith(languageParser.getPackageKeyword())) {
					String pack = line
							.substring(
									languageParser.getPackageKeyword().length() + 1)
							.trim().replace(";", "");

					node.zNodeType = ZNodeType.PACKAGE;
					node.name = pack;
					node.extension = "";
					return node;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					log.error("Error closing file: " + e.getMessage(), e);
					throw new RuntimeException(e);
				}
			}
		}
		return node;
	}

}
