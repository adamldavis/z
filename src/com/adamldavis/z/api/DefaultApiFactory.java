/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.api;

/**
 * Returns null for all methods (except {@link #getCodeFormatter()}). Suitable
 * for things like text and unknown programming languages.
 * 
 * @author Adam L. Davis
 */
public class DefaultApiFactory implements APIFactory {

	public static class DefaultCodeFormatter implements CodeFormatter {
		@Override
		public String format(String code) {
			return code;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.APIFactory#getCodeFormatter()
	 */
	@Override
	public CodeFormatter getCodeFormatter() {
		return new DefaultCodeFormatter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.APIFactory#getDependencyManager()
	 */
	@Override
	public DependencyManager getDependencyManager() {
		// TODO Auto-generated method stub
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.APIFactory#getCompiler()
	 */
	@Override
	public Compiler getCompiler() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.adamldavis.z.api.APIFactory#getCodeRunner()
	 */
	@Override
	public CodeRunner getCodeRunner() {
		// TODO Auto-generated method stub
		return null;
	}

}
