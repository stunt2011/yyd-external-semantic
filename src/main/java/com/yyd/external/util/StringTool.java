package com.yyd.external.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wqc
 * @date 2012-8-15
 * @version v1.1
 */
public class StringTool {

	private StringTool() {
	}

	/**
	 * @param s
	 * @return boolean
	 */
	public static boolean isEmpty(String s) {
		if (s == null)
			return true;
		if (s.trim().equals(""))
			return true;
		if (s.trim().toLowerCase().equals("null"))
			return true;
		return false;
	}

	public static boolean isEmptys(String... strs) {
		for (String str : strs) {
			if (isEmpty(str))
				return true;
		}
		return false;
	}

	/**
	 * URLEncoder
	 */
	public static String URLEncode(String s) {
		try {
			return java.net.URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/**
	 * URLDecoder
	 */
	public static String URLDecode(String s) {
		try {
			return java.net.URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	/***
	 * md5
	 * 
	 * @param string
	 * @return md5
	 */
	public static String md5(String string) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(string.getBytes("UTF-8"));
			byte tmp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Throwable e) {
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 判断是否有xss攻击
	 */
	public static boolean xssfilter(String str) {
		Pattern pattern = Pattern.compile(".*(?i)script.*");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			return false;
		}
		return true;
	}

	/**
	 * 判断url是否正确
	 */
	public static boolean checkUlr(String str) {
		InputStream is = null;
		try {
			URL url = new URL(str);
			is = url.openStream();
			if (is != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
