package com.adamldavis.z;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

/**
 * Responsible for saving things.
 * 
 * @author Adam Davis
 * 
 */
public class ZCodeSaver {

	final CodeFormatter codeFormatter;

	final LanguageParser languageParser;

	final DependencyManager dependencyManager;

	String encoding = "UTF-8";

	public ZCodeSaver(APIFactory apiFactory) {
		this(apiFactory.getCodeFormatter(), apiFactory.getDependencyManager(),
				apiFactory.getLanguageParser());
	}

	public ZCodeSaver(CodeFormatter codeFormatter,
			DependencyManager dependencyManager, LanguageParser languageParser) {
		super();
		this.codeFormatter = codeFormatter;
		this.languageParser = languageParser;
		this.dependencyManager = dependencyManager;
	}

	private List<String> getClassCode(ZNode zNode) {
		final List<String> code = new LinkedList<String>(zNode.getCodeLines());
		int end = zNode.getEndLineNumber(languageParser);
		int i = 0;

		for (String line : zNode.getCodeLines()) {
			i++;
			if (line.startsWith(languageParser.getPackageKeyword())) {
				break;
			}
		}
		for (ZNode imp : zNode.getDependencies()) {
			List<String> imports = new LinkedList<String>(imp.getCodeLines());
			Collections.reverse(imports);
			for (String line : imports) {
				code.add(i, languageParser.getImportKeyword() + " " + line
						+ (languageParser.requiresSemicolon() ? ";" : ""));
			}
			i += imp.getCodeLineSize();
		}
		List<ZNode> methods = new LinkedList<ZNode>(zNode.getSubmodules());
		Collections.reverse(methods);
		for (ZNode method : methods) {
			code.add("");
			code.addAll(end, method.getCodeLines());
		}

		return code;
	}

	public void save(ZNode zNode) {
		switch (zNode.getNodeType()) {
		case CLASS:
			if ("".equals(zNode.getExtension())) {
				zNode.setExtension(languageParser.getValidFileExtensions().get(
						0));
			}
			String filename = zNode.getName() + "." + zNode.getExtension();
			save(new File(zNode.getParentFile(), filename), getClassCode(zNode));
			break;
		case PACKAGE:
			String dir = zNode.getName().replace('.', File.separatorChar);

			if (!zNode.getParentFile().getAbsolutePath().endsWith(dir)) {
				zNode.setParentFile(new File(zNode.getParentFile(), dir));
				zNode.getParentFile().mkdirs(); // make dirs
			}
			if (zNode.isCodeEmpty()) {
				zNode.replaceCode(languageParser.getPackageKeyword() + " "
						+ zNode.getName().replaceAll("\\W", ".")
						+ (languageParser.requiresSemicolon() ? ";" : ""));
			}
			save(new File(zNode.getParentFile(),
					languageParser.getPackageFilename()), zNode.getCodeLines());
			break;
		case MODULE:
		case DEPENDENCY:
			if (!zNode.isCodeEmpty()) {
				dependencyManager.save(zNode);
			}
			break;
		case METHOD:
			saveMethod(zNode);
			updateLineNumbers(zNode);
			break;
		}
	}

	private void updateLineNumbers(ZNode methodNode) {
		List<ZNode> list = new LinkedList<ZNode>(methodNode.getParentNode()
				.getSubmodules());
		boolean pastMethod = false;
		final int diff = methodNode.getCodeLineSize()
				- methodNode.getOriginalSize();

		for (ZNode method : list) {
			if (methodNode == method) {
				pastMethod = true;
				continue;
			}
			if (pastMethod) {
				method.setLineNumber(method.getLineNumber() + diff);
			}
		}
	}

	/** Overwrite only the method represented by given node. */
	private void saveMethod(ZNode zNode) {
		int n = 0, start, end;
		try {
			start = zNode.getLineNumber();
			end = start + zNode.getOriginalSize();
			final File classFile = zNode.getParentFile();
			LineIterator iter = FileUtils.lineIterator(classFile);
			File temp = File.createTempFile(classFile.getName() + "_z", null);

			// iterate through file, copying it, except overwriting the method
			for (String line = null; iter.hasNext(); n++) {
				line = iter.next();
				if (n > start && n < end) {
					// skip these lines
				} else if (n == start) {
					FileUtils.writeLines(temp, zNode.getCodeLines(), true);
					if (start == end) { // new method
						FileUtils.writeLines(temp, asList(line), true);
					}
				} else
					FileUtils.writeLines(temp, asList(line), true);
			}
			iter.close();

			if (!classFile.delete() || !temp.renameTo(classFile)) {
				throw new RuntimeException("rename failed!");
			}
		} catch (IOException e) {
			throw new RuntimeException("file=" + zNode.getParentFile());
		}
	}

	public void save(File file, List<String> data) {
		try {
			FileUtils.writeLines(file, encoding, data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
