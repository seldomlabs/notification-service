package com.notification.common.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.notification.common.exception.ApplicationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public abstract class GenericRedisListCacheService<T> extends RedisListCacheService {

	ObjectMapper mapper = new ObjectMapper();
	
	abstract protected String getPrefix();
	
	abstract protected TypeReference<T> getType();
	
	protected List<T> getCacheValue(String key) throws ApplicationException {

		List<T> output = new ArrayList<T>();
		List<String> valueString = super.getCache(key);
		
		if (valueString == null)
			return null;
		
		for (String value: valueString) {
			try {
				output.add(mapper.readValue(value, getType()));
			} catch (IOException e) {
				throw new ApplicationException("Exception while parsing Redis value", e);
			}
		}
		
		return output;
	}
	
	protected void putCacheValue(String key, List<T> value) throws ApplicationException {
		
		List<String> valueString = new ArrayList<String>();
		for (T val: value) {
			try {
				valueString.add(mapper.writeValueAsString(val));
			} catch (JsonProcessingException e) {
				throw new ApplicationException("Exception while parsing Redis value", e);
			}
		}
		
		super.putCache(key, valueString);
	}
	
}
