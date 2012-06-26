package com.adamldavis.z.api;

/**
 * Returns instances of the various interfaces that have different
 * implementations depending on the programming language.
 * 
 * @author Adam L. Davis
 * 
 */
public interface APIFactory {

	CodeFormatter getCodeFormatter();

	DependencyManager getDependencyManager();

	LanguageParser getLanguageParser();

	Compiler getCompiler();

	CodeRunner getCodeRunner();

}
