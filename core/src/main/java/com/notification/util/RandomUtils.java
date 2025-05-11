package com.notification.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RandomUtils {
	
	public static <T> T getRandomElement(List<T> list) {
		if (list == null || list.isEmpty()) return null;
		return list.get(new Random().nextInt(list.size()));
	}
	
	public static <T> T getRandomElement(Collection<T> col) {
		if (col == null || col.isEmpty()) return null;
		ArrayList<T> list = new ArrayList<>(col);
		return list.get(new Random().nextInt(list.size()));
	}
	
	/* Creating a random UUID (Universally unique identifier). */
	public static String getUUID() {
        return UUID.randomUUID().toString();
    }

	public static String generateUUIDWithTimestamp() {
		// Get the current timestamp in milliseconds
		long timestamp = System.currentTimeMillis();

		// Generate a random UUID
		UUID uuid = UUID.randomUUID();

		// Combine the timestamp and UUID
		return timestamp + "-" + uuid.toString();
	}
	
}
