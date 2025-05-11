package com.notification.common.db.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.persistence.EntityManager;

import com.notification.common.db.domain.AbstractJpaEntity;
import com.notification.common.dto.CommonDto;
import com.notification.common.exception.ApplicationException;

/**
 * This is the generic interface which ensures basic CRUD operations on any java class which represents a database entity
 * 
 * @author abhishek
 *		
 */
public interface CommonDao
{
	
	public EntityManager getEntityManager();
	/**
	 * This method takes a JPA entity and persists it. Upon successful persistence "id" will be filled in.
	 * 
	 * @param entity
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void persist(T entity) throws ApplicationException;
	
	/**
	 * This method takes list of JPA entities and persist them. Upon successful persistence "id" field will be filled in for all the saved entities
	 * 
	 * @param entities
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void persist(List<T> entities) throws ApplicationException;
	
	/**
	 * This method takes a JPA entity and merges (updates) it.
	 * 
	 * @param entity
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> T merge(T entity) throws ApplicationException;
	
	/**
	 * This method takes a list of JPA entities and merges (updates) them.
	 * 
	 * @param entities
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> List<T> merge(List<T> entities) throws ApplicationException;
	
	/**
	 * This method deletes the supplied entity from the database table.
	 * 
	 * @param entity
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void remove(T entity) throws ApplicationException;
	
	/**
	 * This method deletes all the supplied entities from the database table.
	 * 
	 * @param entities
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void remove(List<T> entities) throws ApplicationException;
	
	/**
	 * This method finds the JPA entity based on the supplied ID.
	 * 
	 * @param T
	 * @param id
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> T findById(Class<? extends AbstractJpaEntity> T, long id) throws ApplicationException;
	
	/**
	 * 
	 * This method runs the supplied native SQL query against the database and maps the result set to the supplied class.
	 * 
	 * @param query
	 * @param T
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T) throws ApplicationException;
	
	/**
	 * This method runs the supplied native SQL query against the database and maps the result set to the supplied class.
	 * 
	 * @param query
	 * @param T
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int limit) throws ApplicationException;
	
	/**
	 * This method runs the supplied native SQL query against the database and maps the result set to the supplied class.
	 * 
	 * @param query
	 * @param T
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method runs the named native sql query and maps the result set back to the supplied class.
	 * 
	 * @param query
	 * @param T
	 * @param paramMap
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, Map<String, Object> paramMap, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method runs the named native sql query and maps the result set back to the supplied class.
	 * 
	 * @param T
	 * @param query
	 * @param paramMap
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public List<Object[]> findByQuery(String query, Map<String, Object> paramMap, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method constructs and runs the criteria query against the database with help of the parameter map. It selects all the columns.
	 * 
	 * @param T
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, Map<String, Object> paramMap) throws ApplicationException;
	
	/**
	 * This method constructs and runs the criteria query against the database with help of the parameter map. It selects all the columns.
	 * 
	 * @param T
	 * @param paramMap
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, Map<String, Object> paramMap, int limit) throws ApplicationException;
	
	/**
	 * This method constructs and runs the criteria query against the database with help of the parameter map. It selects all the columns.
	 * 
	 * @param T
	 * @param paramMap
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, Map<String, Object> paramMap, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method runs the supplied criteria query against the database with help of the parameter map.
	 * 
	 * @param T
	 * @param criteriaQuery
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException;
	
	/**
	 * This method runs the supplied criteria query against the database with help of the parameter map.
	 * 
	 * @param T
	 * @param criteriaQuery
	 * @param paramMap
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap, int limit) throws ApplicationException;
	
	/**
	 * This method runs the supplied criteria query against the database with help of the parameter map.
	 * 
	 * @param T
	 * @param criteriaQuery
	 * @param paramMap
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> List<T> selectByCriteria(Class<? extends AbstractJpaEntity> T, String criteriaQuery, Map<String, Object> paramMap, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method constructs and runs the criteria query against the database
	 * with help of the parameter map. It selects all the columns from rows that
	 * match at least one of the parameters.
	 * 
	 * @param T
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> List<T> selectByOrCriteria(Class<T> T, Set<Entry<String, Object>> params, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method constructs and runs the criteria query against the database
	 * with help of the parameter map. It selects all the columns from rows that
	 * match at least one of the parameters.
	 * 
	 * @param T
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> T selectEntityForUpdateByCriteria(Class<? extends AbstractJpaEntity> T, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException;
	
	public void flushAndClear();
	
	public void flush();

	public List<Object[]> findByQuery(String query, String identifier) throws ApplicationException;

	public void updateByQuery(String query, Map<String, Object> paramMap) throws ApplicationException;
	
	public Object getStats();
}
