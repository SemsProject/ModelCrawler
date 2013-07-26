package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.XmlFileRepository.XmlFileRepository;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;

public class PmrChange extends Change {

	protected String repositoryUrl = null;
	protected String fileName = null;
	
	public PmrChange(String modelId, String versionId, Date versionDate, Date crawledDate) {
		super(modelId, versionId, versionDate, crawledDate);
	}
	
	public PmrChange( String repositoryUrl, String fileName, String versionId, Date versionDate, Date crawledDate ) throws UnsupportedEncodingException {
		super( null, versionId, versionDate, crawledDate );
		this.repositoryUrl = repositoryUrl;
		this.fileName = fileName;
		this.modelId = XmlFileRepository.generateModelId(repositoryUrl, fileName);
	}
	
	

}
