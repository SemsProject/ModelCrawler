package de.unirostock.sems.ModelCrawler.GraphDb.exceptions;

public class GraphDatabaseInterfaceException extends GraphDatabaseCommunicationException {

	private static final long serialVersionUID = -1268322249656081056L;

	public GraphDatabaseInterfaceException() {
		super();
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
