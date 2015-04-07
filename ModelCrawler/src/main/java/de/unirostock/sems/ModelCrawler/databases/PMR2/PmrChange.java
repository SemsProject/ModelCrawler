package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.exceptions.XmlNotFoundException;
import de.unirostock.sems.XmlFileServerClient.XmlFileServer;
import de.unirostock.sems.XmlFileServerClient.exceptions.ModelAlreadyExistsException;
import de.unirostock.sems.XmlFileServerClient.exceptions.UnsupportedUriException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerBadRequestException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerProtocollException;

public class PmrChange extends Change {

	private static final long serialVersionUID = 4740459688628719898L;
	
	public PmrChange( URL repositoryUrl, String filePath, String versionId, Date versionDate, Date crawledDate ) throws URISyntaxException {
		super( repositoryUrl, filePath, versionId, versionDate, crawledDate );
	}
	
	public PmrChange( String repositoryUrl, String filePath, String versionId, Date versionDate, Date crawledDate ) throws MalformedURLException, URISyntaxException {
		this( new URL(repositoryUrl), filePath, versionId, versionDate, crawledDate );
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
		URI uri = server.pushModel(repositoryUrl.toString(), filePath, getVersionId(), stream);
		// finally set the document Uri, generated from the XmlFileRepo
		setXmldoc( uri.toString() );
		
		// closes the stream
		stream.close();
		
	}
	
	@Override
	public String toString() {
		return "PmrChg:" + getFileId()+"@"+getVersionId();
	}
	
}
