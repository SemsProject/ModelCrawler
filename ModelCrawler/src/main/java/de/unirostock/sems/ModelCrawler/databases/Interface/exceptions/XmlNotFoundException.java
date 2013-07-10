package de.unirostock.sems.ModelCrawler.databases.Interface.exceptions;

public class XmlNotFoundException extends Exception {

	private static final long serialVersionUID = 9151151806312439280L;

	public XmlNotFoundException() {
		super();
	}

	public XmlNotFoundException(String message) {
		super(message);
	}

	public XmlNotFoundException(Throwable cause) {
		super(cause);
	}

	public XmlNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
