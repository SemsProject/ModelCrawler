package de.unirostock.sems.ModelCrawler.databases.Interface;

import java.io.File;
import java.util.Date;

public abstract class Change {
	
	private Date revisionDate;
	private Date crawledDate;
	private String revisionId;
	
	private File xmlFile = null;
	private String fileHash = null;
	
	public File getXmlFile() {
		return xmlFile;
	}

	public boolean setXmlFile(File xmlFile, String hash) {
		//REMIND the xml file can only be setted once in a Change
		if( this.xmlFile == null ) {
			this.xmlFile = xmlFile;
			this.fileHash = hash;
			return true;
		}
		else
			return false;
	}
	
	public boolean setXmlFile( File xmlFile ) {
		if( this.xmlFile == null )
			return setXmlFile( xmlFile, calcXmlHash(xmlFile) );
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
	
	abstract protected String calcXmlHash( File xmlFile ); 
}
