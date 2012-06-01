package com.adamldavis.z.api;

import java.io.File;
import java.util.List;

import com.adamldavis.z.ZNode;

public interface DependencyManager {

	/** IE pom.xml for maven. */
	String getStandardFileName();

	String getProjectName(File dependencyFile);

	File getSourceFolder(File dependencyFile);

	List<ZNode> getDependencies(File dependencyFile);

	/** Flatten the given file minus dependencies into a String. */
	String loadCode(File dependencyFile);

	void save(ZNode dependencyNode);

	/** folder for binaries of compiled code (or src if dynamic). */
	File getCompiledFolder(File dependencyFile);

}
