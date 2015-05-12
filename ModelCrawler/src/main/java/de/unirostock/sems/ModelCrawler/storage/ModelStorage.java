package de.unirostock.sems.ModelCrawler.storage;

import java.io.Closeable;
import java.io.Serializable;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.exceptions.StorageException;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type" )
@JsonSubTypes({
	@Type( value = FileStorage.class, name = ModelStorage.Types.FILE ),
	@Type( value = FtpStorage.class, name = ModelStorage.Types.FTP )
})
public abstract class ModelStorage implements Serializable, Closeable {
	
	private static final long serialVersionUID = -1028829011896100407L;
	
	public abstract static class Types {
		public static final String FILE = "file";
		public static final String FTP = "ftp";
	}

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
	 * @throws StorageException 
	 */
	public abstract URI storeModel( Change modelChange ) throws StorageException;
	
	/**
	 * Links the source Version to the target Version 
	 * (The target Version should exist already)
	 * 
	 * @param fileId
	 * @param sourceVersionId
	 * @param targetVersionId
	 * @return
	 * @throws StorageException
	 */
	public abstract URI linkModelVersion( String fileId, String sourceVersionId, String targetVersionId) throws StorageException;
	
	/**
	 * Puts a whole ChangeSet of models into the storage system
	 * 
	 * @param changeSet
	 * @return
	 * @throws StorageException 
	 */
	public List<URI> storeModelChangeSet( ChangeSet changeSet ) throws StorageException {
		List<URI> result = new LinkedList<URI>();
		
		for( Change model : changeSet.getChanges() )
			result.add( storeModel(model) );
		
		return result;
	}

}
