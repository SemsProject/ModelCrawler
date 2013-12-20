package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;

public class BioModelsChangeSet extends ChangeSet {
	
	public BioModelsChangeSet(String fileId) {
		super(fileId);
	}
	
	public void addChange( BioModelsChange change ) {
		super.addChange(change);
	}
	
}
