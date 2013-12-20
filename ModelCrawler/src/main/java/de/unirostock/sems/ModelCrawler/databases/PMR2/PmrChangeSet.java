package de.unirostock.sems.ModelCrawler.databases.PMR2;

import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;

public class PmrChangeSet extends ChangeSet {

	public PmrChangeSet(String fileId) {
		super(fileId);
	}
	
	public void addChange( PmrChange change ) {
		super.addChange(change);
	}

}
