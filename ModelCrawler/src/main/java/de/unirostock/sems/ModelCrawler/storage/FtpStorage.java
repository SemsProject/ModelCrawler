package de.unirostock.sems.ModelCrawler.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.unirostock.sems.ModelCrawler.Config;
import de.unirostock.sems.ModelCrawler.Constants;
import de.unirostock.sems.ModelCrawler.exceptions.StorageException;

public class FtpStorage extends FileBasedStorage {
	
	@JsonIgnore
	private static final long serialVersionUID = 1221888596087293844L;
	@JsonIgnore
	private final Log log = LogFactory.getLog( FtpStorage.class );
	
	private URL ftpUrl = null;
	private String ftpUser = null;
	private String ftpPass = null;
	
	@JsonIgnore
	private FTPClient ftpClient = null;
	
	public FtpStorage() {
		super();
		// Just the default constructor
	}
	
	@Override
	public void initConnection() throws StorageException {
		
		if( ftpClient != null )
			throw new IllegalStateException("Storage is already connected");
		
		if( ftpUrl == null )
			throw new IllegalArgumentException("FTP URL is not allowed to be null");
		
		if( httpAccessPath == null )
			throw new IllegalArgumentException("HTTP Access Path is not allowed to be null");
		
		try {
			ftpClient = new FTPClient();
			
			// connect to FTP server
			ftpClient.connect(ftpUrl.getHost(), ftpUrl.getPort() == -1 ? Constants.FTP_DEFAULT_PORT
					: ftpUrl.getPort());
			
			// login
			if( ftpUser != null && !ftpUser.isEmpty() && ftpPass != null && !ftpPass.isEmpty() ) {
				// login with credentials
				if( log.isInfoEnabled() )
					log.info("Login using provided FTP credentials");
				
				if( ftpClient.login( ftpUser, ftpPass ) == false )
					throw new StorageException( "Cannot login using provided FTP credentials" );
				
			}
			else {
				// anonymous login
				if( log.isInfoEnabled() )
					log.info("No ftp login credentials provided. Try anonymous login");
				
				if( ftpClient.login( "anonymous", "anonymous" ) == false ) {
					throw new StorageException( "Cannot login with anonymous account!" );
				}
			}
			
			// set FTP Modes
			ftpClient.setFileType( FTP.BINARY_FILE_TYPE );
			ftpClient.enterLocalPassiveMode();
			
			// switch to working directory
			if( ftpClient.changeWorkingDirectory( ftpUrl.getPath() ) == false )
				throw new StorageException( MessageFormat.format("Cannot change FTP working directory to {0}", ftpUrl.getPath()) );
			
		} catch (IOException e) {
			log.error("Cannot connect to FTP server!", e);
			throw new StorageException("Cannot connect to FTP server", e);
		}
	}

	@Override
	public void closeConnection() {
		
		try {
			if( ftpClient != null ) {
				ftpClient.logout();
				ftpClient.disconnect();
				
				ftpClient = null;
			}
		} catch (IOException e) {
			log.error("Cannot close FTP Connection", e);
		}
		
	}
	
	@Override
	protected void makeDirs(String path) throws StorageException {
		
		String pathSeparator = Config.getConfig().getPathSeparatorString();
		
		try {
			// change to base dir
			if( ftpClient.changeWorkingDirectory("/") == false )
				throw new StorageException( "Cannot change to FTP base directory" );
			
			if( path.startsWith(pathSeparator) == false )
				path = pathSeparator + path;
			
			ftpClient.makeDirectory(path);
		} catch (IOException e) {
			throw new StorageException("Cannot create new ftp dir: " + path, e);
		}
		
	}
	
	@Override
	protected void storeFile(InputStream source, String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected InputStream getFile(String path) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void linkFiles(String sourcePath, String targetPath)
			throws StorageException {
		// TODO Auto-generated method stub
		
	}

	// getter/setter
	
	public URL getFtpUrl() {
		return ftpUrl;
	}

	public void setFtpUrl(URL ftpUrl) {
		this.ftpUrl = ftpUrl;
	}

	public String getFtpUser() {
		return ftpUser;
	}

	public void setFtpUser(String ftpUser) {
		this.ftpUser = ftpUser;
	}

	public String getFtpPass() {
		return ftpPass;
	}

	public void setFtpPass(String ftpPass) {
		this.ftpPass = ftpPass;
	}

	@Override
	protected void storeFile(InputStream source, String path, boolean override)
			throws StorageException {
		// TODO Auto-generated method stub
		
	}

}
