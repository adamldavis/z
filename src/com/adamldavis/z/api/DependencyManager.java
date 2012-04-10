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

}
