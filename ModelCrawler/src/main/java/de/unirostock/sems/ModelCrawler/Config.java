package de.unirostock.sems.ModelCrawler;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.exceptions.ConfigurationException;
import de.unirostock.sems.ModelCrawler.storage.ModelStorage;

public class Config implements Serializable {

	private static final long serialVersionUID = 3875736107816341962L;
	private static final Log log = LogFactory.getLog( Config.class );
	private static volatile Config instance = null;
	private static volatile ObjectMapper mapper = null;
	
	public static enum WorkingMode {
		NORMAL,
		TEMPLATE_CONFIG,
		TEST,
		NO_MORRE
	}
	
	private static WorkingMode workingMode = WorkingMode.NORMAL;
	
	/** 
	 * Gets the config instance
	 * 
	 * @return
	 */
	public static Config getConfig() {

		if( instance == null )
			throw new IllegalStateException("Config not loaded");
		
		return instance;
	}
	
	/**
	 * returns the default jackson object mapper for serializing and deserializing json objects
	 * 
	 * @return
	 */
	public static ObjectMapper getObjectMapper() {
		
		// create new mapper
		if( mapper == null ) {
			mapper = new ObjectMapper();
			mapper.enable( SerializationFeature.INDENT_OUTPUT );
			mapper.enable( SerializationFeature.WRITE_NULL_MAP_VALUES );
			mapper.enable( Feature.ALLOW_COMMENTS );
		}
		
		return mapper;
	}
	
	/**
	 * Loads the config from a Json file
	 * @return
	 * @throws ConfigurationException 
	 */
	public synchronized static Config load( File location ) throws ConfigurationException {
		
		if( instance != null )
			throw new IllegalStateException("Config already loaded");
		
		try {
			// read json config
			instance = getObjectMapper().readValue( location, Config.class );
		} catch (IOException e) {
			log.error("Error while reading config file " + location.getAbsolutePath(), e);
			throw new ConfigurationException("Error while reading config file " + location.getAbsolutePath(), e);
		}
		
		return instance;
	}
	
	/**
	 * Loads some default config parameters
	 * 
	 * @return
	 */
	public synchronized static Config defaultConfig() {
		
		if( instance != null )
			throw new IllegalStateException("Config already loaded");
		
		instance = new Config();
		return instance;
	}
	
	/**
	 * Gets the current working mode of the crawler
	 * @return
	 */
	public static WorkingMode getWorkingMode() {
		return Config.workingMode;
	}
	
	/**
	 * Sets the current working mode of the crawler
	 * 
	 * @param workingMode
	 */
	public static void setWorkingMode(WorkingMode workingMode) {
		Config.workingMode = workingMode; 
	}
	
	// ----------------------------------------
	
	private File workingDir = null;
	private File tempDir = null;
	private String encoding = "UTF-8";
	private char pathSeparator = '/';
	private String[] extensionBlacklist = { "png", "bmp", "jpg", "jpeg", "html", "xhtml", "svg", "pdf", "json", "pl", "rdf", "rar", "msh", "zip" };
	private String tempDirPrefix = "ModelCrawler";
	private String workingDirConfig = "config.json";
	private String urnNamespace = "model";
	
	private String morreUrl = "http://localhost:7474/morre/";
	
	private List<ModelDatabase> databases = new ArrayList<ModelDatabase>();
	private ModelStorage storage = null;
	
	/**
	 * Default private constructor.
	 * 
	 * use {@link getConfig} or {@link load} instead
	 */
	private Config() {
		
	}
	
	/**
	 * Saves the current config to disk
	 * 
	 * @param location
	 * @throws ConfigurationException 
	 */
	public synchronized void save( File location ) throws ConfigurationException {
		
		try {
			Config.getObjectMapper().writeValue( location, this );
		} catch (IOException e) {
			log.error("Error while writing config file " + location.getAbsolutePath(), e);
			throw new ConfigurationException("Error while writing config file " + location.getAbsolutePath(), e);
		}
	}

	public File getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}
	
	public File getTempDir() {
		if( tempDir == null ) {
			try {
				log.warn("Creates default temp dir, because config parameter was not set");
				tempDir = Files.createTempDirectory(Constants.DEFAULT_TEMP_PREFIX, Constants.TEMP_DIR_POSIX_ATTRIBUTES).toFile();
				log.info( MessageFormat.format("temp directory was set to {0}", tempDir.getAbsolutePath()) );
				tempDir.deleteOnExit();
			} catch (IOException e) {
				log.error("Not able to create default temp directory", e);
				return null;
			}
		}
		
		return tempDir;
	}

	public void setTempDir(File tempDir) {
		if( tempDir == null )
			return;
		
		this.tempDir = tempDir;
		tempDir.mkdirs();
		tempDir.deleteOnExit();
		log.info( MessageFormat.format("temp directory was set to {0}", tempDir.getAbsolutePath()) );
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public char getPathSeparator() {
		return pathSeparator;
	}
	
	@JsonIgnore
	public String getPathSeparatorString() {
		return String.valueOf(pathSeparator);
	}

	public void setPathSeparator(char pathSeparator) {
		this.pathSeparator = pathSeparator;
	}

	public String[] getExtensionBlacklist() {
		return extensionBlacklist;
	}

	public void setExtensionBlacklist(String[] extensionBlacklist) {
		this.extensionBlacklist = extensionBlacklist;
	}

	public List<ModelDatabase> getDatabases() {
		return databases;
	}

	public void setDatabases(List<ModelDatabase> databases) {
		this.databases = databases;
	}

	public String getTempDirPrefix() {
		return tempDirPrefix;
	}

	public void setTempDirPrefix(String tempDirPrefix) {
		this.tempDirPrefix = tempDirPrefix;
	}

	public String getWorkingDirConfig() {
		return workingDirConfig;
	}

	public void setWorkingDirConfig(String workingDirConfig) {
		this.workingDirConfig = workingDirConfig;
	}

	public String getMorreUrl() {
		return morreUrl;
	}

	public void setMorreUrl(String morreUrl) {
		this.morreUrl = morreUrl;
	}

	public ModelStorage getStorage() {
		return storage;
	}

	public void setStorage(ModelStorage storage) {
		this.storage = storage;
	}

	public String getUrnNamespace() {
		return urnNamespace;
	}

	public void setUrnNamespace(String urnNamespace) {
		this.urnNamespace = urnNamespace;
	}
	
}
