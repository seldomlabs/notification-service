	

package com.notification.common.db.service.impl;

import java.lang.reflect.Field;	
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;

import jakarta.persistence.Query;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import com.notification.common.db.dao.CommonDao;
import com.notification.common.db.domain.AbstractJpaEntity;
import com.notification.common.db.service.CommonDbService;
import com.notification.common.dto.CommonDto;
import com.notification.common.exception.ApplicationException;
import com.notification.util.ApplicationUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Spring framework is being used for managing all the database transactions.
 * Database transactions are being managed in service classes. All the methods
 * ending with "InOwnTransaction" will be executed in a new transaction, all
 * others will be executed as part of the ongoing transaction. All the
 * configurations are governed via hibernateJpaConfig.xml configured in the
 * applicationContext.xml.
 * 
 * This class implements different methods mentioned in the CommonDbService
 * interface.
 * 
 * @author abhishek
 * 
 */
@Service("commonDbService")
public class CommonDbServiceImpl implements CommonDbService {

	@Autowired
    CommonDao commonDao;

	Logger logger = LogManager.getLogger(CommonDbServiceImpl.class);

	@Override
	public <T extends AbstractJpaEntity> void createEntity(T entity) throws ApplicationException {
		commonDao.persist(entity);
	}
	
	@Override
	public <T extends AbstractJpaEntity> void createEntity(List<T> entities) throws ApplicationException {
		commonDao.persist(entities);
	}

	@Override
	public <T extends AbstractJpaEntity> T getById(Class<T> entityClass, long id) throws ApplicationException {
		return commonDao.findById(entityClass, id);
	}

	@Override
	@Transactional(readOnly=false)
	public <T extends AbstractJpaEntity> T updateEntity(T entity) throws ApplicationException {
		return commonDao.merge(entity);
	}

