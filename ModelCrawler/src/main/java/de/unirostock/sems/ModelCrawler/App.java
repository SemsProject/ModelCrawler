package de.unirostock.sems.ModelCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelRelease;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	System.out.println("creating db connector");
    	
    	BioModelsDb db = null;
    	try {
			
    		db = new BioModelsDb( "ftp://ftp.ebi.ac.uk/pub/databases/biomodels/releases/" );
			System.out.println("connecting...");
	    	db.connect();    	
	    	
	        System.out.println( "Start retrieving ReleaseList..." );
	        List<BioModelRelease> list = db.retrieveReleaseList();
	        
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy");
	        Iterator<BioModelRelease> iter = list.iterator();
	        
	        while( iter.hasNext() ) {
	        	BioModelRelease release = iter.next();
	        	System.out.println( MessageFormat.format("{0} : {1} - {2}", 
	        			release.getReleaseName(), dateFormat.format(release.getReleaseDate()), 
	        			release.getFtpDirectory() ));
	        }
	        
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
}
