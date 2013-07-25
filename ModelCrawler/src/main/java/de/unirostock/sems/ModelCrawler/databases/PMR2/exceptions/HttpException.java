package de.unirostock.sems.ModelCrawler.databases.PMR2.exceptions;

import java.io.IOException;

public class HttpException extends IOException {

	private static final long serialVersionUID = 8155646399693553499L;

	public HttpException() {
	}

	public HttpException(String message) {
		super(message);
	}

	public HttpException(Throwable cause) {
		super(cause);
	}

	public HttpException(String message, Throwable cause) {
		super(message, cause);
	}

}
