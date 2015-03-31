package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;

// TODO: Auto-generated Javadoc
/**
 * The Class BioModelsChangeSet.
 */
public class BioModelsChangeSet extends ChangeSet {
	
	/**
	 * The Constructor.
	 *
	 * @param fileId the file id
	 */
	public BioModelsChangeSet(String fileId) {
		super(fileId);
	}
	
	/**
	 * Adds the change.
	 *
	 * @param change the change
	 */
	public void addChange( BioModelsChange change ) {
		super.addChange(change);
	}
	
}
