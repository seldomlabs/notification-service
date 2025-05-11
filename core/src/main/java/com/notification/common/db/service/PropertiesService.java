
package com.notification.common.db.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.notification.common.exception.ExceptionConstants;
import com.notification.util.AssertUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.notification.common.db.dao.CommonDao;
import com.notification.common.exception.ApplicationException;

//@Service
public class PropertiesService {

	public class PropertyDto {

		private String value;

		private long updateTime;

		public PropertyDto(String value) {
			this.value = value;
			this.updateTime = System.currentTimeMillis();
		}

		public String getValue() {
			return value;
		}

		public long getUpdateTime() {
			return updateTime;
		}

		@Override
		public int hashCode() {
			return this.value.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof PropertyDto && ((PropertyDto) o).getValue().equals(value);
		}

		@Override
		public String toString() {
			return getValue();
		}
	}

	private @Autowired CommonDbService commonDbService;

	private @Autowired CommonDao commonDao;

	private static final String DEFAULT_PROPERT_VALUE = StringUtils.EMPTY;

	private static final String TABLE_NAME = "properties.property";

	private static final String SelectProperty = "select value from " + TABLE_NAME
			+ " where bucket = '%s' and name = '%s'";

	private static Map<String, Map<String, PropertyDto>> properties = new ConcurrentHashMap<>();

	private static final Logger logger = LogManager.getLogger(PropertiesService.class);

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private static final int REFRESH_INTERVAL_SECONDS = 5 * 60;

	protected static final String PropertyBucket = "mail_templates";

