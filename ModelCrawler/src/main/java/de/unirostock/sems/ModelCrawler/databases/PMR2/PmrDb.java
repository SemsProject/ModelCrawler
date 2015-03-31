package de.unirostock.sems.ModelCrawler.databases.PMR2;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import de.binfalse.bfutils.GeneralTools;
import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;
import de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase;
import de.unirostock.sems.ModelCrawler.databases.PMR2.exceptions.HttpException;
import de.unirostock.sems.ModelCrawler.helper.CrawledModelRecord;
import de.unirostock.sems.bives.tools.DocumentClassifier;
import de.unirostock.sems.morre.client.MorreCrawlerInterface;
import de.unirostock.sems.morre.client.exception.MorreCommunicationException;
import de.unirostock.sems.morre.client.exception.MorreException;


/*import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.ExecutionException;
import com.aragost.javahg.commands.LogCommand;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.UpdateCommand;*/
import org.eclipse.jgit.api.CheckoutCommand;

// TODO: Auto-generated Javadoc
/**
 * The Class PmrDb.
 */
public class PmrDb implements ModelDatabase {

	/** The Constant HASH_ALGO. */
	private static final String HASH_ALGO = "MD5";

	/** The log. */
	private final Log log = LogFactory.getLog( PmrDb.class );

	/** The working dir. */
	protected File workingDir;
	
	/** The temp dir. */
	protected File tempDir;
	
	/** The config. */
	protected java.util.Properties config;
	
	/** The morre client. */
	protected MorreCrawlerInterface morreClient;
	
	/** The repo list uri. */
	protected URI repoListUri;
	
	/** The classifier. */
	protected DocumentClassifier classifier = null;
	
	/** The file extension blacklist. */
	protected HashSet<String> fileExtensionBlacklist = null;

	/** The change set map. */
	protected Map<String, ChangeSet> changeSetMap = new HashMap<String, ChangeSet>();
	
	// REMIND there is difference between ChangeSet and Changeset
	// ChangeSet is a ModelCrawler Dataholder class
	// and Changeset a JavaHg Dataholder class

	/**
	 * The Constructor.
	 *
	 * @param morreClient the morre client
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public PmrDb( MorreCrawlerInterface morreClient ) throws IllegalArgumentException {
		this( Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.RepoList"), morreClient );
	}

	/**
	 * The Constructor.
	 *
	 * @param repoListUrl the repo list url
	 * @param morreClient the morre client
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public PmrDb(String repoListUrl, MorreCrawlerInterface morreClient) throws IllegalArgumentException {
		this.morreClient = morreClient;

		try {
			this.repoListUri = new URI(repoListUrl);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Uri Syntax Error in the RepositoryList URL. Maybe a config mistake?", e);
		}

		// Http only!
		if( !repoListUri.getScheme().toLowerCase().equals("http") )
			throw new IllegalArgumentException("Only http is supported for the Repository List at the moment!");

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Init new PMR2 Connector based on Repolist: {0}", this.repoListUri) );

		// Prepare BiVeS Model Classifier
		//try {
			classifier = new DocumentClassifier ();
		/*} catch (ParserConfigurationException e) {
			log.fatal( "ParserConfigurationException while init BiVeS Document Classifier", e );
		}*/

		if( log.isInfoEnabled() )
			log.info("Started BiVeS Classifier");

		// Prepare WorkingDir 
		checkAndInitWorkingDir();
		
