package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Interface ModelDatabase.
 */
public interface ModelDatabase extends Runnable {
	
	/**
	 * lists all Models in the latest revision.
	 *
	 * @return List with all model IDs
	 */
	public List<String> listModels();
	
	/**
	 * Returns a map with all changes made after the last crawl <br>
	 * the fileId is the map key.
	 * 
	 * @return Map<fileId, ChangeSet>
	 */
	public Map<String, ChangeSet> listChanges();
	
	/**
	 * Returns the ChangeSet only for one specific model.
	 *
	 * @param fileId the file id
	 * @return ChangeSet
	 */
	public ChangeSet getModelChanges( String fileId );
	
	/**
	 * Cleans up the working directory.
	 */
	public void cleanUp();
	
	/**
	 * Starts the prozess of crawling for this specific Database.
	 */
	@Override
	public void run();
}
