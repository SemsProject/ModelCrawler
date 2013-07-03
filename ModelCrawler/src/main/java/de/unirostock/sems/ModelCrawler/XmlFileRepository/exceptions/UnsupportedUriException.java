package de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions;

public class UnsupportedUriException extends Exception {

	private static final long serialVersionUID = 5631668525428099077L;

	public UnsupportedUriException() {
	}

	public UnsupportedUriException(String message) {
		super(message);
	}

	public UnsupportedUriException(Throwable cause) {
		super(cause);
	}

	public UnsupportedUriException(String message, Throwable cause) {
		super(message, cause);
	}

}
