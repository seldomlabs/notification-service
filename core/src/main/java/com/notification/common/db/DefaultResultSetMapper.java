package com.notification.common.db;

import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.util.List;

import com.notification.common.exception.ApplicationException;
import org.apache.logging.log4j.Logger;

import org.apache.logging.log4j.LogManager;

public class DefaultResultSetMapper<T> implements ResultSetMapper<T>
{
	
	public Logger logger = LogManager.getLogger(DefaultResultSetMapper.class);
	
	Class<T> entityClass;
	
	@SuppressWarnings("unchecked")
	public DefaultResultSetMapper()
	{
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.entityClass = (Class<T>) genericSuperclass.getActualTypeArguments()[0];
	}
	
	public DefaultResultSetMapper(Class<T> entityClass)
	{
		this.entityClass = entityClass;
	}
	
	@Override
	public List<T> mapResultSet(ResultSet resultSet) throws ApplicationException
	{
		return ResultSetUtility.resultSetMapper(resultSet, entityClass, logger);
	}
	
}
