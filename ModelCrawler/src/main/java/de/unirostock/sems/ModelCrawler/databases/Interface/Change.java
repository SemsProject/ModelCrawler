package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.GraphDb.ModelRecord;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.XmlFileRepository;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;
import de.unirostock.sems.ModelCrawler.databases.Interface.exceptions.XmlNotFoundException;

public abstract class Change extends ModelRecord implements Comparable<Change> {
	
	private Date versionDate = null;
	private Date crawledDate = null;
	
	protected File xmlFile = null;
			
	public Change( String modelId, String versionId, Date versionDate, Date crawledDate ) {
		super(modelId, versionId, versionDate, crawledDate);
		this.versionDate = versionDate;
		this.crawledDate = crawledDate;
	}
	
	public void pushToXmlFileServer() throws XmlNotFoundException, UnsupportedUriException {
		
		if( xmlFile == null )
			throw new XmlNotFoundException("XmlFile is not set!");
		
		if( !xmlFile.exists() || !xmlFile.isFile() )
			throw new XmlNotFoundException("xmlFile does not exists or is no file!");
		
		InputStream stream = null;
		
		try {
			stream = new FileInputStream(xmlFile);
			URI uri = XmlFileRepository.getInstance().pushModel(modelId, versionId, stream);
			// finally set the document Uri, generated from the XmlFileRepo
			setDocumentUri(uri);
			// closes the stream
			stream.close();
			
		} catch (FileNotFoundException e) {
			throw new XmlNotFoundException("Can not open InputStream", e);
		} catch (IOException e) {
			throw new XmlNotFoundException("Can not read XmlFile", e);
		}
		
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
		return "Chg:" + modelId+"@"+versionId;
	}
	
}
