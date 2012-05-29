/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.ComputedValue;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticClassTransformation;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.api.CodeRunner;
import com.adamldavis.z.api.LineExecution;
import com.adamldavis.z.api.MethodExecution;
import com.adamldavis.z.api.Param;
import com.adamldavis.z.runner.LineExecutionImpl;
import com.adamldavis.z.runner.MethodExecutionImpl;
import com.adamldavis.z.runner.ParamImpl;

/**
 * Naive approach to getting runtime values using Tapestry Plastic.
 * 
 * First, discover which methods in the current class and other classes are run
 * in the given lines, and which fields are accessed.
 * 
 * Then, use AOP to decorate those methods and fields to get their values and
 * store them in lists.
 * 
 * Lastly, run the given code and match the values to the lines of code.
 * 
 * @author Adam L. Davis
 */
public class JavaCodeRunner implements CodeRunner {

	static final Logger log = LoggerFactory.getLogger(JavaCodeRunner.class);

	@SuppressWarnings("serial")
	public static class Results extends LinkedList<LineExecution> {

		public synchronized void addResult(LineExecution exe) {
			super.add(exe);
		}
	}

	private final Results results = new Results();

	private boolean recording = false;

	private ClassLoader classLoader;

	@SuppressWarnings("rawtypes")
	private final Map<Class, ClassInstantiator> plastics = new HashMap<Class, ClassInstantiator>();

