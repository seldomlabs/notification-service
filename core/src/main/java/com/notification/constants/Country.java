
package com.notification.constants;

public enum Country
{
	IN(1, 91, "INDIA", "₹"), MY(2, 60, "MALAYSIA","RM"), AE(3, 971, "DUBAI","AED"), ID(4, 62, "INDONESIA","Rp");
	
	private String countryName;
	
	private int country;
	
	private int countryCode;

	private String currencySymbol;

	private Country(int value, int countryCode, String name, String currencySymbol)
	{
		this.country = value;
		this.countryCode = countryCode;
		this.countryName = name;
		this.currencySymbol = currencySymbol;
	}
	
	public int getCountry()
	{
		return country;
	}
	
	public String getCountryName() 
	{
		return countryName;
	}
    
	public int getCountryCode()
	{
		return countryCode;
	}

    public String getCurrencySymbol() {
        return currencySymbol;
    }
}
