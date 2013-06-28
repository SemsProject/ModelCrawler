package de.unirostock.sems.ModelCrawler.GraphDb;

import java.net.URI;
import java.util.Date;

public class QueryResult {
	
	private float score;
	private String modelId;
	private long databaseId;
	
	private String modelName;
	
	private URI documentUri;
	private String filename;
	
	private String revisionId;
	private Date revisionDate;
	
	public QueryResult( String modelId, long databaseId, String modelName, URI documentUri, String filename ) {
		this.modelId = modelId;
		this.databaseId = databaseId;
		this.modelName = modelName;
		this.documentUri = documentUri;
		this.filename = filename;
	}
	
	public QueryResult( String modelId, long databaseId, String modelName, URI documentUri, String filename, String revisionId, Date revisioDate ) {
		this.modelId = modelId;
		this.databaseId = databaseId;
		this.modelName = modelName;
		this.documentUri = documentUri;
		this.filename = filename;
		this.revisionId = revisionId;
		this.revisionDate = revisioDate;
	}

	public float getScore() {
		return score;
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

	public String getFilename() {
		return filename;
	}

	public String getRevisionId() {
		return revisionId;
	}

	public Date getRevisionDate() {
		return revisionDate;
	}

}
