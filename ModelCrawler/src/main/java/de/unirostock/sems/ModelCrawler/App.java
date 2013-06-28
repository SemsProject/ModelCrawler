package de.unirostock.sems.ModelCrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelRelease;
import de.unirostock.sems.ModelCrawler.databases.BioModelsDb.BioModelsDb;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final Log log = LogFactory.getLog( App.class );
	
    public static void main( String[] args ) {
    	// inits the Properties System
    	Properties.init();
    	// working dir
    	Properties.checkAndInitWorkingDir();
    	
    	System.out.println("creating db connector");
    	
    	BioModelsDb db = null;
    	try {
			
    		db = new BioModelsDb( Properties.getProperty("de.unirostock.sems.ModelCrawler.BioModelsDb.ftpUrl") );
			// do it...
    		db.run();
	        
	        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	        Iterator<BioModelRelease> iter = db.getBioModelReleases().iterator();
	        
	        while( iter.hasNext() ) {
	        	BioModelRelease release = iter.next();
	        	System.out.println( MessageFormat.format("{0} : {1} - {2}", 
	        			release.getReleaseName(), dateFormat.format(release.getReleaseDate()), 
	        			release.getFtpDirectory() ));
	        }
	        
//	        System.out.println( "Download the latest release!" );
//	        BioModelRelease latest = list.get( list.size()-1 );
//	        if( db.downloadRelease(latest) == true ) {
//	        	System.out.println( MessageFormat.format( "{0}: {1}", latest.getReleaseName(), latest.getArchivFile().getAbsolutePath() ));
//	        }
//	        else
//	        	System.out.println( "Failed to download file!" );
	        
	        
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (IllegalArgumentException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
    	
    	
    }
    
}
