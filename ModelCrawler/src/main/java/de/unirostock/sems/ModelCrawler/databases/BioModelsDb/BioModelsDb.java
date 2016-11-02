package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.dump.UnsupportedCompressionAlgorithmException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unirostock.sems.ModelCrawler.Config;
import de.unirostock.sems.ModelCrawler.Config.WorkingMode;
import de.unirostock.sems.ModelCrawler.Constants;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.exceptions.ExtractException;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.exceptions.FtpConnectionException;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.exceptions.StorageException;
import de.unirostock.sems.ModelCrawler.helper.CrawledModelRecord;
import de.unirostock.sems.morre.client.exception.MorreCommunicationException;
import de.unirostock.sems.morre.client.exception.MorreException;

public class BioModelsDb extends ModelDatabase {
	
	private static final long serialVersionUID = -1180005276710581809L;

	@JsonIgnore
	private final Log log = LogFactory.getLog( BioModelsDb.class );
	
	/**
	 * class for settings stored in the working dir
	 * @author martin
	 *
	 */
	private static class WorkingDirConfig {
		
		private HashSet<String> knownReleases = new HashSet<String>();
		
		public HashSet<String> getKnownReleases() {
			return knownReleases;
		}

		@SuppressWarnings("unused")
		public void setKnownReleases(HashSet<String> knownReleases) {
			this.knownReleases = knownReleases;
		}
		
	}
	
	private URL ftpUrl = null;
	
	@JsonIgnore
	protected Map<String, ChangeSet> changeSetMap = new HashMap<String, ChangeSet>();
	@JsonIgnore
	private FTPClient ftpClient;
	@JsonIgnore
	protected WorkingDirConfig config = null;

	private File workingDir;
	
	/**
	 * Default empty constructor
	 * 
	 */
	public BioModelsDb() {}
	
	@Override
	public List<String> listModels() {
		return new ArrayList<String>( changeSetMap.keySet() );
	}

	@Override
	public Map<String, ChangeSet> listChanges() {
		return changeSetMap;
	}
	
	@Override
	public ChangeSet getModelChanges(String fileId) {
		return changeSetMap.get(fileId);
	}

