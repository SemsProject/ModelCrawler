package de.unirostock.sems.ModelCrawler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;
import de.unirostock.sems.ModelCrawler.databases.Interface.ChangeSet;

/*
 * Class for interfacing with ModelCrawler's functionalities
 */
public class CrawlerAPI {
    
    public static final String REPOSITORY_URL = new String("repositoryUrl");
    public static final String FILE_PATH = new String("filePath");
    public static final String FILE_NAME = new String("fileName");
    public static final String VERSION_ID = new String("versionId");
    public static final String VERSION_DATE = new String("versionDate");
    public static final String CRAWLED_DATE = new String("crawledDate");
    
    private App crawler = null;
    
    /*
     * a CrawlerAPI gets its crawler
     */
    public CrawlerAPI (String[] args) {
	crawler = App.getApp(args);
    }
    
    /*
     * get all downloaded models
     */
    public static ArrayList<String> getDownloadedModels (App crawler){
	ArrayList<String> models = new ArrayList<String>();
		
	for(Map.Entry<String, ChangeSet> entry : crawler.getChanges().entrySet()){
	    models.add(entry.getKey());
	}
	return models;
    }
    
    public static Map<String, ChangeSet> getChangesPerRelease (App crawler){
	return crawler.getChangesPerRelease();
    }
    
    /*
     * internal utility functions
     */
    
    /*
     * print the given list of models
     */
    private static void printDownloadedModels(ArrayList<String> models){
	Iterator<String> modelIterator = models.iterator();
	
	while(modelIterator.hasNext()){ // print each model
	    String model = modelIterator.next();
	    System.err.println("  Retrieved model " + model);
	}
    }
    
    /*
     * print the given list of changes per release
     */
    private static void printChangesPerRelease(Map<String, ChangeSet> changesPerRelease) {
	
	Iterator<String> elementIterator = changesPerRelease.keySet().iterator();
	while(elementIterator.hasNext()) {
	    String element = elementIterator.next();
	    System.err.println("-------------------------------");
	    System.err.println("  next element " + element);
	    System.err.println("    has associated changeset:");
	    
	    ChangeSet elementChangeSet = changesPerRelease.get(element);
	    Iterator<Change> changeIterator = elementChangeSet.getChanges().iterator();
	    while(changeIterator.hasNext()) {
		Change c = changeIterator.next();
		System.err.println("      repository URL " + c.getChangeRepositoryUrl(c));
		System.err.println("      file path " + c.getChangeFilePath(c));
		System.err.println("      file name " + c.getChangeFileName(c));
		System.err.println("      version ID " + c.getChangeVersionId(c));
		System.err.println("      version date " + c.getChangeVersionDate(c));
		System.err.println("      crawled date " + c.getChangeCrawledDate(c));
		System.err.println("-------------------------------");
	    }
	}
    }
    
    
    /**
     * Return a specific field of the given change
     * @param change
     * @param field
     * @return
     */
    private static String getChange(Change change, String field) {
	String result = null;
	if (field.equals(REPOSITORY_URL)) {
	    result = change.getChangeRepositoryUrl(change);
	} else if (field.equals(FILE_PATH)) {
	    result = change.getChangeFilePath(change);
	} else if (field.equals(FILE_NAME)) {
	    result = change.getChangeFileName(change);
	} else if (field.equals(VERSION_ID)) {
	    result = change.getChangeVersionId(change);
	} else if (field.equals(VERSION_DATE)) {
	    result = change.getChangeVersionDate(change);
	} else if (field.equals(CRAWLED_DATE)) {
	    result = change.getChangeCrawledDate(change);
	}
	return result;
    }
    
    
    /**
     * Return all ChangeSet changes associated with the given modelName
     * @param changesPerRelease
     * @param modelName
     * @return
     */
    public static ChangeSet getModelChangeSet(Map<String, ChangeSet> changesPerRelease, String modelName) {
	return changesPerRelease.get(modelName);
    }
    
    
    /**
     * Return a specific field within the provided model's change
     * @param change
     * @param field
     * @return
     */
    public static String getModelChange(Change change, String field) {
	return getChange(change, field);
    }
    
    
    
    /**
     * Class demo. Uncomment the procedure you wish to test
     * @param args
     */
    public static void main(String[] args){
	CrawlerAPI crawlerAPI = new CrawlerAPI(args);
	
	// retrieve and print all downloaded models
	/*
	ArrayList<String> models = getDownloadedModels(crawlerAPI.crawler);
	printDownloadedModels(models); // print all retrieved models
	*/
	// retrieve and print all downloaded models' changes
	/*
	Map<String, ChangeSet> changesPerRelease = getChangesPerRelease(crawlerAPI.crawler);
	printChangesPerRelease(changesPerRelease); // print all retrieved changes per release
	*/
	
	// retrieve all downloaded models' changes and display a given model's
	// change set using the provided API
	Map<String, ChangeSet> changesPerRelease = getChangesPerRelease(crawlerAPI.crawler);
	
	// IMPORTANT: enable this if you crawled BMDB
	String targetModelName = new String("BIOMD0000000057.xml");
	
	// IMPORTANT: enable this if you crawled CellML
	//String targetModelName = new String("urn:model:models.cellml.org:workspace:zhang_holden_kodama_honjo_lei_varghese_boyett_2000:!:zhang_holden_kodama_honjo_lei_varghese_boyett_2000.cellml");
	
	System.err.println("Retrieve model " + targetModelName + "'s change set:");
	ChangeSet modelChangeSet = getModelChangeSet(changesPerRelease, targetModelName); // use API method
	System.err.println("  Model " + targetModelName + " has change set object " + modelChangeSet.toString() + " containing:");
	
	Iterator<Change> changeIterator = modelChangeSet.getChanges().iterator(); // TODO: API to retrieve a specific change?
	while(changeIterator.hasNext()) {
	    Change modelChange = changeIterator.next();
	    System.err.println("    Change with associated metadata:");
	    System.err.println("      repository URL " + getModelChange(modelChange, CrawlerAPI.REPOSITORY_URL)); // use API method
	    System.err.println("      file path " + getModelChange(modelChange, CrawlerAPI.FILE_PATH)); // use API method
	    System.err.println("      file name " + getModelChange(modelChange, CrawlerAPI.FILE_NAME)); // use API method
	    System.err.println("      version ID " + getModelChange(modelChange, CrawlerAPI.VERSION_ID)); // use API method
	    System.err.println("      version date " + getModelChange(modelChange, CrawlerAPI.VERSION_DATE)); // use API method
	    System.err.println("      crawled date " + getModelChange(modelChange, CrawlerAPI.CRAWLED_DATE)); // use API method
	}
    }
}
