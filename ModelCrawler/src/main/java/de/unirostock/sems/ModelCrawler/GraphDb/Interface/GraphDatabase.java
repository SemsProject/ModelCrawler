package de.unirostock.sems.ModelCrawler.GraphDb.Interface;

import de.unirostock.sems.ModelCrawler.GraphDb.QueryResult;

public interface GraphDatabase {
	
	/**
	 * Checks if database is available
	 * 
	 * @return
	 */
	public boolean isModelManagerAlive();
	
	/**
	 * Checks if database contains data
	 * 
	 * @return
	 */
	public boolean isDatabaseEmpty();
	
	/**
	 * Returns all features included by an cellMl query
	 * 
	 * @return
	 */
	public String[] cellMlModelQueryFeatures();
	
	/**
	 * Requests a single Model by its ModelId
	 * 
	 * 
	 * @param modelId
	 * @return QueryResult or NULL if no match found
	 */
	public QueryResult getCellMlModelFromId( String modelId );
	
	
}
