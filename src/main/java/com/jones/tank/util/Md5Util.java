package com.jones.tank.util;


import com.jones.tank.object.InnerException;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Util {

	private static final char[] HEX_DIGITS =
			{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private Md5Util() {
		// dummy
	}

	public static String uniqueCode(String s){
		return md5(s).substring(0,10);
	}

	public static String md5(String s) {

		byte[] btInput = s.getBytes();

		// 获得MD5摘要算法的 MessageDigest 对象
		MessageDigest mdInst;
		try {
			mdInst = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new InnerException(e);
		}

		// 使用指定的字节更新摘要
		mdInst.update(btInput);

		byte[] md = mdInst.digest(); // 获得密文

		// 把密文转换成十六进制的字符串形式
		int j = md.length;
		char[] str = new char[j * 2];
		int k = 0;
		for (int i = 0; i < j; i++) {
			byte byte0 = md[i];
			str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
			str[k++] = HEX_DIGITS[byte0 & 0xf];
		}

		return new String(str);
	}

	public static void main(String[] args) {
		System.out.println(Md5Util.md5("jones"));
	}

}
