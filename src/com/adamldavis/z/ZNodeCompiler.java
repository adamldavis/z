/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z;

import javax.swing.ProgressMonitor;

import com.adamldavis.z.api.APIFactory;
import com.adamldavis.z.api.ProgressListener;

/**
 * @author Adam L. Davis
 * 
 */
public class ZNodeCompiler {

	private APIFactory apiFactory;

	private ProgressMonitor mon = new ProgressMonitor(null, "Compiling...",
			"The code is being compiled.", 0, 100);

	public ZNodeCompiler(APIFactory apiFactory) {
		this.apiFactory = apiFactory;
	}

	public void compile(ZNode zNode) {
		// TODO do something with errors
		apiFactory.getCompiler().compile(zNode, new ProgressListener() {

			@Override
			public void update(int progress) {
				mon.setProgress(progress);
			}
		});
		mon.setProgress(100);
	}

}
