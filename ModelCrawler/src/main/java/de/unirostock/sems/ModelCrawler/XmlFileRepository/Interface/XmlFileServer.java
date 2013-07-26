package de.unirostock.sems.ModelCrawler.XmlFileRepository.Interface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;

public interface XmlFileServer {
	
	public static final String URL_ENCODING = "UTF-8";
	
	/**
	 * Resolves the model URI and returns the modelSource
	 * 
	 * @param model URI
	 * @return InputStream or {@code NULL} if URI not found
	 * @throws FileNotFoundException 
	 * @throws UnsupportedUriException 
	 */
	public InputStream resolveModelUri( URI model ) throws FileNotFoundException, UnsupportedUriException;
	
	/**
	 * Checks if the model referenced by the URI exits and is available
	 * 
	 * @param model
	 * @return boolean
	 */
	public boolean exist( URI model );
	
	/**
	 * Checks if the URI is resolvable by the FileServer. <br>
	 * (Tests only scheme and host)
	 * 
	 * @param model
	 * @return
	 */
	public boolean isResolvableUri(URI model);
	
	/**
	 * Pushes a new model (-Version) into the storage. <br>
	 * Returns the URI to the new model or <code>null</code> if failed.
	 * 
	 * @param modelId
	 * @param versionId
	 * @param modelSource
	 * @return URI to the new model
	 * @throws IOException 
	 * @throws UnsupportedUriException 
	 */
	public URI pushModel( String modelId, String versionId, InputStream modelSource ) throws IOException, UnsupportedUriException;
	
	/**
	 * Pushes a new model (-Version) into the storage. <br>
	 * Returns the URI to the new model or {@code null} if failed. <br><br>
	 * The link corresponds to following pattern:<br> {schema}://{host}/{repositoryUrl}/{versionId}/{fileUrl} <br>
	 * repositoryUrl and fileUrl together expresses the modelId
	 * 
	 * @param repositoryUrl
	 * @param fileUrl
	 * @param versionId
	 * @param modelSource
	 * @return
	 * @throws IOException
	 * @throws UnsupportedUriException
	 */
	public URI pushModel(String modelId, String versionId, String repositoryUrl, String filePath, InputStream modelSource) throws IOException, UnsupportedUriException;
	
}
