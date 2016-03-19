package de.unirostock.sems.ModelCrawler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

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
	//public ArrayList<String> downloadFiles (File storage){}
	public static ArrayList<String> getDownloadModels (App crawler){
		ArrayList<String> models = new ArrayList<String>();
		
		for(Map.Entry<String, ChangeSet> entry : crawler.getChanges().entrySet()){
			models.add(entry.getKey());
		}
		return models;
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
	 * DEMO CrawlerAPI
	 */
	public static void main(String[] args){
		CrawlerAPI crawlerAPI = new CrawlerAPI(args);
		
		ArrayList<String> models = getDownloadModels(crawlerAPI.crawler);
		
		// print all retrieved models
		printDownloadedModels(models);
		
	}
}
