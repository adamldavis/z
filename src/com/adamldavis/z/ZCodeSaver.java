package com.adamldavis.z;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
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

		for (ZNode method : zNode.submodules) {
			code.addAll(end, method.getCodeLines());
		}

		return code;
	}

	public void save(ZNode zNode) {
		switch (zNode.zNodeType) {
		case CLASS:
			if ("".equals(zNode.extension)) {
				zNode.extension = languageParser.getValidFileExtensions().get(0);
			}
			String filename = zNode.name + "." + zNode.extension;
			save(new File(zNode.parentFile, filename), getClassCode(zNode));
			break;
		case PACKAGE:
			String dir = zNode.name.replace('.', File.separatorChar);

			if (!zNode.parentFile.getAbsolutePath().endsWith(dir)) {
				zNode.parentFile = new File(zNode.parentFile, dir);
				zNode.parentFile.mkdirs(); // make dirs
			}
			if (zNode.isCodeEmpty()) {
				zNode.replaceCode(languageParser.getPackageKeyword() + " "
						+ zNode.name.replaceAll("\\W", ".")
						+ (languageParser.requiresSemicolon() ? ";" : ""));
			}
			save(new File(zNode.parentFile, languageParser.getPackageFilename()),
					zNode.getCodeLines());
			break;
		case MODULE:
		case DEPENDENCY:
			if (!zNode.isCodeEmpty()) {
				dependencyManager.save(zNode);
			}
			break;
		case METHOD:
			saveMethod(zNode);
			// TODO: update all line #'s?
			break;
		}
	}

	/** Overwrite only the method represented by given node. */
	private void saveMethod(ZNode zNode) {
		int n = 0, start, end;
		try {
			if (zNode.extension.matches("\\d+-\\d+")) {
				String[] split = zNode.extension.split("-");
				start = Integer.parseInt(split[0]);
				end = Integer.parseInt(split[1]);
			} else {
				// new method
				start = end = Integer.parseInt(zNode.extension);
			}
			final File classFile = zNode.parentFile;
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
			zNode.extension = start + "-" + (start + zNode.getCodeLineSize());

			if (!classFile.delete() || !temp.renameTo(classFile)) {
				throw new RuntimeException("rename failed!");
			}
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("extension=" + zNode.extension);
		} catch (IOException e) {
			throw new RuntimeException("file=" + zNode.parentFile);
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
