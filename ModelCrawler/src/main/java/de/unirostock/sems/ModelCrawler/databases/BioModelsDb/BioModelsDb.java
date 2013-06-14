package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

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
				return false;
			}
			
			// change directory to the release directory
			if( ftpClient.changeWorkingDirectory(ftpUrl.getPath()) == false ) {
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
		Collections.sort( releaseList );
		
		return releaseList;
	}
}
