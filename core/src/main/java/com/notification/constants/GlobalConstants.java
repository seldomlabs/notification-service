
package com.notification.constants;

import com.notification.util.ApplicationProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GlobalConstants {


	public static final String GOOGLE_BIGQUERY_CREDENTIALS = ApplicationProperties.getInstance().getProperty("google",
	"GOOGLE_BIGQUERY_CREDENTIALS");

	public static ObjectMapper objectMapper = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	public interface ValidationMessages {
		String DATA_INVALID = "Data can't be null";
	}
}
