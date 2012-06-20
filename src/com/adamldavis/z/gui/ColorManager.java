/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.gui;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Uses yaml to load colors from color.yml.
 * 
 * @author Adam L. Davis
 */
public class ColorManager {

	public static final String COLORS = "colors/";

	private String filename = COLORS + "colors1.yml";

	final Map<String, String> colors = new HashMap<String, String>();

	static final Logger log = LoggerFactory.getLogger(ColorManager.class);

	public Color getColorFor(ColorSetting k) {
		if (colors.isEmpty())
			load();

		return Color.decode(colors.get(k.name()).replace("#", "0x"));
	}

	@SuppressWarnings("unchecked")
	public void load() {
		Yaml yaml = new Yaml();
		try {
			colors.putAll((Map<String, String>) yaml.load(new FileReader(
					new File(filename))));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public static void main(String[] args) {
		Yaml yaml = new Yaml();
		final Map<String, String> colors = new HashMap<String, String>();
		colors.put(ColorSetting.BACKGROUND.name(), "#1c1d1d");
		colors.put(ColorSetting.LINE.name(), "#5CCCCC");
		colors.put(ColorSetting.FAIL.name(), "#FD0404");
		colors.put(ColorSetting.OKAY.name(), "#03CA03");
		colors.put(ColorSetting.WARN.name(), "#FD7504");
		colors.put(ColorSetting.TODO.name(), "#029898");
		colors.put(ColorSetting.TEXT.name(), "#f9b175");
		colors.put(ColorSetting.HOVER.name(), "#ff9640");
		colors.put(ColorSetting.TASK.name(), "#008500");
		colors.put(ColorSetting.SELECTED_TASK.name(), "#67E667");
		try {
			FileWriter fw = new FileWriter("color.yml");
			yaml.dump(colors, fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
