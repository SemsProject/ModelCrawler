package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.File;
import java.util.Date;
import java.util.List;

public class BioModelRelease implements Comparable<BioModelRelease> {
	
	private String releaseName;
	private String ftpDirectory;
	private Date releaseDate;
	private File archivFile;
	private List<String> modelList;
	
	public BioModelRelease( String releaseName, String ftpDirectory, Date releaseDate, File archivFile ) {
		this.releaseName	= releaseName;
		this.ftpDirectory	= ftpDirectory;
		this.releaseDate	= releaseDate;
		this.archivFile 	= archivFile;
	}
	
	public BioModelRelease( String releaseName, String ftpDirectory, Date releaseDate ) {
		this.releaseName	= releaseName;
		this.ftpDirectory	= ftpDirectory;
		this.releaseDate	= releaseDate;
	}
	
	public String getReleaseName() {
		return releaseName;
	}
	public Date getReleaseDate() {
		return releaseDate;
	}
	public File getArchivFile() {
		return archivFile;
	}
	public void setArchivFile(File archivFile) {
		this.archivFile = archivFile;
	}
	public List<String> getModelList() {
		return modelList;
	}
	public String getFtpDirectory() {
		return ftpDirectory;
	}

	@Override
	public int compareTo( BioModelRelease model ) {
		return releaseDate.compareTo( model.getReleaseDate() );
	}
	
}
