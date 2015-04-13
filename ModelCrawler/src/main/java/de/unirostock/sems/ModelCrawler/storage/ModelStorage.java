package de.unirostock.sems.ModelCrawler.storage;

import java.io.Closeable;
import java.io.Serializable;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.exceptions.StorageException;

public abstract class ModelStorage implements Serializable, Closeable {
	
	private static final long serialVersionUID = -1028829011896100407L;

	public ModelStorage() {
		
	}
	
	/**
	 * Connects to the storage destination, is supposed to be called
	 * before every other operation
	 * 
	 * @throws StorageException 
	 *  
	 */
	public abstract void connect() throws StorageException;
	
	/**
	 * Closes the connector and cleans things up
	 * 
	 */
	public abstract void close();
	
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
