
package com.notification.common.db.dao.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import com.notification.common.db.domain.AbstractJpaArchiveEntity;
import com.notification.common.kafka.KafkaEvent;
import com.notification.common.kafka.KafkaPushService;
import com.notification.constants.GlobalConstants;
import com.notification.util.MetricsUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.notification.common.db.dao.CommonDao;
import com.notification.common.db.domain.AbstractJpaEntity;
import com.notification.common.db.domain.AbstractJpaLogEntity;
import com.notification.common.dto.CommonDto;
import com.notification.common.exception.ApplicationException;
import com.notification.common.exception.ExceptionConstants;
import com.notification.util.AssertUtil;
import com.notification.util.ReflectivePropertyMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class implements all the methods defined in CommonDao interface. This
 * implementation relies on the JPA support which Spring framework provides.
 * 
 * @author abhishek
 */

@Repository("commonDao")
public class CommonDaoImpl implements CommonDao
{

	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
	
	private static final DbStat dbStat = new DbStat();
	@Autowired
	private KafkaPushService kafkaPushService;
	
	public enum QueryType
	{
		SELECT, UPDATE
	}
	
	public static class QueryStat
	{
		
		
		private LongAdder count = new LongAdder();
		
		private LongAdder execTime = new LongAdder();
		
		private LongAdder totTime = new LongAdder();
		
		public LongAdder getCount()
		{
			return count;
		}
		
		public long getAverageQueryTimeMillis()
		{
			return Math.round(execTime.sum() / (1_000_000 * count.sum()));
		}
		
		public double getQueriesPerSecond()
		{
			return count.sum() / 3_600.0;
		}
		
		public double getProfilingOverheadPercent()
		{
			long tSum = totTime.sum();
			return (tSum - execTime.sum()) * 100.0 / tSum;
		}
	}
	
	public static class DbStat
	{
		
		
		private Map<String, Map<String, QueryStat>> select = new LinkedHashMap<>();
		
		private Map<String, Map<String, QueryStat>> update = new LinkedHashMap<>();
		
		public Map<String, Map<String, QueryStat>> getSelect()
		{
			return select;
		}
		
		public Map<String, Map<String, QueryStat>> getUpdate()
		{
			return update;
		}
		
		public Map<String, Long> getSelectQPS()
		{
			return select.entrySet().stream().collect(Collectors.toMap(
					e -> e.getKey(),
					e -> Math.round(e.getValue().values().stream().collect(Collectors.summingDouble(d -> d.getQueriesPerSecond())))));
		}
		
		public Map<String, Long> getUpdateQPS()
		{
			return update.entrySet().stream().collect(Collectors.toMap(
					e -> e.getKey(),
					e -> Math.round(e.getValue().values().stream().collect(Collectors.summingDouble(d -> d.getQueriesPerSecond())))));
		}
	}
	
	@FunctionalInterface
	private interface CheckedSupplier<T>
	{
		
		
		public T get() throws ApplicationException;
	}
	
	Logger logger = LogManager.getLogger(CommonDaoImpl.class);
	
	@PersistenceContext
	protected EntityManager em;
	
	int batchSize = 1000;
	
	public EntityManager getEntityManager()
	{
		return this.em;
	}
	
	public <T extends AbstractJpaEntity> void persist(T entity) throws ApplicationException
	{
		recordStats(QueryType.UPDATE, entity.getClass(), () -> {
			AssertUtil.assertBool(entity != null, ExceptionConstants.INVALID_ARGUMENTS, "Entity passed is null", logger);
			
			em.persist(em.contains(entity) ? entity : em.merge(entity));
			
			archive(entity);
			return null;
		});
	}
	
	public <T extends AbstractJpaEntity> T merge(T entity) throws ApplicationException
	{
		return recordStats(QueryType.UPDATE, entity.getClass(), () -> {
			AssertUtil.assertBool(entity != null, ExceptionConstants.INVALID_ARGUMENTS, "Entity passed is null", logger);
		
			// em.flush();
			
			T mergedEntity = em.merge(entity);
			
			archive(mergedEntity);
			
			// em.flush();
			
			return mergedEntity;
		});
	}
	
