/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.ruby;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.CodeRunner;
import com.adamldavis.z.api.Compiler;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

/**
 * @author Adam L. Davis
 * 
 */
public class RubyFactory implements APIFactory {

	@Override
	public CodeFormatter getCodeFormatter() {
		return new RubyCodeFormatter();
	}

	@Override
	public DependencyManager getDependencyManager() {
		return new RakeGemManager();
	}

	@Override
	public LanguageParser getLanguageParser() {
		return new RubyParser();
	}

	@Override
	public Compiler getCompiler() {
		return new RubyParser();
	}

	@Override
	public CodeRunner getCodeRunner() {
		// TODO Auto-generated method stub
		return null;
	}

}
