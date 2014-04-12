package de.unirostock.sems.ModelCrawler.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unirostock.sems.morre.client.dataholder.CrawledModel;

public class CrawledModelRecord extends CrawledModel {
	
	private static final long serialVersionUID = 6382870895044981027L;
	public static final String DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss";
	
	public static CrawledModelRecord extendDataholder( CrawledModel model ) {
		if( model != null )
			return new CrawledModelRecord(model);
		else
			return null;
	}
	
	/**
	 * Creates a new CrawledModelRecord based on a simple CrawledModel Dataholder
	 * 
	 * @param model
	 */
	public CrawledModelRecord( CrawledModel model ) {
		super( model.getFileId(), model.getVersionId(), model.getXmldoc(), model.getParentMap(), model.getMetaMap(), model.getModelType() );
	}
	
	public CrawledModelRecord(String fileId, String versionId, String xmldoc, Map<String, List<String>> parentMap, Map<String, String> metaMap, String modelType) {
		super(fileId, versionId, xmldoc, parentMap, metaMap, modelType);
	}
	
	public CrawledModelRecord(String fileId, String versionId, String xmldoc, Map<String, List<String>> parentMap, Map<String, String> metaMap, String modelType, Date versionDate, Date crawledDate) {
		super(fileId, versionId, xmldoc, parentMap, metaMap, modelType);
		setVersionDate(versionDate);
		setCrawledDate(crawledDate);
	}
	
	public CrawledModelRecord(String fileId, String versionId, Date versionDate, Date crawledDate) {
		super(fileId, versionId, null, null, null, null);
		setVersionDate(versionDate);
		setCrawledDate(crawledDate);
	}
	
	/**
	 * Checks if the model dataholder is valid.
	 * 
	 * @return
	 */
	public boolean isAvailable() {
		
		if( getFileId() == null || getFileId().isEmpty() )
			return false;
		
		if( getVersionId() == null || getVersionId().isEmpty() )
			return false;
		
		if( getXmldoc() == null || getXmldoc().isEmpty() )
			return false;
		
		return true;
	}
	
	/**
	 * Returns the value of a meta field or null
	 * 
	 * @param metaField
	 * @return
	 */
	public String getMeta( String metaField ) {
		Map<String, String> metaMap = getMetaMap();
		if( metaMap == null )
			return null;
		else
			return metaMap.get(metaField);
	}
	
	/**
	 * Sets the value of a meta field and overrides the previous value
	 * 
	 * @param metaField
	 * @param value
	 */
	public void setMeta( String metaField, String value ) {
		Map<String, String> metaMap = getMetaMap();
		if( metaMap == null ) {
			metaMap = new HashMap<>();
			setMetaMap(metaMap);
		}
		metaMap.put(metaField, value);
	}
	
	/**
	 * Returns the parsed VersionDate if it is set, or null
	 * 
	 * @return
	 */
	public Date getVersionDate() {
		Date versionDate = null;
		
		String date = getMeta(META_VERSION_DATE);
		if( date == null )
			return null;
		
		try {
			versionDate = new SimpleDateFormat(DATE_FORMAT).parse( date );
		} catch (ParseException e) {
			return null;
		}
		
		return versionDate;
	}
	
	/**
	 * Sets the VersionDate in the Meta Field
	 * 
	 * @param versionDate
	 */
	public void setVersionDate( Date versionDate ) {
		setMeta(META_VERSION_DATE, new SimpleDateFormat(DATE_FORMAT).format(versionDate) );
	}
	
	/**
	 * Returns the parsed CrawledDate if it is set, or null
	 * 
	 * @return
	 */
	public Date getCrawledDate() {
		Date crawledDate = null;
		
		String date = getMeta(META_CRAWLED_DATE);
		if( date == null )
			return null;
		
		try {
			crawledDate = new SimpleDateFormat(DATE_FORMAT).parse( date );
		} catch (ParseException e) {
			return null;
		}
		
		return crawledDate;
	}
	
	/**
	 * Sets the CrawledDate in the Meta Field
	 * 
	 * @param versionDate
	 */
	public void setCrawledDate( Date crawledDate ) {
		setMeta(META_CRAWLED_DATE, new SimpleDateFormat(DATE_FORMAT).format(crawledDate) );
	}
	
	/**
	 * Adds a parent to this model.
	 * 
	 * @param parentFileId
	 * @param parentVersionId
	 */
	public void addParent( String parentFileId, String parentVersionId ) {
		
		// throw exception if some field is null or empty
		if( parentFileId == null || parentFileId.isEmpty() || parentVersionId == null || parentVersionId.isEmpty() )
			throw new IllegalArgumentException("parentFileId and/or parentVersionId are not allowed to be null or empty!");
		
		// if no parent map exists, create one
		Map<String, List<String>> parentMap = null;
		if( getParentMap() == null ) {
			parentMap = new HashMap<String, List<String>>();
			setParentMap(parentMap);
		}
		
		// first parent with this fileId
		List<String> fileIdList = parentMap.get(parentFileId);
		if( fileIdList == null ) {
			fileIdList = new ArrayList<>();
			parentMap.put(parentFileId, fileIdList);
		}
		
		// finally adds the versionId to the list
		fileIdList.add(parentVersionId);
		
	}
	
	/**
	 * Adds a parent to this model with the same fileId
	 * 
	 * @param parentVersionId
	 */
	public void addParent( String parentVersionId ) {
		addParent( getFileId(), parentVersionId );
	}

}
