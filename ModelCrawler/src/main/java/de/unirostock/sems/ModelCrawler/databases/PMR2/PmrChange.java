package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;

public class PmrChange extends Change {

	protected String repositoryUrl = null;
	protected String fileName = null;
	
	public PmrChange(String modelId, String versionId, Date versionDate, Date crawledDate) {
		super(modelId, versionId, versionDate, crawledDate);
	}
	
	public PmrChange( String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( null, versionId, versionDate, crawledDate );
		generateModelId(repositoryUrl, fileName);
	}
	
	public void generateModelId( String repositoryUrl, String fileName ) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder(repositoryUrl);
		
		// if repo Url does not end and the file name does not starts with a slash /
		if( !repositoryUrl.endsWith("/") && !fileName.startsWith("/") )
			// ... adds one
			result.append('/');
		
		result.append(fileName);
		modelId = URLEncoder.encode( result.toString(), "UTF-8" );
		this.repositoryUrl = repositoryUrl;
		this.fileName = fileName;
	}

}
