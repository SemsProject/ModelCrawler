package de.unirostock.sems.ModelCrawler.XmlFileRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.io.Util;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.Interface.XmlFileServer;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;

public class XmlFileRepository implements XmlFileServer {
	
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
	
	@Override
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
		return pushModel( modelId, versionId, null, null, modelSource);
	}
	
	@Override
	public URI pushModel( String modelId, String versionId, String repositoryUrl, String filePath, InputStream modelSource) throws IOException, UnsupportedUriException {
		URI model = null;
		
		// repositoryUrl respectively filePath can be null, but not empty
		if( (repositoryUrl == null || filePath == null) && !(repositoryUrl == null && filePath == null) ) {
			throw new IllegalArgumentException("repositoryUrl and filePath are both suposed to be null or to be setted!");
		}
		else if( repositoryUrl != null && filePath != null ) {
			if( repositoryUrl.isEmpty()|| repositoryUrl.equals("..") || repositoryUrl.equals(".") )
				throw new IllegalArgumentException("Illegal Value for repositoryUrl!");
			
			if( filePath.isEmpty() || filePath.equals("..") || filePath.equals(".") )
				throw new IllegalArgumentException("Illegal Value for fileUrl!");
			
			// if modelId not set -> try to generate one
			if( modelId == null )
				modelId = generateModelId(repositoryUrl, filePath);
			
			// escape the String
			repositoryUrl = URLEncoder.encode(repositoryUrl, URL_ENCODING);
						
			// escape the String
//			filePath = URLEncoder.encode(filePath, URL_ENCODING);
		}
		
		if( modelId == null || modelId.isEmpty()|| modelId.equals("..") || modelId.equals(".") )
			throw new IllegalArgumentException("modelId can not be empty!");
		
		if( versionId == null || versionId.isEmpty() || versionId.equals("..") || versionId.equals(".") )
			throw new IllegalArgumentException("versionId can not be empty!");
		
		if( modelSource == null )
			throw new IllegalArgumentException("no modelSource InputStream were given!");
		
		if( log.isInfoEnabled() ) {
			if( filePath == null )
				log.info( MessageFormat.format("start pushing new model-version {0}:{1} into fileRepo.", repositoryUrl, versionId) );
			else
				log.info( MessageFormat.format("start pushing new model-version {0}:{1}:{2} into file Repo.", repositoryUrl, filePath, versionId) );
		}
		
		try {
			// Builder for the URI Path
			StringBuilder pathString = new StringBuilder( File.separator );
			
			if( repositoryUrl != null )
				pathString.append( repositoryUrl );	// adds the first part of the modelId aka. repositoryUrl
			else
				pathString.append( modelId );		// adds the modelId
			
			pathString.append( File.separator );	// separator (slash)
			pathString.append( versionId );			// adds the versionId
			if( filePath != null ) {
				// when fileUrl is set
				pathString.append( File.separator );
				pathString.append( filePath );		// adds the fileUrl (second part of the modelId)
			}
			
			// creating new model URI
			model = new URI( Properties.getProperty("de.unirostock.sems.ModelCrawler.models.uri.scheme"),		// sets the Uri Scheme e.g. http:// or model://
								Properties.getProperty("de.unirostock.sems.ModelCrawler.models.uri.host"),		// sets the Uri Host e.g. models.sems.uni-rostock.de
								pathString.toString(), null);													// sets the Uri Path (modelId/versionId or repositoryUrl/versionId/fileUrl) 
			
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("New URI is {0}", model) );
			
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
	
	public static String generateModelId( String repositoryUrl, String fileName ) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder(repositoryUrl);
		
		// if repo Url does not end and the file name does not starts with a slash /
		if( !repositoryUrl.endsWith("/") && !fileName.startsWith("/") )
			// ... adds one
			result.append('/');
		
		result.append(fileName);
		return URLEncoder.encode( result.toString(), URL_ENCODING );
	}
	
	private File getModelPath( URI model ) {
		
		String modelId = null, versionId = null;
		String path = model.getPath();
		
		// remove leading slashes
		if( path.startsWith(File.separator) )
			path = path.substring(1);
		
		if( path.endsWith(File.separator) )
			path = path.substring(0, path.length()-1 );
		
		String[] pathParts = path.split("/");
		// path must have at least 2 parts (modelId and versionId)
		if( pathParts.length < 2 )
			return null;
		else if( pathParts.length == 2 ) {
			// exact 2 parts => this looks like a "normal" BMDB URI
			modelId = pathParts[0];
			versionId = pathParts[1];
		}
		else {
			// more than 2 parts => eventually a PMR2 URI
			
			if( pathParts[2].startsWith(pathParts[0]) ) {
				// 3rd part is only a repetition of the modelId => now it looks more like a BMDB URI
				modelId = pathParts[0];
				versionId = pathParts[1];
			}
			
			
			try {
				// decodes the repositoryUrl 
				String repositoryUrl = URLDecoder.decode( pathParts[0], URL_ENCODING );
				
				// builds the filePath from the rest of the UriPath
				StringBuilder filePath = new StringBuilder( pathParts[2] );
				for( int i = 3; i < pathParts.length; i++ ) {
					filePath.append( File.separator );
					filePath.append( pathParts[i] );
				}
				
				// generates ModelId
				modelId = generateModelId( repositoryUrl, filePath.toString() );
				// versionId is always the second part, or better it should be
				versionId = pathParts[1];
			} catch (UnsupportedEncodingException e) {
				log.fatal( MessageFormat.format("Unsupported Encoding while decoding modelUri respectively generate modelId from URI: {0} . Btw. what kind of sorcery is this?", model.toString()), e);
			}
			
		}
		
		File modelDir = new File( location, modelId + File.separator + versionId );
		File modelPath = new File( modelDir, modelId + ".xml" );
		
		return modelPath;
	}

}
