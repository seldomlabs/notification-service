package com.notification.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author abhishek
 * 		
 */
public class ApplicationProperties
{
	
	public static interface MODULE
	{
		
		public static final String PROJECT = "project";
		
		public static final String BUILD = "build";
		
	}
	
	private static enum Singleton
	{
		INSTANCE;
		
		private static final ApplicationProperties singleton = new ApplicationProperties();
		
		ApplicationProperties getSingleton()
		{
			return singleton;
		}
	}
	
	private static final Logger logger = LogManager.getLogger(ApplicationProperties.class);
	
	private static final String PROPERTIES_FILE_LOCATION = "/Users/harsh.shukla/notification-service-properties/properties";
	
	private static final String PROPERTIES_FILE_LOCATION_DEFAULT = "/properties";
	
	private static final String BUILD_PROPERTIES_FILE_LOCATION = "/";
	
	private static final String PROPERTIES_FILE_SUFFIX = ".properties";
	
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		
	public static ApplicationProperties getInstance()
	{
		return ApplicationProperties.Singleton.INSTANCE.getSingleton();
	}
	
	private Map<String, Properties> applicationProperties = new ConcurrentHashMap<String, Properties>();
	
	private static final int REFRESH_INTERVAL_SECONDS = 5 * 60;
	
	private ApplicationProperties()
	{
		scheduler.scheduleWithFixedDelay(() -> {
			try
			{
				applicationProperties.keySet().forEach(this::reload);
			}
			catch (Exception ex)
			{
				logger.error(ex);
			}
		}, REFRESH_INTERVAL_SECONDS, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}
	
	public String getProperty(String module, String property)
	{
		return getProperty(module, property, "");
	}
	
	public String getProperty(String module, String property, String defaultValue) {
		if (ApplicationUtil.isStringEmpty(module))
		{
			return defaultValue;
		}

		Properties properties = applicationProperties.get(module);

		if (properties == null)
		{
			reload(module);
		}

		properties = applicationProperties.get(module);

		if (properties == null)
		{
			return defaultValue;
		}

		return properties.getProperty(property, defaultValue);
	}

	public String getPropertyByENV(String module, String property) {
		return this.getPropertyByENV(module, property, "");
	}
	public String getPropertyByENV(String module, String property, String defaultValue)
	{
		String value = System.getProperty(property);
		if (!StringUtils.isEmpty(value)) {
			return value;
		}
		String env = System.getProperty("ENV");
		String envModule = module;
		if ((env != null) && !env.equals("")) {
			envModule = String.format("%s.%s", module, env);
		}
		value = getProperty(envModule, property, "");
		if (value == null || value.equals("")) {
			value = getProperty(module, property, defaultValue);
		}
		return value;
	}

	public Properties getAllProperties(String moduleName)
	{
		Properties properties = applicationProperties.get(moduleName);
		if (properties == null)
		{
			reload(moduleName);
		}
		
		return applicationProperties.get(moduleName);
	}
	
	public void reload(String moduleName)
	{
		Properties properties = loadProperties(moduleName);
		applicationProperties.put(moduleName, properties);
		logProperties(moduleName, properties);
	}
	
	private Properties loadProperties(String module)
	{
		logger.debug("Loading properties for module: " + module);
		Properties properties = new Properties();
		InputStream moduleInputStream = null;
		
		String moduleFileName = ApplicationUtil.constructFilePath(getExternalFileLocation(module), module + PROPERTIES_FILE_SUFFIX);
		File moduleFile = new File(moduleFileName);
		if (moduleFile.exists() && moduleFile.canRead())
		{
			logger.debug("Loading " + module + " properties from file: " + moduleFileName);
			try
			{
				moduleInputStream = new FileInputStream(moduleFile);
			}
			catch (FileNotFoundException e)
			{
				logger.error("Error reading properties from file: " + moduleFileName, e);
			}
		}
		else
		{
			String moduleLocation = ApplicationUtil.constructFilePath(getDefaultPropertiesLocation(module), module + PROPERTIES_FILE_SUFFIX);
			logger.debug("Reading " + module + " properties from location: " + moduleLocation);
			moduleInputStream = ApplicationProperties.class.getResourceAsStream(moduleLocation);
		}
		
		if (moduleInputStream == null)
		{
			logger.error("Unable to acquire input stream to read properties for module: " + module);
			return properties;
		}
		
		try
		{
			properties.load(moduleInputStream);
		}
		catch (IOException e)
		{
			logger.error("Error reading properties", e);
		}
		finally
		{
			if (moduleInputStream != null)
			{
				try
				{
					moduleInputStream.close();
				}
				catch (IOException e)
				{
					logger.error("Error closing input stream for properties file", e);
				}
			}
		}
		
		return properties;
	}
	
	private String getDefaultPropertiesLocation(String module)
	{
		// to pick property file locations other than /properties
		if (MODULE.BUILD.equalsIgnoreCase(module))
		{
			return BUILD_PROPERTIES_FILE_LOCATION;
		}
		
		return PROPERTIES_FILE_LOCATION_DEFAULT;
	}
	
	private String getExternalFileLocation(String module)
	{
		return PROPERTIES_FILE_LOCATION;
	}
	
	@SuppressWarnings("rawtypes")
	private void logProperties(String moduleName, Properties properties)
	{
        for (Object o : properties.keySet()) {
            String key = (String) o;
            String value = properties.getProperty(key);
            if (key.toLowerCase().contains("password")) {
                value = "*****";
            }
            logger.debug("Loaded " + moduleName + " property: " + key + " = " + value);
        }
	}
}
