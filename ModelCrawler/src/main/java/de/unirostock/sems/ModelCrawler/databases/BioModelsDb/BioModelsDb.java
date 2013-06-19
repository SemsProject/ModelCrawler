package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.dump.UnsupportedCompressionAlgorithmException;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.Util;

public class BioModelsDb {

	private URL ftpUrl;
	private FTPClient ftpClient;
	private List<BioModelRelease> releaseList = new ArrayList<BioModelRelease>();

	public BioModelsDb(String ftpUrl) throws MalformedURLException,
	IllegalArgumentException {
		this.ftpUrl = new URL(ftpUrl);

		if (!this.ftpUrl.getProtocol().toLowerCase().equals("ftp")) {
			// Protocoll is not ftp -> not (yet) supported
			throw new IllegalArgumentException(
					"Only ftp ist support at the moment!");
		}

		ftpClient = new FTPClient();
	}

	public boolean connect() {

		try {

			// connect to FTP Server
			ftpClient.connect(ftpUrl.getHost(), ftpUrl.getPort() == -1 ? 21
					: ftpUrl.getPort());

			// login in
			if( ftpClient.login( "anonymous", "anonymous" ) == false ) {
				// TODO throwing exception
				return false;
			}

			// switches to passiv mode
			ftpClient.enterLocalPassiveMode();
			// set filetype to binary (we should only handle this type of files)
			// DO NOT REMOVE THIS LINE!! ;)
			ftpClient.setFileType( FTP.BINARY_FILE_TYPE );

			// change directory to the release directory
			if( ftpClient.changeWorkingDirectory(ftpUrl.getPath()) == false ) {
				// TODO throwing exception
				return false;
			}

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		return false;
	}

	public void disconnect() {
		try {
			ftpClient.logout();
			ftpClient.disconnect();
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}

	public List<BioModelRelease> retrieveReleaseList() throws IOException {

		// cleares the list
		releaseList.clear();

		// check if ftp client is connected
		if( ftpClient.isConnected() == false )
			throw new IOException( "Not connected to the server!" );

		// prepare the date parser
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyy-mm-dd");

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
		//		Collections.sort( releaseList );

		return releaseList;
	}

	public boolean downloadRelease( BioModelRelease release ) throws UnsupportedCompressionAlgorithmException {
		String archiv;
		File target;
		byte[] buffer = new byte[ 4096 ];

		if( release == null )
			return false;

		try {
			// Changes the directory
			ftpClient.changeToParentDirectory();
			ftpClient.changeWorkingDirectory( release.getFtpDirectory() );

			// Finding the right file to download
			if( (archiv = findSbmlArchivFile()) == null ) {
				// TODO no matching file found
				return false;
			}

			// Creating a TempFile and open OutputStream
			target = File.createTempFile( "BioModelsDb_" + release.getReleaseName() + "_", ".tar" );
			BufferedOutputStream targetStream = new BufferedOutputStream( new FileOutputStream(target) );

			// download it...
			InputStream downStream = ftpClient.retrieveFileStream(archiv);

			// do the uncompress
			InputStream uncompressedStream;
			if( archiv.endsWith(".gz") ) {
				// use gzip uncompression
				uncompressedStream = new GzipCompressorInputStream(downStream);
			}
			else if( archiv.endsWith(".bzip") || archiv.endsWith(".bz") || archiv.endsWith(".bzip2") || archiv.endsWith(".bz2") ) {
				// use bzip2 uncompression
				uncompressedStream = new BZip2CompressorInputStream(downStream);
			}
			else if( archiv.endsWith(".tar") ) {
				// uncompressed tar-ball
				uncompressedStream = downStream;
			}
			else {
				targetStream.close();
				throw new UnsupportedCompressionAlgorithmException( "Unknown compression format!" );
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
			throw e;
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
