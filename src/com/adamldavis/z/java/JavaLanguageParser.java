package com.adamldavis.z.java;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.api.LanguageParser;

public class JavaLanguageParser implements LanguageParser {

	@Override
	public List<String> getValidFileExtensions() {
		return asList("java");
	}

	@Override
	public List<String> getReservedWords() {
		return asList("public", "private", "protected", "class", "interface",
				"instanceof", "implements", "extends", "import", "return",
				"void", "null", "if", "else", "while", "for", "do", "true",
				"false", "enum", "static", "transient", "volatile", "package",
				"switch", "default");
	}

	@Override
	public boolean usesBraces() {
		return true;
	}

	@Override
	public boolean usesParens() {
		return true;
	}

	@Override
	public boolean requiresSemicolon() {
		return true;
	}

	@Override
	public String getImportKeyword() {
		return "import";
	}

	@Override
	public String getPackageKeyword() {
		return "package";
	}

	@Override
	public List<ZNode> getMethods(File file) {
		final List<ZNode> methods = new ArrayList<ZNode>();
		FileReader reader = null;

		try {
			reader = new FileReader(file);
			final BufferedReader br = new BufferedReader(reader);

			while (true) {
				final String line = br.readLine();
				if (line == null) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RuntimeException("Error closing file: " + e);
				}
			}
		}
		return methods;
	}

}
