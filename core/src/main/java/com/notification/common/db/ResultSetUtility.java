package com.notification.common.db;

import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.notification.common.exception.ApplicationException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.Logger;

/**
 * This Class holds result set related utility methods
 * 
 * @author aawasthi
 * 		
 */
public class ResultSetUtility
{
	
	/**
	 * This is generic result set to object mapping utility. This relies on the assumption that result set has exact the same column as defined in the
	 * POJO field. Target class is supposed to be POJO.
	 * 
	 * @param <T>
	 * @param resultSet
	 * @param type
	 * @return
	 * @throws OneshieldException
	 */
	static public <T> List<T> resultSetMapper(ResultSet resultSet, Class<T> type, Logger logger) throws ApplicationException
	{
		List<T> resultList = new ArrayList<T>();
		
		int count = 0;
		
		try
		{
			
			Field[] fields = type.getDeclaredFields();
			
			logger.trace("ResultSetMapper.... Declared Field Count:" + fields.length);
			
			while (resultSet.next())
			{
				count++;
				
				boolean isAddCandidate = false;
				
				T instance = type.newInstance();
				
				for (Field field : fields)
				{
					String columnName = field.getName();
					
					logger.trace("ResultSetMapper.... Selected Column Name:" + columnName + " Field Name:" + field.getName());
					
					if (columnName != null && columnName.length() > 0)
					{
						isAddCandidate = true;
						
						Object value = resultSet.getObject(columnName);
						
						logger.trace("setting " + field.getName() + " to " + value);
						
						// BeanUtils defaults Long, Integer, Double to 0,
						// when the value from database is null.
						if (value != null)
						{
							if (value instanceof Clob)
							{
								BeanUtils.setProperty(instance, field.getName(), ((Clob) value).getSubString(1, (int) ((Clob) value).length()));
								
								// ((Clob) value).free();
							}
							else
							{
								BeanUtils.setProperty(instance, field.getName(), value);
							}
						}
					}
				}
				
				if (isAddCandidate)
				{
					resultList.add(instance);
				}
				
				logger.trace("ResultSet Mapper reporting - Object type " + type.getCanonicalName() + " object count:" + count);
			}
		}
		catch (Throwable t)
		{
			new ApplicationException(t.getMessage(), t);
		}
		
		return resultList;
	}
	
}
