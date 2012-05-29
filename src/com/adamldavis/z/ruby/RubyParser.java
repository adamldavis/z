/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.ruby;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adamldavis.z.ZNode;
import com.adamldavis.z.ZNode.ZNodeType;
import com.adamldavis.z.api.Compiler;
import com.adamldavis.z.api.LanguageParser;
import com.adamldavis.z.api.LineError;
import com.adamldavis.z.api.ProgressListener;

/**
 * @author Adam L. Davis
 * 
 */
public class RubyParser implements LanguageParser, Compiler {

	private static final Logger log = LoggerFactory.getLogger(RubyParser.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#getImportKeyword()
	 */
	@Override
	public String getImportKeyword() {
		return "require";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#getPackageKeyword()
	 */
	@Override
	public String getPackageKeyword() {
		return "module";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#getReservedWords()
	 */
	@Override
	public List<String> getReservedWords() {
		return asList("module", "require", "if", "while", "end", "def",
				"assert", "do", "include", "else", "private", "return", "case",
				"when", "begin", "rescue", "raise", "unless", "class", "self",
				"super", "elsif", "protected");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#getValidFileExtensions()
	 */
	@Override
	public List<String> getValidFileExtensions() {
		return asList("rb");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#getMethods(java.io.File)
	 */
	@Override
	public List<ZNode> getMethods(File file) {
		final List<ZNode> methods = new LinkedList<ZNode>();
		parseRubyFile(file, methods);
		return methods;
	}

	private static class ParserData {
		int n = 0; // line #
		int methodDepth = -1;
		int methodStartLine = -1;
		int currentDepth = 0;
		File file = null;
		String methodName = null;
		final StringBuilder code = new StringBuilder();
	}

	private String parseRubyFile(File file, List<ZNode> methods) {
		try {
			final LineIterator iter = FileUtils.lineIterator(file);
			final ParserData data = new ParserData();
			data.file = file;

			for (String line = iter.next(); iter.hasNext(); data.n++, line = iter
					.next()) {
				parseRubyLine(line, methods, data);
			}
			iter.close();
			return data.code.toString();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return "";
	}

	static final Set<String> beginWords = new HashSet<String>(asList("begin",
			"do", "if", "module", "class", "case"));

	private void parseRubyLine(String line, List<ZNode> methods, ParserData data) {
		if (isCommentStart(line.trim(), 0)) {
			return;
		}
		String[] split = line.split("\\s+"); // split on spaces

		if (split.length > 0 && beginWords.contains(split[0])
				|| beginWords.contains(split[split.length - 1])) {
			data.currentDepth++;
		} else if (line.trim().equals("end")) {
			data.currentDepth--;
			if (data.methodDepth == data.currentDepth) {
				data.methodDepth = -1;
				if (methods != null) {
					data.code.append(line).append('\n');
					methods.add(new ZNode(ZNodeType.METHOD, data.methodName,
							data.code.toString(), data.methodStartLine + "-"
									+ data.n, data.file));
				}
				return;
			}
		} else if (line.trim().startsWith("def ")) {
			data.methodDepth = data.currentDepth;
			data.methodStartLine = data.n;
			data.currentDepth++;

			if (methods != null) {
				data.code.setLength(0);
				final int i = line.indexOf("def ") + 4;
				data.methodName = line.substring(i).trim();
			}
		}
		if (methods != null || data.methodDepth == -1) {
			data.code.append(line).append('\n');
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#getNonMethodPart(java.io.File)
	 */
	@Override
	public String getNonMethodPart(File file) {
		return parseRubyFile(file, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#loadImports(java.io.File)
	 */
	@Override
	public Collection<ZNode> loadImports(File file) {
		final Collection<ZNode> requires = new LinkedList<ZNode>();
		final Map<String, ZNode> map = new LinkedHashMap<String, ZNode>();
		try {
			final LineIterator iter = FileUtils.lineIterator(file);
			int n = 0;

			for (String line = iter.next(); iter.hasNext(); line = iter.next(), n++) {
				final String keyword = getImportKeyword();

				if (line.trim().startsWith(keyword)) {
					final int index = line.indexOf("\'") + 1;
					String pack = line.substring(index, line.lastIndexOf("\'"));
					String key = pack.contains("/") ? pack.substring(0,
							pack.lastIndexOf('/')) : pack;
					final ZNode znode = map.get(key);

					if (znode == null) {
						final ZNode node = new ZNode(ZNodeType.DEPENDENCY, key,
								pack, "" + n, file);
						requires.add(node);
						map.put(key, node);
					} else {
						znode.addCodeLine(pack);
					}
				} else if (line.trim().startsWith("module")) {
					break; // stop looking for requires
				}
			}
			iter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return requires;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#requiresSemicolon()
	 */
	@Override
	public boolean requiresSemicolon() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#usesBraces()
	 */
	@Override
	public boolean usesBraces() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#usesParens()
	 */
	@Override
	public boolean requiresParens() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.LanguageParser#getPackageFilename()
	 */
	@Override
	public String getPackageFilename() {
		return "version.rb";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adamldavis.z.api.LanguageParser#isCommentStart(java.lang.CharSequence
	 * , int)
	 */
	@Override
	public boolean isCommentStart(CharSequence code, int i) {
		if (i >= code.length()) {
			return false;
		}
		return code.charAt(i) == '#';
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.adamldavis.z.api.LanguageParser#findCommentEnd(java.lang.CharSequence
	 * , int)
	 */
	@Override
	public int findCommentEnd(CharSequence code, int i) {
		if (i + 1 >= code.length() || code.charAt(i) == '\n') {
			return i + 1;
		}
		return findCommentEnd(code, i + 1);
	}

	@Override
	public List<LineError> compile(ZNode node, ProgressListener listener) {
		// TODO do nothing since ruby is dynamic
		return new LinkedList<LineError>();
	}

}
