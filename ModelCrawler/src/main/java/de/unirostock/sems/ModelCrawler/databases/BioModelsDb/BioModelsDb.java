package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;


public class BioModelsDb {
	
	private URL ftpUrl;
	private FTPClient ftpClient;
	private List<BioModelRelease> releaseList = new ArrayList<BioModelRelease>();
	
	public BioModelsDb( String ftpUrl ) throws MalformedURLException, IllegalArgumentException {
		this.ftpUrl = new URL(ftpUrl);
		
		if( !this.ftpUrl.getProtocol().toLowerCase().equals("ftp") ) {
			// Protocoll is not ftp -> not (yet) supported
			throw new IllegalArgumentException( "Only ftp ist support at the moment!" );
		}
		
		ftpClient = new FTPClient();
	}
	
	public boolean connect() {
		
		try {
			
			// connect to FTP Server
			ftpClient.connect( ftpUrl.getHost(), ftpUrl.getPort() == -1 ? 21 : ftpUrl.getPort() );
			// change directory to the release directory
			ftpClient.changeWorkingDirectory( ftpUrl.getPath() );
			
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
	
	public List<BioModelRelease> retrieveReleaseList() throws IOException {
		
		// cleares the list
		releaseList.clear();
		
		if( ftpClient.isConnected() == false )
			throw new IOException( "Not connected to the server!" );
		
		// retrieve the dir list
		FTPFile[] dirs = ftpClient.listDirectories();
		for( int index = 0; index < dirs.length; index++ ) {
			// if the "file" not a directory -> jump over
			if( !dirs[index].isDirectory() )
				continue;
			
			BioModelRelease release = new BioModelRelease( dirs[index].getName(),
					ftpUrl.getPath() + dirs[index].getName(), dirs[index].getTimestamp().getTime() );
			releaseList.add(release);
		}
		
		return releaseList;
	}
	
}
