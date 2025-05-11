package com.notification.util;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.notification.constants.Country;
import com.notification.constants.TimePeriod;
import org.springframework.util.StringUtils;

public class CountryUtils {

	public static Map<String, String> COUNTRY_CURRENCY_MAP = new HashMap<>();

	static
	{
		COUNTRY_CURRENCY_MAP.put(Country.IN.name(), "\u20B9");
		COUNTRY_CURRENCY_MAP.put(Country.MY.name(), "RM");
		COUNTRY_CURRENCY_MAP.put(Country.AE.name(), "\u062F\u002E\u0625");
		COUNTRY_CURRENCY_MAP.put(Country.ID.name(), "Rp");
	}

	public static String isValidMobileNumber(String number)
	{
		if (isValidIndianMobileNumber(number) != null)
		{
			return isValidIndianMobileNumber(number);
		}
		else if (isValidMalaysianMobileNumber(number) != null)
		{
			return isValidMalaysianMobileNumber(number);
		}
		else if (isValidUAEMobileNumber(number) != null)
		{
			return isValidUAEMobileNumber(number);
		}
		else if (isValidIndonesianMobileNumber(number) != null)
		{
			return isValidIndonesianMobileNumber(number);
		}
		
		return null;
	}

	public static String isValidIndianMobileNumber(String number) {
		if (number == null || number.length() < 10) {
			return null;
		}
		number = number.trim();
		number = number.replaceAll("\\s","");
		if(number.contains("+")){
			number = number.replace("+","");
		}
		if (number.length() == 10) {
			number = "91" + number;
		}
		if (number.length() != 12) {
			return null;
		}
		String regEx = "^(?:91)[6-9][0-9]{9}$";
		if (number.matches(regEx)) {
			return number;
		} else {
			return null;
		}
	}

	public static String isValidIndianMobileNumberNew(String number) {
		if (number == null || number.length() < 10) {
			return null;
		}
		if (number.length() == 10) {
			number = "91" + number;
		}
		if (number.length() == 11) {
			number = "91" + number.substring(1);
		}

		if (number.length() == 13) {
			number = number.substring(1);
		}

		if (number.length() != 12) {
			return null;
		}
		String regEx = "^(?:91)[6-9][0-9]{9}$";
		if (number.matches(regEx)) {
			return number;
		} else {
			return null;
		}
	}

	public static String isValidMalaysianMobileNumber(String number) {
		if (number == null) {
			return null;
		}
		if (number.length() < 11) {
			return null;
		}
		String regEx = "^(?:60)1[0-9]{8,9}$";
		if (number.matches(regEx)) {
			return number;
		} else {
			return null;
		}
	}

	public static String isValidUAEMobileNumber(String number) {
		if (number == null) {
			return null;
		}
		if (number.length() == 9) {
			number = "971" + number;
		}
		if (number.length() != 12) {
			return null;
		}
		String regEx = "^(?:971)?(?:50|52|54|55|56|58)\\d{7}$";
		if (number.matches(regEx)) {
			return number;
		} else {
			return null;
		}
	}
	
	public static String isValidIndonesianMobileNumber(String number)
	{
		if (number == null)
		{
			return null;
		}
		/*if (number.length() == 10)
		{
			number = "62" + number;
		}
		*/
		if (number.length() != 11 && number.length() != 12 && number.length() != 13 && number.length() != 14)
		{
			return null;
		}
	
		String regEx = "^628[0-9]{8,11}$";
		if (number.matches(regEx))
		{
			return number;
		}
		else
		{
			return null;
		}
	}
	
	public static boolean isValidCountry(String code)
	{
		return Arrays.stream(Country.values()).anyMatch((t) -> t.name().equalsIgnoreCase(code));
	}

	public static Country getCountryByCode(String code)
	{
		if (code == null)
		{
			return null;
		}
		for (Country country : Country.values())
		{
			if (country.name().equalsIgnoreCase(code))
				return country;
		}
		return null;
	}

