package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.util.ArrayList;
import java.util.List;

public abstract class ChangeSet {
	
	private List<Change> changes;
	
	public List<Change> getChanges() {
		return changes;
	}
	public void addChange(Change change) {
		this.changes.add(change);
	}
	
	public ChangeSet() {
		changes = new ArrayList<Change>();
	}
	
}
