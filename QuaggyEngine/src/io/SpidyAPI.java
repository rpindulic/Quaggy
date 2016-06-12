package io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import core.DateTime;
import core.ItemDB;
import core.ItemInfo;
import core.TPItemInfo;
import core.TPSnapshot;

/** Implementation of the API interface using the GW2
    Spidy API. Might be useful to replace soon given that
 *  support for this API seems to be fading.
 * @author Ryan Pindulic
 */
public class SpidyAPI extends API {
	
	// The SpidyAPI uses indexes to refer to object types rather than their names.
	// This maps those indexes to the type names they refer to.
	private Map<Integer, String> typeNames = new HashMap<Integer, String>();
	
	private static final String TYPE_NAMES_LOC = "types";			//The location of the type names inde
	private static final String ALL_ITEMS_LOC = "all-items/all";	//Location where a list of all in-game items is stored.

	public SpidyAPI() {
		super("http://www.gw2spidy.com/api/v0.9/json/");
		initTypeNames();
	}
	
	/** Initialize the mapping from type ids to type names from the API. */
	private void initTypeNames() {
		JSONObject json = JSONInterface.loadJSON(BASE_URL + TYPE_NAMES_LOC);
		JSONArray results = JSONInterface.getArray(json, "results");
		for (int i = 0; i < results.length(); i++) {
			JSONObject curr = JSONInterface.getObjectAtIndex(results, i);
			typeNames.put(JSONInterface.getInt(curr,  "id"), 
					JSONInterface.get(curr, "name"));
		}
	}

	@Override
	public ItemDB getItemDB() {
		//Debug comments
		System.out.println();
		System.out.println("****************");
		System.out.println("Syncing items table from GW2 Spidy DB.");
		//Create the map we will try to fill for the itemdb
		Map<Integer, ItemInfo> itemDB = new HashMap<Integer, ItemInfo>();		
		//Get item json from api
		JSONObject json = JSONInterface.loadJSON(BASE_URL + ALL_ITEMS_LOC);
		JSONArray results = JSONInterface.getArray(json, "results");
		for (int i = 0; i < results.length(); i++) {
			JSONObject item = JSONInterface.getObjectAtIndex(results, i);
			//Load the necessary information from this item.
			int id = JSONInterface.getInt(item, "data_id");
			String name = JSONInterface.get(item, "name");
			int typeID = JSONInterface.getInt(item, "type_id");
			String type = typeNames.get(typeID);
			if (type == null) {
				throw new IllegalArgumentException("Type ID " + id + " detected for item " + 
						id + " but not found in type database!");
			}
			int rarity = JSONInterface.getInt(item, "rarity");
			int level = JSONInterface.getInt(item, "restriction_level");
			//TODO: Load these variables dynamically
			int vendor_value = -1;
			int default_skin = -1;
			String urlLoc = JSONInterface.get(item, "img");
			//Create the new itemInfo
			ItemInfo newInfo = new ItemInfo(id, name, type, ""+rarity, ""+level,
					""+vendor_value, ""+default_skin, urlLoc);
			itemDB.put(id, newInfo);
		}
		System.out.println("Syncing items table complete.");
		return new ItemDB(itemDB);
	}


	@Override
	public TPSnapshot snapshot() {
		//Debug comments
		System.out.println();
		System.out.println("****************");
		System.out.println("Taking snapshot from GW2 Spidy DB.");
		//Create a map we will try to fill for the snapshot
		Map<Integer, TPItemInfo> snapshot = new HashMap<Integer, TPItemInfo>();
		//Get JSON information from remote API
		JSONObject json = JSONInterface.loadJSON(BASE_URL + ALL_ITEMS_LOC);
		JSONArray results = JSONInterface.getArray(json, "results");
		for (int i = 0; i < results.length(); i++) {
			JSONObject curr = JSONInterface.getObjectAtIndex(results, i);
			int id = JSONInterface.getInt(curr, "data_id");
			int sellPrice = JSONInterface.getInt(curr, "min_sale_unit_price");
			int numSell = JSONInterface.getInt(curr, "sale_availability");
			int buyPrice = JSONInterface.getInt(curr, "max_offer_unit_price");
			int numBuy = JSONInterface.getInt(curr, "offer_availability");
			String timestamp = JSONInterface.get(curr, "price_last_changed");
			TPItemInfo currentInfo = new TPItemInfo(id, numBuy, buyPrice, numSell, sellPrice, timestamp);
			snapshot.put(id, currentInfo);
		}
		System.out.println("Taking snapshot complete.");
		return new TPSnapshot(snapshot);
	}

