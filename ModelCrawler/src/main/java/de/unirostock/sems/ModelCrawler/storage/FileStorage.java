package de.unirostock.sems.ModelCrawler.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.MessageFormat;

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
		// create only if necessary
		if( newDirectory.exists() == false ) {
			if( newDirectory.mkdirs() == false )
				throw new StorageException("Cannot create directory: " + newDirectory.toString());
		}
		
	}
	
	@Override
	protected void storeFile(InputStream source, String path) throws StorageException {
		storeFile(source, path, false);
	}
	
	@Override
	protected void storeFile(InputStream source, String path, boolean override) throws StorageException {
		File file = new File(baseDir, path);
		
		try {
			// check if file already exists and if it is symbolic link
			if( Files.isSymbolicLink(file.toPath()) )
				file.delete();
			else if( file.exists() ) {
				// file exists and is not a sym link
				if( override == false )
					throw new StorageException( MessageFormat.format("File exists already. Cannot override {0}", file) );
				else
					// override enabled
					file.delete();
			}
			
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
	
	@Override
	protected void linkFiles(String sourcePath, String targetPath) throws StorageException {
		File sourceFile = new File(baseDir, sourcePath);
		File targetFile = new File(baseDir, targetPath);
		
		if( targetFile.exists() == false )
			throw new StorageException("Cannot create link to a non-existing file: " + targetFile);
		
		try {
			try {
				Files.createSymbolicLink( sourceFile.toPath(), targetFile.toPath() );
			} catch (UnsupportedOperationException e1) {
				// file system cannot handle sym links, try hard linking
				log.warn( MessageFormat.format("File system cannot handle symlink from {0} to {1}. Try hard link instead.", sourceFile, targetFile), e1 );
				Files.createLink( sourceFile.toPath(), targetFile.toPath() );
			}
		} catch (IOException e) {
			log.error( MessageFormat.format("Cannot create link from {0} to {1}.", sourceFile, targetFile), e );
			throw new StorageException( MessageFormat.format("Cannot create link from {0} to {1}.", sourceFile, targetFile), e );
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
