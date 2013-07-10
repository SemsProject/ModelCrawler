package de.unirostock.sems.ModelCrawler.GraphDb;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ModelRecord {
	
	protected String modelId;
	protected String versionId;
		
	protected URI documentUri;
	
	protected Map<String, String> meta = new HashMap<String, String>();
	
	public final String META_CRAWLED_DATE = "crawledDate";
	public final String META_VERSION_DATE = "versionDate";
	public final String META_SOURCE = "source";
	
	public final String DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss";
	
	public ModelRecord( String modelId, String versionId, URI documentUri ) {
		this.modelId = modelId;
		this.versionId = versionId;
		this.documentUri = documentUri;
	}
	
	public ModelRecord( String modelId, String versionId, URI documentUri, Date versionDate, Date crawledDate ) {
		this.modelId = modelId;
		this.versionId = versionId;
		this.documentUri = documentUri;
		
		SimpleDateFormat parser = new SimpleDateFormat(DATE_FORMAT);
		meta.put(META_VERSION_DATE, parser.format(versionDate) );
		meta.put(META_CRAWLED_DATE, parser.format(crawledDate) );
	}

	public String getModelId() {
		return modelId;
	}
	
	public URI getDocumentUri() {
		return documentUri;
	}

	public String getVersionId() {
		return versionId;
	}

	public Date getVersionDate() {
		Date versionDate = null;
		
		String date = meta.get(META_VERSION_DATE);
		if( date == null )
			return null;
		
		try {
			versionDate = new SimpleDateFormat(DATE_FORMAT).parse( date );
		} catch (ParseException e) {
			return null;
		}
		
		return versionDate;
	}
	
	public void setVersionDate( Date versionDate ) {
		meta.put(META_VERSION_DATE, new SimpleDateFormat(DATE_FORMAT).format(versionDate) );
	}
	
	public Date getCrawledDate() {
		Date crawledDate = null;
		
		String date = meta.get(META_CRAWLED_DATE);
		if( date == null )
			return null;
		
		try {
			crawledDate = new SimpleDateFormat(DATE_FORMAT).parse( date );
		} catch (ParseException e) {
			return null;
		}
		
		return crawledDate;
	}
	
	public void setCrawledDate( Date crawledDate ) {
		meta.put(META_CRAWLED_DATE, new SimpleDateFormat(DATE_FORMAT).format(crawledDate) );
	}
	
	public String getMeta( String key ) {
		return meta.get(key);
	}
	
	public void setMeta( String key, String value ) {
		meta.put(key, value);
	}
	
	public Map<String, String> getMetaMap() {
		return meta;
	}

}
