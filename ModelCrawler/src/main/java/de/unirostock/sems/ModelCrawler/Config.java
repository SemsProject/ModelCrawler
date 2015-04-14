package de.unirostock.sems.ModelCrawler;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.exceptions.ConfigurationException;

public class Config implements Serializable {

	private static final long serialVersionUID = 3875736107816341962L;
	private static final Log log = LogFactory.getLog( Config.class );
	private static volatile Config instance = null;
	private static volatile ObjectMapper mapper = null;
	
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
	
	public static ObjectMapper getObjectMapper() {
		
		// create new mapper
		if( mapper == null ) {
			mapper = new ObjectMapper();
			mapper.enable( SerializationFeature.INDENT_OUTPUT );
			mapper.enable( SerializationFeature.WRITE_NULL_MAP_VALUES );
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
	
	// ----------------------------------------
	
	private File workingDir = null;
	private String encoding = "UTF-8";
	private char pathSeparator = '/';
	private String[] extensionBlacklist = { "png", "bmp", "jpg", "jpeg", "html", "xhtml", "svg", "pdf", "json", "pl", "rdf", "rar", "msh", "zip" };
	private String tempDirPrefix = "ModelCrawler";
	private String workingDirConfig = "config.json";
	private String urnNamespace = "model";
	
	private String morreUrl = "http://localhost:7474/morre/";
	private String xmlFsUrl = "http://taylor.informatik.uni-rostock.de:8000/XmlFileServer/";
	
	private List<ModelDatabase> databases = new ArrayList<ModelDatabase>();
	
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

	public String getXmlFsUrl() {
		return xmlFsUrl;
	}

	public void setXmlFsUrl(String xmlFsUrl) {
		this.xmlFsUrl = xmlFsUrl;
	}

	public String getUrnNamespace() {
		return urnNamespace;
	}

	public void setUrnNamespace(String urnNamespace) {
		this.urnNamespace = urnNamespace;
	}
	
}
