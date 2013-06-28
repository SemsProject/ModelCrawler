package de.unirostock.sems.ModelCrawler.GraphDb.Interface;

import java.net.URI;
import java.util.List;
import java.util.Map;

import de.unirostock.sems.ModelCrawler.GraphDb.ModelRecord;

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
	@Deprecated
	public String[] cellMlModelQueryFeatures();
	
	/**
	 * Requests a single Model by its ModelId
	 * 
	 * 
	 * @param modelId
	 * @return QueryResult or NULL if no match found
	 */
	@Deprecated
	public ModelRecord getCellMlModelFromId( String modelId );
	
	/**
	 * Generates and executes a Query to the GraphDB
	 *  
	 * @param feautures e.g. ID, NAME, COMPONENT, VARIABLE, CREATOR, AUTHOR
	 * @return
	 */
	@Deprecated
	public List<ModelRecord> cellMlModelQuery( Map<String, String> feautures );
	
	
	// ------------------------------------------------------------------------
	
	public String[] getAllModelIds();
	
	public List<ModelRecord> getModelVersions( String modelId );
	
	public ModelRecord getLatestModelVersion( String modelId );
	
	public ModelRecord getModel( String modelId, String versionId );
	
	public boolean modifyModelMeta( String modelId, String versionId, Map<String, String> meta );
	
	public boolean insertModel( String modelId, String versionId, String parentVersion, URI model, Map<String, String> meta );
	
	public boolean insertModel( String modelId, String versionId, String parentVersion, URI model );
}
