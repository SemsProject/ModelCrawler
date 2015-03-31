package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;
import de.unirostock.sems.ModelCrawler.databases.PMR2.PmrDb;

@XmlAccessorType(XmlAccessType.FIELD)
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "type" )
@JsonSubTypes({
	@Type( value = ModelDatabase.class, name = ModelDatabase.DatabaseTypes.NONE ),
	@Type( value = BioModelsDb.class, name = ModelDatabase.DatabaseTypes.BMDB ),
	@Type( value = PmrDb.class, name = ModelDatabase.DatabaseTypes.PMR2 )
})
public abstract class ModelDatabase implements Runnable, Closeable {
	
	public abstract class DatabaseTypes {
		public static final String NONE = "";
		public static final String BMDB = "BMDB";
		public static final String PMR2 = "PMR2";
	}
	
	private String type = DatabaseTypes.NONE;
	
	/**
	 * lists all Models in the latest revision
	 * 
	 * @return List with all model IDs
	 */
	public abstract List<String> listModels();
	
	/**
	 * Returns a map with all changes made after the last crawl <br>
	 * the fileId is the map key.
	 * 
	 * @return Map<fileId, ChangeSet>
	 */
	public abstract Map<String, ChangeSet> listChanges();
	
	/** 
	 * Returns the ChangeSet only for one specific model
	 * 
	 * @param fileId
	 * @return ChangeSet
	 */
	public abstract ChangeSet getModelChanges( String fileId );
	
	/**
	 * Cleans up the working directory
	 */
	public abstract void close();
	
	/**
	 * Starts the prozess of crawling for this specific Database
	 * 
	 */
	@Override
	public abstract void run();

	
	public String getType() {
		return type;
	}
	
}
