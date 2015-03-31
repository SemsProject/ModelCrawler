package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import de.unirostock.sems.ModelCrawler.XmlFileRepository.XmlFileRepository;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;

// TODO: Auto-generated Javadoc
/**
 * The Class RelevantFile.
 */
public class RelevantFile {

	/** The file path. */
	private String filePath;
	
	/** The repo url. */
	private String repoUrl = null;
	
	/** The file id. */
	private String fileId;
	
	/** The latest known version id. */
	private String latestKnownVersionId = null;
	
	/** The latest known version date. */
	private Date latestKnownVersionDate = null;
	
	/** The type. */
	private int type = 0;
	
	/** The change set. */
	private PmrChangeSet changeSet = null;
	
	/**
	 * The Constructor.
	 *
	 * @param filePath the file path
	 * @param fileId the file id
	 */
	public RelevantFile( String filePath, String fileId ) {
		this.filePath = filePath;
		this.fileId = fileId;
	}
	
	/**
	 * The Constructor.
	 *
	 * @param filePath the file path
	 */
	public RelevantFile( String filePath ) {
		this.filePath = filePath;
	}
	
	/**
	 * Generate file id.
	 *
	 * @param repoUrl the repo url
	 * @return the string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public String generateFileId( String repoUrl ) throws UnsupportedEncodingException {
		this.repoUrl = repoUrl;
		return this.fileId = XmlFileRepository.generateFileId(repoUrl, filePath);
	}
	
	/**
	 * Gets the file path.
	 *
	 * @return the file path
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Gets the repository url.
	 *
	 * @return the repository url
	 */
	public String getRepositoryUrl() {
		return repoUrl;
	}
	
	/**
	 * Gets the file id.
	 *
	 * @return the file id
	 */
	public String getFileId() {
		return fileId;
	}
	
	/**
	 * Sets the latest known Version of this model and the changeSet of it.
	 *
	 * @param latestVersionId the latest version id
	 * @param latestVersionDate the latest version date
	 * @param changeSet the change set
	 */
	public void setLatestKnownVersion( String latestVersionId, Date latestVersionDate, PmrChangeSet changeSet ) {
		this.latestKnownVersionId = latestVersionId;
		this.latestKnownVersionDate = latestVersionDate;
		this.changeSet = changeSet;
	}
	
	/**
	 * Sets the latest known Version of this model.
	 *
	 * @param latestVersionId the latest version id
	 * @param latestVersionDate the latest version date
	 */
	public void setLatestKnownVersion( String latestVersionId, Date latestVersionDate ) {
		setLatestKnownVersion( latestVersionId, latestVersionDate, null );
	}
	
	/**
	 * Gets the latest known VersionId. Not the real latest, but the versionId setted with setLatestKnownVersion()
	 * 
	 * @return versionId
	 */
	public String getLatestKnownVersionId() {
		return latestKnownVersionId;
	}
	
	/**
	 * Gets the latest known VersionDate. Not the real latest, but the versionDate setted with setLatestKnownVersion()
	 * 
	 * @return versionDate
	 */
	public Date getLatestKnownVersionDate() {
		return latestKnownVersionDate;
	}
	
	/**
	 * Gets the real latest versionId. Either from the latestKnownVersion() or from the changeSet
	 * 
	 * @return versionId
	 */
	public String getLatestVersionId() {
		
		if( changeSet == null ) {
			// no changeSet available
			return latestKnownVersionId;
		}
		
		Change change; 
		if( (change = changeSet.getLatestChange()) != null ) {
			// take the latest change from the changeSet
			return change.getVersionId();
		}
		else {
			// no change stored in the changeSet
			return latestKnownVersionId;
		}
		
	}
	
	/**
	 * Gets the real latest versionDate. Either from the latestKnownVersion() or from the changeSet
	 * 
	 * @return versionDate
	 */
	public Date getLatestVersionDate() {
		
		if( changeSet == null ) {
			// no changeSet available
			return latestKnownVersionDate;
		}
		
		Change change; 
		if( (change = changeSet.getLatestChange()) != null ) {
			// take the latest change from the changeSet
			return change.getVersionDate();
		}
		else {
			// no change stored in the changeSet
			return latestKnownVersionDate;
		}
	
	}
	
	/**
	 * Return the changeSet or null, if no one was setted and no change added.
	 *
	 * @return PmrChangeSet or null
	 */
	public PmrChangeSet getChangeSet() {
		return changeSet;
	}
	
	/**
	 * Adds a change to the changeSet and creates one if necessary.
	 *
	 * @param change the change
	 */
	public void addChange( PmrChange change ) {
		
		// creates ChangeSet, if necessary
		if( changeSet == null ) {
			changeSet = new PmrChangeSet(fileId);
		}
		// sets the parent
		String parentVersionId = getLatestVersionId();
		if( parentVersionId != null && !parentVersionId.isEmpty() )
			change.addParent( getLatestVersionId() );
		
		// adds the change
		changeSet.addChange(change);
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the type
	 */
	public void setType(int type) {
		this.type = type;
	}

}
