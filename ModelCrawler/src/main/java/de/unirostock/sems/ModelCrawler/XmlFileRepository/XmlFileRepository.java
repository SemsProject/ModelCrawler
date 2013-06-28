package de.unirostock.sems.ModelCrawler.XmlFileRepository;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.Interface.XmlFileServer;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;

public class XmlFileRepository implements XmlFileServer {
	
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
			log.info( MessageFormat.format("Opening  XmlFileRepository at {0}", location.getAbsolutePath()) );
		
	}
	
	/**
	 * Checks if the URI is resolvable by the FileServer. <br>
	 * (Tests scheme and host)
	 * 
	 * @param model
	 * @return
	 */
	public boolean isResolvableUri(URI model) {
		
		if( !model.getScheme().equals( Properties.getProperty("de.unirostock.sems.ModelCrawler.models.uri.scheme") ))
			return false;
		
		if( !model.getHost().equals( Properties.getProperty("de.unirostock.sems.ModelCrawler.models.uri.host") ))
			return false;
		
		return true;
	}
	
	@Override
	public InputStream resolveModelUri(URI model) {
		
		if( model.isAbsolute() != true )
			return null;
		
		if( isResolvableUri(model) != true )
			return null;
		
		String path = model.getPath();
		// matches the path against modelId/versionId scheme
		if( path.matches("[a-BA-B0-9\\-\\.\\_]*\\/[a-BA-B0-9\\-\\.\\_]*") != true )
			return null;
		
		File modelPath = new File( location, model.getPath() );
		
		// TODO
		
		return null;
	}

	@Override
	public boolean exist(URI model) {
		if( getModelPath(model).exists() )
			return true;
		else
			return false;
	}

	@Override
	public URI pushModel(String modelId, String versionId,
			InputStream modelSource) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private File getModelPath(URI model) {
		
		// TODO
		
		return null;
	}

}
