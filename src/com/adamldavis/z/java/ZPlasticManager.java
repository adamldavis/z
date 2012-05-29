/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.java;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.plastic.PlasticManager.PlasticManagerBuilder;

/**
 * Makes a public version of getPlasticClass.
 * 
 * @author Adam L. Davis
 * 
 */
public class ZPlasticManager {

	private PlasticManager plasticManager;
	private PlasticManagerBuilder builder;

	/**
	 * Creates a new builder using the thread's context class loader.
	 */
	public ZPlasticManager withContextClassLoader() {
		return withClassLoader(Thread.currentThread().getContextClassLoader());
	}

	/** Creates a new builder using the specified class loader. */
	public ZPlasticManager withClassLoader(ClassLoader loader) {
		builder = PlasticManager.withClassLoader(loader);
		return this;
	}

	public ZPlasticManager packages(Collection<String> packages) {
		builder.packages(packages);
		return this;
	}

	public ZPlasticManager create() {
		plasticManager = builder.create();
		return this;
	}

	/**
	 * Uses reflection to call getPlasticClass of PlasticManager.
	 * 
	 * @param classname
	 *            Full name of class to make plastic.
	 * @return Interface for transforming the class.
	 */
	@SuppressWarnings("rawtypes")
	public PlasticClassTransformation getPlasticClass(String classname) {
		try {
			// call package-protected method:
			Method method = PlasticManager.class.getDeclaredMethod(
					"getPlasticClass", String.class);
			method.setAccessible(true);

			return (PlasticClassTransformation) method.invoke(plasticManager,
					classname);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public PlasticManager getPlasticManager() {
		return plasticManager;
	}

}
