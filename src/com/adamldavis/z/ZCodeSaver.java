package com.adamldavis.z;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

public class ZCodeSaver {

	final CodeFormatter codeFormatter;

	final LanguageParser languageParser;

	final DependencyManager dependencyManager;

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

	public void save(ZNode zNode) {
		switch (zNode.zNodeType) {
		case CLASS:
			save(new File(zNode.directory, zNode.name + "." + zNode.extension),
					zNode.code.getBytes());
		case PACKAGE:
			save(new File(zNode.directory, languageParser.getPackageFilename()),
					zNode.code.getBytes());
		case MODULE:
			if (zNode.code != null && zNode.code.length() > 0) {
				dependencyManager.save(zNode);
			}
		case METHOD:
			// TODO: save only the changes
		}
	}

	public void save(File file, byte[] bytes) {
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(file);
			final BufferedOutputStream bos = new BufferedOutputStream(fos);
			bos.write(bytes);
			bos.flush();
			fos.flush();

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
