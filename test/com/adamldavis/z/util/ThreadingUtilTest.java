package com.adamldavis.z.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.adamldavis.z.util.ThreadingUtil.FindContext;
import com.adamldavis.z.util.ThreadingUtil.Finder;

public class ThreadingUtilTest {

	Random rnd = new Random();

	class ThreadContext implements FindContext {
		public List<String> get() {
			switch (rnd.nextInt(3)) {
			case 0:
				return Arrays.asList("zzz"); // no matches.
			case 1:
				return Arrays.asList("aa1"); // a matches
			default:
				return Arrays.asList("b1", "c1"); // b and c
			}

		}

		@Override
		public boolean isDone() {
			return false;
		}
	}

	@Test
	public void testMultiThreadedFind() {
		Collection<String> find = Arrays.asList("a", "b", "c");
		Finder<String, String, ThreadContext> finder = new Finder<String, String, ThreadingUtilTest.ThreadContext>() {
			@Override
			public String find(String searchObject, ThreadContext context) {
				for (String s : context.get()) {
					if (s.contains(searchObject))
						return s;
				}
				return null;
			}
		};
		ThreadContext context = new ThreadContext();
		Map<String, String> map = ThreadingUtil.multiThreadedFind(find, finder,
				context);
		assertEquals("aa1", map.get("a"));
		assertEquals("b1", map.get("b"));
		assertEquals("c1", map.get("c"));
		System.out.println("map=" + map);
	}

}
