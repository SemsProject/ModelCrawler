package de.unirostock.sems.ModelCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.databases.Interface.exceptions.XmlNotFoundException;
import de.unirostock.sems.ModelCrawler.databases.PMR2.PmrDb;
import de.unirostock.sems.XmlFileServerClient.XmlFileServer;
import de.unirostock.sems.XmlFileServerClient.XmlFileServerClientFactory;
import de.unirostock.sems.XmlFileServerClient.exceptions.ModelAlreadyExistsException;
import de.unirostock.sems.XmlFileServerClient.exceptions.UnsupportedUriException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerBadRequestException;
import de.unirostock.sems.XmlFileServerClient.exceptions.XmlFileServerProtocollException;
import de.unirostock.sems.morre.client.MorreCrawlerInterface;
import de.unirostock.sems.morre.client.exception.MorreException;
import de.unirostock.sems.morre.client.impl.HttpMorreClient;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final Log log = LogFactory.getLog( App.class );

	private static MorreCrawlerInterface morreClient;
	private static ModelDatabase bioModelsDb;
	private static ModelDatabase pmr2Db;

	private static XmlFileServer xmlFileServer = null;

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
//		int n = 1; // limiter

		// going throw all changeSets ...
		    	Iterator<ChangeSet> changesSetIterator = changes.values().iterator();
		    	while( changesSetIterator.hasNext() ) {
		    		// ... and process them
		    		processChangeSet( changesSetIterator.next() );
		    		
		    		// limiter
//		    		if( n++ >= 5 )
//		    			break;
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

		// morre.client connector
		try {
			morreClient = new HttpMorreClient(Properties.getProperty("de.unirostock.sems.ModelCrawler.graphDb.api"));
		} catch (MalformedURLException e) {
			log.fatal("Malformed Url for MORRE in config file", e);
		}

		if( log.isInfoEnabled() )
			log.info("Starting BioModelsDb connector");
		// BioModelsDatabase
		try {
			bioModelsDb = new BioModelsDb(morreClient);
		} catch (MalformedURLException e) {
			log.fatal("Malformed Url for the BioModelsDb in config file", e);
		} catch (IllegalArgumentException e) {
			log.fatal("Something went wrong with the config while starting BioModelsDb connector.", e);
		}

		if( log.isInfoEnabled() )
			log.info("Starting Pmr2Db connector");
		// PMR2 aka CellML
		try {
			pmr2Db = new PmrDb(morreClient);
		} catch (IllegalArgumentException e) {
			log.fatal("IllegalArgument Exception while init the PMR2 connector. Maybe a config error?", e);
		}

		if( log.isInfoEnabled() )
			log.info("Starting Http XmlFileServer connector");

		try {
			xmlFileServer = XmlFileServerClientFactory.getClient( new URI(Properties.getProperty("de.unirostock.sems.ModelCrawler.xmlFileServer")) );
		} catch (URISyntaxException e) {
			log.fatal("Can not start XmlFileServer connector! URI is invalid! Maybe a config erro?", e);
		}

	}

	private static void cleanUp() {
		log.info("Cleans everything up!");

		// cleans BioModelsDb connector workingDir
		bioModelsDb.close();

		// cleans PMR2 connector workingDir
		pmr2Db.close();
	}

	private static void processChangeSet( ChangeSet changeSet ) {
		
		//XXX some sort of filter
//		if( !changeSet.getFileId().equals("http%3A%2F%2Fmodels.cellml.org%2Fworkspace%2F186%2Fdecker_2009.cellml") )
//			return;
		
		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Start processing ChangeSet for model {0} with {1} entrie(s)", changeSet.getFileId(), changeSet.getChanges().size() ) );

		Iterator<Change> changeIterator = changeSet.getChanges().iterator();
		Change change = null;
		try {
			while( changeIterator.hasNext() ) {
				change = (Change) changeIterator.next();

				if( log.isInfoEnabled() )
					log.info( MessageFormat.format("pushes model {0}:{1}", change.getFileId(), change.getVersionId()) );

				// Push it into XmlFileRepository!
				change.pushToXmlFileServer( xmlFileServer );
				// insert the model into MaSyMos via Morre
				morreClient.addModel(change);
			}
		} catch (XmlNotFoundException e) {
			log.fatal( MessageFormat.format("Can not find xml file while pushing model {0} to the server!", change), e);
		} catch (ModelAlreadyExistsException e) {
			log.fatal( MessageFormat.format("The model {0} already exists on the XmlFileServer!", change), e);
		} catch (XmlFileServerBadRequestException e) {
			log.fatal( MessageFormat.format("The XmlFileServer received a Bad Request while pushing model {0} !", change), e);
		} catch (UnsupportedUriException e) {
			log.fatal( MessageFormat.format("The URI from model {0} is not Supported!", change), e);
		} catch (XmlFileServerProtocollException e) {
			log.fatal( MessageFormat.format("ProtocollError while pushing model {0} into the XmlFileServer!", change), e);
		} catch (IOException e) {
			log.fatal( MessageFormat.format("Some IO stuff went wrong while pushing model {0} !", change), e);
		} catch (MorreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
