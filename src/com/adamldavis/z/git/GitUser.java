/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.git;

import com.adamldavis.z.ZNode;

/**
 * Represents an author in git.
 * 
 * @author Adam L. Davis
 * 
 */
public class GitUser extends ZNode {

	private static final long serialVersionUID = -5836315607126335476L;

	private final String name;
	private final String email;

	public GitUser(String name, String email) {
		super();
		this.name = name;
		this.email = email;
		replaceCode(email);
	}

	@Override
	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public int hashCode() {
		return name.hashCode() * 13 + email.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GitUser) {
			GitUser other = (GitUser) obj;
			return name.equals(other.name) && email.equals(other.email);
		}
		return false;
	}

}
