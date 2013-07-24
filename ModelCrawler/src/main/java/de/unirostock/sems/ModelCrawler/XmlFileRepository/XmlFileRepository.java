package de.unirostock.sems.ModelCrawler.XmlFileRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.io.Util;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.Interface.XmlFileServer;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;

public class XmlFileRepository implements XmlFileServer {
	
	//TODO 2 -> more exceptions!?
	
	private static XmlFileRepository xmlFileRepository = null;
	private final Log log = LogFactory.getLog( XmlFileRepository.class );
	private File location = null;
	
	/**
	 * Gets or creates the instance of the XmlFileRepository
	 * @return
	 */
	public static XmlFileRepository getInstance() {
		if( xmlFileRepository == null )
			xmlFileRepository = new XmlFileRepository();
		
		return xmlFileRepository;
	}
	
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
		
		// set this as instance!
//		if( XmlFileRepository.xmlFileRepository == null )
//			XmlFileRepository.xmlFileRepository = this;
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
	public InputStream resolveModelUri(URI model) throws FileNotFoundException, UnsupportedUriException {
		
		if( model.isAbsolute() != true )
			throw new UnsupportedUriException( MessageFormat.format("URI is not absolute. {0}", model.toString()) );
		
		if( isResolvableUri(model) != true )
			throw new UnsupportedUriException( MessageFormat.format("URI can not resolved by this server. {0}", model.toString()) );

		File modelPath = getModelPath(model);
		
		return new FileInputStream(modelPath);
	}

	@Override
	public boolean exist(URI model) {
		if( isResolvableUri(model) == false )
			return false;
		
		if( getModelPath(model).exists() )
			return true;
		else
			return false;
	}

	@Override
	public URI pushModel(String modelId, String versionId, InputStream modelSource) throws IOException, UnsupportedUriException {
		URI model = null;
		
		if( modelId == null || modelId.isEmpty()|| modelId.equals("..") || modelId.equals(".") )
			throw new IllegalArgumentException("modelId can not be empty!");
		
		if( versionId == null || versionId.isEmpty() || versionId.equals("..") || versionId.equals(".") )
			throw new IllegalArgumentException("versionId can not be empty!");
		
		if( modelSource == null )
			throw new IllegalArgumentException("no modelSource InputStream were given!");
		
		try {
			// creating new model URI
			model = new URI( Properties.getProperty("de.unirostock.sems.ModelCrawler.models.uri.scheme"),
								Properties.getProperty("de.unirostock.sems.ModelCrawler.models.uri.host"),
								File.separator + modelId + File.separator + versionId, null);
			
		} catch (URISyntaxException e) {
			log.error("modelId or versionId does not fit into URI format", e);
			throw new UnsupportedUriException("modelId or versionId does not fit into URI format!");
		}
		
		File modelDir  = new File(location, modelId + File.separator + versionId + File.separator ); 
		File modelPath = new File(modelDir, modelId + ".xml" );
		
		try {
			modelDir.mkdirs();
			modelPath.createNewFile();
			
			OutputStream file = new FileOutputStream(modelPath);
			Util.copyStream(modelSource, file);
			file.close();
		}
		catch (IOException e) {
			log.error( "Error writing file to XmlFileRepository!" );
			throw e;
		}
		
		return model;
	}
	
	private File getModelPath( URI model ) {
		
		String path = model.getPath();
		// matches the path against modelId/versionId scheme
		if( path.matches("[a-zA-Z0-9\\-\\.\\_]*\\/[a-ZA-Z0-9\\-\\.\\_]*") != true )
			return null;
		
		String[] pathParts = path.split("/");
		if( pathParts.length < 2 )
			return null;
		
		if( pathParts[0].equals("..") || pathParts[1].equals(".") )
			return null;
		
		File modelDir = new File( location, pathParts[0] + File.separator + pathParts[1] );
		File modelPath = new File( modelDir, pathParts[0] + ".xml" );
		
		return modelPath;
	}

}
