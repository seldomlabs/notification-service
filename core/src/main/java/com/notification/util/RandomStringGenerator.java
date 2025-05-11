
package com.notification.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.SecureRandom;

/**
 * Generates cryptographically secure numeric, alphabetic and alphanumeric
 * strings *
 * 
 * @author raunak
 * 
 */
public class RandomStringGenerator {

	private static final String allDigits = "0123456789";

	private static final String allLowerCase = "abcdefghijklmnopqrstuvwxyz";

	private static final String allUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final String allAlpha = allLowerCase.concat(allUpperCase);

	private static final String allAlphaNum = allDigits.concat(allAlpha);

	private static final String lowAlphaDigits = allDigits.concat(allLowerCase);

	private static final String hiAlphaDigits = allDigits.concat(allUpperCase);

	public static String generateOtp(int len, String mode) {
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++)
			sb.append(mode.charAt(rnd.nextInt(mode.length())));
		return sb.toString();
	}

	public static String generateNumericOtp(int len) {
		return generateOtp(len, allDigits);
	}

	public static String generateAllAlphaNumericOtp(int len) {
		return generateOtp(len, allAlphaNum);
	}

	public static String generateAllAlphaOtp(int len) {
		return generateOtp(len, allAlpha);
	}

	public static String generateSessionToken() {
		return generateAllAlphaNumericOtp(16);
	}

	public static void main(String[] args) throws Exception {
		File file = new File("/opt/aryan/p.sql");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		for (int x = 96011; x <= 96133; x++) {

			bw.write("Update user set salt = '" + generateSessionToken() + "' where id = " + x + ";\n");
		}
		bw.close();
		System.out.println("SUCCESS!");
	}
}
