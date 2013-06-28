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
	
	/**
	 * Get the IDs from all models stored in the GraphDB
	 * 
	 * @return String[]
	 */
	public String[] getAllModelIds();
	
	/**
	 * Returns a list of ModelRecords with all versions of the model with the given ID
	 * 
	 * @param modelId
	 * @return List of ModelRecord
	 */
	public List<ModelRecord> getModelVersions( String modelId );
	
	/**
	 * Returns the latest version from the model with the given ID
	 * 
	 * @param modelId
	 * @return ModelRecord
	 */
	public ModelRecord getLatestModelVersion( String modelId );
	
	/**
	 * Returns the specific version of a model
	 * 
	 * @param modelId
	 * @param versionId
	 * @return
	 */
	public ModelRecord getModel( String modelId, String versionId );
	
	/**
	 * Modifies the meta-data of a model-version
	 * 
	 * @param modelId
	 * @param versionId
	 * @param meta
	 * @return 
	 */
	public boolean modifyModelMeta( String modelId, String versionId, Map<String, String> meta );
	
	/**
	 * Inserts a new model version with meta-data
	 * 
	 * @param modelId
	 * @param versionId
	 * @param parentVersion
	 * @param model
	 * @param meta
	 * @return
	 */
	public boolean insertModel( String modelId, String versionId, String parentVersion, URI model, Map<String, String> meta );
	
	/**
	 * Inserts a new model version
	 * 
	 * @param modelId
	 * @param versionId
	 * @param parentVersion
	 * @param model
	 * @return
	 */
	public boolean insertModel( String modelId, String versionId, String parentVersion, URI model );
}
