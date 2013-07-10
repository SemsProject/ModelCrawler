package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.dump.UnsupportedCompressionAlgorithmException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.exceptions.FtpConnectionException;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;

public class BioModelsDb implements ModelDatabase {

	private final Log log = LogFactory.getLog( BioModelsDb.class );
	
	private URL ftpUrl;
	private FTPClient ftpClient;
	private List<BioModelRelease> releaseList = new ArrayList<BioModelRelease>();

	protected File workingDir, tempDir;
	protected java.util.Properties config;
	
	protected Map<String, BioModelsChangeSet> changeSetMap = new HashMap<String, BioModelsChangeSet>();

	public BioModelsDb(String ftpUrl) throws MalformedURLException,
	IllegalArgumentException {
		this.ftpUrl = new URL(ftpUrl);

		if (!this.ftpUrl.getProtocol().toLowerCase().equals("ftp")) {
			// Protocoll is not ftp -> not (yet) supported
			log.error("Only ftp is support at the moment for BioModelsDataBase!");
			throw new IllegalArgumentException(
					"Only ftp ist support at the moment!");
		}

		log.info("Init new BioModels Database connector. URL: " + ftpUrl );
		// creating a ftp client
		ftpClient = new FTPClient();

		// prepares the working directory
		checkAndInitWorkingDir();

	}

	public BioModelsDb() throws MalformedURLException, IllegalArgumentException {
		this( Properties.getProperty("de.unirostock.sems.ModelCrawler.BioModelsDb.ftpUrl") );
	}

	@Override
	public List<String> listModels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ChangeSet> listChanges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeSet getModelChanges(String modelId) {
		return changeSetMap.get(modelId);
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		List<BioModelRelease> newReleases = new ArrayList<BioModelRelease>();

		log.info("Start cloning the BioModels DataBase by fetching the releases!");

		// Establish connection
		try {

			connect();
			retrieveReleaseList();

		} catch (IOException e) {
			log.fatal( "IOException while connecting and getting the releases!", e );
		} catch (FtpConnectionException e) {
			log.fatal( e );
		}

		if( config.getProperty("knownReleases", null) != null ) {
			// there are some releases we already indexed!
			List<String> knownReleases = Arrays.asList( config.getProperty("knownReleases", "").split(",") );

			// getting only the new releases
			Iterator<BioModelRelease> iter = releaseList.iterator();
			while( iter.hasNext() ) {
				BioModelRelease release = iter.next();
				if( knownReleases.contains( release.getReleaseName() ) == false ) {
					// the release is new and must be downloaded
					newReleases.add(release);
				}
			}

			if( log.isInfoEnabled() )
				log.info( MessageFormat.format( "{0} new release(s)", newReleases.size() ) );
		}
		else {
			// no releases were indexed before. Sadly now we have indexed them all
			// on the other hand, we could simply copy the list with all release now :)
			newReleases.addAll(releaseList);
			if( log.isInfoEnabled() )
				log.info("every release is a new release...");
		}

		// sorting, just in case...
		Collections.sort(newReleases);

		// TODO download the unkwnow releases
		// going throw the new release list an downloads every
		Iterator<BioModelRelease> iter = newReleases.iterator();
		while( iter.hasNext() ) {
			BioModelRelease release = iter.next();
			// do it (download, extract, compare to previous versions)
			processRelease( release );
			
			// if the download was succesfull, add the release to the known releases
			//XXX should I add it already here to the knownRelease list??
			if( release.isDownloaded() && release.isExtracted() )
				config.setProperty( "knownReleases", config.getProperty("knownReleases", "") + "," + release.getReleaseName() );
		}



		// saving the properties
		saveProperties();
	}

	public List<BioModelRelease> getBioModelReleases() {
		return releaseList;
	}
	
	
	/**
	 * Downloads, extracts and indexes the gives release
	 * must called for each new release CHRONOLOGICAL
	 * 
	 * @param release
	 */
	protected void processRelease( BioModelRelease release ) {
		
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
		
		//TODO extract tar archiv
		// see TarArchiveOutputStream
	}
	
	protected void checkAndInitWorkingDir() {

		workingDir = new File( Properties.getWorkingDir(), Properties.getProperty("de.unirostock.sems.ModelCrawler.BioModelsDb.subWorkingDir") );
		tempDir = new File( workingDir, Properties.getProperty("de.unirostock.sems.ModelCrawler.BioModelsDb.subTempDir") );

		log.trace( "Preparing working dir " + workingDir.getAbsolutePath() );

		if( workingDir.exists() == false ) {
			// creates it!
			workingDir.mkdirs();
		}
		if( tempDir.exists() == false ) {
			// creates it!
			tempDir.mkdirs();
		}

		// inits the config
		config = new java.util.Properties();
		log.info("Loading working dir config");
		try {
			File configFile = new File( workingDir, Properties.getProperty("", "config.properties") );
			if( configFile.exists() ) {
				FileReader configFileReader = new FileReader( configFile );
				if( configFileReader != null ) {
					config.load(configFileReader);
					configFileReader.close();
				}

			}

		}
		catch (IOException e) {
			log.fatal( "IOException while reading the workingdir config file", e );
		}

	}

	protected void saveProperties() {

		if( config == null ) {
			config = new java.util.Properties();
		}

		try {
			FileWriter configFile = new FileWriter( new File( workingDir, Properties.getProperty("", "config.properties") ));
			config.store(configFile, null);
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

			ftpClient.connect(ftpUrl.getHost(), ftpUrl.getPort() == -1 ? 21
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

		// cleares the list
		releaseList.clear();

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

	protected boolean downloadRelease( BioModelRelease release ) throws UnsupportedCompressionAlgorithmException {
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
			if( log.isInfoEnabled() )
				log.info("changes to release directory");
			ftpClient.changeToParentDirectory();
			ftpClient.changeWorkingDirectory( release.getFtpDirectory() );

			// Finding the right file to download
			if( log.isInfoEnabled() )
				log.info("trying to find the smbl only file");
			
			if( (archiv = findSbmlArchivFile()) == null ) {
				log.error("No matching file found!");
				return false;
			}

			// Creating a TempFile and open OutputStream
			target = new File( tempDir, "BioModelsDb_" + release.getReleaseName() + ".tar" );
			//target = File.createTempFile( "BioModelsDb_" + release.getReleaseName() + "_", ".tar" );
			BufferedOutputStream targetStream = new BufferedOutputStream( new FileOutputStream(target) );
			
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("download and extract {0} to {1}", archiv, target.getAbsolutePath() ));
			
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
				System.out.println( MessageFormat.format("{0} ({1})", total, red) );
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
				System.out.println( files[index].getSize() );
				return files[index].getName();
			}
		}

		return null;
	}

}
