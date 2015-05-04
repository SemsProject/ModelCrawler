package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.net.URL;

import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;

public class BioModelsChangeSet extends ChangeSet {
	
	public BioModelsChangeSet(URL repositoryUrl, String filePath) {
		super(repositoryUrl, filePath);
	}
	
	public void addChange( BioModelsChange change ) {
		super.addChange(change);
	}
	
}
