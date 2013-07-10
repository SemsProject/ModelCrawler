package de.unirostock.sems.ModelCrawler;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import de.unirostock.sems.ModelCrawler.GraphDb.GraphDb;
import de.unirostock.sems.ModelCrawler.GraphDb.ModelRecord;
import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseCommunicationException;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseError;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseInterfaceException;

public class TestGraphDbApi extends TestCase {

	public static TestSuite suite() {
		return new TestSuite(TestGraphDbApi.class);
	}

	private GraphDatabase db;

	private final String MODEL_ID = "testio";
	private final String MODEL_ID_2 = "blubber";
	private String versionId = "";

	public TestGraphDbApi() throws MalformedURLException {
		this("TestGraphDb");
	}

	public TestGraphDbApi(String name) throws MalformedURLException {
		super(name);

		db = new GraphDb( new URL("http://139.30.6.14:8080/DummyDB/") );
	}
	
	public void testInsertGet() throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError, URISyntaxException {
		
		insertOne();
		getOne();
		
	}
	
	public void testInsertMany() throws URISyntaxException, GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		
		ModelRecord latest = null;
		try {
			latest = db.getLatestModelVersion( MODEL_ID_2 );
		} catch (GraphDatabaseInterfaceException e) {
		} catch (GraphDatabaseCommunicationException e) {
		} catch (GraphDatabaseError e) {
		}
		
		String parentVersionId;
		int version = 1;
		if( latest == null ) {
			parentVersionId = null;
			version = 1;
		}
		else {
			parentVersionId = latest.getVersionId();
			version = Integer.valueOf(parentVersionId);
			System.out.println( "parentVersionId: " + version );
			version++;	// increasing
		}
		
		
		for( int end = version+20 ; version <= end; version++ ) {
			String versionId = String.valueOf(version);
			URI modelUri = new URI( "modell://models.sems.uni-rostock.de/" + MODEL_ID_2 + "/" + versionId );
			
			assertTrue( "Failed insert " + MODEL_ID_2 + " " + versionId, 
					db.insertModel(MODEL_ID_2, versionId, parentVersionId, modelUri) );
			parentVersionId = versionId;
		}
		
	}
	
	public void testGetAll() throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		
		String[] models = db.getAllModelIds();
		System.out.println( Arrays.toString(models) );
		
		for( int index = 0; index < models.length; index++ ) {
			String[] versions = db.getModelVersions(models[index]);
			System.out.println( MessageFormat.format("{0}: {1}", models[index], Arrays.toString(versions) ));
		}
		
	}
	
	private void insertOne() throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError, URISyntaxException  {
		versionId = "";
		String parentVersionId = "";

		ModelRecord parent = null;
		try {
			parent = db.getLatestModelVersion(MODEL_ID);
		} catch (GraphDatabaseInterfaceException e) {
		} catch (GraphDatabaseCommunicationException e) {
		} catch (GraphDatabaseError e) {
		}

		if( parent != null ) {
			parentVersionId = parent.getVersionId();
			versionId = String.valueOf( Integer.valueOf(parentVersionId) + 1 );
			System.out.println( "parent: " + parentVersionId + " version: " + versionId );
		}
		else {
			versionId = "1";
		}

		assertTrue( db.insertModel("testio", versionId, parentVersionId, new URI("modell://models.sems.uni-rostock.de/" + MODEL_ID + "/" + versionId) ) );
	}

	private void getOne() throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {

		String[] versions = db.getModelVersions(MODEL_ID);
		// If there are no versions of MODEL_ID
		assertFalse( "no versions of model " + MODEL_ID, versions.length == 0 );

		// print all versions
		System.out.println( Arrays.toString(versions) );

		if( !versionId.isEmpty() ) {
			boolean found = false;
			for( int index = 0; index < versions.length; index++ ) {
				if( versions[index].equals(versionId) ) {
					found = true;
					System.out.println("found version!");
					break;
				}
			}
			assertTrue("Could not find inserted version " + versionId, found);
		}
		else
			System.out.println( "version not setted!" );

	}
	

}
