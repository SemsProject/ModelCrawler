package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;

public class PmrDb implements ModelDatabase {
	
	private final Log log = LogFactory.getLog( PmrDb.class );
	
	protected File workingDir;
	protected java.util.Properties config;
	protected GraphDatabase graphDb;
	
	public PmrDb(String repoListUrl, GraphDatabase graphDb) {
		this.graphDb = graphDb; 
		
		
		checkAndInitWorkingDir();
	}

	@Override
	public List<String> listModels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, ChangeSet> listChanges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeSet getModelChanges(String modelId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
	
	protected void checkAndInitWorkingDir() {

		workingDir = new File( Properties.getWorkingDir(), Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.subWorkingDir") );

		log.trace( "Preparing working dir " + workingDir.getAbsolutePath() );

		if( workingDir.exists() == false ) {
			// creates it!
			workingDir.mkdirs();
		}

		// inits the config
		config = new java.util.Properties();
		log.info("Loading working dir config");
		try {
			File configFile = new File( workingDir, Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.workingDirConfig", "config.properties") );
			if( configFile.exists() ) {
				FileReader configFileReader = new FileReader( configFile );
				if( configFileReader != null ) {
					config.load(configFileReader);
					configFileReader.close();
				}

			}

		}
		catch (IOException e) {
			log.fatal( "IOException while reading the workingdir config file", e );
		}

	}

	protected void saveProperties() {

		if( config == null ) {
			config = new java.util.Properties();
		}

		try {
			FileWriter configFile = new FileWriter( new File( workingDir, Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.workingDirConfig", "config.properties") ));
			config.store(configFile, null);
			log.info("working dir config saved!");
		} catch (IOException e) {
			log.error( "Can not write the workingDir config file!", e );
		}

	}
	
}
