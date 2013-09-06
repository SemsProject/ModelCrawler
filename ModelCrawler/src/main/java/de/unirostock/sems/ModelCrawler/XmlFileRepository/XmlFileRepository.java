package de.unirostock.sems.ModelCrawler.XmlFileRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.unirostock.sems.ModelCrawler.Properties;

public class XmlFileRepository {
	
	public static final String URL_ENCODING = Properties.getProperty("de.unirostock.sems.ModelCrawler.encoding", "UTF-8");
	public static final String URL_PATH_SEPARATOR = Properties.getProperty("de.unirostock.sems.ModelCrawler.pathSeparator", "/");
	
//	private final Log log = LogFactory.getLog( XmlFileRepository.class );
	
	
	public static String generateModelId( String repositoryUrl, String fileName ) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder(repositoryUrl);
		
		// if repo Url does not end and the file name does not starts with a slash /
		if( !repositoryUrl.endsWith(URL_PATH_SEPARATOR) && !fileName.startsWith(URL_PATH_SEPARATOR) )
			// ... adds one
			result.append(URL_PATH_SEPARATOR);
		
		result.append(fileName);
		return URLEncoder.encode( result.toString(), URL_ENCODING );
	}
	
}
