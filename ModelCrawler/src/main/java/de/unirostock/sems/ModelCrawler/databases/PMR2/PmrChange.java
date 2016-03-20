package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;

public class PmrChange extends Change {

	private static final long serialVersionUID = 4740459688628719898L;
	
	public PmrChange( URL repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws URISyntaxException {
		super( repositoryUrl, fileName, versionId, versionDate, crawledDate );
	}
	
	public PmrChange( String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws MalformedURLException, URISyntaxException {
		this( new URL(repositoryUrl), fileName, versionId, versionDate, crawledDate );
	}
	
	@Override
	public String toString() {
		return "PmrChg:" + getFileId()+"@"+getVersionId();
	}
	
}
