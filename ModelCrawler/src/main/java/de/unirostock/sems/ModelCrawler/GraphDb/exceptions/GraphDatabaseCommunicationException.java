package de.unirostock.sems.ModelCrawler.GraphDb.exceptions;

import java.io.IOException;

public class GraphDatabaseCommunicationException extends IOException {

	private static final long serialVersionUID = 1589736586376163967L;

	public GraphDatabaseCommunicationException() {
		super();
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
