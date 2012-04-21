/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.ruby;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.CodeFormatter;
import com.adamldavis.z.api.DependencyManager;
import com.adamldavis.z.api.LanguageParser;

/**
 * @author Adam L. Davis
 * 
 */
public class RubyFactory implements APIFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.APIFactory#getCodeFormatter()
	 */
	@Override
	public CodeFormatter getCodeFormatter() {
		return new RubyCodeFormatter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.APIFactory#getDependencyManager()
	 */
	@Override
	public DependencyManager getDependencyManager() {
		return new RakeGemManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.APIFactory#getLanguageParser()
	 */
	@Override
	public LanguageParser getLanguageParser() {
		// TODO Auto-generated method stub
		return null;
	}

}
