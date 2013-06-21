package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.util.ArrayList;
import java.util.List;

public abstract class ChangeSet {
	
	protected String modelId;
	protected List<Change> changes;
	
	public List<Change> getChanges() {
		return changes;
	}
	public void addChange(Change change) {
		this.changes.add(change);
	}
	
	public ChangeSet( String modelId ) {
		changes = new ArrayList<Change>();
		this.modelId = modelId;
	}
	
	public String getModelId() {
		return modelId;
	}
	
}
