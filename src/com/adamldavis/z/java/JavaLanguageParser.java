package com.adamldavis.z.java;

import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.LanguageParser;

/** Used for Java 5. */
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
		for (ZNode method : j.getMethods(file)) {
			System.out.println("----------------METHOD--------------");
			System.out.println("name=" + method.getName());
			System.out.println("ext=" + method.getExtension());
			System.out.println(method.getCode());
		}
		System.out.println("----------------CLASS--------------");
		System.out.println(j.getNonMethodPart(file));
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
				"switch", "case", "default", "boolean", "byte", "int", "float",
				"double", "char", "long", "try", "catch", "throw", "throws",
				"this", "super");
	}

	@Override
	public boolean usesBraces() {
		return true;
	}

	@Override
	public boolean requiresParens() {
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
		int methodStart = 0;

		try {
			reader = new FileReader(file);
			final BufferedReader br = new BufferedReader(reader);

			for (int lineNumber = 1; true; lineNumber++) {
				final String line = br.readLine();
				if (line == null) {
					break; // EOF
				} else if (line.startsWith(getImportKeyword())) {
					continue;
				}
				if (hasOpenBracket(line)) {
					braceDepth++;
				}
				if (hasCloseBracket(line)) {
					braceDepth--;
					if (braceDepth == 1) { // end of method or inner-class
						if (inMethod) {
							final ZNode method = methods
									.get(methods.size() - 1);
							int start = 0;

							if (code.substring(0, 1).equals("\n")) {
								start = 1;
							}
							method.replaceCode(code.append(line)
									.substring(start).toString());
							method.setExtension(methodStart + "-" + lineNumber);
							inMethod = false;
						}
						code.setLength(0);
						methodStart = lineNumber + 1;
					} else {
						code.append(line).append('\n');
					}
				} else if (braceDepth >= 1) {
					if (!inMethod && line.contains(";")) {
						code.setLength(0);
						methodStart = lineNumber + 1;
					} else {
						code.append(line).append('\n');
					}
				}
				if (!inMethod && braceDepth >= 1 && isMethodSig(code)) {
					methods.add(new ZNode(ZNodeType.METHOD, toMethodName(code),
							code.toString(), "", file));
					inMethod = true;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
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

	protected boolean hasCloseBracket(final String line) {
		return line.contains("}") && !line.contains("\"}\"");
	}

	protected boolean hasOpenBracket(final String line) {
		return line.contains("{") && !line.contains("\"{\"");
	}

	private String toMethodName(final StringBuilder code) {
		final String trim = code.toString().trim();
		StringBuilder name = new StringBuilder();
		String signature = trim.substring(
				isCommentStart(trim, 0) ? findCommentEnd(trim, 0) : 0).trim();

		for (String s : signature.split("\\s+")) {
			if (!s.startsWith("@") && !"{".equals(s)
					&& !getReservedWords().contains(s)) {
				name.append(s).append(' ');
			}
		}
		return name.toString();
	}

	@Override
	public boolean isCommentStart(CharSequence code, int i) {
		if (i + 1 >= code.length()) {
			return false;
		}
		final char char2 = code.charAt(i + 1);
		return (code.charAt(i) == '/' && (char2 == '*' || char2 == '/'));
	}

	@Override
	public int findCommentEnd(CharSequence code, int i) {
		if (code.charAt(i + 1) == '*') {
			return findCommentBlockEnd(code, i + 2);
		} else {
			return findCommentLineEnd(code, i + 2);
		}
	}

	private int findCommentBlockEnd(CharSequence code, int i) {
		if (i + 1 >= code.length()) {
			return i;
		} else if ("*/".equals(code.subSequence(i, i + 2))) {
			return i + 2;
		}
		return findCommentBlockEnd(code, i + 1);
	}

	private int findCommentLineEnd(CharSequence code, int i) {
		if (i + 1 >= code.length() || code.charAt(i) == '\n') {
			return i + 1;
		}
		return findCommentLineEnd(code, i + 1);
	}

	protected boolean isMethodSig(CharSequence code) {
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

	@Override
	public String getNonMethodPart(File file) {
		FileReader reader = null;
		boolean inMethod = false;
		int braceDepth = 0;
		final StringBuilder code = new StringBuilder();
		int i = 0;

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
				if (hasOpenBracket(line)) {
					braceDepth++;
					if (braceDepth == 1) { // class begins
						i = code.length() + line.length();
					}
				}
				if (hasCloseBracket(line)) {
					braceDepth--;
					if (braceDepth == 1) { // end of method or inner-class
						if (inMethod) {
							inMethod = false;
							continue;
						} else {
							i = code.length() + line.length();
						}
					}
				}
				if (!inMethod) {
					code.append(line).append('\n');

					if (braceDepth >= 1
							&& isMethodSig(code.subSequence(i, code.length()))) {
						inMethod = true;
						code.setLength(i);
					}
					if (line.trim().endsWith(";")) {
						i = code.length();
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new RuntimeException("Error closing file: " + e);
				}
			}
		}
		return code.toString();
	}

	@Override
	public String getPackageFilename() {
		return "package-info.java";
	}

	public Collection<ZNode> loadImports(File file) {
		final Collection<ZNode> imports = new LinkedList<ZNode>();
		final Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		try {
			final LineIterator iter = FileUtils.lineIterator(file);

			for (String line = iter.next(); iter.hasNext(); line = iter.next()) {
				final String keyword = getImportKeyword();

				if (line.startsWith(keyword)) {
					String pack = line.substring(keyword.length()).trim();
					String key = pack.substring(0, pack.lastIndexOf('.'));

					if (pack.charAt(pack.length() - 1) == ';') {
						pack = pack.substring(0, pack.length() - 1);
					}
					final List<String> list = map.get(key);
					if (list == null) {
						map.put(key, new LinkedList<String>(asList(pack)));
					} else {
						list.add(pack);
					}
				}
			}
			for (String key : map.keySet()) {
				final ZNode node = new ZNode(ZNodeType.DEPENDENCY, key, key,
						"", file);
				node.setCode(map.get(key));
				imports.add(node);
			}
			iter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return imports;
	}

	@Override
	public void loadMethodHierarchy(ZNode node) {
		// TODO make this better!
		final Pattern methodPattern = Pattern.compile("([\\.\\w]+)\\(");
		final Set<String> methods = new HashSet<String>();
		boolean inMethod = false;

		for (String line : node.getCodeLines()) {
			Matcher matcher = methodPattern.matcher(line);
			if (inMethod && matcher.find()) {
				String name = matcher.group(1);
				if (!methods.contains(name)) {
					methods.add(name);
					node.getSubmodules().add(
							new ZNode(ZNodeType.CALLEE, name, "", "", node
									.getParentFile()));
				}
			}
			if (hasOpenBracket(line)) {
				inMethod = true;
			}
		}
	}

}
