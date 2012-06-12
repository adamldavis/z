/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.git;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * List of GitLog with ability to parse a git log.
 * 
 * @author Adam L. Davis
 * 
 */
public class GitLogList extends ArrayList<GitLog> {

	private static final long serialVersionUID = 1L;

	private final Set<GitUser> users = new LinkedHashSet<GitUser>();

	private final SimpleDateFormat sdf = new SimpleDateFormat(
			"MMM d kk:mm:ss yyyy Z");

	/** Goes through lines and adds multiple GitLog objects. */
	public void addAll(Iterator<String> logLines) {
		final List<String> subList = new LinkedList<String>();

		for (String line = ""; logLines.hasNext();) {
			line = logLines.next();
			if (line.startsWith("commit")) {
				if (!subList.isEmpty())
					add(Collections.unmodifiableList(subList));

				subList.clear();
			}
			subList.add(line);
		}
	}

	/** Assumes only one git log. */
	public void add(Iterable<String> logLines) {
		Iterator<String> iter = logLines.iterator();

		try {
			super.add(new GitLog(getId(iter.next()), getAuthor(iter),
					getDate(iter.next()), getMessage(iter)));
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private String getId(String line) {
		return line.substring("commit ".length()).trim();
	}

	private Date getDate(String line) throws ParseException {
		return sdf.parse(line.substring("Date:   Sat ".length()).trim());
	}

	private String getMessage(Iterator<String> iter) {
		StringBuilder sb = new StringBuilder();
		while (iter.hasNext()) {
			sb.append(iter.next());
		}
		return sb.toString().trim();
	}

	private GitUser getAuthor(Iterator<String> iter) {
		String line = iter.next();
		if (line.startsWith("Merge")) {
			line = iter.next(); // skip Merge: line
		}
		final int openBracket = line.indexOf('<');
		final int closeBracket = line.lastIndexOf('>');
		GitUser user = new GitUser(line.substring("Author: ".length(),
				openBracket).trim(), line.substring(openBracket + 1,
				closeBracket).trim());
		if (users.contains(user)) {
			for (GitUser gu : users) {
				if (gu.equals(user)) {
					return gu;
				}
			}
		}
		users.add(user);
		return user;
	}

	public Set<GitUser> getUsers() {
		return users;
	}

}
