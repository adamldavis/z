/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import com.adamldavis.z.gui.ColorSetting;

/**
 * Reads txt file exported from {@linkplain http ://colorschemedesigner.com} and
 * saves it as a yaml file for Zeta Code.
 * 
 * @author Adam L. Davis
 * 
 */
public class ColorSchemeUtil {

	static final char hash = '#';
	static final int hexColorLength = "#123456".length();

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out
					.println("Usage: java ColorSchemeUtil <input.txt> [<output.yml>]");
			return;
		}
		try {
			String out = new File(args[0]).getName();
			if (args.length == 2) {
				out = args[1];
			}
			convertTextToYml(args[0], out.replace(".txt", ".yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads txt file exported from {@linkplain http ://colorschemedesigner.com}
	 * and save it as a yaml file for Zeta Code.
	 * 
	 * @param inFile
	 *            name of file.
	 * @param filename
	 *            output yml filename.
	 * @throws IOException
	 */
	public static void convertTextToYml(String inFile, String filename)
			throws IOException {
		Yaml yaml = new Yaml();
		final Map<String, String> colors = new HashMap<String, String>();
		final String[][] arr = parseSchemeText(inFile);
		colors.put(ColorSetting.BACKGROUND.name(), "#1c1d1d");
		colors.put(ColorSetting.LINE.name(), arr[2][4]);
		colors.put(ColorSetting.FAIL.name(), arr[0][0]);
		colors.put(ColorSetting.OKAY.name(), arr[3][0]);
		colors.put(ColorSetting.WARN.name(), arr[1][0]);
		colors.put(ColorSetting.TODO.name(), arr[2][0]);
		colors.put(ColorSetting.TEXT.name(), arr[1][4]);
		colors.put(ColorSetting.HOVER.name(), arr[1][3]);
		colors.put(ColorSetting.TASK.name(), arr[3][2]);
		colors.put(ColorSetting.SELECTED_TASK.name(), arr[3][4]);
		try {
			FileWriter fw = new FileWriter(filename);
			yaml.dump(colors, fw);
			fw.flush();
			fw.close();
			System.out.println("Wrote:" + filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reads txt file exported from {@linkplain http://colorschemedesigner.com}.
	 * 
	 * @param inFile
	 *            name of file.
	 * @return 2 dimensional array (colors*variations).
	 * @throws IOException
	 */
	public static String[][] parseSchemeText(String inFile) throws IOException {
		List<String> lines = FileUtils.readLines(new File(inFile));
		String[][] arr = new String[4][5]; // colors*variations
		int i = -1, j = 0;

		for (String line : lines) {
			if (line.startsWith("**")) {
				i++;
				j = 0;
			} else if (i > -1) {
				int index = line.indexOf(hash);
				if (index > 0) {
					arr[i][j] = line.substring(index, index + hexColorLength);
					j++;
				}
			}
		}
		return arr;
	}

}
