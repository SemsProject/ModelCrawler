package de.unirostock.sems.ModelCrawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.XmlFileServerClient.XmlFileServer;

public class Properties {

	private static java.util.Properties prop = null;
	private static File workingDir = null;
	
	private static final Log log = LogFactory.getLog( Properties.class );
	
	public static final String ELEMENT_SPLITTER = ";";

	/**
	 * Init the Properties System
	 */
	public static void init() {
		prop = new java.util.Properties();

		try {
			// Load the main.properties
			prop.load( ClassLoader.getSystemResourceAsStream("main.properties") );

			// Load all additional properties
			String[] additional = prop.getProperty("de.unirostock.sems.ModelCrawler.additionProperties", "").split(ELEMENT_SPLITTER);
			for( int index = 0; index < additional.length; index++ ) {
				prop.load( ClassLoader.getSystemResourceAsStream( additional[index].trim() ));
			}

		} catch (IOException e) {
			log.fatal( "Can not read Properties file for config!", e );
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
