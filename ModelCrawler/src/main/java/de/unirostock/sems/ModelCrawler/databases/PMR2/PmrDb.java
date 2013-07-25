package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.aragost.javahg.Repository;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.databases.PMR2.exceptions.HttpException;

public class PmrDb implements ModelDatabase {

	private static final String HASH_ALGO = "MD5";

	private final Log log = LogFactory.getLog( PmrDb.class );

	protected File workingDir;
	protected java.util.Properties config;
	protected GraphDatabase graphDb;
	protected URI repoListUri;
	
	public PmrDb( GraphDatabase graphDb ) throws IllegalArgumentException, URISyntaxException {
		this( Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.RepoList"), graphDb );
	}
	
	public PmrDb(String repoListUrl, GraphDatabase graphDb) throws URISyntaxException, IllegalArgumentException {
		this.graphDb = graphDb; 

		this.repoListUri = new URI(repoListUrl);
		// Http only!
		if( !repoListUri.getScheme().toLowerCase().equals("http") )
			throw new IllegalArgumentException("Only http is supported for the Repository List at the moment!");

		// Prepare WorkingDir 
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
		// save the config
		saveProperties();
	}

	@Override
	public void run() {
		
		List<String> repositories = null;		
		
		// list all available Repos
		try {
			repositories = getRepositoryList();
		} catch (HttpException e) {
			log.fatal("Can not download RepositoryList", e);
		}
		
		// TODO get dirs, clone/pull, search for models, log files
		
		Iterator<String> iter = repositories.iterator();
		while( iter.hasNext() ) {
			Repository repo = null;
			boolean hasChanges = false;
			
			String repoName = iter.next();
			File location = getRepositoryDirectory(repoName);
			
			if( location == null ) {
				// Repo is unknown -> make a directory
				location = makeRepositoryDirectory(repoName);
				repo = cloneRepository(location, repoName);
				// of course there are changes
				hasChanges = true;
			}
			else {
				// Repo is already known -> make a pull
				Entry<Repository, Boolean> pullResult = pullRepository(location);
				repo = pullResult.getKey();
				// are there changes in the Repo?
				hasChanges = pullResult.getValue();
			}
			
			// Scan for cellml files and transfer them
			scanAndTransferRepository(location, repo);
			
		}
		

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
	
	
	/**
	 * Retrieves the txt Repository List and puts it in a list
	 * 
	 * @return
	 * @throws HttpException
	 */
	protected List<String> getRepositoryList() throws HttpException {
		List<String> repoList = new LinkedList<String>();

		HttpClient httpClient = new DefaultHttpClient(); 
		HttpGet request = new HttpGet(repoListUri);

		try {
			HttpResponse response = httpClient.execute(request);

			InputStream entityStream = response.getEntity().getContent();
			Scanner scanner = new Scanner(entityStream).useDelimiter("\n");
			while( scanner.hasNext() ) {
				String repo = scanner.next();
				if( repo != null && !repo.isEmpty() )
					repoList.add( scanner.next() );				
			}

		} catch (ClientProtocolException e) {
			throw new HttpException("Can not download RepositoryList", e);
		} catch (IOException e) {
			throw new HttpException("IOException while downloading RepositoryList", e);
		}

		return repoList;
	}
	
	
	/**
	 * Creates the directory for the given Repository
	 *  
	 * @param repository
	 * @return
	 */
	protected File makeRepositoryDirectory( String repository ) {
		
		File directory = null;
		String name = null;
		
		String repoHash = calculateRepositoryHash(repository);
		
		// trys to get the RepoDir from config file
		if( (directory = getRepositoryDirectory(repository)) == null ) {
			// when fails -> generates a name

			//		name = repository.replace("http://", "");
			//		name = repository.replace("/", "_");
			name = repository.substring( repository.lastIndexOf('/') ) + "_" + repository.hashCode();
			
			// check if directory already exists
			directory = new File( workingDir, name );
			if( directory.exists() && directory.isDirectory() ) {
				// exists -> extends with a number
				String newName = name;
				int i = 2;
				do {
					newName = name + "_" + i;
					directory = new File( workingDir, newName );
					i++;
				} while( directory.exists() );
				name = newName;
			}
			
			// store the directory name into the config
			config.setProperty("repo." + repoHash, name);
			
		}
		
		// when directory does not exists...
		if( !directory.exists() ) {
			// ...creates it!
			directory.mkdirs();
		}
		
		return directory;

	}
	
	/**
	 * Gets the Path to the Repository Directory out of Workspace config or null if it fails
	 * 
	 * @param repository
	 * @return File
	 */
	protected File getRepositoryDirectory( String repository ) {
		String repoHash = calculateRepositoryHash(repository);
		String name = null;
		
		if( (name = config.getProperty("repo." + repoHash)) != null )
			return new File( workingDir, name );
		else
			return null;
	}
	
	/**
	 * Calculates the hash from the Repository URL
	 * 
	 * @param repository
	 * @return
	 */
	private String calculateRepositoryHash( String repository ) {
		String repoHash = null;
		
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGO);
			repoHash = new String( digest.digest( repository.getBytes() ) );
		} catch (NoSuchAlgorithmException e) {
			log.fatal( MessageFormat.format("Can not calc Repository Hash for {0}!", repository), e );
		}
		
		return repoHash;
	}
	
	protected Repository cloneRepository(File local, String remote) {
		
		return null;
	}
	
	protected Entry<Repository, Boolean> pullRepository(File location) {
		
		return new AbstractMap.SimpleEntry<Repository, Boolean>(null, false);
	}
	
	protected void scanAndTransferRepository( File location, Repository repo ) {
		
		
	}
}
