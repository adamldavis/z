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

	private final File fileA, fileB;

	private static final Pattern patt = Pattern
			.compile("diff --git a/([\\w/\\.]+) b/([\\w/\\.]+)");

	public static GitDiff newGitDiff(String line, File dir) {
		Matcher matcher = patt.matcher(line);
		if (!matcher.find()) {
			return null;
		}
		return new GitDiff(new File(dir, matcher.group(1)), new File(dir,
				matcher.group(2)));
	}

	public GitDiff(File fileA, File fileB) {
		super();
		this.fileA = fileA;
		this.fileB = fileB;
	}

	public File getFileA() {
		return fileA;
	}

	public File getFileB() {
		return fileB;
	}

	public static boolean isMatching(String line) {
		return patt.matcher(line).matches();
	}

}
