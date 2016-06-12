package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/** Useful class for making REST API calls, typically
 *  to the T1 server. Use API classes for communicating
 *  with GW2 APIs. */
@SuppressWarnings("deprecation")
public class RESTClient {
	
	/** GET command on the provided endpoint. Return result.
	 *  IllegalArgumentException thrown in case of failure.
	 */
	public static String get(String url) {
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(url);
			getRequest.addHeader("accept", "application/json");
			HttpResponse response = client.execute(getRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				client.close();
				throw new RuntimeException("Failed : HTTP Error Code : " + 
						response.getStatusLine());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			String output = "";
			String curr;
			while ((curr = br.readLine()) != null) output += curr + "\n";
			client.getConnectionManager().shutdown();
			client.close();
			return output;
		}
		catch (ClientProtocolException e) {
			throw new IllegalArgumentException(url + " invalid endpoint");
		}
		catch (IOException e) {
			throw new IllegalArgumentException(url + " invalid endpoint");
		}
	}
	
	/** POST command on the provided endpoint. Return result.
	 *  IllegalArgumentException thrown in case of failure.
	 */
	public static String post(String url, String json) {
		try{
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(url);
			StringEntity input = new StringEntity(json);
			input.setContentType("application/json");
			postRequest.setEntity(input);
			HttpResponse response = client.execute(postRequest);
			if (response.getStatusLine().getStatusCode() != 200) {
				client.close();
				throw new RuntimeException("Failed : HTTP Error Code : " + 
						response.getStatusLine());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			String output = "";
			String curr;
			while ((curr = br.readLine()) != null) output += curr + "\n";
			client.getConnectionManager().shutdown();
			client.close();
			return output;
		}
		catch (ClientProtocolException e) {
			throw new IllegalArgumentException(url + " invalid endpoint");
		}
		catch (IOException e) {
			throw new IllegalArgumentException(url + " invalid endpoint");
		}
	}
	
}
