package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;

public class PmrChange extends Change {

	private static final long serialVersionUID = 4740459688628719898L;
	
	//public PmrChange( URL repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws URISyntaxException {
	//	super( repositoryUrl, fileName, versionId, versionDate, crawledDate );
	//}
	public PmrChange( URL repositoryUrl, String filePath, String versionId, Date versionDate, Date crawledDate ) throws URISyntaxException {
		super( repositoryUrl, filePath, versionId, versionDate, crawledDate );
	}
	
	// old:
	//public PmrChange( String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws MalformedURLException, URISyntaxException {
	//	this( new URL(repositoryUrl), fileName, versionId, versionDate, crawledDate );
	//}
	// new:
	//public PmrChange( String repositoryUrl, String filePath, String versionId, Date versionDate, Date crawledDate ) throws MalformedURLException, URISyntaxException {
	//	this( new URL(repositoryUrl), new String(FilenameUtils.getBaseName(filePath) + ".xml"), versionId, versionDate, crawledDate ); // dirty fix
	//}
	
	@Override
	public String toString() {
		return "PmrChg:" + getFileId()+"@"+getVersionId();
	}
	
}
