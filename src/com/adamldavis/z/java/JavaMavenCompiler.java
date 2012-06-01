/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.api.Compiler;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LineError;
import com.adamldavis.z.api.ProgressListener;

/**
 * @author Adam L. Davis
 * 
 */
public class JavaMavenCompiler implements Compiler {

	static final Logger log = LoggerFactory.getLogger(JavaMavenCompiler.class);

	public static File getRoot(final DependencyManager dependencyManager,
			final File parentFile) {
		// find the pom file
		for (File parent = parentFile.isDirectory() ? parentFile : parentFile
				.getParentFile(); parent != null; parent = parent
				.getParentFile()) {
			for (File file : parent.listFiles()) {
				if (file.getName().equals(
						dependencyManager.getStandardFileName()))
					return parent;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.Compiler#compile(com.adamldavis.z.ZNode)
	 */
	@Override
	public List<LineError> compile(ZNode node, ProgressListener listener) {
		List<LineError> errors = new ArrayList<LineError>();
		// TODO determine which files to compile
		// right now, cheats by using maven
		try {
			String m2 = System.getenv("M2_HOME");
			log.info("m2=" + m2);
			// TODO FIX this in linux/mac
			Process p = Runtime.getRuntime()
					.exec(m2 + "/bin/mvn.bat compile",
							null,
							getRoot(new MavenDependencyManager(),
									node.getParentFile()));
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			int i = 0;

			for (String line = br.readLine(); line != null; line = br
					.readLine()) {
				log.info(line);

				listener.update(i++ * 100 / 20); // TODO: fix this

			}
			listener.update(100);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return errors;
	}

}
