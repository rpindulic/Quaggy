package quaggy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A snapshot of the GW2 trading post at any given time.
 * Stores a map from item ids to the information for that item at the given time.
 * @author Ryan Pindulic
 */
public class TPSnapshot {

	private Map<Integer, TPItemInfo> saveState;    	// Maps item ids to their current TP state
	
	/** Constructor for TPSnapshot, given the state as a map. */
	public TPSnapshot(Map<Integer, TPItemInfo> vals) {
		this.saveState = vals;
	}
	
	/** Given an ID, return the TPInfo associated with this ID. */
	public TPItemInfo get(int id) {
		return saveState.get(id);
	}
	
	/** Return a list of valid item ID numbers.
	 */
	public List<Integer> validIDS() {
		List<Integer> tbr = new ArrayList<Integer>();
		for (Integer id : saveState.keySet()) {
			if (saveState.get(id) != null) tbr.add(id);
		}
		return tbr;
	}
	
	/** Return all the information in this snapshot in an easily parsable list form. */
	public List<TPItemInfo> getAllInfo() {
		List<TPItemInfo> result = new ArrayList<TPItemInfo>();
		for (Integer id : saveState.keySet()) {
			result.add(saveState.get(id));
		}
		return result;
	}
}
