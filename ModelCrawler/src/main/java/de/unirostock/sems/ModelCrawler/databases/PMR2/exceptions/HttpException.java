package de.unirostock.sems.ModelCrawler.databases.PMR2.exceptions;

import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class HttpException.
 */
public class HttpException extends IOException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8155646399693553499L;

	/**
	 * The Constructor.
	 */
	public HttpException() {
	}

	/**
	 * The Constructor.
	 *
	 * @param message the message
	 */
	public HttpException(String message) {
		super(message);
	}

	/**
	 * The Constructor.
	 *
	 * @param cause the cause
	 */
	public HttpException(Throwable cause) {
		super(cause);
	}

	/**
	 * The Constructor.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

}
