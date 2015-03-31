package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.XmlFileRepository.XmlFileRepository;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.exceptions.XmlNotFoundException;
import de.unirostock.sems.XmlFileServerClient.XmlFileServer;
import de.unirostock.sems.XmlFileServerClient.exceptions.ModelAlreadyExistsException;
import de.unirostock.sems.XmlFileServerClient.exceptions.UnsupportedUriException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerBadRequestException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerProtocollException;

// TODO: Auto-generated Javadoc
/**
 * The Class PmrChange.
 */
public class PmrChange extends Change {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4740459688628719898L;
	
	/** The repository url. */
	protected transient String repositoryUrl = null;
	
	/** The file name. */
	protected transient String fileName = null;
	
	/**
	 * The Constructor.
	 *
	 * @param fileId the file id
	 * @param versionId the version id
	 * @param versionDate the version date
	 * @param crawledDate the crawled date
	 */
	public PmrChange(String fileId, String versionId, Date versionDate, Date crawledDate) {
		super(fileId, versionId, versionDate, crawledDate);
	}
	
	/**
	 * The Constructor.
	 *
	 * @param repositoryUrl the repository url
	 * @param fileName the file name
	 * @param versionId the version id
	 * @param versionDate the version date
	 * @param crawledDate the crawled date
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public PmrChange( String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( null, versionId, versionDate, crawledDate );
		this.repositoryUrl = repositoryUrl;
		this.fileName = fileName;
		setFileId( XmlFileRepository.generateFileId(repositoryUrl, fileName) );
	}
	
	/**
	 * The Constructor.
	 *
	 * @param fileId the file id
	 * @param repositoryUrl the repository url
	 * @param fileName the file name
	 * @param versionId the version id
	 * @param versionDate the version date
	 * @param crawledDate the crawled date
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public PmrChange( String fileId, String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( fileId, versionId, versionDate, crawledDate );
		this.repositoryUrl = repositoryUrl;
		this.fileName = fileName;
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.Change#pushToXmlFileServer(de.unirostock.sems.XmlFileServerClient.XmlFileServer)
	 */
	@Override
	public void pushToXmlFileServer( XmlFileServer server ) throws XmlNotFoundException, ModelAlreadyExistsException, XmlFileServerBadRequestException, UnsupportedUriException, XmlFileServerProtocollException, IOException {
		if( xmlFile == null )
			throw new XmlNotFoundException("XmlFile is not set!");
		
		if( !xmlFile.exists() || !xmlFile.isFile() )
			throw new XmlNotFoundException("xmlFile does not exists or is no file!");
		
		if( server == null )
			throw new IllegalArgumentException("XmlFileServer can not be null!");
		
		InputStream stream = null;
		
		stream = new FileInputStream(xmlFile);
			
		// do it!
		URI uri = server.pushModel(repositoryUrl, fileName, getVersionId(), stream);
		// finally set the document Uri, generated from the XmlFileRepo
		setXmldoc( uri.toString() );
		
		// closes the stream
		stream.close();
		
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.Change#toString()
	 */
	@Override
	public String toString() {
		return "PmrChg:" + repositoryUrl+":"+fileName+"@"+getVersionId();
	}
	
}
