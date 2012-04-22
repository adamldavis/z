/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.ruby;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.DependencyManager;

/**
 * @author Adam L. Davis
 * 
 */
public class RakeGemManager implements DependencyManager {

	private static final Logger log = LoggerFactory
			.getLogger(RakeGemManager.class);

	static final String ADD_DEP = "add_dependency(";

	static final String GEMSPEC = ".gemspec";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.DependencyManager#getStandardFileName()
	 */
	@Override
	public String getStandardFileName() {
		return "Rakefile";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.DependencyManager#getProjectName(java.io.File)
	 */
	@Override
	public String getProjectName(File dependencyFile) {
		// just return name of parent dir
		return dependencyFile.getParentFile().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.DependencyManager#getSourceFolder(java.io.File)
	 */
	@Override
	public File getSourceFolder(File dependencyFile) {
		File lib = new File(dependencyFile.getParentFile(), "lib");
		if (lib.isDirectory()) {
			return lib;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.DependencyManager#getDependencies(java.io.File)
	 */
	@Override
	public List<ZNode> getDependencies(File dependencyFile) {
		final ArrayList<ZNode> dependencies = new ArrayList<ZNode>();
		File file = findGemSpec(dependencyFile.getParentFile());
		try {
			final LineIterator iter = FileUtils.lineIterator(file);

			for (String line = iter.next(); iter.hasNext(); line = iter.next()) {
				if (line.contains(ADD_DEP)) {
					final int i = line.indexOf("\'", line.indexOf(ADD_DEP));
					final int end = line.indexOf("\'", i + 1);
					if (i > 0 && end > 0) {
						final String name = line.substring(i + 1, end);

						dependencies.add(new ZNode(ZNodeType.DEPENDENCY, name,
								name, "rb", dependencyFile));
					}
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return dependencies;
	}

	private File findGemSpec(File parentFile) {
		for (File f : parentFile.listFiles()) {
			if (f.isFile() && f.getName().endsWith(GEMSPEC)) {
				return f;
			}
		}
		return parentFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.DependencyManager#loadCode(java.io.File)
	 */
	@Override
	public String loadCode(File dependencyFile) {
		try {
			return FileUtils.readFileToString(dependencyFile);
		} catch (IOException e) {
			log.error(e.getMessage());
			return "";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.DependencyManager#save(com.adamldavis.z.ZNode)
	 */
	@Override
	public void save(ZNode dependencyNode) {
		File file = new File(dependencyNode.parentFile, getStandardFileName());
		try {
			FileUtils.write(file, dependencyNode.getCode());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

}
