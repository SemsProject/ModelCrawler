package de.unirostock.sems.ModelCrawler.XmlFileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import de.unirostock.sems.ModelCrawler.Properties;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.Interface.XmlFileServer;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.ModelNotFoundException;
import de.unirostock.sems.ModelCrawler.XmlFileRepository.exceptions.UnsupportedUriException;

public class XmlFileServerClient implements XmlFileServer {
	
	private static final int STATUSCODE_OK = 200;
	private static XmlFileServerClient instance = null;
	
	public static XmlFileServerClient getInstance() throws URISyntaxException {
		if( instance == null )
			instance = new XmlFileServerClient();
		
		return instance;
	}
	
	private URI xmlFileServer;
	private String prefixPath;
	private HttpClient client;
	
	public XmlFileServerClient() throws URISyntaxException {
		this( new URI(Properties.getProperty("de.unirostock.sems.ModelCrawler.xmlFileServer")) );
	}
	
	public XmlFileServerClient( URI xmlFileServer ) {
		this.xmlFileServer = xmlFileServer;
		this.prefixPath = xmlFileServer.getPath();
		
		
		// creates a new a Http Client
		client = new DefaultHttpClient();
	}

	@Override
	public InputStream resolveModelUri(URI model) throws UnsupportedUriException, ModelNotFoundException {
		HttpGet request;
		HttpResponse response = null;
		URI requestUri = null;
		String requestPath;
		
		if( prefixPath.isEmpty() || prefixPath.equals("/") ) {
			// no prefixPath used
			requestPath = model.getPath();
		}
		else {
			requestPath = model.getPath();
			if( !requestPath.toLowerCase().startsWith(prefixPath.toLowerCase()) ) {
				if( !prefixPath.endsWith("/") && !requestPath.startsWith("/") )
					requestPath = prefixPath + "/" + requestPath;
				else if( prefixPath.endsWith("/") && requestPath.startsWith("/") )
					requestPath = prefixPath.substring(1) + requestPath;
			}
		}
		
		try {
			requestUri = new URI( xmlFileServer.getScheme(), null, xmlFileServer.getHost(), xmlFileServer.getPort(), requestPath, null, null);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// creates the Request
		request = new HttpGet(requestUri);
		// performs it
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if( response.getStatusLine().getStatusCode() == STATUSCODE_OK ) {
			try {
				return response.getEntity().getContent();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new ModelNotFoundException( response.getStatusLine().getReasonPhrase() );
		}
		return null;
	}

	@Override
	public boolean exist(URI model) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isResolvableUri(URI model) {

		if( !model.getScheme().equals( xmlFileServer.getScheme() ))
			return false;

		if( !model.getHost().equals( xmlFileServer.getHost() ))
			return false;

		return true;
	}

	@Override
	public URI pushModel(String modelId, String versionId, InputStream modelSource) throws IOException, UnsupportedUriException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URI pushModel(String modelId, String versionId, String repositoryUrl, String filePath, InputStream modelSource) throws IOException, UnsupportedUriException {
		// TODO Auto-generated method stub
		return null;
	}

}
