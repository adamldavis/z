/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.git;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Adam L. Davis
 * 
 */
public class GitDiff {

	private final File file;
	private final String status; // M=modified, A=Added

	private static final Pattern patt = Pattern
			.compile("([MAD])\\s+([\\w/\\.\"]+)");

	public static GitDiff newGitDiff(String line, File dir) {
		Matcher matcher = patt.matcher(line);
		if (!matcher.find()) {
			return null;
		}
		return new GitDiff(new File(dir, matcher.group(2)), matcher.group(1));
	}

	public GitDiff(File file, String status) {
		super();
		this.file = file;
		this.status = status;
	}

	public File getFile() {
		return file;
	}

	public static boolean isMatching(String line) {
		return patt.matcher(line).matches();
	}

	public String getStatus() {
		return status;
	}

}
