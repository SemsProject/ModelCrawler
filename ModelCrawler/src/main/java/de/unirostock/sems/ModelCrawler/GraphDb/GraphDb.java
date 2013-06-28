package de.unirostock.sems.ModelCrawler.GraphDb;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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

public class GraphDb implements GraphDatabase {

	private URL databaseInterface;
	private HttpClient httpClient;
	private JSONParser parser;

	private final String QUERY_MODEL_MANAGER_ALIVE = "is_model_manager_alive";
	private final String QUERY_DATABASE_EMPTY = "is_database_empty";
	private final String QUERY_CELLML_MODEL = "cellml_model_query";
	private final String QUERY_CELLML_MODEL_SIMPLE = "cellml_model_query";
	private final String QUERY_PUBLICATION_MODEL = "publication_model_query";
	private final String QUERY_PERSON = "person_query";
	private final String QUERY_PUBLICATION = "publication_query";
	private final String QUERY_ANNOTATION_MODEL = "annotation_model_query";
	private final String QUERY_ANNOTATION = "annotation_query";
	private final String QUERY_MODEL = "model_query";

	public GraphDb( URL databaseInterface ) {
		this.databaseInterface = databaseInterface;

		// generating a new Client
		httpClient = new DefaultHttpClient();
		// generating a Json Parser
		parser = new JSONParser();
	}

	@Override
	public boolean isModelManagerAlive() {
		
		return false;
	}

	@Override
	public boolean isDatabaseEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] cellMlModelQueryFeatures() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryResult getCellMlModelFromId(String modelId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QueryResult> cellMlModelQuery(Map<String, String> feautures) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private HttpPost generateHttpRequest( String query ) {
		return generateHttpRequest(query, null);
	}
	
	private HttpPost generateHttpRequest( String query, JSONObject parameter ) {
		HttpPost request = null;
		String json = null;

		// building the json
		if( parameter != null )
			json = parameter.toJSONString();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return request;
	}
	
	private JSONArray performHttpRequest( HttpPost request ) {
		JSONArray array = null;
		HttpResponse response = null;
		
		try {
			// execute!
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
			
			// parsing the json
			InputStreamReader entityStreamReader = new InputStreamReader(entity.getContent());
			array = (JSONArray) parser.parse( entityStreamReader );
			entityStreamReader.close();		// cleanUp
			
			// ensure the entity is fully consumed aka. cleanUp
			EntityUtils.consume(entity);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			// ensures the the request entity is consumed fully
			EntityUtils.consumeQuietly( request.getEntity() );
		}
		
		return array;
	}

}
