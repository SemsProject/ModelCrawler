package de.unirostock.sems.ModelCrawler.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unirostock.sems.ModelCrawler.exceptions.StorageException;

public class FileStorage extends FileBasedStorage {
	
	@JsonIgnore
	private static final long serialVersionUID = 139783500897489458L;
	@JsonIgnore
	private static final Log log = LogFactory.getLog( FileBasedStorage.class );
	
	private File baseDir = null;

	public FileStorage() {
		super();
		// default constructor
	}
	
	@Override
	protected void initConnection() throws StorageException {
		
		if( baseDir == null )
			throw new StorageException("No base Directory was set");
		
		if( baseDir.exists() == false || baseDir.isDirectory() == false ) {
			if( baseDir.mkdirs() == false )
				throw new StorageException("Cannot create base directory.");
		}
		
		if( baseDir.canRead() == false || baseDir.canWrite() == false )
			throw new StorageException("Cannot read or write in base directory");
	}

	@Override
	protected void closeConnection() {
		// nothing to do here
	}
	
	@Override
	protected void makeDirs(String path) throws StorageException {
		File newDirectory = new File(baseDir, path);
		if( newDirectory.mkdirs() == false )
			throw new StorageException("Cannot create directory: " + newDirectory.toString());
		
	}

	@Override
	protected void storeFile(InputStream source, String path) throws StorageException {
		File file = new File(baseDir, path);
		
		try {
			// open output file
			OutputStream output = new FileOutputStream(file);
			// copy stuff
			IOUtils.copy(source, output);
			
			// close streams
			output.flush();
			output.close();
			source.close();
		} catch (FileNotFoundException e) {
			throw new StorageException("Cannot create file", e);
		} catch (IOException e) {
			throw new StorageException("Exception while storing file to disk", e);
		}
		
	}

	@Override
	protected InputStream getFile(String path) throws StorageException {
		File file = new File(baseDir, path);
		
		if( file.exists() == false )
			throw new StorageException("Cannot find file: " + file.toString());
		else if( file.canRead() == false || file.isFile() == false )
			throw new StorageException("Cannot access file: " + file.toString());
		else {
			try {
				FileInputStream stream = new FileInputStream(file);
				return stream;
			} catch (FileNotFoundException e) {
				throw new StorageException("Cannot open file: " + file, e);
			}
		}
			
	}

	// getter/setter
	
	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

}