	public static String getCountryByName(String name)
	{
		return getCountryByCountryName(name).name();
	}
	
	public static Country getCountryByCountryName(String name)
	{
		if (!StringUtils.isEmpty(name))
		{
			if (Country.IN.getCountryName().equalsIgnoreCase(name) || Country.IN.name().equalsIgnoreCase(name))
			{
				return Country.IN;
			}
			else if (Country.MY.getCountryName().equalsIgnoreCase(name) || Country.MY.name().equalsIgnoreCase(name))
			{
				return Country.MY;
			}
			else if (Country.AE.getCountryName().equalsIgnoreCase(name) || Country.AE.name().equalsIgnoreCase(name) || "UAE".equalsIgnoreCase(name))
			{
				return Country.AE;
			}
			else if (Country.ID.getCountryName().equalsIgnoreCase(name) || Country.ID.name().equalsIgnoreCase(name))
			{
				return Country.ID;
			}
		}
		return Country.IN;
	}

	public static Country getCountryByMobileNumber(String number)
	{
		if (isValidIndianMobileNumber(number) != null)
		{
			return Country.IN;
		}
		else if (isValidMalaysianMobileNumber(number) != null)
		{
			return Country.MY;
		}
		else if (isValidUAEMobileNumber(number) != null)
		{
			return Country.AE;
		}
		else if (isValidIndonesianMobileNumber(number) != null)
		{
			return Country.ID;
		}
		return Country.IN;
	}

	public static ZonedDateTime getEndOfDayInIst(String country)
	{
		ZoneId zoneId = ZoneId.of(TimePeriod.INDIAN_TIMEZONE);
		if (country.equals(Country.MY.name()))
		{
			zoneId = ZoneId.of(TimePeriod.MALAYSIAN_TIMEZONE);
		}
		else if (country.equals(Country.AE.name()))
		{
			zoneId = ZoneId.of(TimePeriod.UAE_TIMEZONE);
		}
		else if (country.equals(Country.ID.name()))
		{
			zoneId = ZoneId.of(TimePeriod.JAKARTA_TIMEZONE);
		}
	
		ZonedDateTime now = ZonedDateTime.now(zoneId);
		return now.toLocalDate().atStartOfDay(zoneId).plusDays(1).minusSeconds(1);
	}

	public static void main(String args[]) {
		// System.out.println(getEndOfDayInIst(Country.AE.name()));
		// ZoneId india = ZoneId.of(TimePeriod.INDIAN_TIMEZONE);
		// Date nextDayEnd = Date.from(
		// CountryUtils.getEndOfDayInIst(Country.AE.name()).plusDays(1).withZoneSameInstant(india).toInstant());
		// Calendar cal = Calendar.getInstance();
		// cal.setTime(nextDayEnd);
		// System.out.println(cal.get(Calendar.DATE) + " " +
		// cal.get(Calendar.HOUR) + " " + cal.get(Calendar.MINUTE) + " "
		// + cal.getTime());
		// System.err.println(COUNTRY_CURRENCY_MAP.get("AE"));
		//System.err.println(isValidIndonesianMobileNumber("6281280724057"));
		System.out.println(Country.IN.name().equalsIgnoreCase(CountryUtils.getCountryByName("India")));
	}

	public static BigDecimal getCountryCurrencyMultiplierByCountryOrCode(String country){
		if (!StringUtils.isEmpty(country))
		{
			if (Country.IN.getCountryName().equalsIgnoreCase(country) || Country.IN.name().equalsIgnoreCase(country))
			{
				return new BigDecimal(1);
			}
			else if (Country.ID.getCountryName().equalsIgnoreCase(country) || Country.ID.name().equalsIgnoreCase(country)) {
				return new BigDecimal(200);
			}
		}
		return new BigDecimal(1);
	}

}