	@Override
	public void resyncHistory(DB database, boolean fresh, int historyHorizon, int start) {
		DateTime firstDate = DateTime.daysBack(historyHorizon);
		//Completely refresh the table if we want it to be fresh
		if (fresh) {
			database.dropListingsTable();
			database.createListingsTable();
		}
		//Debug print statements
		System.out.println();
		System.out.println("*****************");
		System.out.println("Beginning Spidy DB History Sync.");
		//Grab the list of items we'll need from the API
		ItemDB items = getItemDB();
		
		//For each valid ID, get its history and write that history to the listings table
		int numDone = 0;
		for (Integer id : items.validIDS()) {
			numDone++;
			//Skip until we've found the appropriate starting location
			if (id < start) continue;
			//Provide update on our progress
			System.out.println("Spidy DB History Sync Percent Complete : " + 
					formatPercent(numDone, items.validIDS().size()) + ". Loading " + id);
			//Get the history for this item from the API
			ItemInfo augmented = getItemHistory(id, items);
			//Don't bother wasting DB space if this isn't tradeable
			int historySize = augmented.getHistory().size();
			if (historySize == 0) continue;
			//Add the history to the DB only if necessary.
			for (TPItemInfo historical : augmented.getHistory()) {
				if (historical.time().compareTo(firstDate) >= 0) {
					database.addListing(historical);
				}
			}
		}
		System.out.println("Spidy DB History Sync Complete.");
	}
	

	/** Given an itemID, return that item's entire history, as stored
	 * by the GW2Spidy API, as an ItemInfo object. Requires the current itemDB
	 * so it knows how to initialize the item without history.
	 */
	private ItemInfo getItemHistory(int itemID, ItemDB items) {
		//Base item info, w/o history
		ItemInfo baseInfo = new ItemInfo(items.getItemInfo(itemID));
		//Map from timestamps to their corresponding info
		HashMap<String, TPItemInfo> history = new HashMap<String, TPItemInfo>();
		
		//First, add sell listings
		int currPage = 1, lastPage = -1; //for traversing API
		do{
			String url = BASE_URL + "listings/" + itemID + "/sell/" + currPage;
			JSONObject sellJSON = JSONInterface.loadJSON(url);
			//Update our page navigation information
			currPage = JSONInterface.getInt(sellJSON, "page");
			lastPage = JSONInterface.getInt(sellJSON, "last_page");
			//Get the results
			JSONArray results = JSONInterface.getArray(sellJSON, "results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject currResult = JSONInterface.getObjectAtIndex(results, i);
				int sellPrice = JSONInterface.getInt(currResult, "unit_price");
				int numSell = JSONInterface.getInt(currResult, "quantity");
				int sellListings = JSONInterface.getInt(currResult, "listings");
				String timestamp = JSONInterface.get(currResult, "listing_datetime");
				history.put(timestamp, new TPItemInfo(
						itemID, 0, 0, 0, numSell, sellPrice, sellListings, timestamp));
			}
			currPage++;
		}
		while (currPage <= lastPage);
		
		//Next, add buy listings
		currPage = 1; lastPage = -1; //for traversing API
		do{
			String url = BASE_URL + "listings/" + itemID + "/buy/" + currPage;
			JSONObject sellJSON = JSONInterface.loadJSON(url);
			//Update our page navigation information
			currPage = JSONInterface.getInt(sellJSON, "page");
			lastPage = JSONInterface.getInt(sellJSON, "last_page");
			//Get the results
			JSONArray results = JSONInterface.getArray(sellJSON, "results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject currResult = JSONInterface.getObjectAtIndex(results, i);
				int buyPrice = JSONInterface.getInt(currResult, "unit_price");
				int numBuy = JSONInterface.getInt(currResult, "quantity");
				int buyListings = JSONInterface.getInt(currResult, "listings");
				String timestamp = JSONInterface.get(currResult, "listing_datetime");
				//If we didn't have any sell data add fresh value
				if (!history.containsKey(timestamp)) {
					history.put(timestamp, new TPItemInfo(itemID,0,0,0,0,0,0,timestamp));
				}
				//Update the buy values as appropriate
				history.get(timestamp).set(TPItemInfo.Attribute.BuyListings, buyListings);
				history.get(timestamp).set(TPItemInfo.Attribute.BuyPrice, buyPrice);
				history.get(timestamp).set(TPItemInfo.Attribute.NumBuy, numBuy);
			}
			currPage++;
		}
		while (currPage <= lastPage);
		
		//Convert our hashmap to an arraylist of all timestamps for which we have information
		ArrayList<TPItemInfo> finalList = new ArrayList<TPItemInfo>();
		for (String timestamp : history.keySet()) {
			TPItemInfo snap = history.get(timestamp);
			finalList.add(snap);
		}
		
		//Sort list by timestamp
		Collections.sort(finalList);
		//Update our (copied) item DB to include history and return it
		baseInfo.setHistory(finalList);
		return baseInfo;
			
	}

}
