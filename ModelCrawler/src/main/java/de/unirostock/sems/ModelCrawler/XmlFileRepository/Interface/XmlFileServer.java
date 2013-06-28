package de.unirostock.sems.ModelCrawler.XmlFileRepository.Interface;

import java.io.InputStream;
import java.net.URI;

public interface XmlFileServer {
	
	/**
	 * Resolves the model URI and returns the modelSource
	 * 
	 * @param model URI
	 * @return InputStream or {@code NULL} if URI not found
	 */
	public InputStream resolveModelUri( URI model );
	
	/**
	 * Checks if the model referenced by the URI exits and is available
	 * 
	 * @param model
	 * @return boolean
	 */
	public boolean exist( URI model );
	
	/**
	 * Pushes a new model (-Version) into the storage. <br>
	 * Returns the URI the new model or {@code null} if failed.
	 * 
	 * @param modelId
	 * @param versionId
	 * @param modelSource
	 * @return URI to the new model
	 */
	public URI pushModel( String modelId, String versionId, InputStream modelSource );
	
}
