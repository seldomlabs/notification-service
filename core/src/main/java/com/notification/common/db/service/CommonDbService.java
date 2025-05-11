package com.notification.common.db.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import com.notification.common.db.domain.AbstractJpaEntity;
import com.notification.common.dto.CommonDto;
import com.notification.common.exception.ApplicationException;
import com.fasterxml.jackson.core.JsonProcessingException;


/**
 * DO NOT FORMAT THIS USING ECLIPSE FORMATTER. MAINTAIN THE EXISTING FORMAT. 
 * This interface defines the root level methods to perform CRUD operations on DB tables.
 * 
 * @author abhishek
 *		
 */
public interface CommonDbService
{
	
	/**
	 * This method takes the entity object and persists it in the DB.
	 * 
	 * @param entity
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void createEntity(T entity) throws ApplicationException;
	
	
	<T extends AbstractJpaEntity> void createEntity(List<T> entities) throws ApplicationException;
	
	/**
	 * This method takes the entity object and persists it in the DB using a new transaction.
	 * 
	 * @param entity
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void persistInOwnTransaction(T entity) throws ApplicationException;
	
	
	/**
	 * This method takes the entity class and a map of field/values.It instantiates an entity object and sets the field values using reflection APIs and then persists the object in DB.
	 * 
	 * @param entity
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> T createEntity(Class<T> entityClass, Map<String, String> fields) throws ApplicationException;
	
	/**
	 * This method takes primary key and entity class. It returns the entity object representing the table row corresponding to the supplied primary key.
	 * 
	 * @param T
	 * @param id
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> T getById(Class<T> entityClass, long id) throws ApplicationException;
	
	/**
	 * This method updates the supplied entity in DB.
	 * @param entity
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> T updateEntity(T entity) throws ApplicationException;
	
	/**
	 * This method updates the supplied entity in DB in a new transaction.
	 * 
	 * @param entity
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> T updateInOwnTransaction(T entity) throws ApplicationException;
	
	/**
	 * This method updates entities based on the supplied paramMap and fields. It selects the entities based on the paramMap and changes the field of entities as per the "fields".
	 * 
	 * @param entity
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> List<T> updateEntities(Class<T> entityClass, Map<String, String> fields, Map<String, Object> paramMap) throws ApplicationException;
	
	/**
	 * This method deletes the supplied entity from entity manager as well as from DB. 
	 * @param entity
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void deleteEntity(T entity) throws ApplicationException;
	
	/**
	 * This method deletes entities matching supplied paramMap from entity manager as well as from DB. 
	 * @param T
	 * @param paramMap
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> void deleteEntities(Class<T> entityClass, Map<String, Object> paramMap) throws ApplicationException;
	
	/**
	 * This method deletes entities matching supplied paramMap from entity manager as well as from DB. 
	 * @param T
	 * @param criteriaQuery
	 * @param paramMap
	 * @throws ApplicationException
	 * @throws JsonProcessingException 
	 */
	<T extends AbstractJpaEntity> int deleteEntities(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException;
		
	/**
	 * This method takes the entity class and paramMap. It returns list of entity class instances matching the supplied parameter map.
	 * This method uses HQL internally to build the select criteria.
	 * @param T
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, Map<String, Object> paramMap) throws ApplicationException;
	
	<T extends AbstractJpaEntity> List<T> selectByOrCriteria(Class<T> T, Set<Entry<String, Object>> paramMap) throws ApplicationException;
	
	/**
	 * This method takes the entity class and paramMap. It returns list of entity class instances matching the supplied parameter map capped by the limit.
	 * This method uses HQL internally to build the select criteria.
	 * @param T
	 * @param paramMap
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> List<T> selectByCriteriaWithLimit(Class<T> entityClass, Map<String, Object> paramMap, int limit) throws ApplicationException;
	
	/**
	 * This method takes the entity class and paramMap. It returns list of entity class instances matching the supplied parameter map. List boundary is dictated by offset and limit. 
	 * This method uses HQL internally to build the select criteria.
	 * @param T
	 * @param paramMap
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> List<T> selectByCriteriaWithLimit(Class<T> entityClass, Map<String, Object> paramMap, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method takes the entity class and paramMap. It returns list of entity class instances matching the supplied parameter map. List boundary is dictated by offset and limit. 
	 * This method uses criteria query to build the select criteria.
	 * @param T
	 * @param criteriaQuery
	 * @param paramMap
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> List<T> selectByCriteriaWithLimit(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method takes the entity class and paramMap. It returns the first entity class instance matching the supplied parameter.
	 * This method uses HQL internally to build the select criteria.
	 * @param T
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> T selectEntityByCriteria(Class<T> entityClass, Map<String, Object> paramMap) throws ApplicationException;
	
	/**
	 * This method takes the entity class and paramMap. It returns the first entity class instance matching the supplied parameter.
	 * This method uses supplied criteria query to build the select criteria. 
	 * @param type
	 * @param criteriaQuery
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> T selectEntityByCriteria(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException;
	

	/**
	 * This method takes the entity class and paramMap. It returns the list of entity class instances matching the supplied parameter.
	 * This method uses supplied criteria query to build the select criteria. 
	 * @param type
	 * @param criteriaQuery
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	<T extends AbstractJpaEntity> List<T> selectByCriteria(Class<T> entityClass, String criteriaQuery, Map<String, Object> paramMap) throws ApplicationException;
	
	public void flushAndClear();
	
	public void flush();
	
	/**
	 * This method takes native SQL SELECT query and executes it. 
	 * It tries to map the result set to the supplied Class. It returns List of Objects of supplied entity class. 
	 * This relies on passed query, developer should be extra careful while using this and take care of precautions e.g. SQL injection etc.
	 * @param query
	 * @param T
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T) throws ApplicationException;
	
	/**
	 * This method takes native SQL SELECT query and executes it. 
	 * It tries to map the result set to the supplied Class. It returns List of Objects of supplied entity class. 
	 * This relies on passed query, developer should be extra careful while using this and take care of precautions e.g. SQL injection etc.
	 * @param query
	 * @param T
	 * @param limit - No of max objects to be returned
	 * @return
	 * @throws ApplicationException
	 */	
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int limit) throws ApplicationException;
	
