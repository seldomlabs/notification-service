
package com.notification.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

public class ReflectivePropertyMapper
{
	
	private static Map<Class<?>, Set<String>> _consumedClasses = new HashMap<>();
	
	private static Map<String, String> _classColumnNameToPropertyNameMap = new HashMap<>();
	
	public static String getPropertyNameFor(Class<?> clazz, String columnName)
	{
		
		ReflectivePropertyMapper.initClass(clazz);
		String propertyName = _classColumnNameToPropertyNameMap
				.get(ReflectivePropertyMapper.getUniqueString(clazz, columnName));
		return Optional.ofNullable(propertyName).orElse(columnName);
	}
	
	public static Set<String> getAllJoinFields(Class<?> clazz)
	{
		ReflectivePropertyMapper.initClass(clazz);
		return ReflectivePropertyMapper._consumedClasses.get(clazz);
	}
	
	private static void initClass(Class<?> clazz)
	{
		if (!ReflectivePropertyMapper._consumedClasses.containsKey(clazz))
		{
			Set<Field> fields = ReflectivePropertyMapper.getDeclaredFields(clazz, new HashSet<>());
			for (Field field : fields)
			{
				Optional.ofNullable(field.getAnnotation(Column.class))
						.ifPresent(anno -> ReflectivePropertyMapper._classColumnNameToPropertyNameMap
								.put(ReflectivePropertyMapper.getUniqueString(clazz, anno.name()), field.getName()));
				Optional.ofNullable(field.getAnnotation(JoinColumn.class))
						.ifPresent(anno -> ReflectivePropertyMapper._classColumnNameToPropertyNameMap
								.put(ReflectivePropertyMapper.getUniqueString(clazz, anno.name()), field.getName()));				
			}
			_consumedClasses.put(
					clazz,
					fields.stream()
							.filter(f -> Optional.ofNullable(f.getAnnotation(Fetch.class))
									.map(a -> a.value() == FetchMode.JOIN).orElse(false))
							.map(Field::getName).collect(Collectors.toSet()));
		}
	}
	
	private static Set<Field> getDeclaredFields(Class<?> clazz, Set<Field> elements)
	{
		elements.addAll(Arrays.asList(clazz.getDeclaredFields()));
		return clazz.getSuperclass() == null ? elements
				: ReflectivePropertyMapper.getDeclaredFields(clazz.getSuperclass(), elements);
	}
	
	private static String getUniqueString(Class<?> clazz, String columnName)
	{
		return clazz.getName() + ":" + columnName;
	}
}
