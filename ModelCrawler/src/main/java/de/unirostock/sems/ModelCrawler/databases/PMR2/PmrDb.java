package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;

public class PmrDb implements ModelDatabase {
	
	private final Log log = LogFactory.getLog( PmrDb.class );
	
	public PmrDb(String repoListUrl, GraphDatabase graphDb) {
		
		
		
	}

	@Override
	public List<String> listModels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ChangeSet> listChanges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeSet getModelChanges(String modelId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
