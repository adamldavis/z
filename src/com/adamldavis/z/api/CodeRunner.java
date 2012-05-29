/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.api;

import java.io.File;
import java.util.List;

/**
 * Interface for generating results for each line of code.
 * 
 * @author Adam L. Davis
 * 
 */
public interface CodeRunner {

	/**
	 * Runs the given lines of code and returns a result to display for each
	 * line.
	 * 
	 * @param exe
	 *            Which method to execute with what parameters, if any.
	 * @param compilePath
	 *            Base directories of source Files and compiled files.
	 * 
	 * @return LineExecution's are in the order they were executed (in a for
	 *         loop, for example).
	 */
	List<LineExecution> run(MethodExecution exe, File... compilePath);

}
