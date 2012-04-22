/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

/**
 * Reads a properties file to map file-endings to Languages, etc.
 * 
 * @author Adam L. Davis
 * 
 */
public class ZFactory {

	private static final String LANG = "lang.";

	private static final Logger log = LoggerFactory.getLogger(ZFactory.class);

	Properties props = new Properties();

	public ZFactory(InputStream zproperties) {
		try {
			props.load(zproperties);
			log.info("z=" + props);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	/** List of available Languages as file endings. */
	public Collection<String> getAvailableLangs() {
		final List<String> list = new LinkedList<String>();

		for (Object key : props.keySet()) {
			String name = key.toString();
			if (name.startsWith(LANG)) {
				list.add(name.substring(LANG.length()));
			}
		}
		return list;
	}

	public APIFactory getApiFactory(String fileEnding) {
		final String className = props.getProperty(LANG + fileEnding);
		return (APIFactory) newInstance(className);
	}

	public CodeFormatter getCodeFormatter(String fileEnding) {
		final APIFactory apiFactory = getApiFactory(fileEnding);
		if (apiFactory == null) {
			return null;
		}
		return apiFactory.getCodeFormatter();
	}

	public DependencyManager getDependencyManager(String filename) {
		final String className = props.getProperty(filename);
		return (DependencyManager) newInstance(className);
	}

	public LanguageParser getLanguageParser(String fileEnding) {
		final APIFactory apiFactory = getApiFactory(fileEnding);
		if (apiFactory == null) {
			return null;
		}
		return apiFactory.getLanguageParser();
	}

	private Object newInstance(final String className) {
		if (className != null) {
			try {
				return Class.forName(className).newInstance();
			} catch (InstantiationException e) {
				log.error(e.getMessage());
			} catch (IllegalAccessException e) {
				log.error(e.getMessage());
			} catch (ClassNotFoundException e) {
				log.error(e.getMessage());
			}
		}
		return null;
	}

	public APIFactory getApiFactory(File file) {
		return getApiFactory(file, 0);
	}

	public APIFactory getApiFactory(File file, int depth) {
		APIFactory factory = null;

		if (file.isFile()) {
			if (file.getName().contains(".")) {
				String ext = file.getName().substring(
						file.getName().indexOf(".") + 1);
				factory = getApiFactory(ext);
			}
			if (factory == null && depth == 0) {
				return getApiFactory(file.getParentFile(), depth + 1);
			}
		} else if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				factory = getApiFactory(f, depth + 1);
				if (factory != null) {
					break;
				}
			}
		}
		return factory;
	}

}
