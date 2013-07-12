package de.unirostock.sems.ModelCrawler.databases.BioModelsDb.exceptions;

public class ExtractException extends Exception {

	private static final long serialVersionUID = -4419252545030334590L;

	public ExtractException() {
	}

	public ExtractException(String message) {
		super(message);
	}

	public ExtractException(Throwable cause) {
		super(cause);
	}

	public ExtractException(String message, Throwable cause) {
		super(message, cause);
	}

}
