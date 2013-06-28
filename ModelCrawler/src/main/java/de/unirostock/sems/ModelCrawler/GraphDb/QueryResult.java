package de.unirostock.sems.ModelCrawler.GraphDb;

import java.net.URI;

public class QueryResult {
	
	private float score;
	private String modelId;
	private long databaseId;
	
	private String modelName;
	
	private URI documentUri;
	private String filename;
	
	public QueryResult( String modelId, long databaseId, String modelName, URI documentUri, String filename ) {
		this.modelId = modelId;
		this.databaseId = databaseId;
		this.modelName = modelName;
		this.documentUri = documentUri;
		this.filename = filename;
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

}
