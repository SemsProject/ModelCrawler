package de.unirostock.sems.ModelCrawler;

import java.io.File;
import java.io.IOException;

public class Properties {

	private static java.util.Properties prop = null;
	private static File workingDir = null;

	/**
	 * Init the Properties System
	 */
	public static void init() {
		prop = new java.util.Properties();

		try {
			// Load the main.properties
			prop.load( ClassLoader.getSystemResourceAsStream("main.properties") );

			// Load all additional properties
			String[] additional = prop.getProperty("de.unirostock.sems.ModelCrawler.additionProperties", "").split(";");
			for( int index = 0; index < additional.length; index++ ) {
				prop.load( ClassLoader.getSystemResourceAsStream( additional[index].trim() ));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getProperty( String key, String defaultValue ) {
		if( prop != null )
			return prop.getProperty(key, defaultValue);
		else
			return null;
	}

	public static String getProperty( String key ) {
		if( prop != null )
			return prop.getProperty(key);
		else
			return null;
	}

	public static void checkAndInitWorkingDir() {
		
		// only once needed...
		if( workingDir != null )
			return;
		
		// scanning for working directory
		workingDir = new File( Properties.getProperty("de.unirostock.sems.ModelCrawler.workingDir") );
		if( !workingDir.exists() ) {
			// not existing, creates it!
			workingDir.mkdirs();
		}

	}

	public static File getWorkingDir() {
		return workingDir;
	}

}
