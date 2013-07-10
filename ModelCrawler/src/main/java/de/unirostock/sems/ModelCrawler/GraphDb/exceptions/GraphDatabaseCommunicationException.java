package de.unirostock.sems.ModelCrawler.GraphDb.exceptions;

import java.io.IOException;

public class GraphDatabaseCommunicationException extends IOException {

	public GraphDatabaseCommunicationException() {
	}

	public GraphDatabaseCommunicationException(String message) {
		super(message);
	}

	public GraphDatabaseCommunicationException(Throwable cause) {
		super(cause);
	}

	public GraphDatabaseCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

}