	public PropertiesService() {
		scheduler.scheduleWithFixedDelay(() -> {
			try {
				logger.debug("Reloading properties");
				for (Entry<String, Map<String, PropertyDto>> property : properties.entrySet()) {
					for (Entry<String, PropertyDto> entry : property.getValue().entrySet()) {
						this.load(property.getKey(), entry.getKey());
					}
				}
				logger.debug("Current properties : " + properties);
			} catch (Exception e) {
				logger.error(ExceptionUtils.getRootCauseMessage(e));
			}
		}, REFRESH_INTERVAL_SECONDS, REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}

	public PropertyDto getProperty(String bucket, String name) throws ApplicationException {
		Map<String, PropertyDto> bucketProperties = properties.get(bucket);
		if (bucketProperties == null || bucketProperties.get(name) == null){
			load(bucket, name);
			bucketProperties = properties.get(bucket);
		}
		return bucketProperties.get(name);
	}

	public PropertyDto getPropertyByENV(String bucket, String name) throws ApplicationException {
		String env = System.getProperty("ENV");
		String envBucket = bucket;
		if (!Objects.equals(env, "")) {
			envBucket = String.format("%s.%s", bucket, env);
		}
		PropertyDto value = getProperty(envBucket, name);
		if (value == null || value.getValue() == null || Objects.equals(value.getValue(), "")) {
			value = getProperty(bucket, name);
		}
		return value;
	}

	public void updateProperty(String bucket, String name, String value) throws ApplicationException {
		String insertIntoPropertyQuery = "UPDATE properties.property SET value = :value WHERE  property.bucket =  "
				+ ":bucket AND property.name =  :name";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("value", value);
		paramMap.put("bucket", bucket);
		paramMap.put("name", name);
		commonDao.updateByQuery(insertIntoPropertyQuery, paramMap);
	}

	public void mergeProperty(String bucket, String name, String value) throws ApplicationException {
		String insertUpdateQuery = "INSERT into  properties.property (bucket,name,value) values (:bucket,:name,:value)  ON DUPLICATE KEY UPDATE value = :value";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("value", value);
		paramMap.put("bucket", bucket);
		paramMap.put("name", name);
		commonDao.updateByQuery(insertUpdateQuery, paramMap);
	}

	public void insertProperty(String bucket, String name, String value) throws ApplicationException {
		String insertIntoPropertyQuery = "INSERT into  properties.property (bucket,name,value) values (:bucket,:name,:value)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("value", value);
		paramMap.put("bucket", bucket);
		paramMap.put("name", name);
		commonDao.updateByQuery(insertIntoPropertyQuery, paramMap);
	}

	public List<Object[]> selectProperty(String bucket, String name) throws ApplicationException {
		List<Object[]> result = commonDbService.findByQuery(String.format(SelectProperty, bucket, name), TABLE_NAME);
		return result;
	}

	public String getTemplate(String templateName) throws ApplicationException {
		return getProperty(PropertyBucket, templateName).getValue();
	}

	private void load(String bucket, String name) throws ApplicationException {
		logger.debug(String.format("Loading value for [%s, %s]", bucket, name));
		Map<String, PropertyDto> bucketProperties = properties.get(bucket);
		if (bucketProperties == null) {
			bucketProperties = new ConcurrentHashMap<>();
			properties.put(bucket, bucketProperties);
		}
		List<Object[]> result = commonDbService.findByQuery(String.format(SelectProperty, bucket, name), TABLE_NAME);
		String newProperty = DEFAULT_PROPERT_VALUE;
		if (!result.isEmpty()) {
			newProperty = (String) result.get(0)[0];
		}
		PropertyDto propertyRead = new PropertyDto(newProperty);
		PropertyDto existingProperty = bucketProperties.get(name);
		if (!propertyRead.equals(existingProperty)) {
			bucketProperties.put(name, propertyRead);
		}
		logger.debug(String.format("Value after load for [%s, %s] => [%s]", bucket, name, getProperty(bucket, name)));
	}

	public String getProperty(String bucket, String name, String defaultValue) throws ApplicationException{
		PropertyDto propertyDto = getProperty(bucket, name);
		return StringUtils.isNotEmpty(propertyDto.getValue()) ? propertyDto.getValue() : defaultValue;

	}

	public String getPropertyByENV(String bucket, String name, String defaultValue) throws ApplicationException{
		PropertyDto propertyDto = getPropertyByENV(bucket, name);
		return StringUtils.isNotEmpty(propertyDto.getValue()) ? propertyDto.getValue() : defaultValue;

	}

	public Integer getIntegerProperty(String bucket, String name, String defaultValue) throws ApplicationException{
		return getNumericProperty(bucket,name,defaultValue,Integer.class);
	}

	public Long getLongProperty(String bucket,String name,String defaultValue) throws ApplicationException{
		return getNumericProperty(bucket,name,defaultValue,Long.class);
	}

	public Float getFloatProperty(String bucket, String name, String defaultValue) throws ApplicationException{
		return getNumericProperty(bucket,name,defaultValue,Float.class);
	}

	public Double getDoubleProperty(String bucket, String name, String defaultValue) throws ApplicationException{
		return getNumericProperty(bucket,name,defaultValue,Double.class);
	}

	public BigDecimal getBigDecimalProperty(String bucket, String name, String defaultValue) throws ApplicationException{
		return getNumericProperty(bucket,name,defaultValue,BigDecimal.class);
	}


	private <T extends Number> T getNumericProperty(String bucket, String name, String defaultValue, Class<T> tClass) throws ApplicationException{
		AssertUtil.assertBool(Number.class.isAssignableFrom(tClass), ExceptionConstants.INVALID_ARGUMENTS,"Cannot use" +
				" for Non Numeric Properties",logger);
		PropertyDto propertyDto = getProperty(bucket, name);
		String propertyValue = StringUtils.isNotEmpty(propertyDto.getValue()) ? propertyDto.getValue() : defaultValue;
		AssertUtil.assertBool(NumberUtils.isNumber(propertyValue), ExceptionConstants.INVALID_ARGUMENTS,"Cannot parse" +
				" to Number", logger);
		Number numericProperty = null;
		try{
			switch (tClass.getSimpleName()){
				case "Short":
					numericProperty = Short.valueOf(propertyValue);
					break;
				case "Integer":
					numericProperty = Integer.valueOf(propertyValue);
					break;
				case "Long":
					numericProperty = Long.valueOf(propertyValue);
					break;
				case "Float":
					numericProperty = Float.valueOf(propertyValue);
					break;
				case "BigInteger":
					numericProperty = new BigInteger(propertyValue);
					break;
				case "Double":
					numericProperty = Double.valueOf(propertyValue);
					break;
				case "BigDecimal":
					numericProperty = new BigDecimal(propertyValue);
					break;
			}
			return tClass.cast(numericProperty);
		} catch (NumberFormatException ne){
			logger.error("Unable to parse property "+ Arrays.asList(bucket,name,defaultValue)
					+"to supplied class "+tClass.getSimpleName());
			throw new ApplicationException("Cannot Parse String to Number");
		}
		catch (ClassCastException ce){
			logger.error("Unable to cast the property "+ Arrays.asList(bucket,name,defaultValue)
					+"to supplied class "+tClass.getSimpleName());
			throw new ApplicationException("Cannot cast property to "+tClass.getSimpleName());
		}
	}

	public List<String> getValuesFromProperty(String value) {
		return (value == null || value.isEmpty()) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value.split(",")));
	}


	public String getRealTimeProperty(String bucket, String name) throws ApplicationException {
		String propertyQuery = "select property.name,property.value from properties.property WHERE  property.bucket =  "
				+ ":bucket AND property.name =  :name";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("bucket", bucket);
		paramMap.put("name", name);

		List<Object[]> result = commonDao.findByQuery(propertyQuery, paramMap, 0, 1);
		if (result == null || result.size() == 0) {
			return null;
		}
		 return (String) result.get(0)[1];
	}
}
