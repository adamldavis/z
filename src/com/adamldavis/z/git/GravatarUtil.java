/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import neoe.ne.PicView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author Adam L. Davis
 * 
 */
public class GravatarUtil {

	public static String hex(byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
					1, 3));
		}
		return sb.toString();
	}

	public static String md5Hex(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return hex(md.digest(message.getBytes("CP1252")));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/** Uses default values of 64 for size and "retro" for deflt. */
	public static String getGravatarUrl(String email) {
		return getGravatarUrl(email, 64, "retro");
	}

	/**
	 * Get URL for Gravatar. See http://en.gravatar.com/site/implement/images/.
	 * 
	 * @param email
	 *            Full email address of user.
	 * @param size
	 *            Size in pixels of both width and height (square).
	 * @param deflt
	 *            Default to use if no image found (mm, identicon, monterid,
	 *            wavatar, retro).
	 * @return Just a String URL for the image to get.
	 */
	public static String getGravatarUrl(String email, int size, String deflt) {
		return "http://www.gravatar.com/avatar/" + md5Hex(email) + ".jpg?s="
				+ size + "&d=" + deflt;
	}

	/**
	 * Uses default values of 64 for size and "retro" for deflt.
	 * 
	 * @throws IOException
	 * @see {@link #getGravatar(String, int, String)}
	 */
	public static byte[] getGravatar(String email) throws IOException {
		return getGravatar(email, 64);
	}

	/**
	 * Uses default values of "retro" for deflt.
	 * 
	 * @throws IOException
	 * @see {@link #getGravatar(String, int, String)}
	 */
	public static byte[] getGravatar(String email, int size) throws IOException {
		return getGravatar(email, size, "retro");
	}

	/**
	 * Get bytes of image from Gravatar.
	 * 
	 * @param email
	 *            Full email address of user.
	 * @param size
	 *            Size in pixels of both width and height (square).
	 * @param deflt
	 *            Default to use if no image found (mm, identicon, monterid,
	 *            wavatar, retro).
	 * @return Just a String URL for the image to get.
	 * @throws IOException
	 *             If something goes wrong.
	 */
	public static byte[] getGravatar(String email, int size, String deflt)
			throws IOException {
		URL url = new URL(getGravatarUrl(email, size, deflt));
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		IOUtils.copy(url.openStream(), out);
		return out.toByteArray();
	}

	public static void main(String[] args) {
		String email = "ada.m.d.a.v@gmail.com";
		File f = new File("adam.jpg");
		try {
			FileUtils.writeByteArrayToFile(f, getGravatar(email));
			new PicView().show(f);
			f.delete();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
