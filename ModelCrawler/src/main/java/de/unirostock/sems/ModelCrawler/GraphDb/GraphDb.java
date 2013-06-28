package de.unirostock.sems.ModelCrawler.GraphDb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.utils.IOUtils;
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
	
	private final String FEAUTURE_ID = "ID";
	private final String FEAUTURE_NAME = "NAME";
	//TODO to be continued...

	public GraphDb( URL databaseInterface ) {
		this.databaseInterface = databaseInterface;

		// generating a new Client
		httpClient = new DefaultHttpClient();
		// generating a Json Parser
		parser = new JSONParser();
	}

	@Override
	public boolean isModelManagerAlive() {

		HttpPost request = generateHttpRequest(QUERY_MODEL_MANAGER_ALIVE);
		String result = performHttpRequestString(request).toLowerCase();
		
		if( result.equals("true") )
			return true;
		else
			return false;
	}

	@Override
	public boolean isDatabaseEmpty() {
		HttpPost request = generateHttpRequest(QUERY_DATABASE_EMPTY);
		String result = performHttpRequestString(request).toLowerCase();
		
		if( result.equals("true") )
			return true;
		else
			return false;
	}

	@Override
	public String[] cellMlModelQueryFeatures() {
		HttpPost request = generateHttpRequest(QUERY_CELLML_MODEL);
		JSONArray array = performHttpRequestJSON(request);
		
		return (String[]) array.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ModelRecord getCellMlModelFromId(String modelId) {
		// curl -X POST http://morre.sems.uni-rostock.de:7474/morre/query/cellml_model_query/ -H "Content-Type: application/json" -d '{"features":["ID"], "keywords":["novak_1993"]}'
		ModelRecord record = null;
		
		// Parameter
		JSONObject parameter = new JSONObject();
		JSONArray featureList = new JSONArray();
		JSONArray keywordsList = new JSONArray();
		
		featureList.add( FEAUTURE_ID );
		keywordsList.add(modelId);
		
		parameter.put( "feautures", featureList );
		parameter.put( "keywords", keywordsList );
		
		// performing query
		HttpPost request = generateHttpRequest(QUERY_CELLML_MODEL, parameter);
		JSONArray array = performHttpRequestJSON(request);
		
		// take the first result and parse it!
		JSONObject first = (JSONObject) array.get(0);
		if( first == null )
			return null;
		
		// TODO
		
		return null;
	}

	@Override
	public List<ModelRecord> cellMlModelQuery(Map<String, String> feautures) {
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

	private JSONArray performHttpRequestJSON( HttpPost request ) {
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

	private String performHttpRequestString( HttpPost request ) {
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			// ensures the the request entity is consumed fully
			EntityUtils.consumeQuietly( request.getEntity() );
		}

		return result;
	}
}
