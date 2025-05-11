
package com.notification.util;

import java.io.FileInputStream;
import java.util.*;

import com.notification.constants.GlobalConstants;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class GoogleBigQueryUtils
{
	
	public static BigQuery bigquery = null;

	static final Logger log = LogManager.getLogger(GoogleBigQueryUtils.class);
	
	static
	{
		try
		{
			bigquery = BigQueryOptions.newBuilder()
			.setCredentials(GoogleCredentials
			.fromStream(new FileInputStream(GlobalConstants.GOOGLE_BIGQUERY_CREDENTIALS)))
			.build().getService();
		}
		catch (Exception e)
		{
			log.error("Exception in initializing google bigquery cloud service ", e);
		}
	}
	
	public static Dataset createDataset(String datasetName)
	{
		DatasetInfo datasetInfo = DatasetInfo.newBuilder(datasetName).build();
		// Creates the dataset
		return bigquery.create(datasetInfo);
	}
	
	/**
	 * return the unsuccessful count of the batch insert
	 * @param datasetName
	 * @param tableName
	 * @param records
	 * @return
	 */
	public static int insertDataIntoBigquery(String datasetName, String tableName, List<Map<String, Object>> records)
	{
		TableId tableId = TableId.of(datasetName, tableName);
		
		InsertAllRequest.Builder batchInsert = InsertAllRequest.newBuilder(tableId);
		
		for (Map<String, Object> row : records)
		{
			batchInsert.addRow(row);
		}
		
		InsertAllResponse response = bigquery.insertAll(batchInsert.build());

		for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()){
			for(BigQueryError bigQueryError : entry.getValue()){
				log.error("big query message " + bigQueryError.toString());
			}
		}

		return response.getInsertErrors().size();
	}

	public static TableResult getDataFromBigQuery(String query) throws InterruptedException {
		QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).setUseLegacySql(false).build();
		JobId jobId = JobId.of(UUID.randomUUID().toString());
		Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());
		Long currentTime = new Date().getTime();
		queryJob = queryJob.waitFor();
		log.info("time taken to query " + (new Date().getTime() - currentTime));
		if (queryJob == null) {
			throw new RuntimeException("Job no longer exists");
		} else if (queryJob.getStatus().getError() != null) {
			throw new RuntimeException(queryJob.getStatus().getError().toString());
		}
		return queryJob.getQueryResults();
	}
}
