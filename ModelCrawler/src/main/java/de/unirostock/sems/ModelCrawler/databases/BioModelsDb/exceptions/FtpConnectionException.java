package de.unirostock.sems.ModelCrawler.databases.BioModelsDb.exceptions;

public class FtpConnectionException extends Exception {

	private static final long serialVersionUID = -4858702130474478887L;

	public FtpConnectionException() {
		super();
	}

	public FtpConnectionException(String message) {
		super(message);
	}

	public FtpConnectionException(Throwable cause) {
		super(cause);
	}

	public FtpConnectionException(String message, Throwable cause) {
		super(message, cause);
	}

}
