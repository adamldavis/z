/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.git;

import java.util.Date;

/**
 * @author Adam L. Davis
 * 
 */
public class GitLog {

	private final GitUser author;
	private final String id;
	private final Date date;
	private final String message;

	public GitLog(String id, GitUser author, Date date, String message) {
		super();
		this.author = author;
		this.id = id;
		this.date = date;
		this.message = message;
	}

	public GitUser getAuthor() {
		return author;
	}

	public String getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public String getMessage() {
		return message;
	}

}
