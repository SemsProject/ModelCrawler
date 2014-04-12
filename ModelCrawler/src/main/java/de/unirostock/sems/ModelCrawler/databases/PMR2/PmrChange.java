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

public class PmrChange extends Change {

	private static final long serialVersionUID = 4740459688628719898L;
	protected transient String repositoryUrl = null;
	protected transient String fileName = null;
	
	public PmrChange(String fileId, String versionId, Date versionDate, Date crawledDate) {
		super(fileId, versionId, versionDate, crawledDate);
	}
	
	public PmrChange( String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( null, versionId, versionDate, crawledDate );
		this.repositoryUrl = repositoryUrl;
		this.fileName = fileName;
		setFileId( XmlFileRepository.generateFileId(repositoryUrl, fileName) );
	}
	
	public PmrChange( String fileId, String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( fileId, versionId, versionDate, crawledDate );
		this.repositoryUrl = repositoryUrl;
		this.fileName = fileName;
	}
	
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
	
	@Override
	public String toString() {
		return "PmrChg:" + repositoryUrl+":"+fileName+"@"+getVersionId();
	}
	
}
