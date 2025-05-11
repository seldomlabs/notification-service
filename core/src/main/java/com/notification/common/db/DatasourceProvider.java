package com.notification.common.db;

public class DatasourceProvider
{
	
	private static final ThreadLocal<AvailableDataSources> datasourceHolder = new ThreadLocal<AvailableDataSources>();
	
	public static void setDatasource(final AvailableDataSources customerType)
	{
		datasourceHolder.set(customerType);
	}
	
	public static AvailableDataSources getDatasource()
	{
		return (AvailableDataSources) datasourceHolder.get();
	}
	
	public static void clearDatasource()
	{
		datasourceHolder.remove();
	}
	
}
