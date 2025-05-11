package com.notification.util;

import com.notification.common.exception.ApplicationException;
import org.apache.logging.log4j.Logger;

public class AssertUtil
{
	
	public static void assertBool(boolean assertion, int errorCode, String message, Logger logger) throws ApplicationException
	{
		if (!assertion)
		{
			throw generateException(errorCode, message, null, logger);
		}
	}
	
	public static ApplicationException generateException(int errorCode, String message, Throwable causingException, Logger logger)
	{
		return new ApplicationException(errorCode, message, causingException);
	}

	public static void assertBool(boolean assertion, int errorCode, String message) throws ApplicationException{
		if(!assertion){
			throw  generateException(errorCode, message, null);
		}
	}

	public static ApplicationException generateException(int errorCode, String message, Throwable causingException){
		return new ApplicationException(errorCode, message, causingException);
	}
}