		// load fileExtensionBlacklist
		fileExtensionBlacklist = new HashSet<String>();
		String blacklist[] = Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.extensionBlacklist", "html").split(Properties.ELEMENT_SPLITTER) ;
		for( int index = 0; index < blacklist.length; index++ )  {
			fileExtensionBlacklist.add( blacklist[index] );
		}
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase#listModels()
	 */
	@Override
	public List<String> listModels() {
		return new ArrayList<String>( changeSetMap.keySet() );
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase#listChanges()
	 */
	@Override
	public Map<String, ChangeSet> listChanges() {
		return changeSetMap;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase#getModelChanges(java.lang.String)
	 */
	@Override
	public ChangeSet getModelChanges(String fileId) {
		return changeSetMap.get(fileId);
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase#cleanUp()
	 */
	@Override
	public void cleanUp() {
		// save the config
		saveProperties();

		// deletes the tempDir recursively
		try {
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			log.error("Error while cleaning up the temp dir!", e);
		}
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.ModelDatabase#run()
	 */
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

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Iterate throw {0} repositories", repositories.size()) );

		// XXX Limiter
		int limiter = 1;

		Iterator<String> iter = repositories.iterator();
		while( iter.hasNext() ) {
			Git repo = null;
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
				Entry<Git, Boolean> pullResult = pullRepository(location);
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
				// Scan for cellml and other model files and transfer them
				scanAndTransferRepository(repoName, location, repo);
			}
			
			// closes the repo
			if( repo != null )
				repo.close();
			
//			if( limiter++ >= 5 )
//				break;

		}

		log.info("Finished crawling PMR2 Database.");

	}

	/**
	 * Check and init working dir.
	 */
	protected void checkAndInitWorkingDir() {

		workingDir = new File( Properties.getWorkingDir(), Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.subWorkingDir") );
		tempDir = new File( Properties.getWorkingDir(), Properties.getProperty("de.unirostock.sems.ModelCrawler.PMR2.subTempDir") );

		log.trace( "Preparing working dir " + workingDir.getAbsolutePath() );

		if( workingDir.exists() == false ) {
			// creates it!
			workingDir.mkdirs();
		}
		if( tempDir.exists() == false ) {
			// creates it!
			tempDir.mkdirs();
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

	/**
	 * Returns a non existent temporary file.
	 *
	 * @return the temp file
	 */
	protected File getTempFile() {
		File temp = new File( tempDir, UUID.randomUUID().toString() );
		while( temp.exists() ) {
			temp = new File( tempDir, UUID.randomUUID().toString() );
		} 

		return temp;
	}

	/**
	 * Save properties.
	 */
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
	 * Retrieves the txt Repository List and puts it in a list.
	 *
	 * @return the repository list
	 * @throws HttpException the http exception
	 */
	protected List<String> getRepositoryList() throws HttpException {
		List<String> repoList = new LinkedList<String>();

		HttpClient httpClient = HttpClientBuilder.create().build(); 
		HttpGet request = new HttpGet(repoListUri);

		try {
			HttpResponse response = httpClient.execute(request);

			InputStream entityStream = response.getEntity().getContent();
			Scanner scanner = new Scanner(entityStream).useDelimiter("\n");
			while( scanner.hasNext() ) {
				String repo = scanner.next();
				if( repo != null && !repo.isEmpty() )
					repoList.add( repo );				
			}
			scanner.close();

		} catch (ClientProtocolException e) {
			throw new HttpException("Can not download RepositoryList", e);
		} catch (IOException e) {
			throw new HttpException("IOException while downloading RepositoryList", e);
		}

		return repoList;
	}


	/**
	 * Creates the directory for the given Repository.
	 *
	 * @param repository the repository
	 * @return the file
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
	 * Gets the Path to the Repository Directory out of Workspace config or null if it fails.
	 *
	 * @param repository the repository
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
	 * Calculates the hash from the Repository URL.
	 *
	 * @param repository the repository
	 * @return the string
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

	/**
	 * Clone repository.
	 *
	 * @param local the local
	 * @param remote the remote
	 * @return the repository
	 */
	protected Git cloneRepository(File local, String remote) {
		
		
		
		Git repo = null;
		
		try
		{
			repo = Git.cloneRepository()
				.setURI( remote )
				.setDirectory( local )
				.setCloneSubmodules(true)		// include all submodules -> important for PMR2-Project
				.call();
		}
		catch (GitAPIException e)
		{
			log.fatal (MessageFormat.format("Can not clone Mercurial Repository {0} into {1}", remote, local.getAbsolutePath()), e);
		}
		//Repository.clone(local, remote);
		if( repo == null )
			log.fatal( MessageFormat.format("Can not clone Mercurial Repository {0} into {1}", remote, local.getAbsolutePath()) );

		return repo;
	}

	/**
	 * Pull repository.
	 *
	 * @param location the location
	 * @return the entry< repository, boolean>
	 */
	protected Entry<Git, Boolean> pullRepository(File location) {
		boolean hasChanges = false;
		
		Git repo = null;
		
		try
		{
			repo = Git.open (location);
		}
		catch (IOException e)
		{
			log.fatal( MessageFormat.format("Can not open Git Repository in {0}", location.getAbsolutePath()), e);
		}

		if( repo != null) {
			//PullCommand pull = new PullCommand(repo);
			PullCommand pull = repo.pull ();

			try {
				PullResult pr = pull.call ();
				if (pr.isSuccessful () && pr.getFetchResult ().getTrackingRefUpdates ().size () > 0)
				
				/*List<Changeset> changes = pull.execute();
				// when pull was successful and there are some Changes
				if( pull.isSuccessful() && changes.size() > 0)*/
					hasChanges = true;

			} catch (GitAPIException e) {
				log.fatal( MessageFormat.format("Can not pull Git Repository into {0}", location.getAbsolutePath()), e);
			}
		}

		return new AbstractMap.SimpleEntry<Git, Boolean>(repo, hasChanges);
	}

	/**
	 * Scan and transfer repository.
	 *
	 * @param repoUrl the repo url
	 * @param location the location
	 * @param repo the repo
	 */
	protected void scanAndTransferRepository( String repoUrl, File location, Git repo ) {
		// select all relevant files
		// than going throw the versions
		List<RelevantFile> relevantFiles;
		Iterable<RevCommit> relevantVersions;

		// TODO Logging!

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Start scanning {0} for changes", repoUrl) );

		// select all relevant files
		relevantFiles = scanRepository(location, repo);

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Found {0} relevant files.", relevantFiles.size()) );

		// generating the fileId and looking for the latestVersion
		try {
			Iterator<RelevantFile> iter = relevantFiles.iterator();
			while( iter.hasNext() ) {
				RelevantFile file = iter.next();
				file.generateFileId(repoUrl);
				if( log.isDebugEnabled() )
					log.debug( MessageFormat.format("Generated fileId {0} for file {1}", file.getFileId(), file.getFilePath()) );

				searchLatestKnownVersion( file );
			}
		}
		catch (UnsupportedEncodingException e) {
			log.fatal("Unsupported Encoding. Can not generate fileId", e);
		}

		// detect all relevant versions
		relevantVersions = detectRelevantVersions(repo, relevantFiles);

		if( relevantVersions == null )
			// no version is relevant - exit
			return;

		// sorting them (just in case...)
		/*Collections.sort(relevantVersions, new Comparator<Changeset>() {
			@Override
			public int compare(Changeset cs1, Changeset cs2) {
				return cs1.getTimestamp().getDate().compareTo( cs2.getTimestamp().getDate() );
			}
		} );*/

		// make it!
		// (going throw each relevant Version and saves all relevant Files in every relevant - and new - Version)
		try {
			iterateRelevantVersions(repo, location, relevantFiles, relevantVersions);
		} catch (IOException e) {
			log.fatal( MessageFormat.format("IOException while iteration throw relevant Versions in {0}",  location), e );
		}

		for( RelevantFile file : relevantFiles ) {
			if( file.getChangeSet() != null ) {
				// when the RelevantFile class contains a ChangeSet Object
				// than there are some changes to store, so we can it to the changeSetMap
				changeSetMap.put( file.getFileId(), file.getChangeSet() );
			}
		}

	}

	/**
	 * Scan repository.
	 *
	 * @param location the location
	 * @param repo the repo
	 * @return the list< relevant file>
	 */
	protected List<RelevantFile> scanRepository( File location, Git repo ) {
		List<RelevantFile> relevantFiles = new LinkedList<RelevantFile>();

		// scans the directory recursively
		scanRepositoryDir( location, location, relevantFiles );

		return relevantFiles;
	}

	/**
	 * Scan repository dir.
	 *
	 * @param base the base
	 * @param dir the dir
	 * @param relevantFiles the relevant files
	 */
	private void scanRepositoryDir( File base, File dir, List<RelevantFile> relevantFiles ) {

		if( log.isTraceEnabled() )
			log.trace( MessageFormat.format("Scanning {0}", base) );

		String[] entries = dir.list();
		// nothing to scan in this dir
		if( entries == null )
			return;

		// looping throw all directory elements
		for( int index = 0; index < entries.length; index++ ) {
			File entry = new File( dir, entries[index] );

			if( entry.isDirectory() && entry.exists() && !entry.getName().startsWith(".") ) {
				// Entry is a directory and not hidden (begins with a dot) -> recursive
				scanRepositoryDir(base, entry, relevantFiles);
			}
			else if( entry.isFile() && entry.exists() ) {
				// Entry is a file -> check if it is relevant
				
				if( log.isTraceEnabled() )
					log.trace( MessageFormat.format("Found {0}. Check relevance...", entry) );
				
				if( fileExtensionBlacklist.contains( FilenameUtils.getExtension(entry.getName()) ) ) {
					// file extension is blacklisted
					if( log.isTraceEnabled() )
						log.trace("file extension is blacklisted. Skip this file...");
					continue;
				}
				
				RelevantFile file;
				if( (file = isRelevant(base, entry)) != null ) {
					// adds it
					relevantFiles.add(file);
					if( log.isTraceEnabled() )
						log.trace("Is relevant. Adds it.");
				} else if( log.isTraceEnabled() )
					log.trace("Is not relevant.");

			}

		}

	}

	/**
	 * Checks if the file is a model aka relevant <br>
	 * Returns a RelevantFile object if it is or null.
	 *
	 * @param base the base
	 * @param model the model
	 * @return the relevant file
	 */
	private RelevantFile isRelevant( File base, File model ) {
		int type = 0;
		RelevantFile relevantFile = null;
		// classify the file and check if it is relevant
		type = classifier.classify(model);

		if( (type & DocumentClassifier.XML) > 0 && ((type & DocumentClassifier.SBML) > 0 || (type & DocumentClassifier.CELLML) > 0) ) {
			// File is an xml document and consists of sbml or cellml model data
			// create a relevant file object

			// make path relative to Repo base dir
			Path basePath = Paths.get( base.toString() );
			Path modelPath = Paths.get( model.toString() );
			Path relativPath = basePath.relativize(modelPath);

			// creating relevantFile object
			relevantFile = new RelevantFile( relativPath.toString() );
			relevantFile.setType(type);

			//			try {
			//				relevantFile = new RelevantFile( RelativPath.getRelativeFile(model, base).toString() );
			//				relevantFile.setType(type);
			//			} catch (IOException e) {
			//				log.error( MessageFormat.format("IOException while generating relativ path to file {0} in repository {1}", model, base), e);
			//			}

		}

		return relevantFile;
	}

	/**
	 * Search latest known version.
	 *
	 * @param relevantFile the relevant file
	 */
	protected void searchLatestKnownVersion( RelevantFile relevantFile ) {
		String versionId = null;
		Date versionDate = null;
		ChangeSet changeSet = null;

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Searches latest known version for model {0}", relevantFile.getFileId()) );

		if( (changeSet = changeSetMap.get(relevantFile.getFileId())) != null ) {
			// there is a changeSet for this fileId, get the latestChange

			if( log.isDebugEnabled() )
				log.debug("ChangeSet available");

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

			if( log.isDebugEnabled() )
				log.debug("Start database request");

			// search in database
			CrawledModelRecord latest = null;
			try {
				latest = CrawledModelRecord.extendDataholder( morreClient.getLatestModelVersion( relevantFile.getFileId() ) );
			} catch (MorreCommunicationException e) {
				log.fatal( MessageFormat.format("Getting latest model version from {0}, to check, if processed model version is new, failed", relevantFile.getFileId()), e);
			} catch (MorreException e) {
				// error occurs, when fileId is unknown to the database -> so we can assume the change is new!
				log.warn("GraphDatabaseError while checking, if processed model version is new. It will be assumed, that this is unknown to the database!", e);
			}

			if( latest != null && latest.isAvailable() ) {
				versionId = latest.getVersionId();
				versionDate = latest.getVersionDate();
			}
		}

		if( log.isInfoEnabled() ) {
			if( versionId != null && versionDate != null )
				log.info( MessageFormat.format("Found latest version for {0} : {1}@{2}", relevantFile.getFileId(), versionId, versionDate) );
			else
				log.info( MessageFormat.format("Found no latest version for {0}. Must be the first occure", relevantFile.getFileId()) );
		}

		relevantFile.setLatestKnownVersion(versionId, versionDate, (PmrChangeSet) changeSet);

	}

	/**
	 * Detect relevant versions.
	 *
	 * @param repo the repo
	 * @param relevantFiles the relevant files
	 * @return the list< changeset>
	 */
	protected Iterable<RevCommit> detectRelevantVersions( Git repo, List<RelevantFile> relevantFiles ) {
		//String[] files;
		Date oldestLatestVersionDate = null;
		boolean foundOldestLatestVersionDate = false;
		Iterable<RevCommit>
		//List<Changeset> 
		relevantVersions = null;

		if( log.isInfoEnabled() )
			log.info("start detection of relevant git versions");

		if( relevantFiles.size() == 0 ) {
			if( log.isInfoEnabled() )
				log.info( "List of relevantFiles is empty. So no version is relevant." );

			return null;
		}

		// make a list of all relevant files
		//files = new String[relevantFiles.size()];
		int index = 0;

		LogCommand logCmd = repo.log();
		
		// put the list into the array and gets the oldestLatestVersion :)
		Iterator<RelevantFile> fileIter = relevantFiles.iterator();
		while( fileIter.hasNext() ) {
			RelevantFile file = fileIter.next();
			//files[index] =
			logCmd.addPath (file.getFilePath());
			index++;

			// checks if the current processed relevantFile has an older latestVersion as the
			// former olderLatestVersion or some file hasn't a parent, so we can not delete any version from the list
			if( oldestLatestVersionDate == null ) {
				if( foundOldestLatestVersionDate == false ) {
					oldestLatestVersionDate = file.getLatestVersionDate();
					foundOldestLatestVersionDate = true;
				}
			}
			else if( file.getLatestVersionDate().compareTo(oldestLatestVersionDate) < 0 ) {
				oldestLatestVersionDate = file.getLatestVersionDate();
			}

		}
		
		if( log.isDebugEnabled() )
			log.debug( MessageFormat.format("execute Log command for {0} file(s)", index) );

		// perform the log command to evaluate all interesting hg changesets
		//Iterable<RevCommit> logs
		try
		{
			relevantVersions = logCmd.call();
		}
		catch (GitAPIException e)
		{
			log.error( "cannot call git log ", e );
		}
		/*LogCommand logCmd = new LogCommand(repo);
		relevantVersions = logCmd.execute(files);*/

		int numVersions = GeneralTools.sizeOfIterable (relevantVersions);/*0;
		if (relevantVersions instanceof Collection<?>)
		  numVersions = ((Collection<?>)relevantVersions).size();
		else
			for(RevCommit v : relevantVersions) {
				numVersions++;
			}*/
		
		if( oldestLatestVersionDate == null ) {
			// oldestLatestVersionDate is null -> there is no latest version known for any of the relevantFiles/-Models
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("Found {0} Changesets. Can not skip any of them, because no one is indexed", numVersions) );
		}
		else {
			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("Found {0} Changesets, removes all Changeset older as {1} (oldestLatestVersion) from the list", numVersions, oldestLatestVersionDate) );

			// remove every Changeset which is older as the oldestLatestVersion (because they are really uninteresting)
			Iterator<RevCommit> changesetIter = relevantVersions.iterator();
			while( changesetIter.hasNext() )
			{
				if (new Date (changesetIter.next().getCommitTime ()).compareTo(oldestLatestVersionDate) < 0)
					changesetIter.remove();
			}

			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("{0} Changsets left for examination", GeneralTools.sizeOfIterable (relevantVersions)) );

		}


		return relevantVersions;
	}

	/**
	 * Iterate relevant versions.
	 *
	 * @param repo the repo
	 * @param location the location
	 * @param relevantFiles the relevant files
	 * @param relevantVersions the relevant versions
	 * @throws IOException the IO exception
	 */
	protected void iterateRelevantVersions( Git repo, File location, List<RelevantFile> relevantFiles, Iterable<RevCommit> relevantVersions ) throws IOException {
		Date crawledDate = new Date();

		if( log.isInfoEnabled() )
			log.info( MessageFormat.format("Going throw all relevant versions of {0}", location) );

		for( RevCommit currentChangeset : relevantVersions ) {
	    // node a changeset ID, must be 40 hexadecimal characters.
			ObjectId currentNodeId = currentChangeset.getId ();//.getNode();
			Date currentVersionDate = new Date (currentChangeset.getCommitTime ());//.getTimestamp().getDate();

			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("Update to {0} Message: {1}", currentNodeId.toString (), currentChangeset.getShortMessage ()) );

			// update to currentChangeset
			
			//UpdateCommand updateCmd = new UpdateCommand(repo);
			CheckoutCommand co = repo.checkout ().setStartPoint (currentChangeset);
			//updateCmd.rev(currentChangeset);
			try {
				co.call ();//.execute();
			} catch (GitAPIException e) {
				log.error( MessageFormat.format("IOException while updating {0} to {1}. skip this repo after now.", location, currentNodeId.toString ()), e);
				return;
			}/* catch (ExecutionException e) {
				log.error( MessageFormat.format("IOException while updating {0} to {1}. skip this repo after now.", location, currentNodeId.toString ()), e);
				return;
			}*/
			
			
			// get all added or modified files in this Changeset
			List<String> changedFiles = new ArrayList<String>();/*
			changedFiles.addAll( currentChangeset.getAddedFiles() );
			changedFiles.addAll( currentChangeset.getModifiedFiles() );*/

			

			Repository repository = repo.getRepository ();
			RevWalk rw = new RevWalk(repository);
			ObjectId head = repository.resolve(Constants.HEAD);
			RevCommit commit = rw.parseCommit (head);
			RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
			DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
			df.setRepository(repository);
			df.setDiffComparator(RawTextComparator.DEFAULT);
			df.setDetectRenames(true);
			List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
			for (DiffEntry diff : diffs) {
				changedFiles.add (diff.getNewPath());
			}
			

			if( log.isInfoEnabled() )
				log.info( MessageFormat.format("{0} changed files in this version", changedFiles.size()) );

			// going throw the relevant files
			for( RelevantFile file : relevantFiles ) {
				boolean hasChanges = false;

				if( log.isInfoEnabled() )
					log.info( MessageFormat.format("Check model {0}", file.getFileId()) );
				
				File fileLocation = new File( location, file.getFilePath() );
				if( !fileLocation.exists() ) {
					// file does not exists -> skip
					if( log.isInfoEnabled() )
						log.info("Model does not exists in this version -> skip it.");
					continue;
				}
				
				// there is already a parent version
				if( file.getLatestVersionId() != null && file.getLatestVersionDate() != null ) {
					if( file.getLatestVersionId().equals(currentNodeId) || file.getLatestVersionDate().compareTo(currentVersionDate) >= 0 ) {
						// if latest version of this file is newer or equal with the current processed Version
						// skip this file
						if( log.isInfoEnabled() )
							log.info("Current version is to old -> no changes.");

						continue;
					}
				}
				else {
					// there is no parent Version -> so there are changes
					hasChanges = true;
					if( log.isDebugEnabled() )
						log.debug("Model has no parents -> this is a new version.");
				}

				// if there are no change detected so far, so have to go deeper
				if( hasChanges == false ) {

					if( log.isTraceEnabled() )
						log.trace("Check if model is in the changed files list");

					// file is in the list of changedFiles
					if( changedFiles.contains(file.getFilePath()) == true ) {
						hasChanges = true;
						if( log.isDebugEnabled() )
							log.debug("Model is in the changed files list.");
					}
				}

				if( hasChanges ) {
					// this file has change or is new -> archive it!
					if( log.isInfoEnabled() )
						log.info("Model has changes. Adds it to its ChangeSet");

					PmrChange change = new PmrChange(file.getFileId(), file.getRepositoryUrl(), file.getFilePath(), currentNodeId.toString (), currentVersionDate, crawledDate);
					// set some Meta information
					change.setMeta( CrawledModelRecord.META_SOURCE, CrawledModelRecord.SOURCE_PMR2 );
					if( (file.getType() & DocumentClassifier.SBML) > 0 )
						change.setModelType( CrawledModelRecord.TYPE_SBML );
					else if( (file.getType() & DocumentClassifier.CELLML) > 0 )
						change.setModelType( CrawledModelRecord.TYPE_CELLML );

					// copy the file to a templocation
					File tempFile = getTempFile();
					FileUtils.copyFile( fileLocation, tempFile);
					change.setXmlFile(tempFile);

					// add the change to the ChangeSet (ChangeSet is administrated by RelevantFile
					file.addChange(change);
				}
				else if( log.isInfoEnabled() )
					log.info("Model has no changes.");

			}

		}

	}

}
