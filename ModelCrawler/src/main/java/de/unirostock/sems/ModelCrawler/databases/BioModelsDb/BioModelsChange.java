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

public class BioModelsChange extends Change {

	private final Log log = LogFactory.getLog( BioModelsChange.class );

	public final String HASH_ALGORITHM = "SHA256";
	public final String HASH_ALGORITHM_FALLBACK = "SHA";
	
	public final String META_HASH = "filehash";

	public BioModelsChange( String modelId, String versionId, Date versionDate, Date crawledDate ) {
		super(modelId, versionId, versionDate, crawledDate);
	}

	public boolean setXmlFile(File xmlFile, String hash) {
		//REMIND the xml file can only be setted once in a Change
		if( this.xmlFile == null && hash != null ) {
			this.xmlFile = xmlFile;
			meta.put(META_HASH, hash);

			return true;
		}
		else if( hash == null )
			log.error("file hash is null!");
		else 
			log.error("xmlFile is already setted!");

		return false;
	}

	@Override
	public boolean setXmlFile( File xmlFile ) {
		return setXmlFile( xmlFile, calcXmlHash(xmlFile) );
	}
	
	public String getHash() {
		return meta.get(META_HASH);
	}

	protected String calcXmlHash( File xmlFile ) {
		return calcXmlHash( xmlFile, HASH_ALGORITHM );
	}

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
