/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.java;

import static java.util.Arrays.asList;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.adamldavis.z.editor.Playground;

public class GroovyPlay implements Runnable {

	private File script;
	private GroovyShell shell;

	private final List<String> out = Collections
			.synchronizedList(new LinkedList<String>());
	private final Map<Pattern, String> methodMap = new HashMap<Pattern, String>();

	Pattern assign = Pattern.compile("([\\w\\.]+) = ([^/]*)");
	Pattern eq = Pattern.compile("\\(([^\\s]+) == ([^/]*)\\)");
	Pattern neq = Pattern.compile("\\((\\w+) != ([^/]*)\\)");
	Pattern ret = Pattern.compile("return ([^/]*)");
	Pattern params1 = Pattern.compile("def (\\w+)\\((\\w+)\\)");
	Pattern params2 = Pattern.compile("def (\\w+)\\((\\w+), (\\w+)\\)");
	Pattern println = Pattern.compile("println ([^/]*)");

	final List<Pattern> patterns = asList(assign, eq, neq, ret, params1,
			params2, println);
	final List<Pattern> paramPatterns = asList(params1, params2);

	public GroovyPlay(File script) {
		Binding binding = new Binding();
		binding.setVariable("script_path", script.getAbsolutePath());
		binding.setVariable("_out_", out);
		shell = new GroovyShell(binding);
		this.script = script;
		methodMap.put(assign, "replAssign");
		methodMap.put(eq, "replEq");
		methodMap.put(neq, "replNeq");
		methodMap.put(ret, "replReturn");
		methodMap.put(params1, "replParams");
		methodMap.put(params2, "replParams");
		methodMap.put(println, "replPrintln");
	}

	@Override
	public void run() {
		try {
			shell.evaluate(modify(script));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private File modify(File script) {
		File file = null;
		try {
			file = File.createTempFile("modified", ".groovy");
			List<String> code = FileUtils.readLines(script);
			List<String> result = new LinkedList<String>();
			int i = 0; // line#

			for (String line : code) {
				boolean foundMatch = false;
				for (Pattern patt : patterns) {
					if (patt.matcher(line).find()) {
						result.add(handle(line, patt, i));
						foundMatch = true;
						break;
					}
				}
				if (!foundMatch) {
					result.add(line);
				}
				i++;
			}
			result.add("def replAssign(a, b, n) {_out_.add(\"$n:$a = $b\" as String); }");
			result.add("def replNeq(a, b, n) {_out_.add(\"$n:$a != $b\" as String); a != b}");
			result.add("def replEq(a, b, n) {_out_.add(\"$n:$a == $b\" as String); a == b}");
			result.add("def replReturn(a, n) {_out_.add(\"$n:return $a\" as String); a}");
			result.add("def replParams(n, m, ... a) {_out_.add(n + ':def ' + m + ' ' + a.join(', '))}");
			result.add("def replPrintln(a, n) {_out_.add(\"$n:println $a\" as String); println a}");
			FileUtils.writeLines(file, result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	// converts the LOC to use a repl
	private String handle(String line, Pattern pattern, int n) {
		String methodName = methodMap.get(pattern);
		Matcher m = pattern.matcher(line);
		m.find();
		int start = m.start(), end = m.end();
		if (pattern == ret || pattern == println) {
			return line.substring(0, start) + methodName + "(" + m.group(1)
					+ "," + n + ")";
		} else if (pattern == assign) {
			// refers to left side only, so side-effects don't occur twice
			return line + ";" + methodName + "('" + m.group(1) + "',"
					+ m.group(1) + "," + n + ");";
		} else if (pattern == params1) {
			return line + methodName + "(" + n + ",'" + m.group(1) + "',"
					+ m.group(2) + ");";
		} else if (pattern == params2) {
			return line + methodName + "(" + n + ",'" + m.group(1) + "',"
					+ m.group(2) + "," + m.group(3) + ");";
		} else {
			return line.substring(0, start) + "(" + methodName + "("
					+ m.group(1) + "," + m.group(2) + "," + n + "))"
					+ line.substring(end);
		}
	}

	public List<String> getOut() {
		return out;
	}

	/** Test main. */
	public static void main(String[] args) {
		try {
			File file = File.createTempFile("temp", ".groovy");
			IOUtils.copy(Playground.class.getResourceAsStream("/lcm.groovy"),
					new FileOutputStream(file));
			file = new GroovyPlay(file).modify(file);
			IOUtils.copy(new FileInputStream(file), System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
