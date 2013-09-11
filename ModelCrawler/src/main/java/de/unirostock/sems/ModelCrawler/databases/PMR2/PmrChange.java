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

	protected String repositoryUrl = null;
	protected String fileName = null;
	
	public PmrChange(String modelId, String versionId, Date versionDate, Date crawledDate) {
		super(modelId, versionId, versionDate, crawledDate);
	}
	
	public PmrChange( String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( null, versionId, versionDate, crawledDate );
		this.repositoryUrl = repositoryUrl;
		this.fileName = fileName;
		this.modelId = XmlFileRepository.generateModelId(repositoryUrl, fileName);
	}
	
	public PmrChange( String modelId, String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( modelId, versionId, versionDate, crawledDate );
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
		URI uri = server.pushModel(repositoryUrl, fileName, versionId, stream);
		// finally set the document Uri, generated from the XmlFileRepo
		setDocumentUri(uri);
		
		// closes the stream
		stream.close();
		
	}
	
	@Override
	public String toString() {
		return "PmrChg:" + repositoryUrl+":"+fileName+"@"+versionId;
	}
	
}
