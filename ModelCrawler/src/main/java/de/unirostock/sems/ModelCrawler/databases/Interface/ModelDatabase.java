package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.unirostock.sems.ModelCrawler.Config;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;
import de.unirostock.sems.ModelCrawler.databases.PMR2.PmrDb;
import de.unirostock.sems.morre.client.MorreCrawlerInterface;

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
public abstract class ModelDatabase implements Callable<Map<String, ChangeSet>>, Closeable, Serializable {
	
	private static final long serialVersionUID = -3406749011901702763L;

	public abstract class DatabaseTypes {
		public static final String NONE = "";
		public static final String BMDB = "BMDB";
		public static final String PMR2 = "PMR2";
	}
	
	@JsonIgnore
	private final Log log = LogFactory.getLog( ModelDatabase.class );
	
	protected String type = DatabaseTypes.NONE;
	protected File workingDir = null;
	protected int limit = 0;
	
	@JsonIgnore
	protected File tempDir = null;
	@JsonIgnore
	protected MorreCrawlerInterface morreClient = null;
	
	public ModelDatabase() {}		
	
	/**
	 * creates a new and empty temporary directory and sets the class variable
	 * 
	 * @return
	 */
	protected synchronized File createTempDir() {
		
		// create temp dir
		try {
			tempDir = Files.createTempDirectory( Config.getConfig().getTempDirPrefix(),
							PosixFilePermissions.asFileAttribute( PosixFilePermissions.fromString("rwx------")
					)).toFile();
		} catch (IOException e) {
			tempDir = new File("temp/" + new Random( new Date().getTime() ).nextLong() );
			tempDir.mkdirs();
			
			log.error("Cannot create TempDirectory, using '" + tempDir.getAbsolutePath() + "' instead.", e);
		}
		
		return tempDir;
	}
	
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
	 * @return 
	 * 
	 */
	public abstract Map<String, ChangeSet> call();

	
	public String getType() {
		return type;
	}

	public String getWorkingDir() {
		return workingDir.toPath().relativize( Config.getConfig().getWorkingDir().toPath() ).toString();
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = new File( Config.getConfig().getWorkingDir(), workingDir );
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public MorreCrawlerInterface getMorreClient() {
		return morreClient;
	}

	public void setMorreClient(MorreCrawlerInterface morreClient) {
		this.morreClient = morreClient;
	}
	
}