package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

public abstract class ChangeSet {
	
	protected String modelId;
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
		if( change.getModelId().equals(modelId) )
			changes.add(change);
	}
	
	public ChangeSet( String modelId ) {
		changes = new TreeSet<Change>();
		this.modelId = modelId;
	}
	
	public String getModelId() {
		return modelId;
	}
	
	@Override
	public String toString() {
		return "CS:" + modelId + "-" + changes.size();
	}
	
}
