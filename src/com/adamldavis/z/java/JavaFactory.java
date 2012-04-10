package com.adamldavis.z.java;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

public class JavaFactory implements APIFactory {

	@Override
	public CodeFormatter getCodeFormatter() {
		return new JavaCodeFormatter();
	}

	@Override
	public DependencyManager getDependencyManager() {
		return new MavenDependencyManager();
	}

	@Override
	public LanguageParser getLanguageParser() {
		return new JavaLanguageParser();
	}

}
