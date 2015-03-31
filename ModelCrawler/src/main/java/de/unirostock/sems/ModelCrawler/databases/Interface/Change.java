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

// TODO: Auto-generated Javadoc
/**
 * The Class Change.
 */
public abstract class Change extends CrawledModelRecord implements Comparable<Change> {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3880353134783045794L;
	
	/** The version date. */
	private transient Date versionDate = null;
	
	/** The crawled date. */
	private transient Date crawledDate = null;
	
	/** The xml file. */
	protected transient File xmlFile = null;
			
	/**
	 * The Constructor.
	 *
	 * @param fileId the file id
	 * @param versionId the version id
	 * @param versionDate the version date
	 * @param crawledDate the crawled date
	 */
	public Change( String fileId, String versionId, Date versionDate, Date crawledDate ) {
		super(fileId, versionId, versionDate, crawledDate);
		this.versionDate = versionDate;
		this.crawledDate = crawledDate;
	}
	
	/**
	 * Push to xml file server.
	 *
	 * @param server the server
	 * @throws XmlNotFoundException the xml not found exception
	 * @throws ModelAlreadyExistsException the model already exists exception
	 * @throws XmlFileServerBadRequestException the xml file server bad request exception
	 * @throws UnsupportedUriException the unsupported uri exception
	 * @throws XmlFileServerProtocollException the xml file server protocoll exception
	 * @throws IOException the IO exception
	 */
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
	
	/**
	 * Gets the xml file.
	 *
	 * @return the xml file
	 */
	public File getXmlFile() {
		return xmlFile;
	}
	
	/**
	 * Sets the xml file.
	 *
	 * @param xmlFile the xml file
	 * @return true, if sets the xml file
	 */
	public boolean setXmlFile( File xmlFile ) {
		//REMIND the xml file can only be setted once in a Change
		if( this.xmlFile == null ) {
			this.xmlFile = xmlFile;
			return true;
		}
		else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.helper.CrawledModelRecord#setVersionDate(java.util.Date)
	 */
	@Override
	public void setVersionDate(Date versionDate) {
		super.setVersionDate(versionDate);
		this.versionDate = versionDate;
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.helper.CrawledModelRecord#getVersionDate()
	 */
	@Override
	public Date getVersionDate() {
		if( versionDate == null )
			versionDate = super.getVersionDate();
		
		return versionDate;
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.helper.CrawledModelRecord#setCrawledDate(java.util.Date)
	 */
	@Override
	public void setCrawledDate(Date crawledDate) {
		super.setCrawledDate(crawledDate);
		this.crawledDate = crawledDate;
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.helper.CrawledModelRecord#getCrawledDate()
	 */
	@Override
	public Date getCrawledDate() {
		if( crawledDate == null )
			crawledDate = super.getCrawledDate();
		
		return crawledDate;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo( Change change ) {
		return getVersionDate().compareTo( change.getVersionDate() );
	}
	
	/* (non-Javadoc)
	 * @see de.unirostock.sems.morre.client.dataholder.CrawledModel#toString()
	 */
	@Override
	public String toString() {
		return "Chg:" + getFileId()+"@"+getVersionId();
	}
	
}
