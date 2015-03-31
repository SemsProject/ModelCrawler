package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

public abstract class ModelDatabase implements Runnable, Closeable {
	
	/**
	 * lists all Models in the latest revision
	 * 
	 * @return List with all model IDs
	 */
	public abstract List<String> listModels();
	
	/**
	 * Returns a map with all changes made after the last crawl <br>
	 * the fileId is the map key.
	 * 
	 * @return Map<fileId, ChangeSet>
	 */
	public abstract Map<String, ChangeSet> listChanges();
	
	/** 
	 * Returns the ChangeSet only for one specific model
	 * 
	 * @param fileId
	 * @return ChangeSet
	 */
	public abstract ChangeSet getModelChanges( String fileId );
	
	/**
	 * Cleans up the working directory
	 */
	public abstract void close();
	
	/**
	 * Starts the prozess of crawling for this specific Database
	 * 
	 */
	@Override
	public abstract void run();
}
