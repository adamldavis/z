package com.adamldavis.z.java;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.LanguageParser;

public class JavaLanguageParser implements LanguageParser {

	public static void main(String[] args) {
		JavaLanguageParser j = new JavaLanguageParser();
		System.out.println(j.isMethodSig("public class Barf {"));
		System.out.println(j
				.isMethodSig("public Integer foo = new Integer(1);"));
		System.out.println(j.isMethodSig("int barf(int i) {"));
		System.out.println(j.isMethodSig("void barf(int i,\n int j)\n {"));

		final File file = new File("src/com/adamldavis/z/ZNode.java");
		System.out.println(file.getAbsolutePath());
		System.out.println(j.getMethods(file));
	}

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
				"switch", "default", "boolean", "byte", "int", "float",
				"double", "char");
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
		boolean inMethod = false;
		int braceDepth = 0;
		final StringBuilder code = new StringBuilder();

		try {
			reader = new FileReader(file);
			final BufferedReader br = new BufferedReader(reader);

			while (true) {
				final String line = br.readLine();
				if (line == null) {
					break; // EOF
				} else if (line.startsWith(getImportKeyword())) {
					continue;
				}
				if (line.contains("{")) {
					braceDepth++;
				}
				if (line.contains("}")) {
					braceDepth--;
					if (braceDepth == 1) { // end of method or inner-class
						if (inMethod) {
							methods.get(methods.size() - 1).code = toMethodName(code);
							inMethod = false;
						}
						code.setLength(0);
					}
				} else if (braceDepth >= 1 && (inMethod || !line.contains(";"))) {
					code.append(line).append('\n');
				}
				if (!inMethod && braceDepth >= 1 && isMethodSig(code)) {
					methods.add(new ZNode(ZNodeType.METHOD, toMethodName(code),
							code.toString(), "", file.lastModified()));
					inMethod = true;
					code.setLength(0);
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

	private String toMethodName(final StringBuilder code) {
		String[] split = code.toString().trim().split("\\s+");
		StringBuilder name = new StringBuilder();
		for (String s : split) {
			if (!s.startsWith("@") && !"{".equals(s)
					&& !getReservedWords().contains(s)) {
				name.append(s).append(' ');
			}
		}
		return name.toString();
	}

	private boolean isMethodSig(CharSequence code) {
		return isMethodSigFind(code, 0);
	}

	private boolean isMethodSigFind(CharSequence code, int i) {
		if (i >= code.length()) {
			return false;
		} else if (code.charAt(i) == '{' || code.charAt(i) == '=') {
			return false;
		} else if (code.charAt(i) == '(') {
			return isMethodSigFindClose(code, i + 1);
		} else {
			return isMethodSigFind(code, i + 1);
		}
	}

	private boolean isMethodSigFindClose(CharSequence code, int i) {
		if (i >= code.length() || code.charAt(i) == '('
				|| code.charAt(i) == '=') {
			return false;
		} else if (code.charAt(i) == ')') {
			return isMethodSigFindBracket(code, i + 1);
		} else {
			return isMethodSigFindClose(code, i + 1);
		}
	}

	private boolean isMethodSigFindBracket(CharSequence code, int i) {
		if (i >= code.length() || code.charAt(i) == '('
				|| code.charAt(i) == '=') {
			return false;
		} else if (code.charAt(i) == '{') {
			return true;
		} else {
			return isMethodSigFindBracket(code, i + 1);
		}
	}

}
