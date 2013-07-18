package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.util.List;
import java.util.Map;

public interface ModelDatabase extends Runnable {
	
	/**
	 * lists all Models in the latest revision
	 * 
	 * @return List with all model IDs
	 */
	public List<String> listModels();
	
	/**
	 * Returns a map with all changes made after the last crawl <br>
	 * the modelId is the map key.
	 * 
	 * @return Map<ModelId, ChangeSet>
	 */
	public Map<String, ChangeSet> listChanges();
	
	/** 
	 * Returns the ChangeSet only for one specific model
	 * 
	 * @param modelId
	 * @return ChangeSet
	 */
	public ChangeSet getModelChanges( String modelId );
	
	/**
	 * Cleans up the working directory
	 */
	public void cleanUp();
	
	/**
	 * Starts the prozess of crawling for this specific Database
	 * 
	 */
	@Override
	public void run();
}
