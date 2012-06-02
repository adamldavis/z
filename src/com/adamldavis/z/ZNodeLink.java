/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z;

/**
 * Represents a link between two nodes such as a method-call.
 * 
 * @author Adam L. Davis
 * 
 */
public class ZNodeLink {

	public enum LinkType {
		METHOD_CALL, EXTENDS, HAS_A, REQUIRES
	}

	private final ZNode node1;

	private final ZNode node2;

	private final LinkType linkType;

	public ZNodeLink(ZNode node1, ZNode node2, LinkType linkType) {
		super();
		this.node1 = node1;
		this.node2 = node2;
		this.linkType = linkType;
	}

	public ZNode getNode1() {
		return node1;
	}

	public ZNode getNode2() {
		return node2;
	}

	public LinkType getLinkType() {
		return linkType;
	}

}
