package com.adamldavis.z.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adamldavis.z.api.LineExecution;
import com.adamldavis.z.api.Param;
import com.adamldavis.z.runner.MethodExecutionImpl;
import com.adamldavis.z.runner.ParamImpl;

public class JavaCodeRunnerTest {

	private static final String JAVA_OBJECT = "com.adamldavis.z.java.JavaObject";

	private static final String TEST = "test/";

	private static final String BIN = "bin/";

	private JavaCodeRunner runner;

	@Before
	public void setUp() throws Exception {
		runner = new JavaCodeRunner();
		JavaObject javaObject = new JavaObject();
		System.out.println(javaObject.toString());
	}

	@After
	public void tearDown() throws Exception {
		runner = null;
	}

	private List<Param> empty() {
		return new LinkedList<Param>();
	}

	@Test
	public void testRun1() {
		List<LineExecution> res = runner.run(new MethodExecutionImpl(0, "",
				JAVA_OBJECT, "one", empty()), new File(TEST), new File(BIN));

		assertNotNull(res);
		assertEquals(1, res.size());
	}

	@Test
	public void testRun2() {
		List<LineExecution> res = runner.run(new MethodExecutionImpl(0, "",
				JAVA_OBJECT, "two", empty()), new File(TEST), new File(BIN));
		assertNotNull(res);
		System.out.println(res);
		assertEquals(2, res.size());
	}

	@Test
	public void testRun3() {
		List<Param> params = new LinkedList<Param>();
		params.add(new ParamImpl("x", 13, "int"));

		List<LineExecution> res = runner.run(new MethodExecutionImpl(0, "",
				JAVA_OBJECT, "three", params), new File(TEST), new File(BIN));
		assertNotNull(res);
		System.out.println(res);
		assertEquals(3, res.size());
	}

	@Test
	public void testRun4() {
		List<Param> params = new LinkedList<Param>();
		params.add(new ParamImpl("str", "TEST", "java.lang.String"));
		params.add(new ParamImpl("x", 13, "java.lang.Integer"));

		List<LineExecution> res = runner.run(new MethodExecutionImpl(0, "",
				JAVA_OBJECT, "four", params), new File(TEST), new File(BIN));
		assertNotNull(res);
		System.out.println(res);
		assertEquals(12, res.size());
	}

}
