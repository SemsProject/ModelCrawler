package de.unirostock.sems.ModelCrawler.GraphDb;

public interface GraphDatabase {
	
	/**
	 * Checks if database is available
	 * 
	 * @return
	 */
	public boolean isModelManagerAlive();
	
	/**
	 * Checks if database contains data
	 * 
	 * @return
	 */
	public boolean isDatabaseEmpty();
	
	/**
	 * Returns all features included by an cellMl query
	 * 
	 * @return
	 */
	public String[] cellMlModelQueryFeatures();
	
	
	
}
