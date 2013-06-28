package de.unirostock.sems.ModelCrawler.GraphDb.Interface;

import java.util.List;
import java.util.Map;

import de.unirostock.sems.ModelCrawler.GraphDb.QueryResult;

public interface GraphDatabase {
	
	public final String FEAUTURE_ID = "ID";
	public final String FEAUTURE_NAME = "NAME";
	//TODO to be continued...
	
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
	
	/**
	 * Generates and executes a Query to the GraphDB
	 *  
	 * @param feautures e.g. ID, NAME, COMPONENT, VARIABLE, CREATOR, AUTHOR
	 * @return
	 */
	public List<QueryResult> cellMlModelQuery( Map<String, String> feautures );
}
