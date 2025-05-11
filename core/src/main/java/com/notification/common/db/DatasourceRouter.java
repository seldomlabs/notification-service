package com.notification.common.db;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DatasourceRouter extends AbstractRoutingDataSource
{
	@Override
	public Object determineCurrentLookupKey()
	{
		AvailableDataSources dataSource = DatasourceProvider.getDatasource();
		if (dataSource != null && dataSource.toString().equalsIgnoreCase("READ")) {
			return "READ";
		}
		else {
			return "WRITE";
		}
	}
}
