package com.adamldavis.z;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class UserSettings {

	public static final String DIRECTION = "direction";

	public static final String LAST_LOCATION = "lastLocation";

	public static final String LAYOUT = "layout";

	public static final String ORDER = "order";

	public static final String Z = ".z";

	Properties props = new Properties();

	public UserSettings() {
		String home = System.getProperty("user.home");
		try {
			if (home == null) {
				props.load(new FileReader(new File(Z)));
			} else {
				props.load(new FileReader(new File(home, Z)));
			}
		} catch (FileNotFoundException e) {
			// that's okay
			System.out.println("Warning: no .z file");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getFile(String key) {
		return new File(props.getProperty(key));
	}

	public String getProperty(String key) {
		return props.getProperty(key);
	}

	public void save() {
		String home = System.getProperty("user.home");

		try {
			if (home == null) {
				props.store(new FileWriter(Z), "");
			} else {
				props.store(new FileWriter(new File(home, Z)), "");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setProperty(String key, String string) {
		props.setProperty(key, string);
	}

}
