package de.unirostock.sems.ModelCrawler.databases.BioModelsDb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.unirostock.sems.ModelCrawler.databases.Interface.Change;

// TODO: Auto-generated Javadoc
/**
 * The Class BioModelsChange.
 */
public class BioModelsChange extends Change {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8763419545605089673L;

	/** The log. */
	private final Log log = LogFactory.getLog( BioModelsChange.class );

	/** The Constant HASH_ALGORITHM. */
	public final static String HASH_ALGORITHM = "SHA-256";
	
	/** The Constant HASH_ALGORITHM_FALLBACK. */
	public final static String HASH_ALGORITHM_FALLBACK = "SHA";
	
	/** The Constant META_HASH. */
	public final static String META_HASH = "filehash";

	/**
	 * The Constructor.
	 *
	 * @param fileId the file id
	 * @param versionId the version id
	 * @param versionDate the version date
	 * @param crawledDate the crawled date
	 */
	public BioModelsChange( String fileId, String versionId, Date versionDate, Date crawledDate ) {
		super(fileId, versionId, versionDate, crawledDate);
	}
	
	/**
	 * Sets the xml file.
	 *
	 * @param xmlFile the xml file
	 * @param hash the hash
	 * @return true, if sets the xml file
	 */
	public boolean setXmlFile(File xmlFile, String hash) {
		//REMIND the xml file can only be setted once in a Change
		if( this.xmlFile == null && hash != null ) {
			this.xmlFile = xmlFile;
			setMeta(META_HASH, hash);

			return true;
		}
		else if( hash == null )
			log.error("file hash is null!");
		else 
			log.error("xmlFile is already setted!");
		
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unirostock.sems.ModelCrawler.databases.Interface.Change#setXmlFile(java.io.File)
	 */
	@Override
	public boolean setXmlFile( File xmlFile ) {
		return setXmlFile( xmlFile, calcXmlHash(xmlFile) );
	}
	
	/**
	 * Gets the hash.
	 *
	 * @return the hash
	 */
	public String getHash() {
		return getMeta(META_HASH);
	}

	/**
	 * Calc xml hash.
	 *
	 * @param xmlFile the xml file
	 * @return the string
	 */
	protected String calcXmlHash( File xmlFile ) {
		return calcXmlHash( xmlFile, HASH_ALGORITHM );
	}

	/**
	 * Calc xml hash.
	 *
	 * @param xmlFile the xml file
	 * @param algo the algo
	 * @return the string
	 */
	protected String calcXmlHash(File xmlFile, String algo) {
		String hash = null;

		try {
			// opening file & create MessageDigest to calc the hash
			FileInputStream stream = new FileInputStream(xmlFile);
			MessageDigest digest = MessageDigest.getInstance( algo );

			// reading the file
			byte[] buffer = new byte[1024];
			int read = 0;
			while( (read = stream.read(buffer)) > 0 ) {
				digest.update(buffer, 0, read);
			}

			// file close
			stream.close();

			// getting hash
			hash = (new BigInteger( digest.digest() )).toString(16);

		} catch (FileNotFoundException e) {
			// File not found -> resetting everything to null
			xmlFile = null;
			hash = null;
			// log the error!
			log.fatal("File not found to calc the hash!", e);
			return null;

		} catch (NoSuchAlgorithmException e) {
			// trying with a fallback algorithm
			if( algo.equals(HASH_ALGORITHM_FALLBACK) ) {
				// its already the fallback!
				log.fatal("Even fallback hash algorithm does not work!", e);
				return null;
			}
			else {
				log.warn("Using fallback hash algorithm for " + xmlFile.getAbsolutePath(), e);
				calcXmlHash(xmlFile, HASH_ALGORITHM_FALLBACK);
			}

		} catch (IOException e) {
			// fatal error while reading the file!
			log.fatal("Can not read file for hash calc! " + xmlFile.getAbsolutePath(), e);
			return null;
		}

		return hash;
	}


}