	/**
	 * This method takes native SQL SELECT query and executes it. 
	 * It tries to map the result set to the supplied Class. It returns List of Objects of supplied entity class. 
	 * This relies on passed query, developer should be extra careful while using this and take care of precautions e.g. SQL injection etc.
	 * @param query
	 * @param T
	 * @param offset
	 * @param limit
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends CommonDto> List<T> findByQuery(String query, Class<? extends CommonDto> T, int offset, int limit) throws ApplicationException;
	
	/**
	 * This method takes native prepared statement, binds the parameters using the supplied map and executes it. 
	 * It tries to map the result set to the supplied Class. It returns List of Objects of supplied entity class. 
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
	 * This method takes a native update/insert query and executes it on DB. 
	 * It returns the IDs of affected rows of the database tables. 
	 * This relies on passed query, developer should be extra careful while using this and take care of precautions e.g. SQL injection etc.
	 * @param nativeQuery
	 * @return
	 * @throws ApplicationException
	 */
	public List<Long> executeNativeUpdates(String nativeQuery) throws ApplicationException;

	/**
	 * This method takes a native select query and executes it on DB. 
	 * It returns the selected data set as List<Object[]>. 
	 * This relies on passed query, developer should be extra careful while using this and take care of precautions e.g. SQL injection etc.
	 * @param nativeQuery
	 * @return
	 * @throws ApplicationException
	 */
	public List<Object[]> findByQuery(String query, String identifier) throws ApplicationException;

    int executeUpdates(String hqlQuery) throws ApplicationException;

    public CriteriaBuilder getCriteriaBuilder();
	
	public <A> List<A> executeQuery(CriteriaQuery<A> criteriaQuery);
	
	/**
	 * This method updates the selective field of the given entity. 
	 * @param entityClass
	 * @param id - Identifier of the matching entity
	 * @param paramMap - represents the values to be modified
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> int updateEntityFields(Class<T> entityClass, long id, Map<String, String> paramMap) throws ApplicationException;
	
	/**
	 * Updates the columntToBeUpdated with cases identified by Column idName.
	 * paramMap contains the idName id and columnToBeUpdate value
	 * @param entityClass
	 * @param idName
	 * @param columnToBeUpdated
	 * @param paramMap
	 * @return
	 * @throws ApplicationException
	 */
	public <T extends AbstractJpaEntity> int updateEntitesField(Class<T> entityClass, String idName, String columnToBeUpdated, Map<String, String> paramMap) throws ApplicationException;
	
}
