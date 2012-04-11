package com.adamldavis.z;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

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

	private String getClassCode(ZNode zNode) {
		final StringBuilder builder = new StringBuilder(zNode.code);

		for (ZNode method : zNode.dependencies) {
			builder.append("\n\n").append(method.code);
		}
		if (languageParser.usesBraces()) {
			builder.append("\n}\n");
		}

		return builder.toString();
	}

	public void save(ZNode zNode) {
		switch (zNode.zNodeType) {
		case CLASS:
			save(new File(zNode.parentFile, zNode.name + "." + zNode.extension),
					getClassCode(zNode));
			break;
		case PACKAGE:
			save(new File(zNode.parentFile, languageParser.getPackageFilename()),
					zNode.code);
			break;
		case MODULE:
			if (zNode.code != null && zNode.code.length() > 0) {
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
				if (n >= start && n < end) {
					FileUtils.write(temp, zNode.code + "\n", true);
				} else
					FileUtils.write(temp, line + "\n", true);
			}
			temp.renameTo(classFile);
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("extension=" + zNode.extension);
		} catch (IOException e) {
			throw new RuntimeException("file=" + zNode.parentFile);
		}
	}

	public void save(File file, String data) {
		try {
			FileUtils.writeStringToFile(file, data, encoding);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
