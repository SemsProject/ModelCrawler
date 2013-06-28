package de.unirostock.sems.ModelCrawler.XmlFileRepository;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;

public class XmlFileRepository {
	
	private static XmlFileRepository xmlFileRepository = null;
	
	/**
	 * Gets or creates the instance of the XmlFileRepository
	 * @return
	 */
	public static XmlFileRepository getInstance() {
		if( xmlFileRepository == null )
			xmlFileRepository = new XmlFileRepository();
		
		return xmlFileRepository;
	}
	
	private final Log log = LogFactory.getLog( BioModelsDb.class );
	File location = null;
	
	/**
	 * Creates a new XmlFileRepository on the configured location<br>
	 * Please use {@link XmlFileRepository.getInstance getInstance} instead!
	 * 
	 */
	public XmlFileRepository() {
		
		// creates it at the presetted location!
		this( Properties.getProperty("de.unirostock.sems.ModelCrawler.xmlFileRepo") );
		
	}
	
	/**
	 * Creates a new XmlFileRepository<br>
	 * Please use {@link XmlFileRepository.getInstance getInstance} instead!
	 * 
	 */
	public XmlFileRepository( String locationStr ) {
		
		location = new File(locationStr);
		
		// creates the directory if necessary
		if( !location.exists() || !location.isDirectory() ) {
			if( location.mkdirs() == false )
				log.fatal( MessageFormat.format("Can not create directory {0} for XmlFileRepository", location.getAbsolutePath()) );
		}
		
		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Openining  XmlFileRepository at {0}", location.getAbsolutePath()) );
		
		
		
	}

}
