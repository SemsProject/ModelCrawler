package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class BioModelRelease.
 */
public class BioModelRelease implements Comparable<BioModelRelease> {
	
	/** The release name. */
	private String releaseName;
	
	/** The ftp directory. */
	private String ftpDirectory;
	
	/** The release date. */
	private Date releaseDate;
	
	/** The archiv file. */
	private File archivFile = null;
	
	/** The content dir. */
	private File contentDir = null;
	
	/** The model map. */
	private Map<String, File> modelMap;
	
	/**
	 * The Constructor.
	 *
	 * @param releaseName the release name
	 * @param ftpDirectory the ftp directory
	 * @param releaseDate the release date
	 * @param archivFile the archiv file
	 */
	public BioModelRelease( String releaseName, String ftpDirectory, Date releaseDate, File archivFile ) {
		this.releaseName	= releaseName;
		this.ftpDirectory	= ftpDirectory;
		this.releaseDate	= releaseDate;
		this.archivFile 	= archivFile;
	}
	
	/**
	 * The Constructor.
	 *
	 * @param releaseName the release name
	 * @param ftpDirectory the ftp directory
	 * @param releaseDate the release date
	 */
	public BioModelRelease( String releaseName, String ftpDirectory, Date releaseDate ) {
		this.releaseName	= releaseName;
		this.ftpDirectory	= ftpDirectory;
		this.releaseDate	= releaseDate;
	}
	
	/**
	 * Gets the release name.
	 *
	 * @return the release name
	 */
	public String getReleaseName() {
		return releaseName;
	}
	
	/**
	 * Gets the release date.
	 *
	 * @return the release date
	 */
	public Date getReleaseDate() {
		return releaseDate;
	}
	
	/**
	 * Gets the archiv file.
	 *
	 * @return the archiv file
	 */
	public File getArchivFile() {
		return archivFile;
	}
	
	/**
	 * Sets the archiv file.
	 *
	 * @param archivFile the archiv file
	 * @return true, if sets the archiv file
	 */
	public boolean setArchivFile(File archivFile) {
		//REMIND the archiv file could only be setted once!
		if( this.archivFile == null ) {
			this.archivFile = archivFile;
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Gets the model list.
	 *
	 * @return the model list
	 */
	public Set<String> getModelList() {
		return modelMap.keySet();
	}
	
	/**
	 * Gets the model path.
	 *
	 * @param fileId the file id
	 * @return the model path
	 */
	public File getModelPath( String fileId ) {
		return modelMap.get(fileId);
	}
	
	/**
	 * Gets the ftp directory.
	 *
	 * @return the ftp directory
	 */
	public String getFtpDirectory() {
		return ftpDirectory;
	}

	/**
	 * Gets the content dir.
	 *
	 * @return the content dir
	 */
	public File getContentDir() {
		return contentDir;
	}

	/**
	 * Sets the content dir.
	 *
	 * @param contentDir the content dir
	 * @param modelMap the model map
	 * @return true, if sets the content dir
	 */
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

	/**
	 * Checks if is downloaded.
	 *
	 * @return true, if checks if is downloaded
	 */
	public boolean isDownloaded() {
		return archivFile == null ? false : true;
	}
	
	/**
	 * Checks if is extracted.
	 *
	 * @return true, if checks if is extracted
	 */
	public boolean isExtracted() {
		return contentDir == null ? false : true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo( BioModelRelease model ) {
		return releaseDate.compareTo( model.getReleaseDate() );
	}
	
}