	/**
	 * Derives classpath from given file.
	 * 
	 * @see com.adamldavis.z.api.CodeRunner#run(MethodExecution, File...)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<LineExecution> run(MethodExecution exe, File... compilePath) {
		// find imports, add those packages and classes
		results.clear();
		Set<String> packages = new HashSet<String>();
		List<Class> classes = new ArrayList<Class>();
		String pack = "";
		Class mainClass = null;
		File file = null;
		final String filename = exe.getLocation().replace('.', '/') + ".java";
		final String methodName = exe.getMethodName();

		for (File cp : compilePath) {
			final File f = new File(cp, filename);
			if (f.exists() && f.isFile()) {
				file = f;
				break;
			}
		}
		if (file == null) {
			throw new IllegalArgumentException("Can't find file=" + filename);
		}
		try {
			for (String line : FileUtils.readLines(file)) {
				int end = line.indexOf(';');
				if (end < 0)
					end = line.length();

				if (line.startsWith("package ")) {
					pack = line.substring(8, end);
					packages.add(pack);
					List<URL> urls = new LinkedList<URL>();
					for (File cp : compilePath) {
						urls.add(cp.toURI().toURL());
					}
					classLoader = Thread.currentThread()
							.getContextClassLoader();
					classLoader = new URLClassLoader(urls.toArray(new URL[0]),
							classLoader);
				} else if (line.startsWith("import ")
						&& !line.contains("static ")) {
					final String imprt = line.substring(7, end).trim();
					if (imprt.startsWith("java.") || imprt.startsWith("javax."))
						continue;

					if (imprt.endsWith(".*")) {
						handleImportStar(imprt, classes, compilePath);
					} else {
						classes.add(Class.forName(imprt, true, classLoader));
					}
					packages.add(imprt.substring(0, imprt.lastIndexOf('.')));
				} else if (line.contains("class ")) {
					break;
				}
			}
			mainClass = Class.forName(exe.getLocation(), true, classLoader);
			classes.add(mainClass);
			log.info("mainClass={}", mainClass);
			// .enable(TransformationOption.FIELD_WRITEBEHIND)
			ZPlasticManager mgr = new ZPlasticManager()
					.withClassLoader(classLoader).packages(packages).create();

			for (final Class baseClass : classes) {
				if (baseClass.isAnnotation() || baseClass.isInterface()
						|| baseClass.isEnum() || baseClass.isPrimitive()
						|| Param.class.isAssignableFrom(baseClass))
					continue;
				else if (plastics.get(baseClass) == null) {
					PlasticClassTransformation tran = mgr
							.getPlasticClass(baseClass.getName());
					final PlasticClass pc = tran.getPlasticClass();
					modifyClass(baseClass, pc, exe);
					if (mainClass.equals(baseClass)) {
						for (PlasticField field : pc.getAllFields()) {
							transformMainField(field);
						}
					}
					plastics.put(baseClass, tran.createInstantiator());
				}
			}
			// TODO modify the method to use Plastic Classes!
			ClassInstantiator main = plastics.get(mainClass);
			Object instance = main.newInstance();
			List<Object> args = new LinkedList<Object>();
			for (Param param : exe.getParameters()) {
				args.add(getValue(param));
			}

			invokeMethod(methodName, instance, args);

			return mapResultsToLines(exe);

		} catch (MalformedURLException e) {
			log.error("Problem finding the classpath", e);
		} catch (IOException e) {
			log.error("Problem reading the file: " + file.getAbsolutePath(), e);
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IllegalArgumentException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw ex; // we don't want to squash these.
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null; // there was an error
	}

	// TODO: remove this; set line-numbers during execution.
	private List<LineExecution> mapResultsToLines(MethodExecution exe) {
		List<LineExecution> list = new ArrayList<LineExecution>();
		int i = 0;

		for (LineExecution le : results) {
			((LineExecutionImpl) le).setLineNumber(i++);
		}
		list.addAll(results);
		return list;
	}

	public void invokeMethod(final String methodName, Object instance,
			List<Object> args) throws IllegalAccessException,
			InvocationTargetException {
		for (Method m : instance.getClass().getMethods()) {
			if (m.getName().equals(methodName)) {
				if (args.isEmpty())
					m.invoke(instance);
				else if (args.size() == 1)
					m.invoke(instance, args.get(0));
				else if (args.size() == 2)
					m.invoke(instance, args.get(0), args.get(1));
				else if (args.size() == 3)
					m.invoke(instance, args.get(0), args.get(1), args.get(2));
				break;
			}
		}
	}

	// record all field changes and accesses
	protected void transformMainField(final PlasticField field) {
		log.debug("name={}", field.getName());
		log.debug("typename={}", field.getTypeName());
		final String name = field.getName();
		field.setComputedConduit(new ComputedValue<FieldConduit<Object>>() {

			@Override
			public FieldConduit<Object> get(InstanceContext context) {
				return new RunnerFieldConduit(name);
			}
		});
		// field.claim(this);
	}

	public class RunnerFieldConduit implements FieldConduit<Object> {

		Object value; // replaces actual field

		String name;

		public RunnerFieldConduit(String name) {
			super();
			this.name = name;
		}

		@Override
		public Object get(Object obj, InstanceContext context) {
			if (recording)
				results.addResult(new LineExecutionImpl(0, String
						.valueOf(value)));
			return value;
		}

		@Override
		public void set(Object obj, InstanceContext context, Object value) {
			if (recording)
				results.addResult(new LineExecutionImpl(0, String
						.valueOf(value)));
			this.value = value;
		}

	}

	Object getValue(Param param) {
		String val = param.getValue();
		if ("null".equals(val)) {
			return null;
		}
		try {
			if ("int".equals(param.getType())) {
				return Integer.parseInt(val);
			} else if ("long".equals(param.getType())) {
				return Long.parseLong(val);
			} else if ("float".equals(param.getType())) {
				return Float.parseFloat(val);
			} else if ("double".equals(param.getType())) {
				return Double.parseDouble(val);
			}
			Class<?> clazz = Class.forName(param.getType(), true, classLoader);
			if (plastics.containsKey(clazz)) {
				Object obj = plastics.get(clazz).newInstance();
				// TODO: make a JavaParamImpl that takes care of this
				return obj;
			}
			if (String.class.equals(clazz)) {
				return val;
			} else if (Integer.class.equals(clazz)) {
				return new Integer(val);
			} else if (Long.class.equals(clazz)) {
				return new Long(val);
			} else if (Float.class.equals(clazz)) {
				return new Float(val);
			} else if (Double.class.equals(clazz)) {
				return new Double(val);
			} else if (Short.class.equals(clazz)) {
				return new Short(val);
			} else if (Boolean.class.equals(clazz)) {
				return new Boolean(val);
			} else if (BigDecimal.class.equals(clazz)) {
				return new BigDecimal(val);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private void handleImportStar(String imprt, List<Class> classes,
			File... paths) throws ClassNotFoundException {
		String pack = imprt.substring(0, imprt.lastIndexOf(".") + 1);

		for (File path : paths) {
			// org.apache.z. -> org/apache/z/
			File dir = new File(path, pack.replace(".", "/"));
			if (dir.isDirectory()) {
				handleImportStar(classes, pack, dir);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void handleImportStar(List<Class> classes, String pack, File dir)
			throws ClassNotFoundException {
		for (File f : dir.listFiles()) {
			if (f.getName().endsWith(".class")) {
				final String name = f.getName().substring(0,
						f.getName().indexOf("."));
				classes.add(Class.forName(pack + name, true, classLoader));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void modifyClass(final Class baseClass, final PlasticClass pc,
			final MethodExecution exe) {
		// PlasticField field = pc.introduceField(
		// Results.class, "results").inject(
		// results);
		for (PlasticMethod method : pc.getMethods()) {
			log.debug("Advising method {}", method);
			method.addAdvice(new MethodAdvice() {

				@Override
				public void advise(MethodInvocation inv) {
					final String name = inv.getMethod().getName();
					final Class<?>[] parameterTypes = inv.getMethod()
							.getParameterTypes();
					int num = parameterTypes.length;
					StringBuilder sb = new StringBuilder(name);
					final List<Param> params = new LinkedList<Param>();

					if (num == 0)
						sb.append("()");
					else {
						sb.append("(");
						sb.append(inv.getParameter(0));
						for (int i = 1; i < num; i++) {
							sb.append(',').append(inv.getParameter(i));
						}
						sb.append(")");
						for (int i = 0; i < num; i++) {
							params.add(new ParamImpl("arg" + i, inv
									.getParameter(i), parameterTypes[i]
									.getName()));
						}
					}
					boolean wasRecording = recording;
					if (name.equals(exe.getMethodName())) {
						recording = true;
					}
					if (recording) {
						results.addResult(new MethodExecutionImpl(0, sb
								.toString(), inv.getMethod()
								.getDeclaringClass().getName(), name, params));
					}
					if (!name.equals(exe.getMethodName())) {
						recording = false;
					}
					inv.proceed(); // call method
					recording = wasRecording;
				}
			});
		}
		pc.addToString(baseClass.toString());
	}
}
