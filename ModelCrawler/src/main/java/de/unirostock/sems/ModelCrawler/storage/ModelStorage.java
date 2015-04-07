package de.unirostock.sems.ModelCrawler.storage;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;

public abstract class ModelStorage {
	
	
	
	public ModelStorage() {
		
	}
	
	/**
	 * Puts a model into the storage system and returns the URI
	 * 
	 * @param modelChange
	 * @return
	 */
	public abstract URI storeModel( Change modelChange );
	
	/**
	 * Puts a whole ChangeSet of models into the storage system
	 * 
	 * @param changeSet
	 * @return
	 */
	public List<URI> storeModelChangeSet( ChangeSet changeSet ) {
		List<URI> result = new LinkedList<URI>();
		
		for( Change model : changeSet.getChanges() )
			result.add( storeModel(model) );
		
		return result;
	}

}
