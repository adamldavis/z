package com.adamldavis.z.java;

import com.adamldavis.z.api.CodeFormatter;

public class JavaCodeFormatter implements CodeFormatter {

	@Override
	public String format(String code) {
		int slength = 0;
		for (; slength < code.length()
				&& (code.charAt(slength) == ' ' || code.charAt(slength) == '\t'); slength++)
			;

		if (slength > 1) {
			return code.replace(code.substring(0, slength), "");
		}

		return code;
	}

}
