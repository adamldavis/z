package com.adamldavis.z;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.adamldavis.z.api.CodeFormatter;

public class ZCodeSaver {

	CodeFormatter codeFormatter;

	public ZCodeSaver(CodeFormatter codeFormatter) {
		super();
		this.codeFormatter = codeFormatter;
	}

	public void save(ZNode zNode) {
		FileOutputStream fos = null;

		try {
			fos = new FileOutputStream(zNode.name + "." + zNode.extension);
			new BufferedOutputStream(fos).write(zNode.code.getBytes());
			fos.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					System.err.println("Error closing file: " + e);
				}
			}
		}
	}
}