	@Override
	public <T extends AbstractJpaEntity> void deleteEntity(T entity) throws ApplicationException {
		commonDao.remove(entity);
	}

	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T)
			throws ApplicationException {
		return commonDao.findByQuery(query, T);
	}

	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int limit)
			throws ApplicationException {
		return commonDao.findByQuery(query, T, limit);
	}

	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int offset, int limit)
			throws ApplicationException {
		return commonDao.findByQuery(query, T, offset, limit);
	}

	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T,
			Map<String, Object> paramMap, int offset, int limit) throws ApplicationException {
		return commonDao.findByQuery(query, T, paramMap, offset, limit);
	}

	@Override
	public List<Object[]> findByQuery(String query, String identifier) throws ApplicationException {
		return commonDao.findByQuery(query, identifier);
	}

	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteriaWithLimit(Class<T> entityClass,
			Map<String, Object> paramMap, int limit) throws ApplicationException {
		return commonDao.selectByCriteria(entityClass, paramMap, limit);
	}

	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteriaWithLimit(Class<T> entityClass,
			Map<String, Object> paramMap, int offset, int limit) throws ApplicationException {
		return commonDao.selectByCriteria(entityClass, paramMap, offset, limit);
	}

	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteriaWithLimit(Class<T> entityClass, String criteriaQuery,
			Map<String, Object> paramMap, int offset, int limit) throws ApplicationException {
		return commonDao.selectByCriteria(entityClass, criteriaQuery, paramMap, offset, limit);
	}

	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, Map<String, Object> paramMap)
			throws ApplicationException {
		return commonDao.selectByCriteria(entityClass, paramMap);
	}

	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, String criteriaQuery,
			Map<String, Object> paramMap) throws ApplicationException {
		return commonDao.selectByCriteria(entityClass, criteriaQuery, paramMap);
	}

	@Override
	public <T extends AbstractJpaEntity> List<T> selectByOrCriteria(Class<T> T, Set<Entry<String, Object>> params)
			throws ApplicationException {
		return commonDao.selectByOrCriteria(T, params, 0, 0);
	}

	@Override
	public <T extends AbstractJpaEntity> T selectEntityByCriteria(Class<T> entityClass, Map<String, Object> paramMap)
			throws ApplicationException {
		List<T> entities = selectByCriteria(entityClass, paramMap);

		if (entities != null && entities.size() > 0) {
			return entities.get(0);
		}

		return null;
	}

	@Override
	public <T extends AbstractJpaEntity> T selectEntityByCriteria(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException
	{
		List<T> entities = selectByCriteriaWithLimit(entityClass, criteriaQuery, paramMap, 0, 1);
		
		if (entities != null && entities.size() > 0)
		{
			return entities.get(0);
		}
		
		return null;
	}

	@Override
	public <T extends AbstractJpaEntity> void persistInOwnTransaction(T entity) throws ApplicationException {
		commonDao.persist(entity);
	}

	@Override
	public <T extends AbstractJpaEntity> T updateInOwnTransaction(T entity) throws ApplicationException {
		return commonDao.merge(entity);
	}

	public <T extends AbstractJpaEntity> T createEntity(Class<T> entityClass, Map<String, String> fields)
			throws ApplicationException {
		try {
			T entity = (T) entityClass.newInstance();
			setFields(entity, fields);
			commonDao.persist(entity);
			return entity;
		} catch (InstantiationException e) {
			throw new ApplicationException("Instantiation Exception", e);
		} catch (IllegalAccessException e) {
			throw new ApplicationException("Illegal Access Exception", e);
		}

	}

	public <T extends AbstractJpaEntity> List<T> updateEntities(Class<T> entityClass, Map<String, String> fields, Map<String, Object> paramMap) throws ApplicationException
	{
		List<T> entities = selectByCriteria(entityClass, paramMap);
		
		List<T> updatedEntities = new ArrayList<T>();
		
		for (T entity : entities)
		{
			try
			{
				setFields(entity, fields);
				updateEntity(entity);
				updatedEntities.add(entity);
			}
			catch (ApplicationException e)
			{
				logger.error("Could not update entity " + e.getMessage());
			}
		}
		return updatedEntities;
	}

	@Override
	public <T extends AbstractJpaEntity> void deleteEntities(Class<T> entityClass, Map<String, Object> paramMap) throws ApplicationException
	{
		List<T> entities = selectByCriteria(entityClass, paramMap);
		
		for (T entity : entities)
		{
			try
			{
				deleteEntity(entity);
			}
			catch (ApplicationException e)
			{
				logger.error("Could not delete entity " + e.getMessage());
			}
		}
		
	}
	
	@Override
	public <T extends AbstractJpaEntity> int deleteEntities(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException
	{
		int count = 0;
		List<T> entities = selectByCriteria(entityClass, criteriaQuery, paramMap);
		for (T entity : entities)
		{
			try
			{
				deleteEntity(entity);
				count++;
			}
			catch (ApplicationException e)
			{
				logger.error("Could not delete entity " + e.getMessage());
			}
		}
		return count;
	}
	
	@Override
	public void flushAndClear() {
		commonDao.flushAndClear();
	}

	@Override
	public void flush() {
		commonDao.flush();
	}

	@Override
	public List<Long> executeNativeUpdates(String rawQuery) throws ApplicationException {
		try {
			Session session = commonDao.getEntityManager().unwrap(Session.class);
			UpdateWork work = new UpdateWork(rawQuery);
			session.doWork(work);
			return work.getGeneratedIds();
		} catch (Exception e) {
			throw new ApplicationException("exception while executing native updates", e);
		}
	}

	@Override
    public int executeUpdates(String hqlQuery) throws ApplicationException {
        try {
            Query query = commonDao.getEntityManager().createQuery(hqlQuery);
            return query.executeUpdate();
        } catch (Exception e) {
            throw new ApplicationException("exception while executing updates", e);
        }
    }

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		return this.commonDao.getEntityManager().getCriteriaBuilder();
	}

	@Override
	public <A> List<A> executeQuery(CriteriaQuery<A> criteriaQuery) {
		return this.commonDao.getEntityManager().createQuery(criteriaQuery).getResultList();
	}

	@Override
	public <T extends AbstractJpaEntity> int updateEntityFields(Class<T> entityClass, long id, Map<String, String> paramMap) throws ApplicationException
	{
		String tableName = entityClass.getSimpleName();
		if (entityClass.isAnnotationPresent(Table.class))
		{
			Table table = entityClass.getAnnotation(Table.class);
			tableName = table.name();
		}
		
		StringBuffer updateQuery = new StringBuffer("update ").append(tableName).append(" set ");
		int i = 0;
		for (String key : paramMap.keySet())
		{
			updateQuery.append(key + " = '"+ paramMap.get(key)+"'");
			if (++i < paramMap.size())
				updateQuery.append(" ,");
			
		}
		updateQuery.append(" where id = " + id);
		logger.debug("update query *** " + updateQuery.toString());
		return this.executeNativeUpdates(updateQuery.toString()).size();
	}
	
	@Override
	public <T extends AbstractJpaEntity> int updateEntitesField(Class<T> entityClass, String idName, String columnToBeUpdated, Map<String, String> paramMap) throws ApplicationException
	{
		if(paramMap.isEmpty()) {
			return -1;
		}
		String tableName = entityClass.getSimpleName();
		if (entityClass.isAnnotationPresent(Table.class))
		{
			Table table = entityClass.getAnnotation(Table.class);
			tableName = table.name();
		}
		
		StringBuffer updateQuery = new StringBuffer("update ").append(tableName).append(" set " + columnToBeUpdated + " = (case "); 
		int i = 0;
		for (String key : paramMap.keySet())
		{
			updateQuery.append("when " + idName + " = " + "'" + key+"'" + " then " + "'" +paramMap.get(key) + "' ");
			if (++i < paramMap.size())
				updateQuery.append("\n");
			
		}
		updateQuery.append("\nend)");
		String ids = StringUtils.join(paramMap.keySet(), ",");
		updateQuery.append(" where " + idName + " in (" + ids + ");");
		logger.info("updateEntitesField query *** " + updateQuery.toString());
		return this.executeNativeUpdates(updateQuery.toString()).size();
	}
	
	private class UpdateWork implements Work {

		private String updateQuery;

		private List<Long> generatedIds = new ArrayList<>();

		public UpdateWork(String updateQuery) {
			this.updateQuery = updateQuery;
		}

		@Override
		public void execute(Connection connection) throws SQLException {
			try (Statement stmt = connection.createStatement()) {
				stmt.executeUpdate(updateQuery, Statement.RETURN_GENERATED_KEYS);
				try (ResultSet rs = stmt.getGeneratedKeys()) {
					while (rs.next()) {
						generatedIds.add(rs.getLong(1));
					}
				}
			}
		}

		public List<Long> getGeneratedIds() {
			return generatedIds;
		}
	}

	private <T> void setFields(T entity, Map<String, String> fields) throws ApplicationException {
		try {
			Field[] classFields = getAllClassFields(entity.getClass());

			for (Field field : classFields) {
				if (fields.containsKey(field.getName())) {
					String fieldValue = fields.get(field.getName());
					Method method = entity.getClass()
							.getMethod("set" + ApplicationUtil.capitalizeFirst(field.getName()), field.getType());
					method.invoke(entity, convertToObject(field, fieldValue));
				}
			}
		} catch (IllegalAccessException e) {
			throw new ApplicationException("IllegalAccess Exception", e);
		} catch (IllegalArgumentException e) {
			throw new ApplicationException("Illegal Argument Exception", e);
		} catch (SecurityException e) {
			throw new ApplicationException("Security Exception", e);
		} catch (InvocationTargetException e) {
			throw new ApplicationException("Invocation Exception", e);
		} catch (NoSuchMethodException e) {
			throw new ApplicationException("NoSuch Method Exception", e);
		}
	}

	private Object convertToObject(Field field, String value) throws ApplicationException {
		try {
			if (field.getType().isPrimitive()) {
				if (field.getType().getName().equals(int.class.getName())) {
					return ApplicationUtil.isStringEmpty(value) ? 0 : Integer.valueOf(value);
				}
				if (field.getType().getName().equals(double.class.getName())) {
					return ApplicationUtil.isStringEmpty(value) ? 0.0 : Double.valueOf(value);
				}
				if (field.getType().getName().equals(long.class.getName())) {
					return ApplicationUtil.isStringEmpty(value) ? 0L : Long.valueOf(value);
				}
				if (field.getType().getName().equals(boolean.class.getName())) {
					return Boolean.valueOf(value);
				}
			}

			return Class.forName(field.getType().getName()).getConstructor(String.class).newInstance(value);
		} catch (Exception ex) {
			logger.error("Exception while converting field to object - className: " + field.getType().getName()
					+ ", value: " + value);
			throw new ApplicationException("Exception while converting field to object ", ex);
		}
	}

	private Field[] getAllClassFields(Class<?> entityClass) {
		Field[] fields = entityClass.getDeclaredFields();

		entityClass = entityClass.getSuperclass();

		while (entityClass != null && entityClass != Object.class) {
			Field[] superFields = entityClass.getDeclaredFields();

			fields = (Field[]) ArrayUtils.addAll(fields, superFields);

			entityClass = entityClass.getSuperclass();
		}

		return fields;

	}
	
	public static void main (String[] args) throws ApplicationException
	{
		Map<String, String> map = new HashMap<>();
		map.put("deal_transaction_id", "1222");
		map.put("d_id", "1222");
		new CommonDbServiceImpl().updateEntityFields(AbstractJpaEntity.class, 11L, map);
	}
}
