package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Handles communication with webpages to fetch data
 *  from APIs in JSON form.
 *  @author Ryan Pindulic
 */
public abstract class JSONInterface {
	
	/** Given a URL, load the plaintext of the URL. */
	public static String loadFromWeb(String urlText) throws MalformedURLException, IOException{
		final URL url = new URL(urlText);
		final BufferedReader in = new BufferedReader (new InputStreamReader(
				url.openConnection().getInputStream()));
		StringBuilder totalText = new StringBuilder();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			totalText.append(inputLine);
		}
		in.close();
		return totalText.toString();
	}
	
	/** Loads the text from a URL, but without checked exceptions.
	 *  Throws IllegalArgumentException in the case of bad URL.
	 */
	public static String loadFromWebSafe(String urlText) {
		try{
			return loadFromWeb(urlText);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Error loading text from site : " 
					+ urlText + ". See : " + e.toString());
		}
	}
	
	/** Given an input file, read the text and return as a String.
	 *  Requires full path to file.
	 *  Throws IllegalArgumentException in case of bad input file.
	 */
	public static String parseFile(String file) {
		try{
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuilder totalText = new StringBuilder();
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				totalText.append(inputLine);
			}
			br.close();
			return totalText.toString();
		}
		catch (Exception e) {
			throw new IllegalArgumentException("File " + file + " not found.");
		}
	}
	
	/** Given a URL location corresponding to a JSON API page,
	 *  return a JSONObject built from this page.
	 *  In the case of failure, throws an IllegalArgumentException.
	 */
	public static JSONObject loadJSON(String url) {
		try{
			String urlText = loadFromWeb(url);
			JSONObject info = new JSONObject(urlText);
			return info;
		}
		catch (Exception e) {
			throw new IllegalArgumentException(
					"Error loading JSON Object from GW2 API : " + e.toString());
		}
	}
	
	/** Given raw text, parse it into a JSON object.
	 *  In the case of failure, throws an IllegalArgumentException. 
	 */
	public static JSONObject loadFromText(String text) {
		try{
			return new JSONObject(text);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("JSON Syntax error:\n" + text);
		}
	}
	
	/** Gets a string value from a JSON Object given a key 'name'. 
	 *  Throws IllegalArgument Exception for invalid key.
	 */
	public static String get(JSONObject obj, String name) {
		try{
			return obj.getString(name);
		}
		catch (JSONException e) {
			throw new IllegalArgumentException("JSON get : bad name " + name);
		}
	}
	
	/** Gets a JSON Object value from a JSON Object given a key 'name'.
	 *  Throws IllegalArgumentException for invalid key.
	 */
	public static JSONObject getObject(JSONObject obj, String name) {
		try{
			return obj.getJSONObject(name);
		}
		catch (JSONException e) {
			throw new IllegalArgumentException("JSON get object : bad name " + name);
		}
	}
	
	/** Gets an integer value from a JSON object given a key 'name'.
	 *  Throws IllegalArgument Exception for invalid key.
	 */
	public static int getInt(JSONObject obj, String name) {
		try{
			return obj.getInt(name);
		}
		catch (JSONException e) {
			throw new IllegalArgumentException("JSON get int : bad name " + name);
		}
	}
	
	/** Gets a double value from a JSON object given key 'name'.
	 *  Throws IllegalArgument exception for invalid key.
	 */
	public static double getDouble(JSONObject obj, String name) {
		try{
			return obj.getDouble(name);
		}
		catch (JSONException e) {
			throw new IllegalArgumentException("JSON get double : bad name " + name);
		}
	}
	
	/** Gets a JSON array from a JSON object given key 'name'.
	 * Throws IllegalArgument Exception for invalid key. */
	public static JSONArray getArray(JSONObject obj, String name) {
		try{
			return obj.getJSONArray(name);
		}
		catch (JSONException e) {
			throw new IllegalArgumentException("JSON get array : bad name " + name);
		}
	}
	
	/** Gets the ith JSON object in a JSON array.
	 *  Throws IllegalArgumentException for invalid index. */
	public static JSONObject getObjectAtIndex(JSONArray arr, int i) {
		try{
			return arr.getJSONObject(i);
		}
		catch (JSONException e) {
			throw new IllegalArgumentException("JSON get object at index : bad index " + i);
		}
	}
	
	/** Gets the ith String in a JSON array.
	 *  Throws IllegalArgumentException for invalid index. */
	public static String getAtIndex(JSONArray arr, int i) {
		try{
			return arr.getString(i);
		}
		catch (JSONException e) {
			throw new IllegalArgumentException("JSON get at index : bad index " + i);
		}
	}
}
