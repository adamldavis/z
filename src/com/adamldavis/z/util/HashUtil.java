/** Copyright 2012, Adam L. Davis, all rights reserved. */
package com.adamldavis.z.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.hash.Hashing;

/**
 * @author Adam L. Davis
 * 
 */
public class HashUtil {

	/** Used by GravatarUtil to emulate md5sum. */
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

	public static String hex(byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
					1, 3));
		}
		return sb.toString();
	}

	public static String shaHex(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			return hex(md.digest(message.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String sha256Hex(String message) {
		return hex(Hashing.sha256().hashString(message).asBytes());
	}

	public static String sha512Hex(String message) {
		return hex(Hashing.sha512().hashString(message).asBytes());
	}

	public static void main(String[] args) {
		System.out.println(shaHex("abc"));
		System.out.println(shaHex("cba"));
		System.out.println(shaHex("cab"));
		System.out.println(shaHex("cab").length());
	}

}
