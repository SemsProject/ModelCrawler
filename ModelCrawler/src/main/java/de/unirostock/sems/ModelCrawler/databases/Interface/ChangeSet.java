package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public abstract class ChangeSet {
	
	protected String fileId;
	protected NavigableSet<Change> changes;
	
	public Set<Change> getChanges() {
		return changes;
	}
	
	public Change getLatestChange() {
		if( changes.size() > 0 )
			return changes.last();
		else
			return null;
	}
	
	public void addChange(Change change) {
		if( change.getFileId().equals(fileId) )
			changes.add(change);
	}
	
	public ChangeSet( String fileId ) {
		changes = new TreeSet<Change>();
		this.fileId = fileId;
	}
	
	public String getFileId() {
		return fileId;
	}
	
	@Override
	public String toString() {
		return "CS:" + fileId + "-" + changes.size();
	}
	
}
