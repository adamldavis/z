package com.adamldavis.z.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helpful methods for doing the dirty work of multithreading.
 * 
 * @author Adam L. Davis
 * 
 */
public class ThreadingUtil {

	public interface FindContext {
		boolean isDone();
	}

	public interface Finder<S, T, U extends FindContext> {
		T find(S searchObject, U context);
	}

	/** Uses ten threads. */
	public static <S, T, U extends FindContext> Map<S, T> multiThreadedFind(
			final Collection<S> find, final Finder<S, T, U> finder,
			final U context) {
		return multiThreadedFind(10, find, finder, context);
	}

	/**
	 * Multi-threading for finding multiple search values and the possibility of
	 * failure for each search value.
	 * 
	 * For example, let's say you're searching for multiple tokens in multiple
	 * files. Then S=String and T=File and the context keeps track of files.
	 * 
	 * @param <S>
	 *            Class of search object.
	 * @param <T>
	 *            Class of return (finding).
	 * @param <U>
	 *            Class of context.
	 * @param numThreads
	 *            Number of threads to use.
	 * @param find
	 *            Things to find.
	 * @param finder
	 *            Does the actual work of finding.
	 * @param context
	 *            Shared context for any shared data (not synchronized!).
	 * @return Map of search-objects to found-objects.
	 */
	public static <S, T, U extends FindContext> Map<S, T> multiThreadedFind(
			final int numThreads, final Collection<S> find,
			final Finder<S, T, U> finder, final U context) {
		final Set<S> found = Collections
				.synchronizedSet(new LinkedHashSet<S>());
		final Map<S, T> map = new HashMap<S, T>();
		Thread[] threads = new Thread[numThreads];

		// create X threads, synchronize finding on map
		for (int i = 0; i < threads.length; i++) {
			threads[i] = runAsThread(new Runnable() {
				public void run() {
					// keep looping till all found
					while (!found.containsAll(find) && !context.isDone()) {
						List<S> toFind = new ArrayList<S>(find);
						synchronized (found) {
							toFind.removeAll(found);
						}
						for (S s : toFind) {
							T t = finder.find(s, context);
							synchronized (map) {
								if (t != null && !found.contains(s)) {
									found.add(s);
									map.put(s, t);
								}
							}
						}
					}
				}
			});
		}
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
			}
		}
		return map;
	}

	/** runs as a daemon (will not keep JVM running). */
	public static Thread runAsThread(Runnable runnable) {
		return runAsThread(runnable, true);
	}

	public static Thread runAsThread(Runnable runnable, boolean daemon) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(daemon);
		thread.start();
		return thread;
	}

	// just a util
	private ThreadingUtil() {
	}

}
