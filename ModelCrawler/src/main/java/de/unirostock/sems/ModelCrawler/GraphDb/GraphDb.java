package de.unirostock.sems.ModelCrawler.GraphDb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.unirostock.sems.ModelCrawler.GraphDb.Interface.GraphDatabase;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseCommunicationException;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseError;
import de.unirostock.sems.ModelCrawler.GraphDb.exceptions.GraphDatabaseInterfaceException;

public class GraphDb implements GraphDatabase {

	private URL databaseInterface;
	private HttpClient httpClient;
	private JSONParser parser;

	private final Log log = LogFactory.getLog(GraphDb.class);

	private static final String QUERY_GET_MODEL = "get";
	private static final String QUERY_GET_LATEST = "get/latest";
	private static final String QUERY_MODIFY_MODEL = "modify";
	private static final String QUERY_INSERT_MODEL = "insert";

	private static final String FEAUTURE_MODEL_ID = "modelID";
	private static final String FEAUTURE_VERSION_ID = "versionID";
	private static final String FEAUTURE_PARENT_VERSION = "parent";
	private static final String FEAUTURE_XML_URI = "xmldoc";
	private static final String FEAUTURE_MODEL_META = "meta";

	private final String FEAUTURE_RETURN = "return";
	private final String FEAUTURE_ERROR = "error";

	private final String RESULT_FAILED = "failed";

	public GraphDb( URL databaseInterface ) {
		this.databaseInterface = databaseInterface;

		// generating a new Client
		httpClient = new DefaultHttpClient();
		// generating a Json Parser
		parser = new JSONParser();
	}

	private HttpPost generateHttpRequest( String query ) throws GraphDatabaseInterfaceException {
		return generateHttpRequest(query, null);
	}

	private HttpPost generateHttpRequest( String query, JSONObject parameter ) throws GraphDatabaseInterfaceException {
		HttpPost request = null;
		String json = null;

		// building the json
		if( parameter != null ) {
			json = parameter.toJSONString();
			if( log.isDebugEnabled() )
				log.debug( MessageFormat.format("generating HTTP JSON Request to {1} - json: {0}", json, query) );
		}
		//TODO throwing exception, if json building fails!

		// generating the request
		try {
			request = new HttpPost( (new URL(databaseInterface, query)).toURI() );
			request.addHeader("Content-Type", "application/json");

			// check if parameter is setted and json was generated
			if( json != null && parameter != null ) {
				// adding the entity to the request 
				HttpEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON );
				request.setEntity(entity);
			}

		} catch (MalformedURLException e) {
			log.error("Malformed Url! Maybe a config mistake?");
			throw new GraphDatabaseInterfaceException("Malformed Url! Maybe a config mistake?", e);
		} catch (URISyntaxException e) {
			log.error("Bad Url syntax! Maybe a config mistake?");
			throw new GraphDatabaseInterfaceException("Bad Url syntax! Maybe a config mistake?", e);
		}

