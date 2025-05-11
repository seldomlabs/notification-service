
package com.notification.common.dto.util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class StringCodec {

	private static final String UTF8 = "utf-8";

	private static Logger logger = LogManager.getLogger(StringCodec.class);

	// FIXME Should not be used anywhere for db operations
	public static String decodeUtf8(String encoded) {
		try {
			if (encoded != null) {
				String decoded = URLDecoder.decode(encoded, UTF8);
				return decoded.equals(encoded) ? decoded : decodeUtf8(decoded);
			}
		} catch (Exception e) {
			// logger.error(e);
		}
		return encoded;
	}

	public static String encodeUtf8(String decoded) {
		try {
			return decoded == null ? null : URLEncoder.encode(decoded, UTF8);
		} catch (Exception e) {
			logger.error(ExceptionUtils.getRootCauseMessage(e));
			return decoded;
		}
	}

	public static String toString(Blob blob) throws SQLException {
		return blob == null ? null : new String(blob.getBytes(1, (int) blob.length()));
	}

	public static Blob toBlob(String string) throws SerialException, SQLException {
		return string == null ? null : new SerialBlob(string.getBytes());
	}

	public static float getPercentageSimilarityInTwoStrings(String smallString, String largeString) {
		if (smallString.length() >= largeString.length()) {
			return 0f;
		}
		int maxIteration = Math.max(0, largeString.length() - smallString.length());
		int minDistance = smallString.length();
		for (int i = 0; i <= maxIteration && minDistance > 0; i++) {
			int distance = StringUtils.getLevenshteinDistance(largeString.substring(i, i + smallString.length()),
					smallString);
			minDistance = minDistance < distance ? minDistance : distance;
		}
		return (float) (smallString.length() - minDistance) / (float) smallString.length();
	}

	public static float getPercentageSimilarityInTwoStringsWithAnyAsSmall(String smallString, String largeString) {
		if (smallString.length() > largeString.length()) {
			return getPercentageSimilarityInTwoStrings(largeString, smallString);
		}
		int maxIteration = Math.max(0, largeString.length() - smallString.length());
		int minDistance = smallString.length();
		for (int i = 0; i <= maxIteration && minDistance > 0; i++) {
			int distance = StringUtils.getLevenshteinDistance(largeString.substring(i, i + smallString.length()),
					smallString);
			minDistance = minDistance < distance ? minDistance : distance;
		}
		return (float) (smallString.length() - minDistance) / (float) smallString.length();
	}


}
