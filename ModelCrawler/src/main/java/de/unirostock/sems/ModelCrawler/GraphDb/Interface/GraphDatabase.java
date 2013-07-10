package de.unirostock.sems.ModelCrawler.GraphDb.Interface;

import java.net.URI;
import java.util.List;
import java.util.Map;

import de.unirostock.sems.ModelCrawler.GraphDb.ModelRecord;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseCommunicationException;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseError;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseInterfaceException;

public interface GraphDatabase {
	
	/**
	 * Checks if database is available
	 * 
	 * @deprecated
	 * @return
	 * @throws GraphDatabaseCommunicationException 
	 */
	@Deprecated
	public boolean isModelManagerAlive() throws GraphDatabaseCommunicationException;
	
	/**
	 * Checks if database contains data
	 * 
	 * @deprecated
	 * @return
	 * @throws GraphDatabaseCommunicationException 
	 */
	@Deprecated
	public boolean isDatabaseEmpty() throws GraphDatabaseCommunicationException;
	
	/**
	 * Returns all features included by an cellMl query
	 * 
	 * @deprecated
	 * @return
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 */
	@Deprecated
	public String[] cellMlModelQueryFeatures() throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException;
	
	/**
	 * Requests a single Model by its ModelId
	 * 
	 * @deprecated
	 * @param modelId
	 * @return QueryResult or NULL if no match found
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 */
	@Deprecated
	public ModelRecord getCellMlModelFromId( String modelId ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException;
	
	/**
	 * Generates and executes a Query to the GraphDB
	 *  
	 * @deprecated
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
	 * @throws GraphDatabaseInterfaceException 
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseError 
	 */
	public String[] getAllModelIds() throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError;
	
	/**
	 * Returns a list of ModelRecords with all versions of the model with the given ID
	 * 
	 * @param modelId
	 * @return List of ModelRecord
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 * @throws GraphDatabaseError 
	 */
	public String[] getModelVersions( String modelId ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError;
	
	/**
	 * Returns the latest version from the model with the given ID
	 * 
	 * @param modelId
	 * @return ModelRecord
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 * @throws GraphDatabaseError 
	 */
	public ModelRecord getLatestModelVersion( String modelId ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError;
	
	/**
	 * Returns the specific version of a model
	 * 
	 * @param modelId
	 * @param versionId
	 * @return
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 * @throws GraphDatabaseError 
	 */
	public ModelRecord getModel( String modelId, String versionId ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError;
	
	/**
	 * Modifies the meta-data of a model-version
	 * 
	 * @param modelId
	 * @param versionId
	 * @param meta
	 * @return 
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 * @throws GraphDatabaseError 
	 */
	public boolean modifyModelMeta( String modelId, String versionId, Map<String, String> meta ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError;
	
	/**
	 * Inserts a new model version with meta-data
	 * 
	 * @param modelId
	 * @param versionId
	 * @param parentVersion
	 * @param model
	 * @param meta
	 * @return
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 * @throws GraphDatabaseError 
	 */
	public boolean insertModel( String modelId, String versionId, String parentVersion, URI model, Map<String, String> meta ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError;
	
	/**
	 * Inserts a new model version
	 * 
	 * @param modelId
	 * @param versionId
	 * @param parentVersion
	 * @param model
	 * @return
	 * @throws GraphDatabaseCommunicationException 
	 * @throws GraphDatabaseInterfaceException 
	 * @throws GraphDatabaseError 
	 */
	public boolean insertModel( String modelId, String versionId, String parentVersion, URI model ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError;
}
