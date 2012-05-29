package com.adamldavis.z.api;

public interface APIFactory {

	CodeFormatter getCodeFormatter();

	DependencyManager getDependencyManager();

	LanguageParser getLanguageParser();

	Compiler getCompiler();

	CodeRunner getCodeRunner();

}
