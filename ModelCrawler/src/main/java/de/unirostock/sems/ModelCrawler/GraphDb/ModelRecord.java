package de.unirostock.sems.ModelCrawler.GraphDb;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;

public class ModelRecord {
	
	private String modelId;
	private long databaseId;
	
	private String modelName;
	
	private URI documentUri;
	
	private String versionId;
	private Date versionDate;
	
	private HashMap<String, Object> meta;
	
	public ModelRecord( String modelId, long databaseId, String modelName, URI documentUri ) {
		this.modelId = modelId;
		this.databaseId = databaseId;
		this.modelName = modelName;
		this.documentUri = documentUri;
	}
	
	public ModelRecord( String modelId, long databaseId, String modelName, URI documentUri, String versionId, Date versionDate ) {
		this.modelId = modelId;
		this.databaseId = databaseId;
		this.modelName = modelName;
		this.documentUri = documentUri;
		this.versionId = versionId;
		this.versionDate = versionDate;
	}

	public String getModelId() {
		return modelId;
	}

	public long getDatabaseId() {
		return databaseId;
	}

	public String getModelName() {
		return modelName;
	}

	public URI getDocumentUri() {
		return documentUri;
	}

	public String getVersionId() {
		return versionId;
	}

	public Date getVersionDate() {
		return versionDate;
	}

}
