package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class BioModelRelease implements Comparable<BioModelRelease> {
	
	private String releaseName;
	private String ftpDirectory;
	private Date releaseDate;
	
	private File archivFile = null;
	private File contentDir = null;
	
	private Map<String, File> modelMap;
	
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
	
	public boolean setArchivFile(File archivFile) {
		//REMIND the archiv file could only be setted once!
		if( this.archivFile == null ) {
			this.archivFile = archivFile;
			return true;
		}
		else
			return false;
	}
	
	public Set<String> getModelList() {
		return modelMap.keySet();
	}
	
	public File getModelPath( String fileId ) {
		return modelMap.get(fileId);
	}
	
	public String getFtpDirectory() {
		return ftpDirectory;
	}

	public File getContentDir() {
		return contentDir;
	}

	public boolean setContentDir(File contentDir, Map<String, File> modelMap) {
		// REMIND the contentDir can only be setted once!
		if( this.contentDir == null && this.modelMap == null ) {
			this.modelMap = modelMap;
			this.contentDir = contentDir;
			return true;
		}
		else
			return false;
	}

	public boolean isDownloaded() {
		return archivFile == null ? false : true;
	}
	
	public boolean isExtracted() {
		return contentDir == null ? false : true;
	}
	
	@Override
	public int compareTo( BioModelRelease model ) {
		return releaseDate.compareTo( model.getReleaseDate() );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((releaseDate == null) ? 0 : releaseDate.hashCode());
		result = prime * result
				+ ((releaseName == null) ? 0 : releaseName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BioModelRelease other = (BioModelRelease) obj;
		if (releaseDate == null) {
			if (other.releaseDate != null)
				return false;
		} else if (!releaseDate.equals(other.releaseDate))
			return false;
		if (releaseName == null) {
			if (other.releaseName != null)
				return false;
		} else if (!releaseName.equals(other.releaseName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BioModelRelease [" + releaseName + "]";
	}
	
}
