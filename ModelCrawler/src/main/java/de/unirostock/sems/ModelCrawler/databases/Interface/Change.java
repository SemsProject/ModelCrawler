package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.databases.Interface.exceptions.XmlNotFoundException;
import de.unirostock.sems.ModelCrawler.helper.CrawledModelRecord;
import de.unirostock.sems.XmlFileServerClient.XmlFileServer;
import de.unirostock.sems.XmlFileServerClient.exceptions.ModelAlreadyExistsException;
import de.unirostock.sems.XmlFileServerClient.exceptions.UnsupportedUriException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerBadRequestException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerProtocollException;

public abstract class Change extends CrawledModelRecord implements Comparable<Change> {
	
	private transient Date versionDate = null;
	private transient Date crawledDate = null;
	
	protected transient File xmlFile = null;
			
	public Change( String fileId, String versionId, Date versionDate, Date crawledDate ) {
		super(fileId, versionId, versionDate, crawledDate);
		this.versionDate = versionDate;
		this.crawledDate = crawledDate;
	}
	
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
		URI uri = server.pushModel(getFileId(), getVersionId(), stream);
		// finally set the document Uri, generated from the XmlFileRepo
		setXmldoc( uri.toString() );
		
		// closes the stream
		stream.close();
		
	}
	
	public File getXmlFile() {
		return xmlFile;
	}
	
	public boolean setXmlFile( File xmlFile ) {
		//REMIND the xml file can only be setted once in a Change
		if( this.xmlFile == null ) {
			this.xmlFile = xmlFile;
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void setVersionDate(Date versionDate) {
		super.setVersionDate(versionDate);
		this.versionDate = versionDate;
	}
	
	@Override
	public Date getVersionDate() {
		if( versionDate == null )
			versionDate = super.getVersionDate();
		
		return versionDate;
	}
	
	@Override
	public void setCrawledDate(Date crawledDate) {
		super.setCrawledDate(crawledDate);
		this.crawledDate = crawledDate;
	}
	
	@Override
	public Date getCrawledDate() {
		if( crawledDate == null )
			crawledDate = super.getCrawledDate();
		
		return crawledDate;
	}
	
	@Override
	public int compareTo( Change change ) {
		return getVersionDate().compareTo( change.getVersionDate() );
	}
	
	@Override
	public String toString() {
		return "Chg:" + getFileId()+"@"+getVersionId();
	}
	
}
