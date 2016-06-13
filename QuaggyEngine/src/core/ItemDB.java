package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Database of items from GW2. Stores mapping from Item ID #s
 * to all the information we have about that item.
 * @author Ryan Pindulic
 *
 */
public class ItemDB {
	// Stores a mapping from item id #s to the info.
	private Map<Integer, ItemInfo> db;
	// Stores a sorted list of item ids for traversing the db in sorted order.
	private List<Integer> validItemIDs;
	
	/** Initialize an empty item database. */
	public ItemDB() {
		this(new HashMap<Integer, ItemInfo>());
	}
	
	/** Initialize an ItemDB from a map from integers to item info. */
	public ItemDB(Map<Integer, ItemInfo> db) {
		this.db = db;
		this.validItemIDs = new ArrayList<Integer>();
		for (int id : db.keySet()) {
			validItemIDs.add(id);
		}
		Collections.sort(validItemIDs);
	}
	
	/** Purges all history from more than horizon days ago from main memory.
	 *  This data will not be removed from the backing MySQL store. 
	 *  
	 *  If horizon < 0, throws IllegalArgumentException.
	 */
	public void purge(int horizon) {
		DateTime firstDate = DateTime.daysBack(horizon);
		for (int id : validItemIDs) {
			db.get(id).purge(firstDate);
		}
	}
	
	/** Adds the current snapshot to the ItemDB as the 
	 * most recent state. Useful for performing up-to-date computations.
	 * No permanent changes will be made to database.
	 */
	public void addCurrentState(TPSnapshot curr) {
		for (TPItemInfo info : curr.getAllInfo()) {
			if (db.containsKey(info.getID())) {
				db.get(info.getID()).getHistory().add(info);
			}
		}
	}
	
	/** Takes a snapshot, including every item at its most recent price,
	 *  from the item database. Requires history.
	 *  
	 *  This is useful for making a 'mock snapshot' for testing purposes
	 *  when you don't really want to wait for the API.
	 */
	public TPSnapshot snapshot() {
		Map<Integer, TPItemInfo> snapshot = new HashMap<Integer, TPItemInfo>();
		for (int i : validItemIDs) {
			List<TPItemInfo> history = db.get(i).getHistory();
			if (history.size() > 0) {
				snapshot.put(i, history.get(0));
			}
		}
		return new TPSnapshot(snapshot);
	}
	
	/**Get a list of valid item ids */
	public List<Integer> validIDS() {
		return validItemIDs;
	}
	
	/** Get the info for the item with ID itemID */
	public ItemInfo getItemInfo(int itemID) {
		return db.get(itemID);
	}
	
	@Override
	/** Print every item in the DB to the screen. 
	 *  Note that this may take some time to complete.
	 */
	public String toString() {
		String res = "";
		for (int id : validItemIDs) {
			res += db.get(id).toString() + "\n";
		}
		return res;
	}
}
