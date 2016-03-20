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
    private String getChange(Change change, String field) {
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
    public ChangeSet getModelChangeSet(Map<String, ChangeSet> changesPerRelease, String modelName) {
	return changesPerRelease.get(modelName);
    }
    
    
    /**
     * Return a specific field within the specified change of the given modelName
     * @param changesPerRelease
     * @param modelName
     * @param change
     * @param field
     * @return
     */
    public String getModelChangeRepositoryUrl(Map<String, ChangeSet> changesPerRelease,
	    String modelName, Change change, String field) {
	String result = null;
	Iterator<Change> changeIterator = changesPerRelease.get(modelName).getChanges().iterator();
	while(changeIterator.hasNext()) {
	    Change c = changeIterator.next();
	    if(c.equals(change)) {
		result = getChange(c, field);
	    }
	}
	return result;
    }
    
    
    /*
     * DEMO CrawlerAPI
     */
    public static void main(String[] args){
	CrawlerAPI crawlerAPI = new CrawlerAPI(args);
	
	ArrayList<String> models = getDownloadedModels(crawlerAPI.crawler);
	printDownloadedModels(models); // print all retrieved models
	
	Map<String, ChangeSet> changesPerRelease = getChangesPerRelease(crawlerAPI.crawler);
	printChangesPerRelease(changesPerRelease); // print all retrieved changes per release
    }
}
