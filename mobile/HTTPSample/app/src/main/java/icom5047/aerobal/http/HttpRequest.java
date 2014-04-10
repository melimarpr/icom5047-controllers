package icom5047.aerobal.http;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;

public class HttpRequest extends AsyncTask<Void, Void, JSONObject> {

	private HttpClient client;
	private HttpCallback callback;
	
	private Bundle params;
	private JSONObject payload;
	
	private HttpUriRequest request;
	
	private boolean success;
	
	public HttpRequest(Bundle params, HttpCallback callback) {
		this.params = params;
		this.callback = callback;
		
		try {
			processParams(params);
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HttpRequest(Bundle params, JSONObject payload, HttpCallback callback) {
		this.params = params;
		this.callback = callback;
		this.payload = payload;
		
		try {
			processParams(params);
		} catch (InvalidParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void processParams(Bundle params) throws InvalidParameterException, UnsupportedEncodingException, MalformedURLException {
		if(!params.containsKey("url")) {
			throw new InvalidParameterException("Missing url parameter");
		}
		
		URL url = new URL(params.getString("url"));
		
		if(!params.containsKey("method")) {
			throw new InvalidParameterException("Missing method parameter");
		} else {
			String method = params.getString("method");
			
			if(method.equalsIgnoreCase("GET")) {
				request = new HttpGet(url.toString());
			} else if (method.equalsIgnoreCase("POST")) {
				request = new HttpPost(url.toString());
				if(payload != null) {
					HttpPost post = (HttpPost) request;
					post.setEntity(new StringEntity(payload.toString()));
					post.setHeader("Content-Type", "application/json");
				}
			} else if (method.equalsIgnoreCase("PUT")) {
				request = new HttpPut(url.toString());
				if(payload != null) {
					HttpPut put = (HttpPut) request;
					put.setEntity(new StringEntity(payload.toString()));
					put.setHeader("Content-Type", "application/json");
				}
			} else if (method.equalsIgnoreCase("DELETE")) {
				request = new HttpDelete(url.toString());
			} else {
				throw new InvalidParameterException("Invalid http method parameter");
			}
		}
	}
	
	@Override
	protected void onPreExecute() {
		int timeout = 10000;
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
		HttpConnectionParams.setSoTimeout(httpParameters, timeout);

		client = new DefaultHttpClient(httpParameters);
	}

	@Override
	protected JSONObject doInBackground(Void... arg0) {
		JSONObject result;
		
		try {
		  
		    Log.d("HttpRequest", "Performing request: " + params.getString("method") +
		        " " + params.getString("url"));
		    
		    String authToken = "1234567890";
		    if(authToken != null) {
		    	request.setHeader("auth_token", authToken);
		    }
		
			HttpResponse response = client.execute(request);
			
			int statusCode = response.getStatusLine().getStatusCode();
			
			Log.d("HttpRequest", "Response Code: " + statusCode);
			
			if(statusCode == HttpStatus.SC_OK){
				String fullRes = EntityUtils.toString(response.getEntity());
				
				result = new JSONObject(fullRes);
				
				success = true;
				return result;
			} else {
				Log.d("HttpRequest", "HttpRequest Code: " + statusCode);
				success = false;
			}
		
		} catch (JSONException e) {
			Log.d("JSONLog", "Error HttpRequest JSON object", e);
			success = false;
		} catch (ClientProtocolException e) {
			success = false;
			Log.d("HttpRequest", "Error HttpRequest", e);
		} catch (IOException e) {
			success = false;
			Log.d("HttpRequest", "Error HttpRequest", e);
		}
		
		return null;
	}
	
	

	@Override
	protected void onProgressUpdate(Void... values) {
		//callback.onProgress();

	}

	@Override
	protected void onPostExecute(JSONObject result) {
		if(success) {
			callback.onSucess(result);
		} else {
			callback.onFailed();
		}
		
		callback.onDone();
	}
	
	public static abstract class HttpCallback {
		public abstract void onSucess(JSONObject json);
		//public abstract void onProgress();
		public abstract void onFailed();
		public void onDone() {};
	}

}
