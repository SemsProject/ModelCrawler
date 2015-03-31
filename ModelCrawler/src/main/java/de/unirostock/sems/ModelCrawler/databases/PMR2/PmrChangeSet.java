package de.unirostock.sems.ModelCrawler.databases.PMR2;

import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;

// TODO: Auto-generated Javadoc
/**
 * The Class PmrChangeSet.
 */
public class PmrChangeSet extends ChangeSet {

	/**
	 * The Constructor.
	 *
	 * @param fileId the file id
	 */
	public PmrChangeSet(String fileId) {
		super(fileId);
	}
	
	/**
	 * Adds the change.
	 *
	 * @param change the change
	 */
	public void addChange( PmrChange change ) {
		super.addChange(change);
	}

}
