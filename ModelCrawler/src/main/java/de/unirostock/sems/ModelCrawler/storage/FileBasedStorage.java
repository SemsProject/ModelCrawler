package de.unirostock.sems.ModelCrawler.storage;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
	private final Log log = LogFactory.getLog( FileBasedStorage.class );
	
	protected URL httpAccessPath = null;
	@JsonIgnore
	protected Config config = null;
	
	public FileBasedStorage() {
		// Default constructor
	}
	
	protected abstract void initConnection() throws StorageException;
	protected abstract void closeConnection();
	protected abstract void makeDirs( String path ) throws StorageException;
	protected abstract void storeFile( InputStream source, String path ) throws StorageException;
	protected abstract InputStream getFile( String path ) throws StorageException;
	protected abstract void linkFiles( String sourcePath, String targetPath ) throws StorageException;
	
	protected static class VersionInfo {
		private String fileId = null; 
		private List<String> versions = new LinkedList<String>();
		
		public String getFileId() {
			return fileId;
		}
		public void setFileId(String fileId) {
			this.fileId = fileId;
		}
		public List<String> getVersions() {
			return versions;
		}
		public void setVersions(List<String> versions) {
			this.versions = versions;
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
	public URI storeModel(Change modelChange) throws StorageException {
		
		String[] fileId = splitFileId( modelChange.getFileId() );
		String outerPath = fileId[0];
		String innerPath = fileId[1];
		String fileName = fileId[2];
		
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
			info.getVersions().add( modelChange.getVersionId() );
			
			// store model
			InputStream input = new FileInputStream( modelChange.getXmlFile() );
			storeFile(input, pathToFile + fileName);
			input.close();
			
			// write info back
			writeVersionInfo(outerPath, info);
			
			String accessPath = httpAccessPath.getPath();
			if( !accessPath.endsWith(config.getPathSeparatorString()) )
				accessPath = accessPath + config.getPathSeparatorString();
			
			// add file name
			accessPath = accessPath + pathToFile + fileName;
			URI uri = new URI( httpAccessPath.getProtocol(), httpAccessPath.getHost(), accessPath, null);
			return uri;
		}
		catch (StorageException e) {
			log.error("Exception while accessing storage layer", e);
			throw e;
		} catch (FileNotFoundException e) {
			log.error("Cannot find xml document.", e);
			throw new StorageException("Cannot find xml document", e);
		} catch (IOException e) {
			log.error("Cannot store xml document.", e);
			throw new StorageException("Cannot store xml document.", e);
		} catch (URISyntaxException e) {
			log.error("Exception while building access URI", e);
			throw new StorageException("Exception while building access URI", e);
		}
		
	}
	
	@Override
	public URI linkModelVersion(String fileId, String sourceVersionId, String targetVersionId) throws StorageException {
		
		if( fileId == null || fileId.isEmpty() )
			throw new StorageException("FileId should not be null");
		if( sourceVersionId == null || sourceVersionId.isEmpty() || targetVersionId == null | targetVersionId.isEmpty() )
			throw new StorageException("VersionId should not be null");
		if( sourceVersionId.equals(targetVersionId) )
			throw new StorageException("target and source versionId are not allowed to be equal");
		
		String[] splittedFileId = splitFileId(fileId);
		String pathToSource = splittedFileId[0] + sourceVersionId + config.getPathSeparatorString() + splittedFileId[1];
		String sourcePath =  pathToSource + splittedFileId[2];
		String targetPath = splittedFileId[0] + targetVersionId + config.getPathSeparatorString() + splittedFileId[1] + splittedFileId[2];
		
		try {
			makeDirs( pathToSource );
			linkFiles(sourcePath, targetPath);
			
			String accessPath = httpAccessPath.getPath();
			if( !accessPath.endsWith(config.getPathSeparatorString()) )
				accessPath = accessPath + config.getPathSeparatorString();
			
			// add file name
			accessPath = accessPath + sourcePath;
			URI uri = new URI( httpAccessPath.getProtocol(), httpAccessPath.getHost(), accessPath, null);
			return uri;
		}
		catch (StorageException e) {
			log.error("Exception while accessing storage layer", e);
			throw e;
		} catch (URISyntaxException e) {
			log.error("Exception while building access URI", e);
			throw new StorageException("Exception while building access URI", e);
		}
	}

	/**
	 * Splits the fileId into handy parts<br>
	 *  0 - outerPath<br>
	 *  1 - innerPath<br>
	 *  2 - fileName<br>
	 *  
	 * @param fileId
	 * @return
	 */
	private String[] splitFileId( String fileId ) {
		final int OUTER_PATH = 0;
		final int INNER_PATH = 1;
		final int FILENAME = 2;
		
		String[] result = new String[3];
		
		// split fileId along the urn separator
		String[] splittedFileId = fileId.split( Constants.URN_SEPARATOR );
		
		// ignore first 2 fields (urn and namespace)
		StringBuilder path = new StringBuilder();
		for( int index = 2; index < splittedFileId.length; index++ ) {
			if( splittedFileId[index] == null || splittedFileId[index].isEmpty() )
				continue;
			else if( splittedFileId[index].equals(Constants.URN_VERSION_PLACEHOLDER) && result[OUTER_PATH] == null ) {
				result[OUTER_PATH] = path.toString();
				path = new StringBuilder();
			}
			else if( index == splittedFileId.length-1 && result[OUTER_PATH] != null && result[INNER_PATH] == null ) {
				result[INNER_PATH] = path.toString();
				result[FILENAME] = splittedFileId[index];
			}
			else {
				path.append( splittedFileId[index] );
				path.append( config.getPathSeparator() );
			}
		}
		
		return result;
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
		} catch (IOException | StorageException e) {
			log.error("Cannot write VersionInfo", e);
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
