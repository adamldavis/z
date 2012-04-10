package com.adamldavis.z.api;

import java.io.File;
import java.util.List;

import com.adamldavis.z.ZNode;

public interface LanguageParser {

	String getImportKeyword();

	String getPackageKeyword();

	List<String> getReservedWords();

	List<String> getValidFileExtensions();

	List<ZNode> getMethods(File file);

	boolean requiresSemicolon();

	boolean usesBraces();

	boolean usesParens();

}
