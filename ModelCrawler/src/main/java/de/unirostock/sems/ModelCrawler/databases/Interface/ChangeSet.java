package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

// TODO: Auto-generated Javadoc
/**
 * The Class ChangeSet.
 */
public abstract class ChangeSet {
	
	/** The file id. */
	protected String fileId;
	
	/** The changes. */
	protected NavigableSet<Change> changes;
	
	/**
	 * Gets the changes.
	 *
	 * @return the changes
	 */
	public Set<Change> getChanges() {
		return changes;
	}
	
	/**
	 * Gets the latest change.
	 *
	 * @return the latest change
	 */
	public Change getLatestChange() {
		if( changes.size() > 0 )
			return changes.last();
		else
			return null;
	}
	
	/**
	 * Adds the change.
	 *
	 * @param change the change
	 */
	public void addChange(Change change) {
		if( change.getFileId().equals(fileId) )
			changes.add(change);
	}
	
	/**
	 * The Constructor.
	 *
	 * @param fileId the file id
	 */
	public ChangeSet( String fileId ) {
		changes = new TreeSet<Change>();
		this.fileId = fileId;
	}
	
	/**
	 * Gets the file id.
	 *
	 * @return the file id
	 */
	public String getFileId() {
		return fileId;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CS:" + fileId + "-" + changes.size();
	}
	
}
