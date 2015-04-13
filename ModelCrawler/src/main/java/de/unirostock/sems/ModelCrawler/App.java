package de.unirostock.sems.ModelCrawler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
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
import de.unirostock.sems.ModelCrawler.databases.PMR2.PmrDb;
import de.unirostock.sems.ModelCrawler.exceptions.ConfigurationException;
import de.unirostock.sems.ModelCrawler.storage.ModelStorage;
import de.unirostock.sems.morre.client.MorreCrawlerInterface;
import de.unirostock.sems.morre.client.exception.MorreException;
import de.unirostock.sems.morre.client.impl.HttpMorreClient;

/**
 * Hello world!
 *
 */
public class App {
	private static final Log log = LogFactory.getLog( App.class );
	
	public static enum WorkingMode {
		NORMAL,
		TEMPLATE_CONFIG,
		TEST
	}
	
	private static MorreCrawlerInterface morreClient;
	private static ModelStorage storage = null;

	public static void main( String[] args ) {
		File configFile = null;
		WorkingMode mode = WorkingMode.NORMAL;
		
		if( args.length == 0 ) {
			printHelp();
			System.exit(0);
		}
		
		for( int index = 0; index < args.length; index++ ) {
			
			if( args[index].equals("-c") || args[index].equals("--config") )
				configFile = new File(args[++index]);
			else if( args[index].equals("--template") )
				mode = WorkingMode.TEMPLATE_CONFIG;
			else if( args[index].equals("--test") )
				mode = WorkingMode.TEST;
		}
		
		log.info("ModelCrawler startet");
		
		if( mode == WorkingMode.TEMPLATE_CONFIG ) {
			if( configFile == null ) {
				log.error("No config file provided, use -c flag");
				System.exit(0);
			}
			
			log.info( MessageFormat.format("Writing default config to {0}", configFile) );
			
			Config config = Config.defaultConfig();
			config.getDatabases().add( new BioModelsDb() );
			config.getDatabases().add( new PmrDb() );
			
			try {
				Config.getConfig().save(configFile);
			} catch (ConfigurationException e) {
				log.fatal( MessageFormat.format("Can not save config file {0}", configFile), e );
			}
			
			log.info("done.");
			System.exit(0);
		}
		
		// load config
		try {
			Config.load( configFile );
		} catch (ConfigurationException e) {
			log.fatal( MessageFormat.format("Can not load config file {0}", configFile), e );
		}
		
		
		// Connectors
		Config config = Config.getConfig();
		initConnectors( config );

		// map for all changes!
		Map<String, ChangeSet> changes = new HashMap<String, ChangeSet>();

		// run it!
		for( ModelDatabase database : config.getDatabases() ) {
			
			if( database.isEnabled() == false )
				continue;
			
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("running crawler for {0}", database.getClass().getName()) );
			
			database.call();
			
			// add all changes to the change Map
			changes.putAll( database.listChanges() );
			
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("finished crawling for {0}", database.getClass().getName()) );
		}
		
		if( mode == WorkingMode.TEST )
			log.info("Don not push ChangeSets to morre in test-mode");
		else {
	    	Iterator<ChangeSet> changesSetIterator = changes.values().iterator();
	    	while( changesSetIterator.hasNext() ) {
	    		// ... and process them
	    		processChangeSet( changesSetIterator.next() );
	    	}
		}

		// After everthing is done: Hide the bodies...
		close();

		log.info("finished crawling");
	}

	private static void printHelp() {
		System.out.println("ModelCrawler");
		System.out.println(
				"  -c               Path to config\n" + 
				"  --config \n" +
				"  --template       Writes down a template config file (overrides existing config!) \n" +
				"  --test           Test mode. Nothing is pushed to morre \n"
		);
	}

	private static void initConnectors(Config config) {

		if( log.isInfoEnabled() )
			log.info("Start GraphDb/MORRE connector");

		// morre.client connector
		try {
			morreClient = new HttpMorreClient( config.getMorreUrl() );
		} catch (MalformedURLException e) {
			log.fatal("Malformed Url for MORRE in config file", e);
		}
		
		// setting morre for each database connector
		for( ModelDatabase connector : config.getDatabases() ) {
			connector.setMorreClient(morreClient);
		}
		
		// TODO create ModelStorage

	}

	private static void close() {
		log.info("Cleans everything up!");
		
		// closing every database connector
		for( ModelDatabase database : Config.getConfig().getDatabases() ) {
			if( database.isEnabled() == false )
				continue;
			
			database.close();
		}
	}

	private static void processChangeSet( ChangeSet changeSet ) {
		
		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Start processing ChangeSet for model {0} with {1} entrie(s)", changeSet.getFileId(), changeSet.getChanges().size() ) );

		Iterator<Change> changeIterator = changeSet.getChanges().iterator();
		Change change = null;
		try {
			while( changeIterator.hasNext() ) {
				change = (Change) changeIterator.next();

				if( log.isInfoEnabled() )
					log.info( MessageFormat.format("pushes model {0}:{1}", change.getFileId(), change.getVersionId()) );

				// store the model
				URI modelUri = storage.storeModel(change);
				change.setXmldoc( modelUri.toString() );
				
				// insert the model into MaSyMos via Morre
				morreClient.addModel(change);
			}
		} catch (IOException e) {
			log.fatal( MessageFormat.format("Some IO stuff went wrong while pushing model {0} !", change), e);
		} catch (MorreException e) {
			log.error( MessageFormat.format("Morre encountered an error while puschin model {0} : {1}", change, e.getMessage()), e);
		}

	}

}
