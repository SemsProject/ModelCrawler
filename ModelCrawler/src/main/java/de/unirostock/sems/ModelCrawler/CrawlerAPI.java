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
	private static void printChangesPerRelease(Map<String, ChangeSet> changesPerRelease){
		
		Iterator<String> elementIterator = changesPerRelease.keySet().iterator();
		while(elementIterator.hasNext()){
			String element = elementIterator.next();
			System.err.println("-------------------------------");
			System.err.println("  next element " + element);
			System.err.println("    has associated changeset:");
			
			ChangeSet elementChangeSet = changesPerRelease.get(element);
			Iterator<Change> changeIterator = elementChangeSet.getChanges().iterator();
			while(changeIterator.hasNext()){
				Change c = changeIterator.next();
				System.err.println("      repository URL " + c.getChangeRepositoryUrl(c));
				System.err.println("      file path " + c.getChangeFilePath(c));
				System.err.println("      file name " + c.getChangeFileName(c));
				System.err.println("      version ID " + c.getChangeVersionId(c));
				System.err.println("      version ID " + c.getChangeVersionDate(c));
				System.err.println("      version ID " + c.getChangeCrawledDate(c));
				System.err.println("-------------------------------");
			}
		}
	}
	
	
	
	/*
	 * DEMO CrawlerAPI
	 */
	public static void main(String[] args){
		CrawlerAPI crawlerAPI = new CrawlerAPI(args);
		
		ArrayList<String> models = getDownloadedModels(crawlerAPI.crawler);
		// print all retrieved models
		printDownloadedModels(models);
		
		Map<String, ChangeSet> changesPerRelease = getChangesPerRelease(crawlerAPI.crawler);
		// print all retrieved changes per release
		printChangesPerRelease(changesPerRelease);
	}
}
