package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.LogCommand;
import com.aragost.javahg.commands.PullCommand;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.GraphDb.ModelRecord;
import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseCommunicationException;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseError;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseInterfaceException;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
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

	protected Map<String, ChangeSet> changeSetMap = new HashMap<String, ChangeSet>();

	public PmrDb( GraphDatabase graphDb ) throws IllegalArgumentException {
		this( Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.RepoList"), graphDb );
	}

	public PmrDb(String repoListUrl, GraphDatabase graphDb) throws IllegalArgumentException {
		this.graphDb = graphDb; 

		try {
			this.repoListUri = new URI(repoListUrl);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Uri Syntax Error in the RepositoryList URL. Maybe a config mistake?", e);
		}
		
		// Http only!
		if( !repoListUri.getScheme().toLowerCase().equals("http") )
			throw new IllegalArgumentException("Only http is supported for the Repository List at the moment!");

		// Prepare WorkingDir 
		checkAndInitWorkingDir();
	}

	@Override
	public List<String> listModels() {
		return new ArrayList<String>( changeSetMap.keySet() );
	}

	@Override
	public Map<String, ChangeSet> listChanges() {
		return changeSetMap;
	}

	@Override
	public ChangeSet getModelChanges(String modelId) {
		return changeSetMap.get(modelId);
	}

	@Override
	public void cleanUp() {
		// save the config
		saveProperties();
	}

	@Override
	public void run() {

		List<String> repositories = null;		

		log.info("Start crawling the PMR2 Database by going throw the Mercurial Workspaces");

		// list all available Repos
		try {
			repositories = getRepositoryList();
		} catch (HttpException e) {
			log.fatal("Can not download RepositoryList", e);
		}

		// TODO get dirs, clone/pull, search for models, log files

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Iterate throw {0} repositories", repositories.size()) );
		
		// XXX Limiter
		int limiter = 1;
		
		Iterator<String> iter = repositories.iterator();
		while( iter.hasNext() ) {
			Repository repo = null;
			boolean hasChanges = false;
			String repoName = iter.next();

			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("Check Repository {0}", repoName ) );

			File location = getRepositoryDirectory(repoName);
			if( location == null ) {

				if( log.isDebugEnabled() )
					log.debug( MessageFormat.format("Repository {0} is unknown. Create new folder and clone it", repoName) );

				// Repo is unknown -> make a directory
				location = makeRepositoryDirectory(repoName);
				repo = cloneRepository(location, repoName);

				if( log.isInfoEnabled() )
					log.info( MessageFormat.format("Repository {0} has been cloned into {1}", repoName, location.getAbsolutePath()) );

				// of course there are changes
				hasChanges = true;
			}
			else {

				if( log.isDebugEnabled() )
					log.debug( MessageFormat.format("Repository {0} is known. Perform a Pull-Request into local copy {1}", repoName, location.getAbsolutePath()) );

				// Repo is already known -> make a pull
				Entry<Repository, Boolean> pullResult = pullRepository(location);
				repo = pullResult.getKey();
				// are there changes in the Repo?
				hasChanges = pullResult.getValue();

				if( log.isInfoEnabled() ) {
					if( hasChanges )
						log.info( MessageFormat.format("Pulled changes from {0} into local copy {1}", repoName, location.getAbsolutePath()) );
					else
						log.info( "No changes to pull. Local copy is up to date." );
				}
			}

			if( hasChanges ) {
				// Scan for cellml files and transfer them
				scanAndTransferRepository(location, repo);
			}
			
			if( limiter++ >= 5 )
				break;
			
		}

		log.info("Finished crawling PMR2 Database.");

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
			digest.update( repository.getBytes() );
			repoHash = (new BigInteger( digest.digest() )).toString(16);
		} catch (NoSuchAlgorithmException e) {
			log.fatal( MessageFormat.format("Can not calc Repository Hash for {0}!", repository), e );
		}

		return repoHash;
	}

	protected Repository cloneRepository(File local, String remote) {
		Repository repo = Repository.clone(local, remote);
		if( repo == null )
			log.fatal( MessageFormat.format("Can not clone Mercurial Repository {0} into {1}", remote, local.getAbsolutePath()) );

		return repo;
	}

	protected Entry<Repository, Boolean> pullRepository(File location) {
		boolean hasChanges = false;
		Repository repo = Repository.open(location);

		if( repo != null) {
			PullCommand pull = new PullCommand(repo);

			try {
				List<Changeset> changes = pull.execute();
				// when pull was successful and there are some Changes
				if( pull.isSuccessful() && changes.size() > 0)
					hasChanges = true;

			} catch (IOException e) {
				log.fatal( MessageFormat.format("Can not pull Mercurial Repository into {0}", location.getAbsolutePath()), e);
			}
		}

		return new AbstractMap.SimpleEntry<Repository, Boolean>(repo, hasChanges);
	}
	
	protected void scanAndTransferRepository( File location, Repository repo ) {
		// select all relevant files
		// than going throw the versions
		List<RelevantFile> relevantFiles;
		List<Changeset> relevantVersions;
		
		
		// select all relevant files
		relevantFiles = scanRepository(location, repo);
		// looking for the latestVersion
		Iterator<RelevantFile> iter = relevantFiles.iterator();
		while( iter.hasNext() ) {
			searchLatestKnownVersion( iter.next() );
		}
		
		// detect all relevant versions
		relevantVersions = detectRelevantVersions(repo, relevantFiles);
		// sorting them (just in case...)
		Collections.sort(relevantVersions, new Comparator<Changeset>() {
			@Override
			public int compare(Changeset cs1, Changeset cs2) {
				return cs1.getTimestamp().getDate().compareTo( cs2.getTimestamp().getDate() );
			}
		} );
		
		
		
	}
	
	protected List<RelevantFile> scanRepository( File location, Repository repo ) {
		List<RelevantFile> relevantFiles = new LinkedList<RelevantFile>();
		
		return relevantFiles;
	}
	
	protected void searchLatestKnownVersion( RelevantFile relevantFile ) {
		String versionId = null;
		Date versionDate = null;
		ChangeSet changeSet = null;
		
		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Searches latest known version for model {0}", relevantFile.getModelId()) );
		
		if( (changeSet = changeSetMap.get(relevantFile.getModelId())) != null ) {
			// there is a changeSet for this modelId, get the latestChange
			
			if( log.isInfoEnabled() )
				log.info("ChangeSet available");
			
			Change latestChange = changeSet.getLatestChange();
			if( latestChange != null ) {
				versionId = latestChange.getVersionId();
				versionDate = latestChange.getVersionDate();
			}
			else if( log.isDebugEnabled() ) {
				log.debug("But no change setted");
			}
		}
		
		// versionId and versionDate are still not set
		if( versionId == null && versionDate == null ) {
			
			if( log.isInfoEnabled() )
				log.info("Start database request");
			
			// search in database
			ModelRecord latest = null;
			try {
				latest = graphDb.getLatestModelVersion( relevantFile.getModelId() );
			} catch (GraphDatabaseCommunicationException e) {
				log.fatal( MessageFormat.format("Getting latest model version from {0}, to check, if processed model version is new, failed", relevantFile.getModelId()), e);
			} catch (GraphDatabaseError e) {
				// error occurs, when modelId is unknown to the database -> so we can assume the change is new!
				log.warn("GraphDatabaseError while checking, if processed model version is new. It will be assumed, that this is unknown to the database!", e);
			}
			
			if( latest != null ) {
				versionId = latest.getVersionId();
				versionDate = latest.getVersionDate();
			}
		}
		
		relevantFile.setLatestKnownVersion(versionId, versionDate, (PmrChangeSet) changeSet);
		
	}
	
	protected List<Changeset> detectRelevantVersions( Repository repo, List<RelevantFile> relevantFiles ) {
		String[] files;
		String oldestLatestVersionId = null;
		Date oldestLatestVersionDate = null;
		List<Changeset> relevantVersions;
		
		// make a list of all relevant files
		files = new String[relevantFiles.size()];
		int index = 0;
		
		// put the list into the array and gets the oldestLatestVersion :)
		Iterator<RelevantFile> fileIter = relevantFiles.iterator();
		while( fileIter.hasNext() ) {
			RelevantFile file = fileIter.next();
			
			files[index] = file.getFilePath();
			index++;
			
			// checks if the current processed relevantFile has an older latestVersion as the
			// former olderLatestVersion
			if( oldestLatestVersionDate == null ) {
				oldestLatestVersionId = file.getLatestVersionId();
				oldestLatestVersionDate = file.getLatestVersionDate();
			}
			else if( file.getLatestVersionDate().compareTo(oldestLatestVersionDate) < 0 ) {
				oldestLatestVersionId = file.getLatestVersionId();
				oldestLatestVersionDate = file.getLatestVersionDate();
			}
			
		}
		
		// perform the log command to evaluate all interesting hg changesets
		LogCommand logCmd = new LogCommand(repo);
		relevantVersions = logCmd.execute(files);
		
		// remove every Changeset which is older as the oldestLatestVersion (because they are really uninteresting)
		Iterator<Changeset> changesetIter = relevantVersions.iterator();
		while( changesetIter.hasNext() ) {
			if( changesetIter.next().getTimestamp().getDate().compareTo(oldestLatestVersionDate) < 0 )
				changesetIter.remove();
		}
		
		
		return relevantVersions;
	}
	
}
