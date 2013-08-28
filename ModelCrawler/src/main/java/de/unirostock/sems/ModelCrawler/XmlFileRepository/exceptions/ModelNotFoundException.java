package de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions;

public class ModelNotFoundException extends Exception {

	private static final long serialVersionUID = 3154370058641446149L;
	public ModelNotFoundException() {
	}

	public ModelNotFoundException(String message) {
		super(message);
	}

	public ModelNotFoundException(Throwable cause) {
		super(cause);
	}

	public ModelNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelNotFoundException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
