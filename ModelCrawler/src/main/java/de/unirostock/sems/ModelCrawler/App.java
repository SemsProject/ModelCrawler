package de.unirostock.sems.ModelCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.GraphDb.GraphDb;
import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseCommunicationException;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseError;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseInterfaceException;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelRelease;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.databases.Interface.exceptions.XmlNotFoundException;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final Log log = LogFactory.getLog( App.class );
	
	private static GraphDatabase graphDb;
	private static ModelDatabase bioModelsDb;
	
    public static void main( String[] args ) {
    	
    	log.info("ModelCrawler startet");
    	// Properties and WorkingDir
    	prepare();
    	// Connectors
    	initConnectors();
    	
    	// run it!
    	
    	if( log.isInfoEnabled() )
    		log.info("running BioModelsDb Crawler");
    	
    	bioModelsDb.run();
    	
    	Map<String, ChangeSet> changes = new HashMap<String, ChangeSet>();
    	// add all changes from BioModelsDb to the change Map
    	changes.putAll( bioModelsDb.listChanges() );
    	
    	if( log.isInfoEnabled() )
    		log.info("crawling model changes finished. Now start pushing");
    	
    	// XXX Limiter!
    	int n = 1; // limiter
    	
    	// going throw all changeSets ...
    	Iterator<ChangeSet> changesSetIterator = changes.values().iterator();
    	while( changesSetIterator.hasNext() ) {
    		// ... and process them
    		processChangeSet( changesSetIterator.next() );
    		
    		// limiter
    		if( n++ >= 5 )
    			break;
    	}
    	
    	// After everthing is done: Hide the bodies...
    	cleanUp();
    }
    
    private static void prepare() {
    	
    	if( log.isInfoEnabled() )
    		log.info("Loading Properties");
    	    	
    	// inits the Properties System
    	Properties.init();
    	
    	if( log.isInfoEnabled() )
    		log.info("prepare working directory");
    	
    	// working dir
    	Properties.checkAndInitWorkingDir();
    }
    
    private static void initConnectors() {
    	
    	if( log.isInfoEnabled() )
    		log.info("Start GraphDb/MORRE connector");
    	
    	// GraphDb connector
    	try {
			graphDb = new GraphDb( new URL( Properties.getProperty("de.unirostock.sems.ModelCrawler.graphDb.api") ) );
		} catch (MalformedURLException e) {
			log.fatal("Malformed Url for MORRE in config file", e);
		}
    	
    	if( log.isInfoEnabled() )
    		log.info("Start BioModelsDb connector");
    	
    	try {
			bioModelsDb = new BioModelsDb(graphDb);
		} catch (MalformedURLException e) {
			log.fatal("Malformed Url for the BioModelsDb in config file", e);
		} catch (IllegalArgumentException e) {
			log.fatal("Something went wrong with the config while starting BioModelsDb connector.", e);
		}
    	
    }
    
    private static void cleanUp() {
    	// cleanes BioModelsDb connector workingDir
    	bioModelsDb.cleanUp();
    }
    
    private static void processChangeSet( ChangeSet changeSet ) {
    	
    	if( log.isInfoEnabled() )
    		log.info( MessageFormat.format("Start processing ChangeSet for model {0} with {1} entrie(s)", changeSet.getModelId(), changeSet.getChanges().size() ) );
    	
    	Iterator<Change> changeIterator = changeSet.getChanges().iterator();
		while( changeIterator.hasNext() ) {
			Change change = changeIterator.next();
			
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("pushes model {0}:{1}", change.getModelId(), change.getVersionId()) );
			
			// Push it into XmlFileRepository!
			try {
				change.pushToXmlFileServer();
			} catch (XmlNotFoundException e) {
				log.error( "Can not find xml file while pushing to the server! This version of the model will not be pushed to the XmlFileServer.", e);
			} catch (UnsupportedUriException e) {
				log.error( "Can not push the file to the XmlFileServer. Unsupported URL.", e);
			}
			
			// insert it into GraphDb via MORRE
			try {
				graphDb.insertModel( change );
			} catch (GraphDatabaseCommunicationException e) {
				log.error( MessageFormat.format("CommunicationError while pushing model {0}:{1} into the database!", change.getModelId(), change.getVersionId()), e);
			} catch (GraphDatabaseError e) {
				log.error( MessageFormat.format("Error from database while pushing model {0}:{1} into the database!", change.getModelId(), change.getVersionId()), e);
			}
		}
    }
    
}