		return request;
	}

	private Object performHttpRequestJSON( HttpPost request ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException {
		Object json = null;
		HttpResponse response = null;

		try {
			// execute!
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			// parsing the json
			InputStreamReader entityStreamReader = new InputStreamReader(entity.getContent());
			json = parser.parse( entityStreamReader );
			entityStreamReader.close();		// cleanUp

			// ensure the entity is fully consumed aka. cleanUp
			EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			throw new GraphDatabaseCommunicationException("Protocoll Exception", e);
		} catch (IOException e) {
			throw new GraphDatabaseCommunicationException("IOException while downloading and parsing response data", e);
		} catch (ParseException e) {
			throw new GraphDatabaseInterfaceException("Error while parsing the json response.", e);
		}
		finally {
			// ensures the the request entity is consumed fully
			EntityUtils.consumeQuietly( request.getEntity() );
		}

		return json;
	}

	private String performHttpRequestString( HttpPost request ) throws GraphDatabaseCommunicationException {
		String result = null;
		HttpResponse response = null;

		try {
			// execute!
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();

			// getting InputStream
			InputStream entityStream = entity.getContent();
			// copy the InputStream into String
			result = new String( IOUtils.toByteArray(entityStream), ContentType.getOrDefault(entity).getCharset() );

			// ensure the entity is fully consumed aka. cleanUp
			EntityUtils.consume(entity);

		} catch (ClientProtocolException e) {
			throw new GraphDatabaseCommunicationException("Protocoll Exception", e);
		} catch (IOException e) {
			throw new GraphDatabaseCommunicationException("IOException while downloading and reading response data", e);
		} 
		finally {
			// ensures the the request entity is consumed fully
			EntityUtils.consumeQuietly( request.getEntity() );
		}

		return result;
	}

	private ModelRecord getModelRecordFromJson( JSONObject json ) {

		ModelRecord record = null;
		String modelId		= (String) json.get(FEAUTURE_MODEL_ID);
		String versionId	= (String) json.get(FEAUTURE_VERSION_ID);
		String docURI		= (String) json.get(FEAUTURE_XML_URI);

		// check if something is not set
		if( modelId == null || modelId.isEmpty() || versionId == null || versionId.isEmpty() )
			return null;

		// parsing the URI, if setted!
		URI model = null;
		if( docURI != null && !docURI.isEmpty() ) {
			try {
				model = new URI(docURI);
			} catch (URISyntaxException e) {
				log.error("model record from database contains a record with an invalid model uri!", e);
			}
		}

		// create a new model record object
		record = new ModelRecord(modelId, versionId, model);

		// setting meta information
		JSONObject meta = (JSONObject) json.get(FEAUTURE_MODEL_META);
		Iterator<String> keys = meta.keySet().iterator();
		while( keys.hasNext() ) {
			String key = keys.next();
			record.setMeta(key, (String) meta.get(key) );
		}

		return record;
	}

	// ------------------------------------------------------------------------

	@Override
	public String[] getAllModelIds() throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {

		// performing the request!
		HttpPost request = generateHttpRequest(QUERY_GET_MODEL);
		if( request == null )
			return null;

		JSONObject json = (JSONObject) performHttpRequestJSON(request);

		if( json == null )
			return null;

		// check if result failed
		if( json.get(FEAUTURE_RETURN).equals(RESULT_FAILED) ) {
			log.error( MessageFormat.format( "Getting all models failed: {0}", json.get(FEAUTURE_ERROR) ));
			throw new GraphDatabaseError( (String) json.get(FEAUTURE_ERROR) );
		}

		// generating key set -> all model names
		Set<String> models = ((JSONObject) json.get(FEAUTURE_RETURN)).keySet();
		String[] result = new String[models.size()];

		Iterator<String> iter = models.iterator();
		for( int index = 0; index < result.length; index++ ) {
			if( !iter.hasNext() )
				break;

			result[index] = iter.next();
		}

		return result;
	}

	@Override
	public String[] getModelVersions(String modelId) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {

		if( modelId == null || modelId.isEmpty() )
			throw new IllegalArgumentException("modelId can not be null or empty");

		JSONObject parameter = new JSONObject();
		parameter.put(FEAUTURE_MODEL_ID, modelId);

		HttpPost request = generateHttpRequest(QUERY_GET_MODEL, parameter);
		if( request == null )
			return null;

		JSONObject json = (JSONObject) performHttpRequestJSON(request);

		if( json == null )
			return null;

		// check if result failed
		if( json.get(FEAUTURE_RETURN).equals(RESULT_FAILED) ) {
			log.error( MessageFormat.format( "Getting model failed: {0}", json.get(FEAUTURE_ERROR) ));
			throw new GraphDatabaseError( (String) json.get(FEAUTURE_ERROR) );
		}

		JSONArray versions = (JSONArray) ((JSONObject) json.get(FEAUTURE_RETURN)).get(modelId);
		if( versions == null )
			return null;

		String[] result = new String[versions.size()];
		Iterator<String> iter = versions.iterator();
		for( int index = 0; index < result.length; index++ ) {
			if( !iter.hasNext() )
				break;

			result[index] = iter.next();
		}

		return result;
	}

	@Override
	public ModelRecord getLatestModelVersion(String modelId) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		if( modelId == null || modelId.isEmpty() )
			throw new IllegalArgumentException("modelId can not be null or empty");

		JSONObject parameter = new JSONObject();
		parameter.put(FEAUTURE_MODEL_ID, modelId);

		HttpPost request = generateHttpRequest(QUERY_GET_LATEST, parameter);
		if( request == null )
			return null;

		JSONObject json = (JSONObject) performHttpRequestJSON(request);

		if( json == null )
			return null;

		// check if result failed
		if( json.get(FEAUTURE_RETURN).equals(RESULT_FAILED) ) {
			log.error( MessageFormat.format( "Getting latest model failed: {0}", json.get(FEAUTURE_ERROR) ));
			throw new GraphDatabaseError( (String) json.get(FEAUTURE_ERROR) );
		}

		return getModelRecordFromJson( (JSONObject) json.get(FEAUTURE_RETURN) );
	}

	@Override
	public ModelRecord getModel(String modelId, String versionId) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		if( modelId == null || modelId.isEmpty() || versionId == null || versionId.isEmpty() )
			throw new IllegalArgumentException("modelId and/or can not be null or empty");

		JSONObject parameter = new JSONObject();
		parameter.put(FEAUTURE_MODEL_ID, modelId);
		parameter.put(FEAUTURE_VERSION_ID, versionId);

		HttpPost request = generateHttpRequest(QUERY_GET_MODEL, parameter);
		if( request == null )
			return null;

		JSONObject json = (JSONObject) performHttpRequestJSON(request);

		if( json == null )
			return null;

		// check if result failed
		if( json.get(FEAUTURE_RETURN).equals(RESULT_FAILED) ) {
			log.error( MessageFormat.format( "Getting latest model failed: {0}", json.get(FEAUTURE_ERROR) ));
			throw new GraphDatabaseError( (String) json.get(FEAUTURE_ERROR) );
		}

		return getModelRecordFromJson( (JSONObject) json.get(FEAUTURE_RETURN) );
	}

	@Override
	public boolean modifyModelMeta(String modelId, String versionId, Map<String, String> meta) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		if( modelId == null || modelId.isEmpty() || versionId == null || versionId.isEmpty() )
			throw new IllegalArgumentException("modelId and/or can not be null or empty");

		if( meta == null )
			throw new IllegalArgumentException("the meta map should not be null, if you want to change some meta information!");

		// set modelId and versionId
		JSONObject parameter = new JSONObject();
		parameter.put(FEAUTURE_MODEL_ID, modelId);
		parameter.put(FEAUTURE_VERSION_ID, versionId);

		// imports all meta key/value pairs which are going to be modified
		JSONObject metaJson = new JSONObject();
		metaJson.putAll(meta);

		// generate request
		HttpPost request = generateHttpRequest(QUERY_MODIFY_MODEL, parameter);
		if( request == null )
			return false;

		JSONObject json = (JSONObject) performHttpRequestJSON(request);
		if( json == null )
			return false;

		if( ((Boolean) json.get(FEAUTURE_RETURN)) == Boolean.TRUE )
			return true;
		else {
			String message = (String) json.get(FEAUTURE_ERROR);
			log.error( MessageFormat.format("{0} while modifying model record", message) );
			throw new GraphDatabaseError(message);
		}

	}

	@Override
	public boolean insertModel(String modelId, String versionId, String parentVersion, URI model, Map<String, String> meta) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		if( modelId == null || modelId.isEmpty() || versionId == null || versionId.isEmpty() )
			throw new IllegalArgumentException("modelId and/or can not be null or empty");

		// when parent not set -> warn
		if( log.isWarnEnabled() ) {
			if( parentVersion == null || parentVersion.isEmpty() )
				log.warn( MessageFormat.format("Inserting {0}/{1} parentVersion is not set! This model should be the first version!" , modelId, versionId) );
		}

		// set modelId, versionId, xmlDoc aka modelUri
		JSONObject parameter = new JSONObject();
		parameter.put(FEAUTURE_MODEL_ID, modelId);
		parameter.put(FEAUTURE_VERSION_ID, versionId);
		parameter.put(FEAUTURE_XML_URI, model.toString());

		// when parentversion is setted -> add it to the request
		if( parentVersion != null && !parentVersion.isEmpty() )
			parameter.put(FEAUTURE_PARENT_VERSION, parentVersion);

		// if meta data are set...
		if( meta != null && !meta.isEmpty() ) {
			// ... imports all meta key/value pairs which are going to be modified
			JSONObject metaJson = new JSONObject();
			metaJson.putAll(meta);
			
			// add it to the Request
			parameter.put(FEAUTURE_MODEL_META, metaJson);
		}

		// generate request
		HttpPost request = generateHttpRequest(QUERY_INSERT_MODEL, parameter);
		if( request == null )
			return false;

		JSONObject json = (JSONObject) performHttpRequestJSON(request);
		if( json == null )
			return false;

		if( json.get(FEAUTURE_RETURN).equals(RESULT_FAILED) ) {
			String message = (String) json.get(FEAUTURE_ERROR);
			log.error( MessageFormat.format("{0} while modifying model record", message) );
			throw new GraphDatabaseError(message);
		}
		else if( ((Boolean) json.get(FEAUTURE_RETURN)) == Boolean.TRUE )
			return true;
		else {
			log.error( "Unknown error while inserting model!" );
			throw new GraphDatabaseError("Unknown error while inserting model!");
		}

	}

	@Override
	public boolean insertModel(String modelId, String versionId, String parentVersion, URI model) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		return insertModel(modelId, versionId, parentVersion, model, null);
	}

	@Override
	public boolean insertModel( ModelRecord record, String parentVersion ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		return insertModel( record.getModelId(), record.getVersionId(), parentVersion, record.getDocumentUri(), record.getMetaMap() );
	}
	
	@Override
	public boolean insertModel( ModelRecord record ) throws GraphDatabaseInterfaceException, GraphDatabaseCommunicationException, GraphDatabaseError {
		return insertModel( record, record.getParentVersionId() );
	}

}
