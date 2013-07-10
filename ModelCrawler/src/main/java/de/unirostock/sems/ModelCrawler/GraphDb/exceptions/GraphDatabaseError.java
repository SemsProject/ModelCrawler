package de.unirostock.sems.ModelCrawler.GraphDb.exceptions;

public class GraphDatabaseError extends Exception {

	private static final long serialVersionUID = -7629276841894307941L;

	public GraphDatabaseError() {
		super();
	}

	public GraphDatabaseError(String message) {
		super(message);
	}

	public GraphDatabaseError(Throwable cause) {
		super(cause);
	}

	public GraphDatabaseError(String message, Throwable cause) {
		super(message, cause);
	}

}
