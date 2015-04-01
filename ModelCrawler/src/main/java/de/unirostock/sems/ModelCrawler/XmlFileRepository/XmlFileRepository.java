package de.unirostock.sems.ModelCrawler.XmlFileRepository;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.unirostock.sems.ModelCrawler.Config;

public class XmlFileRepository {
	
	
	
	public static String generateFileId( String repositoryUrl, String fileName ) throws UnsupportedEncodingException {
		String urlEncoding = Config.getConfig().getEncoding();
		String urlPathSeparator = String.valueOf( Config.getConfig().getPathSeparator() ); 
				
		StringBuilder result = new StringBuilder(repositoryUrl);
		
		// if repo Url does not end and the file name does not starts with a slash /
		if( !repositoryUrl.endsWith(urlPathSeparator) && !fileName.startsWith(urlPathSeparator) )
			// ... adds one
			result.append(urlPathSeparator);
		
		result.append(fileName);
		return URLEncoder.encode( result.toString(), urlEncoding );
	}
	
}
