/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.runner;

import com.adamldavis.z.api.Param;

/**
 * @author Adam L. Davis
 * 
 */
public class ParamImpl implements Param {

	String name;
	Object value;
	String type;

	public ParamImpl(String name, Object value, String type) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
	}

	public ParamImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return String.valueOf(value);
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
