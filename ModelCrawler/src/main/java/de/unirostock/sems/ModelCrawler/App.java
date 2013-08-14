package de.unirostock.sems.ModelCrawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.GraphDb.GraphDb;
import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseCommunicationException;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseError;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.databases.Interface.exceptions.XmlNotFoundException;
import de.unirostock.sems.ModelCrawler.databases.PMR2.PmrDb;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final Log log = LogFactory.getLog( App.class );
	
	private static GraphDatabase graphDb;
	private static ModelDatabase bioModelsDb;
	private static ModelDatabase pmr2Db;
	
    public static void main( String[] args ) {
    	
    	log.info("ModelCrawler startet");
    	// Properties and WorkingDir
    	prepare();
    	// Connectors
    	initConnectors();
    	
    	// map for all changes!
    	Map<String, ChangeSet> changes = new HashMap<String, ChangeSet>();
    	
    	// run it!
    	
    	if( log.isInfoEnabled() )
    		log.info("running BioModelsDb Crawler");
    	
    	bioModelsDb.run();
    	
    	// add all changes from BioModelsDb to the change Map
    	changes.putAll( bioModelsDb.listChanges() );
    	
    	
    	if( log.isInfoEnabled() )
    		log.info("running PMR2 Crawler");
    	
    	pmr2Db.run();
    	// add all changes from PMR2 to the change Map
    	changes.putAll( pmr2Db.listChanges() );
    	
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
    	
    	log.info("finished crawling");
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
    		log.info("Starting BioModelsDb connector");
    	
    	try {
			bioModelsDb = new BioModelsDb(graphDb);
		} catch (MalformedURLException e) {
			log.fatal("Malformed Url for the BioModelsDb in config file", e);
		} catch (IllegalArgumentException e) {
			log.fatal("Something went wrong with the config while starting BioModelsDb connector.", e);
		}
    	
    	if( log.isInfoEnabled() )
    		log.info("Starting Pmr2Db connector");
    	
    	try {
			pmr2Db = new PmrDb(graphDb);
		} catch (IllegalArgumentException e) {
			log.fatal("IllegalArgument Exception while init the PMR2 connector. Maybe a config error?", e);
		}
    	
    }
    
    private static void cleanUp() {
    	log.info("Cleans everything up!");
    	
    	// cleans BioModelsDb connector workingDir
    	bioModelsDb.cleanUp();
    	
    	// cleans PMR2 connector workingDir
    	pmr2Db.cleanUp();
    }
    
    private static void processChangeSet( ChangeSet changeSet ) {
    	
    	if( log.isInfoEnabled() )
    		log.info( MessageFormat.format("Start processing ChangeSet for model {0} with {1} entrie(s)", changeSet.getModelId(), changeSet.getChanges().size() ) );
    	
    	Iterator<Change> changeIterator = changeSet.getChanges().iterator();
    	Change change = null;
    	try {
		while( changeIterator.hasNext() ) {
			change = changeIterator.next();
			
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("pushes model {0}:{1}", change.getModelId(), change.getVersionId()) );
			
			// Push it into XmlFileRepository!
			change.pushToXmlFileServer();
			// insert it into GraphDb via MORRE
			graphDb.insertModel( change );
		}
    	} catch (XmlNotFoundException e) {
    		log.fatal( MessageFormat.format("Can not find xml file while pushing model {0}:{1} to the server!", change.getModelId(), change.getVersionId()), e);
		} catch (UnsupportedUriException e) {
			log.fatal( MessageFormat.format("Can not push the file to the XmlFileServer. Unsupported URL: {0}", change.getDocumentUri()), e );
		} catch (GraphDatabaseCommunicationException e) {
			log.fatal( MessageFormat.format("CommunicationError while pushing model {0}:{1} into the database!", change.getModelId(), change.getVersionId()), e);
		} catch (GraphDatabaseError e) {
			log.fatal( MessageFormat.format("Error from database while pushing model {0}:{1} into the database!", change.getModelId(), change.getVersionId()), e);
		}
    	
    }
    
}
