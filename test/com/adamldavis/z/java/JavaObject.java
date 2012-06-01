package com.adamldavis.z.java;

/**
 * 
 * @author Adam L. Davis
 * 
 */
public class JavaObject {

	private int i = 0;

	private Integer num;

	private String str;

	/** one. */
	public int one() {
		return 1;
	}

	public int two() {
		return i;
	}

	public void three(int x) {
		num = x;
		System.out.println("num=" + num);
	}

	public String four(String str, Integer x) {
		num = x;
		for (int i = 0; i < str.length(); i++) {
			num += x;
		}
		this.str = str;
		return this.str;
	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}
}
