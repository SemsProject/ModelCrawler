package de.unirostock.sems.ModelCrawler.GraphDb.exceptions;

public class GraphDatabaseInterfaceException extends
		GraphDatabaseCommunicationException {

	public GraphDatabaseInterfaceException() {
	}

	public GraphDatabaseInterfaceException(String message) {
		super(message);
	}

	public GraphDatabaseInterfaceException(Throwable cause) {
		super(cause);
	}

	public GraphDatabaseInterfaceException(String message, Throwable cause) {
		super(message, cause);
	}

}