	public <T extends AbstractJpaEntity> void remove(T entity) throws ApplicationException
	{
		AssertUtil.assertBool(entity != null, ExceptionConstants.INVALID_ARGUMENTS, "Entity passed is null", logger);
		
		em.remove(em.contains(entity) ? entity : em.merge(entity));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AbstractJpaEntity> T findById(Class<? extends AbstractJpaEntity> T, long id) throws ApplicationException
	{
		AssertUtil.assertBool(id > 0, ExceptionConstants.INVALID_ARGUMENTS, "Id passed is invalid for: " + T, logger);
		return recordStats(QueryType.SELECT, T, () -> {
			try
			{
				AbstractJpaEntity entity = em.find(T, id);
				
				return (T) entity;
			}
			catch (NoResultException nre)
			{
				throw new ApplicationException("No entity found for id: " + id + ", class: " + T, nre);
			}
		});
	}
	
	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T) throws ApplicationException
	{
		AssertUtil.assertBool(query != null && T != null, ExceptionConstants.INVALID_ARGUMENTS, "Search Query or Entity Class is null", logger);
		
		return findByQuery(query, T, 0, 0);
	}
	
	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int limit) throws ApplicationException
	{
		AssertUtil
				.assertBool(
						query != null && T != null && limit > 0,
						ExceptionConstants.INVALID_ARGUMENTS,
						"Search Query or Entity Class is null",
						logger);
		
		return findByQuery(query, T, 0, limit);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int offset, int limit) throws ApplicationException
	{
		return recordStats(QueryType.SELECT, T, () -> {
			AssertUtil.assertBool(query != null && T != null, ExceptionConstants.INVALID_ARGUMENTS, "Search Query or Entity Class is null", logger);
			
			Query pQuery = em.createNativeQuery(query, T);
			
			if (offset > 0)
			{
				pQuery.setFirstResult(offset);
			}
			
			if (limit > 0)
			{
				pQuery.setMaxResults(limit);
			}
			
			return pQuery.getResultList();
		});
	}
	
	@SuppressWarnings({ "unchecked" })
	@Override
	public List<Object[]> findByQuery(String query, String identifier) throws ApplicationException
	{
		//getting blocked threads on this
		//https://stackoverflow.com/questions/42240615/detached-entity-on-flush
		em.setFlushMode(FlushModeType.COMMIT);
		return recordStats(QueryType.SELECT, identifier, () -> {
			AssertUtil.assertBool(query != null, ExceptionConstants.INVALID_ARGUMENTS, "Search Query or Entity Class is null", logger);
			Query pQuery = em.createNativeQuery(query);
			List<Object> lo = pQuery.getResultList();
			if (lo.size() > 0 && lo.get(0).getClass().isArray())
			{
				return lo.stream().map(o -> (Object[]) o).collect(Collectors.toList());
			}
			else
			{
				return lo.stream().map(o -> new Object[] { o }).collect(Collectors.toList());
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findByQuery(String query, Map<String, Object> paramMap, int offset, int limit) throws ApplicationException
	{
		return recordStats(QueryType.SELECT, Object.class, () -> {
			AssertUtil.assertBool(query != null, ExceptionConstants.INVALID_ARGUMENTS, "Search Query is null", logger);
			
			Query pQuery = em.createNativeQuery(query);

			fillWhereClause(pQuery, paramMap);
			
			if (offset > 0)
			{
				pQuery.setFirstResult(offset);
			}
			
			if (limit > 0)
			{
				pQuery.setMaxResults(limit);
			}
			
			return pQuery.getResultList();
		});
	}
	
	@Override
	@Transactional
	public void updateByQuery(String query, Map<String, Object> paramMap) throws ApplicationException
	{
		recordStats(QueryType.UPDATE, Object.class, () -> {
			AssertUtil.assertBool(query != null, ExceptionConstants.INVALID_ARGUMENTS, "Search Query is null", logger);
			
			Query pQuery = em.createNativeQuery(query);
			
			fillWhereClause(pQuery, paramMap);
			
			em.joinTransaction();
			
			pQuery.executeUpdate();
			return null;
		});
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, Map<String, Object> paramMap, int offset, int limit)
																																						throws ApplicationException
	{
		return recordStats(QueryType.SELECT, T, () -> {
			AssertUtil.assertBool(query != null && T != null, ExceptionConstants.INVALID_ARGUMENTS, "Search Query or Entity Class is null", logger);
			
			Query pQuery = em.createNativeQuery(query, T);
			
			fillWhereClause(pQuery, paramMap);
			
			if (offset > 0)
			{
				pQuery.setFirstResult(offset);
			}
			
			if (limit > 0)
			{
				pQuery.setMaxResults(limit);
			}
			
			return pQuery.getResultList();
		});
	}
	
	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> T, Map<String, Object> paramMap) throws ApplicationException
	{
		return selectByCriteria(T, paramMap, 0, 0);
	}
	
	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> T, Map<String, Object> paramMap, int limit) throws ApplicationException
	{
		return selectByCriteria(T, paramMap, 0, limit);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> T, Map<String, Object> paramMap, int offset, int limit)
																																	throws ApplicationException
	{
		return recordStats(QueryType.SELECT, T, () -> {
			AssertUtil.assertBool(T != null, ExceptionConstants.INVALID_ARGUMENTS, "Result Generic class passed is null", logger);
			
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(T);
			List<Predicate> predicates = new ArrayList<Predicate>();
			Root<T> root = criteriaQuery.from(T);
			criteriaQuery.select(root);
			for (Entry<String, Object> clause : paramMap.entrySet())
			{
				predicates.add(
						criteriaBuilder.equal(root.get(ReflectivePropertyMapper.getPropertyNameFor(T, clause.getKey())), clause.getValue()));
			}
			criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
			Query pQuery = em.createQuery(criteriaQuery);
			if (offset > 0)
			{
				pQuery.setFirstResult(offset);
			}
			if (limit > 0)
			{
				pQuery.setMaxResults(limit);
			}
			return pQuery.getResultList();
		});
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractJpaEntity> List<T> selectByOrCriteria(
																	Class<T> entityClass,
																	Set<Entry<String, Object>> params,
																	int offset,
																	int limit) throws ApplicationException
	{
		return recordStats(QueryType.SELECT, entityClass, () -> {
			AssertUtil.assertBool(entityClass != null, ExceptionConstants.INVALID_ARGUMENTS, "Result Generic class passed is null", logger);
			
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
			List<Predicate> predicates = new ArrayList<Predicate>();
			Root<T> root = criteriaQuery.from(entityClass);
			criteriaQuery.select(root);
			for (Entry<String, Object> clause : params)
			{
				predicates.add(
						criteriaBuilder
								.equal(root.get(ReflectivePropertyMapper.getPropertyNameFor(entityClass, clause.getKey())), clause.getValue()));
			}
			criteriaQuery.where(criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()])));
			Query pQuery = em.createQuery(criteriaQuery);
			if (offset > 0)
			{
				pQuery.setFirstResult(offset);
			}
			if (limit > 0)
			{
				pQuery.setMaxResults(limit);
			}
			return pQuery.getResultList();
		});
	}
	
	@Override
	public <T extends AbstractJpaEntity> void persist(List<T> entities) throws ApplicationException
	{
		
		for (int i = 0; i < entities.size(); i++)
		{
			T entity = entities.get(i);
			recordStats(QueryType.UPDATE, entity.getClass(), () -> {
				em.persist(entity);
				return null;
			});
			
			if (i % batchSize == 0)
			{
				em.flush();
				em.clear();
			}
		}
		
		em.flush();
		em.clear();
		
	}
	
	@Override
	public <T extends AbstractJpaEntity> List<T> merge(List<T> entities) throws ApplicationException
	{
		List<T> mergeEntities = new ArrayList<T>();
		
		for (int i = 0; i < entities.size(); i++)
		{
			T entity = entities.get(i);
			recordStats(QueryType.UPDATE, entity.getClass(), () -> {
				em.persist(entity);
				return null;
			});
			if (i % batchSize == 0)
			{
				em.flush();
				em.clear();
			}
			
			mergeEntities.add(entity);
		}
		
		em.flush();
		em.clear();
		
		return mergeEntities;
	}
	
	@Override
	public <T extends AbstractJpaEntity> void remove(List<T> entities) throws ApplicationException
	{
		for (int i = 0; i < entities.size(); i++)
		{
			T entity = entities.get(i);
			
			em.remove(em.contains(entity) ? entity : em.merge(entity));
			
			if (i % batchSize == 0)
			{
				em.flush();
				em.clear();
			}
		}
		
		em.flush();
		em.clear();
		
	}
	
	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> T, String criteriaQuery, Map<String, Object> paramMap)
																																	throws ApplicationException
	{
		return selectByCriteria(T, criteriaQuery, paramMap, 0, 0);
	}
	
	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> T, String criteriaQuery, Map<String, Object> paramMap, int limit)
																																				throws ApplicationException
	{
		return selectByCriteria(T, criteriaQuery, paramMap, 0, limit);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(
																	Class<? extends AbstractJpaEntity> T,
																	String criteriaQuery,
																	Map<String, Object> paramMap,
																	int offset,
																	int limit) throws ApplicationException
	{
		return recordStats(QueryType.SELECT, T, () -> {
			AssertUtil.assertBool(!StringUtils.isEmpty(criteriaQuery), ExceptionConstants.INVALID_ARGUMENTS, "Criteria query passed is null", logger);
			
			AssertUtil.assertBool(T != null, ExceptionConstants.INVALID_ARGUMENTS, "Result Generic class passed is null", logger);
			
			Query pQuery = em.createQuery(criteriaQuery);

			fillWhereClause(pQuery, paramMap);
			
			if (offset > 0)
			{
				pQuery.setFirstResult(offset);
			}
			
			if (limit > 0)
			{
				pQuery.setMaxResults(limit);
			}
			
			return pQuery.getResultList();
		});
	}
	
	private void fillWhereClause(Query query, Map<String, Object> paramMap) throws ApplicationException
	{
		if (paramMap != null && !paramMap.isEmpty())
		{
			for (String param : paramMap.keySet())
			{
				query.setParameter(param, paramMap.get(param));
			}
		}
	}
	
	private void archive(AbstractJpaEntity entity)
	{
		try
		{
			AbstractJpaEntity archiveEntity = convertToLog(entity);


			if (archiveEntity != null)
			{
				em.merge(archiveEntity);
			}
			archiveToBigTable(entity);
		}
		catch (ApplicationException e)
		{
			logger.error("Exception while creating log class " + entity.getClass().getName()
					+ "Log");
		}
	}

	private void archiveToBigTable(AbstractJpaEntity entity) {
			try{

				AbstractJpaArchiveEntity archiveEntity= (AbstractJpaArchiveEntity) entity;
				if(archiveEntity.getArchived()) {
					String payload = GlobalConstants.objectMapper.writeValueAsString(entity);
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("payload", payload);
					jsonObject.put("class", entity.getClass().getSimpleName());
					kafkaPushService.sendToKafka(KafkaEvent.bigtable_archive.name(), jsonObject.toString());
				}
			} catch (JsonProcessingException  j){
				logger.error("IOException in pushArchiveToKafka:");
				MetricsUtil.publishCountMetricWithPrometheus("Archive.Aryan.Failure.IO");
			}catch (ClassCastException e){
				logger.debug("pass silently");
			}
	}

	private AbstractJpaEntity convertToLog(AbstractJpaEntity entity) throws ApplicationException
	{
		AbstractJpaLogEntity logEntity;
		try
		{
			logEntity = (AbstractJpaLogEntity) Class.forName(entity.getClass().getName() + "Log").newInstance();
			String[] ignoreProperties = {"id", "updateDate", "createDate"};
			BeanUtils.copyProperties(entity, logEntity, ignoreProperties);
			logEntity.setArchivedId(entity.getId());
			return (AbstractJpaEntity) logEntity;
		}
		catch (InstantiationException e)
		{
			throw new ApplicationException("Exception while creating log entity", e);
		}
		catch (IllegalAccessException e)
		{
			throw new ApplicationException("Exception while creating log entity", e);
		}
		catch (ClassNotFoundException e)
		{
			// throw new ApplicationException("Exception while creating log
			// entity", e);
			return null;
		}

	}
	
	public void flushAndClear()
	{
		em.flush();
		em.clear();
	}
	
	public void flush()
	{
		em.flush();
	}
	
	@Override
	public DbStat getStats()
	{
		return dbStat;
	}
	
	private static String getCurrentDateAndHour()
	{
		return df.format(new Date());
	}
	
	private <T> T recordStats(QueryType qt, Class<?> clazz, CheckedSupplier<T> supplier) throws ApplicationException
	{
		return recordStats(qt, clazz.toString(), supplier);
	}
	
	private <T> T recordStats(QueryType qt, String clazz, CheckedSupplier<T> supplier) throws ApplicationException
	{
		long profStartTime = System.nanoTime();
		long startTime = System.nanoTime();
		T t = supplier.get();
		long duration = System.nanoTime() - startTime;
		//statsd.recordExecutionTime(qt.toString()+ "." + clazz , duration);
		
		try
		{
			Map<String, Map<String, QueryStat>> storage = qt == QueryType.SELECT ? dbStat.select : dbStat.update;
			String timeString = getCurrentDateAndHour();
			Map<String, QueryStat> hourlyStorage = storage.get(timeString);
			if (hourlyStorage == null)
			{
				hourlyStorage = new HashMap<>();
				storage.put(timeString, hourlyStorage);
			}
			QueryStat stat = hourlyStorage.get(clazz);
			if (stat == null)
			{
				stat = new QueryStat();
				hourlyStorage.put(clazz, stat);
			}
			stat.count.increment();
			stat.execTime.add(duration);
			stat.totTime.add(System.nanoTime() - profStartTime);
		}
		catch (Exception e)
		{
			logger.error("Sorry :'( - Your Buddy, CommonDao", e);
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends AbstractJpaEntity> T selectEntityForUpdateByCriteria(Class<? extends AbstractJpaEntity> T, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException
	{
		AssertUtil.assertBool(!StringUtils.isEmpty(criteriaQuery), ExceptionConstants.INVALID_ARGUMENTS, "Criteria query passed is null", logger);
		
		AssertUtil.assertBool(T != null, ExceptionConstants.INVALID_ARGUMENTS, "Result Generic class passed is null", logger);
		
		Query pQuery = em.createQuery(criteriaQuery);
		
		fillWhereClause(pQuery, paramMap);
		
		pQuery.setMaxResults(1);
		
		pQuery.setLockMode(LockModeType.PESSIMISTIC_WRITE);
		
		List<T> resultList = pQuery.getResultList();
		
		if (resultList != null && resultList.size() > 0)
		{
			return resultList.get(0);
		}
		else
		{
			return null;
		}
	}
}
