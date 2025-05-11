
package com.notification.constants;

import org.apache.commons.lang3.StringUtils;

public enum TimePeriod
{
	TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, THIS_QUARTER, ALL_TIME;
	
	public static final String INDIAN_TIMEZONE = "Asia/Kolkata";
	
	public static final String MALAYSIAN_TIMEZONE = "Asia/Kuala_Lumpur";
	
	public static final String UAE_TIMEZONE = "Asia/Dubai";
	
	public static final String JAKARTA_TIMEZONE = "Asia/Jakarta";

	public static final String MAKASSAR_TIMEZONE = "Asia/Makassar";

	public static final String JAYAPURA_TIMEZONE = "Asia/Jayapura";
	
	public static final String UTC = "UTC";
	
	public static TimePeriod getTimePeriod(String period)
	{
		if (StringUtils.isEmpty(period))
		{
			return TODAY;
		}
		switch (period)
		{
			case ("TODAY"):
			case ("DAILY"):
			case ("DAY"):
				return TimePeriod.TODAY;
			case ("WEEK"):
			case ("WEEKLY"):
			case ("THIS_WEEK"):
				return TimePeriod.THIS_WEEK;
			case ("MONTH"):
			case ("MONTHLY"):
				return TimePeriod.THIS_MONTH;
			case ("QUARTER"):
			case ("QUARTERLY"):
				return TimePeriod.THIS_QUARTER;
			case ("YEAR"):
			case ("YEARLY"):
			case ("ANNUAL"):
				return TimePeriod.THIS_YEAR;
			default:
				//If all else fails, default to just Today
				return TimePeriod.TODAY;
		}
	}
	
}
