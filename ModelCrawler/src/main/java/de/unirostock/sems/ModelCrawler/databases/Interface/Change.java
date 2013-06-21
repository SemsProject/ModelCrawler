package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.io.File;
import java.util.Date;

public abstract class Change {
	
	private Date revisionDate;
	private Date crawledDate;
	private String revisionId;
	
	protected File xmlFile = null;
	
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
	
	public boolean setXmlFile( String xmlFile ) {
		if( this.xmlFile == null ) {
			return setXmlFile( new File(xmlFile) );
		}
		else
			return false;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

	public Date getCrawledDate() {
		return crawledDate;
	}

	public String getRevisionId() {
		return revisionId;
	}

	public Change( Date revisionDate, Date crawledDate, String revisionId ) {
		this.revisionDate = revisionDate;
		this.crawledDate = crawledDate;
		this.revisionId = revisionId;
	}
	
}
