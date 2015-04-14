package de.unirostock.sems.ModelCrawler.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unirostock.sems.ModelCrawler.Config;
import de.unirostock.sems.ModelCrawler.Constants;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.exceptions.StorageException;

public abstract class FileBasedStorage extends ModelStorage {
	
	@JsonIgnore
	private static final long serialVersionUID = 5628650761119478142L;
	@JsonIgnore
	private final Log log = LogFactory.getLog( FtpStorage.class );
	
	protected URL httpAccessPath = null;
	@JsonIgnore
	protected Config config = null;
	
	public FileBasedStorage() {
		// Default constructor
	}
	
	protected abstract boolean makeDirs( String path ) throws StorageException;
	protected abstract void storeFile( InputStream source, String path ) throws StorageException;
	protected abstract InputStream getFile( String path ) throws StorageException;
	protected abstract void initConnection() throws StorageException;
	protected abstract void closeConnection();
	
	protected static class VersionInfo {
		private String fileId = null; 
		private List<String> verisons = new LinkedList<String>();
		
		public String getFileId() {
			return fileId;
		}
		public void setFileId(String fileId) {
			this.fileId = fileId;
		}
		public List<String> getVerisons() {
			return verisons;
		}
		public void setVerisons(List<String> verisons) {
			this.verisons = verisons;
		}
	}
	
	public void  connect() throws StorageException {
		config = Config.getConfig();
		initConnection();
	}
	
	public void close() {
		closeConnection();
	}
	
	@Override
	public URI storeModel(Change modelChange) {
		String outerPath = null;
		String innerPath = null;
		String fileName = null;
		
		// split fileId along the urn separator
		String[] fileId = modelChange.getFileId().split( Constants.URN_SEPARATOR );
		
		// ignore first 2 fields (urn and namespace)
		StringBuilder path = new StringBuilder();
		for( int index = 2; index < fileId.length; index++ ) {
			if( fileId[index] == null || fileId[index].isEmpty() )
				continue;
			else if( fileId[index].equals(Constants.URN_VERSION_PLACEHOLDER) && outerPath == null ) {
				outerPath = path.toString();
				path = new StringBuilder();
			}
			else if( index == fileId.length-1 && outerPath != null && innerPath == null ) {
				innerPath = path.toString();
				fileName = fileId[index];
			}
			else {
				path.append( fileId[index] );
				path.append( config.getPathSeparator() );
			}
		}
		
		try {
			// create outer Path
			makeDirs( outerPath );
			VersionInfo info = getVersionInfo(outerPath);
			
			// create directory for version
			String pathToFile = outerPath + modelChange.getVersionId() + config.getPathSeparatorString() + innerPath;
			makeDirs( pathToFile );
			
			// add version to info
			if( info == null ) {
				info = new VersionInfo();
				info.setFileId( modelChange.getFileId() );
			}
			info.getVerisons().add( modelChange.getVersionId() );
			
			modelChange.getXmlFile();
			
			// write info back
			writeVersionInfo(outerPath, info);
		}
		catch (StorageException e) {
			// TODO
		}
		
		return null;
	}
	
	private VersionInfo getVersionInfo( String outerPath ) throws StorageException {
		VersionInfo info = null;
		
		if( outerPath == null || outerPath.isEmpty() )
			throw new IllegalArgumentException("outerPath is not allowed to be empty");
		
		if( !outerPath.endsWith( String.valueOf(config.getPathSeparator()) ) )
			outerPath = outerPath + String.valueOf(config.getPathSeparator());
		
		try {
			InputStream stream = getFile( outerPath + Constants.VERSION_INFO_FILENAME );
			if( stream != null )
				info = Config.getObjectMapper().readValue(stream, VersionInfo.class);
		} catch (StorageException e) {
			log.error("Cannot get VersionInfo from: " + outerPath, e);
		} catch (IOException e) {
			log.error("Exception while reading VersionInfo!", e);
			throw new StorageException("Exception while reading VersionInfo", e);
		}
		
		return info;
	}
	
	private void writeVersionInfo( String outerPath, VersionInfo info ) {
		
		if( outerPath == null || outerPath.isEmpty() )
			throw new IllegalArgumentException("outerPath is not allowed to be empty");
		
		if( info == null )
			info = new VersionInfo();
		
		if( !outerPath.endsWith( String.valueOf(config.getPathSeparator()) ) )
			outerPath = outerPath + String.valueOf(config.getPathSeparator());
		
		try {
			// serialize info buffer to input stream
			ByteArrayInputStream input = new ByteArrayInputStream( Config.getObjectMapper().writeValueAsBytes(info) );
			// write file
			storeFile(input, outerPath + Constants.VERSION_INFO_FILENAME);
			input.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// getter/setter
	
	public URL getHttpAccessPath() {
		return httpAccessPath;
	}

	public void setHttpAccessPath(URL httpAccessPath) {
		this.httpAccessPath = httpAccessPath;
	}
	
}