	@Override
	public void close() {
		
		// saving the properties
		saveProperties();
		
		// deletes the tempDir recursively
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			log.error("Error while cleaning up the temp dir!", e);
		}
	}
	
	@Override
	public Map<String, ChangeSet> call() {
		List<BioModelRelease> newReleases = new ArrayList<BioModelRelease>();
		
		if( ftpUrl == null ) {
			log.error("Url for BMDB crawler not set!");
			throw new IllegalArgumentException("Url for BMDB crawler not set!");
		}
		
		if( morreClient == null && Config.getWorkingMode() != WorkingMode.NO_MORRE ) {
			log.error("No Morre crawler interface provided!");
			throw new IllegalArgumentException("No Morre crawler interface provided!");
		}
		
		// check url protocol
		if (!this.ftpUrl.getProtocol().toLowerCase().equals("ftp")) {
			// Protocol is not ftp -> not supported
			log.error("Only ftp is supported at the moment for BioModelsDataBase!");
			throw new IllegalArgumentException("Only ftp is supported at the moment!");
		}

		log.info("Init new BioModels Database connector. URL: " + ftpUrl );
		// creating a ftp client
		ftpClient = new FTPClient();

		// prepares the working directory
		init();
		
		log.info("Start cloning the BioModels DataBase by fetching the releases!");

		// Establish connection
		try {

			connect();
			newReleases = retrieveReleaseList();

		} catch (IOException e) {
			log.fatal( "IOException while connecting and getting the releases!", e );
		} catch (FtpConnectionException e) {
			log.fatal( e );
		}

		if( config.getKnownReleases().size() > 0 ) {
			// there are some releases we already indexed!

			// removing the known releases
			Iterator<BioModelRelease> iter = newReleases.iterator();
			while( iter.hasNext() ) {
				BioModelRelease release = iter.next();
				if( config.getKnownReleases().contains( release.getReleaseName() ) )
					iter.remove();
			}

			if( log.isInfoEnabled() )
				log.info( MessageFormat.format( "{0} new release(s)", newReleases.size() ) );
		}
		else if( log.isInfoEnabled() ) {
			// no releases were indexed before. Sadly now we have process them all
			// on the other hand, we could simply copy the list with all releases now :)
			log.info("every release is a new release...");
		}
		
		// sorting, just in case...
		Collections.sort(newReleases);
		
		// limiting releases
		if( limit > 0 ) {
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("Limit processed Releases to {0}", limit) );
			
			newReleases = newReleases.subList(0, limit);
		}

		// going throw the new release list an downloads every
		for( BioModelRelease release : newReleases ) {
			// do it (download, extract, compare to previous versions)
			processRelease( release );

			// if the download was succesfull, add the release to the known releases
			if( release.isDownloaded() && release.isExtracted() )
				config.getKnownReleases().add( release.getReleaseName() );
		}

		log.info("finished cloning BioModelsDatabase!");
		return changeSetMap;
	}

	/**
	 * Downloads, extracts and indexes the gives release
	 * must called for each new release CHRONOLOGICAL
	 * 
	 * @param release
	 */
	protected void processRelease( BioModelRelease release ) {
		
		// getting the current Date, for crawling TimeStamp
		Date crawledDate = new Date();
		
		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("start processing release {0}", release.getReleaseName()) );
		
		// try to download
		try {
			if( downloadRelease(release) == false ) {
				log.fatal( MessageFormat.format("Can not process release {0}", release.getReleaseName()) );
				return;
			}
		} catch (UnsupportedCompressionAlgorithmException e) {
			log.fatal("Can not download-extract the release! Unsupported CompressionAlgorithm" , e);
			return;
		}

		// try to extract
		try {
			extractRelease(release);
		}
		catch (IllegalArgumentException e) {
			log.fatal("Something went wrong with the release Object! (IllegalArgumentException) ", e);
			return;
		} catch (ExtractException e) {
			log.fatal("Error while extracting", e);
			return;
		}
		
		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("start transfering changes from release {0} into change sets", release.getReleaseName()) );
		
		// transfer the index from release
		Iterator<String> iter = release.getModelList().iterator();
		while( iter.hasNext() ) {
			tranferChange( iter.next(), release, crawledDate );
		}
		
		// clean up
		try {
			FileUtils.deleteDirectory( release.getContentDir() );
		} catch (IOException e) {
			log.warn("Cannot clean up tmp dir for repository", e);
		}
	}

	protected void init() {
		
		workingDir = obtainWorkingDir();
		log.trace( "Preparing working dir " + workingDir.getAbsolutePath() );

		if( workingDir.exists() == false ) {
			// creates it!
			workingDir.mkdirs();
		}
		// create temp dir
		createTempDir();

		try {
			// inits the config
			log.info("Loading working dir config");
			File configFile = new File( workingDir, Config.getConfig().getWorkingDirConfig() );
			if( configFile.exists() )
				config = Config.getObjectMapper().readValue( configFile, WorkingDirConfig.class );
			else
				config = new WorkingDirConfig();
		}
		catch (IOException e) {
			log.fatal( "Exception while reading the workingdir config file", e );
		}

	}

	protected void saveProperties() {

		if( config == null ) {
			config = new WorkingDirConfig();
		}

		try {
			// save the config
			log.info("Saving working dir config");
			
			File configFile = new File( workingDir, Config.getConfig().getWorkingDirConfig() );
			Config.getObjectMapper().writeValue(configFile, config);
			
			log.info("working dir config saved!");
		} catch (IOException e) {
			log.error( "Can not write the workingDir config file!", e );
		}

	}

	protected void connect() throws FtpConnectionException, IOException, SocketException {

		log.info("connecting to ftp server");

		try {

			// connect to FTP Server
			if( log.isTraceEnabled() )
				log.trace("establish socket connection");

			ftpClient.connect(ftpUrl.getHost(), ftpUrl.getPort() == -1 ? Constants.FTP_DEFAULT_PORT
					: ftpUrl.getPort());

			// login in
			if( log.isTraceEnabled() )
				log.trace("logging in");

			if( ftpClient.login( "anonymous", "anonymous" ) == false ) {
				throw new FtpConnectionException("Can not login with anonymous account!");
			}

			// switches to passiv mode
			if( log.isTraceEnabled() )
				log.trace("entering passiv mode");

			ftpClient.enterLocalPassiveMode();
			// set filetype to binary (we should only handle this type of files)
			// DO NOT REMOVE THIS LINE!! ;)
			ftpClient.setFileType( FTP.BINARY_FILE_TYPE );

			// change directory to the release directory
			if( log.isTraceEnabled() )
				log.trace("change directory to release directory");

			if( ftpClient.changeWorkingDirectory(ftpUrl.getPath()) == false ) {
				throw new FtpConnectionException("Can not change directory to release directory!");
			}

		} catch (SocketException e) {
			log.error("Can not connect to ftp server!", e);
			throw e;
		} catch (IOException e) {
			log.fatal("Can not connect to ftp server, IOException", e);
			throw e;
		}

	}

	protected void disconnect() {
		try {
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (IOException e) {
			log.error("Error while disconnecting from ftp server, IOException", e);
		}
	}

	protected List<BioModelRelease> retrieveReleaseList() throws IOException {
		
		List<BioModelRelease> releaseList = new LinkedList<BioModelRelease>();
		// cleares the list
//		releaseList.clear();

		if( log.isInfoEnabled() )
			log.info("retrieving release list form ftp server");

		// check if ftp client is connected
		if( ftpClient.isConnected() == false )
			throw new IOException( "Not connected to the server!" );

		// prepare the date parser
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// retrieve the dir list
		FTPFile[] dirs = ftpClient.listDirectories();
		for( int index = 0; index < dirs.length; index++ ) {
			// if the "file" is not a directory -> jump over
			if( !dirs[index].isDirectory() )
				continue;

			// Getting the TimeStamp of the Release
			Date releaseTimeStamp;
			try {
				// trying to parse it from the folder name, i.e. 2009-03-25
				releaseTimeStamp = dateFormat.parse( dirs[index].getName() );
			} catch (ParseException e) {
				// parser error -> pattern didn't match?
				// take the file timeStamp as fallback.
				releaseTimeStamp = dirs[index].getTimestamp().getTime();
			}

			// create a new release dataset
			BioModelRelease release = new BioModelRelease( dirs[index].getName(),
					ftpUrl.getPath() + dirs[index].getName(), releaseTimeStamp );

			// adding it to the list
			releaseList.add(release);
		}

		// sorting the list after release date ascending
		Collections.sort( releaseList );

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("{0} releases on the server", releaseList.size() ) );

		return releaseList;
	}

	private boolean downloadRelease( BioModelRelease release ) throws UnsupportedCompressionAlgorithmException {
		String archiv;
		File target;
		byte[] buffer = new byte[ 4096 ];

		if( release == null )
			return false;

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format( "Start download release {0} from {1}", release.getReleaseName(), release.getFtpDirectory() ) );

		// if release already downloaded or extracted
		if( release.isDownloaded() || release.isExtracted() ) {
			log.warn( "The release is already download and/or extracted!" );
			return true;
		}

		try {
			// Changes the directory
			if( log.isDebugEnabled() )
				log.debug("changes to release directory");
			ftpClient.changeToParentDirectory();
			ftpClient.changeWorkingDirectory( release.getFtpDirectory() );

			// Finding the right file to download
			if( log.isDebugEnabled() )
				log.debug("trying to find the smbl only file");

			if( (archiv = findSbmlArchivFile()) == null ) {
				log.error("No matching file found!");
				return false;
			}
			
			if( log.isDebugEnabled() )
				log.debug( MessageFormat.format("found sbml file: {0}", archiv) );

			// Creating a TempFile and open OutputStream
			target = new File( tempDir, "BioModelsDb_" + release.getReleaseName() + ".tar" );
			//target = File.createTempFile( "BioModelsDb_" + release.getReleaseName() + "_", ".tar" );
			BufferedOutputStream targetStream = new BufferedOutputStream( new FileOutputStream(target) );

			if( log.isDebugEnabled() )
				log.debug( MessageFormat.format("download and uncompress {0} to {1}", archiv, target.getAbsolutePath() ));

			// download it...
			InputStream downStream = ftpClient.retrieveFileStream(archiv);

			// do the uncompress
			InputStream uncompressedStream;
			if( archiv.endsWith(".gz") ) {
				// use gzip uncompression
				uncompressedStream = new GzipCompressorInputStream(downStream);
				if( log.isTraceEnabled() )
					log.trace("using gzip");
			}
			else if( archiv.endsWith(".bzip") || archiv.endsWith(".bz") || archiv.endsWith(".bzip2") || archiv.endsWith(".bz2") ) {
				// use bzip2 uncompression
				uncompressedStream = new BZip2CompressorInputStream(downStream);
				if( log.isTraceEnabled() )
					log.trace("using bzip");
			}
			else if( archiv.endsWith(".tar") ) {
				// uncompressed tar-ball
				uncompressedStream = downStream;
				if( log.isTraceEnabled() )
					log.trace("no compression, just a simple tar ball");
			}
			else {
				targetStream.close();
				throw new UnsupportedCompressionAlgorithmException( "Unknown file extension!" );
			}

			// do it...
			int total = 0, red = 0;
			while( (red = uncompressedStream.read(buffer)) != -1 ) {
				targetStream.write(buffer, 0, red);
				total = total + red;
//				System.out.println( MessageFormat.format("{0} ({1})", total, red) );
			}

			// close the output Stream
			targetStream.flush();
			targetStream.close();

			// close the input Stream
			downStream.close();

			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("download complete, {0} bytes", total) );

			if( ftpClient.completePendingCommand() == false ) {
				// file transfer was not successful!
				//				target.delete();
				return false;
			}

			// successful!
			// setting position of tar-ball in Release DataHolder
			release.setArchivFile(target);			

		}
		catch (UnsupportedCompressionAlgorithmException e) {
			log.error("Can not uncompress the release! Unsupported Compression Algo!", e);
			return false;
		}
		catch (IOException e) {
			log.error("IOException while downloading and extracting the release!", e);
			return false;
		}

		return true;
	}

	private String findSbmlArchivFile() throws IOException {

		FTPFile[] files = ftpClient.listFiles();
		for( int index = 0; index < files.length; index++ ) {

			// matching the filename
			// ^BioModels_Database-r{[0-9]*}_pub-sbml_files\\.tar \\.bz2$
			// simple it ends with sbml file declaration and an archiv file extension
			//if( files[index].getName().endsWith("sbml_files.tar.bz2") == true ) {
			if( files[index].getName().contains("sbml_file") == true ) {
				//System.out.println( files[index].getSize() );
				return files[index].getName();
			}
		}

		return null;
	}

	private void extractRelease( BioModelRelease release ) throws IllegalArgumentException, ExtractException {

		// already extracted or not even downloaded - just for safety... 
		if( !release.isDownloaded() || release.isExtracted() )
			throw new IllegalArgumentException("The release is suposed to be downloaded and not extracted!");

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Start extracting release {0}", release.getReleaseName()) );

		// Map for Biomodel-Files
		// Key = fileId, Value = Path in ContentDirectory
		Map<String, File> fileMap = new HashMap<String, File>();

		// extract dir
		File contentDir = new File(tempDir, release.getReleaseName() );
		contentDir.mkdirs();	// creates content dir in temp dir

		try {
			if( log.isDebugEnabled() )
				log.debug("Opening archive");

			// opens Archivfile for reading
			InputStream tarFileStream = new FileInputStream( release.getArchivFile() );
			// creates InputStream for uncompressing - Format is set to tar
			// ArchiveInputStream archivStream = (ArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream(tarFileStream);
			ArchiveInputStream archivStream = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.TAR, tarFileStream);

			ArchiveEntry entry = null;

			if( log.isDebugEnabled() )
				log.debug("extract entries!");

			// looping throw entries
			while ( (entry = archivStream.getNextEntry()) != null ) {
				File entryFile = new File( contentDir, entry.getName() );

				if( entry.isDirectory() ) {
					// directory

					if( log.isTraceEnabled() )
						log.trace( MessageFormat.format("Extract directory {0}", entryFile.getAbsolutePath() ));

					if( !entryFile.exists() ) {
						// directroy does not exists
						if( !entryFile.mkdirs() )
							throw new IllegalStateException("Can not create directory " + entryFile.getAbsolutePath() );
					}
				}
				else {
					// file

					if( log.isTraceEnabled() )
						log.trace( MessageFormat.format("Extract file {0}", entryFile.getAbsolutePath() ));
					
					File parentDir = entryFile.getAbsoluteFile().getParentFile();
					if( !parentDir.exists() ) {
						// directory does not exists
						if( log.isDebugEnabled() )
							log.debug( MessageFormat.format("Create directory {0} to store file {1}", parentDir, entryFile) );
						
						if( !parentDir.mkdirs() )
							throw new IllegalStateException("Can not create directory " + parentDir.getAbsolutePath() );
					}

					OutputStream entryStream = new FileOutputStream(entryFile);
					IOUtils.copy(archivStream, entryStream);
					entryStream.close();

					// add file to fileMap 
					// in BioModelsDB the filename is the fileId + .xml
					String fileName = entryFile.getName();
					int extensionPos = fileName.lastIndexOf('.');
					if( fileName.substring(extensionPos+1).toLowerCase().equals("xml") ) {

						if( log.isDebugEnabled() )
							log.debug( MessageFormat.format("Found model {0} from file {1}", fileName, fileName) );

						// put it in the map
						fileMap.put(fileName, entryFile);
					}
				}

				// looping
			}

			// set the result into the release
			release.setContentDir(contentDir, fileMap);
		}
		catch (IllegalStateException e) {
			log.error( e.getMessage() );
			throw new ExtractException(e);
		} catch (ArchiveException e) {
			String message = MessageFormat.format("ArchiveException while extracting release {0}", release.getReleaseName());
			log.error( message );
			throw new ExtractException(message, e);
		} catch (IOException e) {
			String message = MessageFormat.format("IOException while extracting release {0}", release.getReleaseName());
			log.error( message );
			throw new ExtractException(message, e);
		} 
	}

	private void tranferChange( String fileName, BioModelRelease release, Date crawledDate ) {

		boolean isChangeNew = false;
		URL repositoryUrl = null;
		
		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Check if model {0} from release {1} is a new change", fileName, release.getReleaseName()) );
		
		BioModelsChangeSet changeSet = null;
		if( changeSetMap.containsKey(fileName) ) {
			// if fileId is already known -> get it from changeSetMap
			changeSet = (BioModelsChangeSet) changeSetMap.get(fileName);
			if( log.isDebugEnabled() )
				log.debug("ChangeSet exists, fileId is not unknown!");
		}
		
		BioModelsChange change = null;
		try {
			// emulate repository Url
			String filePath = ftpUrl.getFile();
			if( filePath.endsWith( String.valueOf(Config.getConfig().getPathSeparator()) ) )
				filePath = filePath + fileName;
			else
				filePath = filePath + String.valueOf(Config.getConfig().getPathSeparator()) + fileName;
			
			repositoryUrl = new URL( ftpUrl.getProtocol(), ftpUrl.getHost(), filePath );
			
			// create the Change-Entry
			change = new BioModelsChange(repositoryUrl, fileName, release.getReleaseName(), release.getReleaseDate(), crawledDate);
			
			// sets soure meta information
			change.setMeta(CrawledModelRecord.META_SOURCE, CrawledModelRecord.SOURCE_BIOMODELS_DB);
			change.setModelType( CrawledModelRecord.TYPE_SBML );
			
			// set up the xml file and calc the hash
			change.setXmlFile( release.getModelPath(fileName) );
			if( log.isTraceEnabled() )
				log.trace( MessageFormat.format("calced file hash: {0}", change.getHash()) );
			
		} catch (MalformedURLException | URISyntaxException e) {
			// TODO
		}
		
		// --------
		// try to find some parents for this model.
		// is a changeSet for this model available?
		if( changeSet != null ) {
			// yes -> compare hashes from current and latest
			
			if( log.isTraceEnabled() )
				log.trace("compare hash with latest from changeSet");
			
			BioModelsChange latest = ((BioModelsChange) changeSet.getLatestChange());
			// null check
			if( latest != null ) {
				// compare hashes and checks if the "latest" version is older than the processing change
				if( change.getHash().equals(latest.getHash()) == false && latest.getVersionDate().compareTo( change.getVersionDate() ) < 0 ) {
					isChangeNew = true;
					// add the parent
					change.addParent( latest.getFileId(), latest.getVersionId() );
					if( log.isInfoEnabled() )
						log.info("hashs are not equal -> new version");
				}
			}
		}
		else {
			// no changeSet available...
			
			if( log.isDebugEnabled() )
				log.debug("ChangeSet does not exists, checking database");
			
			// ... creates one ...
			changeSet = new BioModelsChangeSet(repositoryUrl, fileName);
			// only put this in the map, if there is a latest version anywhere

			// if GraphDb is available for this instance
			if( morreClient != null && Config.getWorkingMode() != WorkingMode.NO_MORRE ) {

				// TODO cache the result of the latest request!
				
				if( log.isTraceEnabled() )
					log.trace("start checking database");
				
				// try to get the latest version of this model
				CrawledModelRecord latest = null;
				try {
					latest = CrawledModelRecord.extendDataholder( morreClient.getLatestModelVersion(fileName) );
				} catch (MorreCommunicationException e) {
					log.fatal("Getting latest model version, to check, if processed model version is new, failed", e);
				} catch (MorreException e) {
					// error occures, when fileId is unknown to the database -> so we can assume the change is new!
					log.warn("GraphDatabaseError while checking, if processed model version is new. It will be assumed, that this is unknown to the database!", e);
					// set no parent
					isChangeNew = true;
				}
				
				if( latest == null || !latest.isAvailable() ) {
					// there is no latest version, neither in the database than in the local changeset
					isChangeNew = true;
				}
				else {
					// latest model available
					
					if( log.isTraceEnabled() )
						log.trace("successfully received latest from database");
					
					String latestHash = latest.getMeta(BioModelsChange.META_HASH);
					if( latestHash == null ) {
						// TODO
						log.error("There is no hash in the latest model. Maybe the database is inconsistent.");
						isChangeNew = true;
					}
					// if hash exists
					// compare hashes and checks if the "latest" version is older than the processing change
					else if( latestHash.equals( change.getHash() ) == false && latest.getVersionDate().compareTo( change.getVersionDate() ) < 0 ) {
						isChangeNew = true;
						// set parent
						change.addParent( latest.getFileId(), latest.getVersionId() );
						
						if( log.isInfoEnabled() )
							log.info("hashs are not equal -> new version");
					}
				}
			}
			else if( Config.getWorkingMode() == WorkingMode.NO_MORRE ) {
				// without morre, we can just assume its new. May cause doubles
				isChangeNew = true;
			}

		}

		if( isChangeNew )  {
			
			// pushes the model to the storage
			try {
				URI modelUri = modelStorage.storeModel(change);
				change.setXmldoc( modelUri.toString() );
			} catch (StorageException e) {
				log.fatal("Error while storing model. Abort processing current change.", e);
				return;
			}
			
			// pushs it into changeSet
			changeSet.addChange(change);
			if( !changeSetMap.containsKey(fileName) ) {
				// make this changeset public!
				changeSetMap.put(fileName, changeSet);
			}
			
			if( log.isDebugEnabled() )
				log.debug("put new version into change set");
		} else if( log.isDebugEnabled() ) {
			log.debug("not a new version of model");
		}
			

	}

	public URL getFtpUrl() {
		return ftpUrl;
	}

	public void setFtpUrl(URL ftpUrl) {
		this.ftpUrl = ftpUrl;
	}
	
}
