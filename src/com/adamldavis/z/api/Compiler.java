package com.adamldavis.z.api;

import java.util.List;

import com.adamldavis.z.ZNode;

public interface Compiler {

	// TODO: this should return results of compile and classpath?
	List<LineError> compile(ZNode node, ProgressListener listener);

}
