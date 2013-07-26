package de.unirostock.sems.ModelCrawler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.XmlFileRepository;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.Interface.XmlFileServer;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;

public class TestXmlFileRepo extends TestCase {

	XmlFileServer repo = null;

	public TestXmlFileRepo() {
		this("TestXmlFileRepo");
	}

	public TestXmlFileRepo(String name) {
		super(name);

		// inits the Properties
		Properties.init();
		// opens the XmlFileRepo
		repo = new XmlFileRepository();
	}

	public static TestSuite suit() {
		return new TestSuite(TestXmlFileRepo.class);
	}

	public void testInsertCellMlModel() throws IOException, UnsupportedUriException {

		// creates a Stream for testing
		byte[] testSource = new byte[1024];
		byte[] testTarget = new byte[1024];
		Arrays.fill(testSource, (byte) 42);

		InputStream stream = new ByteArrayInputStream( testSource );

		URI model = repo.pushModel(null, "1", "http://models.cellml.org/w/alberto/CorriasPurkinje", "corrias_rabbit_purkinje_model_2011.cellml", stream);

		assertTrue("URI is not resolvable", repo.isResolvableUri(model));
		assertTrue("model ist not stored under URI", repo.exist(model));

		InputStream stream2 = repo.resolveModelUri(model);
		stream2.read(testTarget);

		assertTrue( "Source and Target are not equal", Arrays.equals(testTarget, testSource) );

	}

	public void testInsertBioModel() throws IOException, UnsupportedUriException {

		// creates a Stream for testing
		byte[] testSource = new byte[1024];
		byte[] testTarget = new byte[1024];
		Arrays.fill(testSource, (byte) 42);

		InputStream stream = new ByteArrayInputStream( testSource );

		URI model = repo.pushModel("BIOMD0000000013", "2006-01-31", stream);

		assertTrue("URI is not resolvable", repo.isResolvableUri(model));
		assertTrue("model ist not stored under URI", repo.exist(model));

		InputStream stream2 = repo.resolveModelUri(model);
		stream2.read(testTarget);

		assertTrue( "Source and Target are not equal", Arrays.equals(testTarget, testSource) );

	}

}
