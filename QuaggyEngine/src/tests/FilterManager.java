package tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import core.FeatureVector;
import core.ItemDB;
import core.ItemType;
import core.TPSnapshot;
import core.FeatureVector.Feature;
import core.FeatureVector.Mode;
import io.JSONInterface;

/** Creates an object which can parse a JSON
 *  filter files and store the resulting 
 *  filter preferences in an easily accessible way
 *  
 *  Note: This is used only for testing purposes, since this computation
 *  has now been moved to the edge servers.
 * @author Ryan Pindulic
 */
public class FilterManager {
	
	private int historyDays;					// How many historical days should we consider?
	private Mode buyMode;						// Should we put a buy bid or do it instantly?
	private Mode sellMode;						// Should we put a sell bid or do it instantly?
	
	private Map<Feature, Double> minVals;		// Minimum values allowed for a given feature
	private Map<Feature, Double> maxVals;		// Maximum values allowed for a given feature
	private Set<ItemType> itemTypes;			// Allowed item types
	
	private Feature sortFeature;				// Which feature should we sort filter results by?
	private boolean sortAsc;					// If true, sort ascending. Else, sort descending.
	
	/** Create a new PreferenceParser and perform the analysis.
	 *  Throws IllegalArgumentException in case of invalid
	 *  or improperly formatted input file. 
	 *  Assumes base directory of "config" and extension ".json"
	 *  for file.
	 */
	public FilterManager(String file) {
		minVals = new HashMap<Feature, Double>();
		maxVals = new HashMap<Feature, Double>();
		itemTypes = new HashSet<ItemType>();
		// Parse the input file as JSON.
		String jsonText = JSONInterface.parseFile("config/" + file + ".json");
		JSONObject root = JSONInterface.loadFromText(jsonText);
		historyDays = JSONInterface.getInt(root, "HistoryDays");
		
		// Parse modes into enums.
		String buyModeS = JSONInterface.get(root, "BuyMode");
		String sellModeS = JSONInterface.get(root, "SellMode");
		if (!buyModeS.equalsIgnoreCase("bid") && !buyModeS.equalsIgnoreCase("instant")) {
			throw new IllegalArgumentException("Buy Mode : " + buyModeS + " invalid.");
		}
		if (!sellModeS.equalsIgnoreCase("bid") && !sellModeS.equalsIgnoreCase("instant")) {
			throw new IllegalArgumentException("Sell Mode : " + sellModeS + " invalid.");
		}
		buyMode = (buyModeS.equalsIgnoreCase("bid")) ? Mode.BID : Mode.INSTANT;
		sellMode = (sellModeS.equalsIgnoreCase("bid")) ? Mode.BID : Mode.INSTANT;
		
		// Try to parse min/max values into maps.
		JSONObject bounds = JSONInterface.getObject(root, "Bounds");
		for (Feature f : Feature.values()) {
			try{
				JSONObject fOb = JSONInterface.getObject(bounds, f.name());
				try {
					minVals.put(f, JSONInterface.getDouble(fOb, "Min"));
				} catch (IllegalArgumentException e) {}
				try {
					maxVals.put(f, JSONInterface.getDouble(fOb, "Max"));
				} catch (IllegalArgumentException e) {}
			}
			catch (IllegalArgumentException e) {}
		}
		
		// Parse the list of itemTypes we're considering.
		JSONArray types = JSONInterface.getArray(root, "Types");
		for (int i = 0; i < types.length(); i++) {
			itemTypes.add(ItemType.fromString(JSONInterface.getAtIndex(types, i)));
		}
		
		// Parse sort information.
		sortFeature = Feature.valueOf(JSONInterface.get(root, "SortBy"));
		String sortOrder = JSONInterface.get(root, "SortOrder");
		if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
			throw new IllegalArgumentException("Sort order " + sortOrder + " but must be ASC or DESC");
		}
		sortAsc = (sortOrder.equalsIgnoreCase("ASC"));
	}
	
	/** Gets the number of historical days we want to consider. */
	public int historyDays() {
		return historyDays;
	}
	
	/** Gets the mode we will be using to buy items, instantly or bid. */
	public Mode buyMode() {
		return buyMode;
	}

	/** Gets the mode we will be using to sell items, instantly or bid. */
	public Mode sellMode() {
		return sellMode;
	}
	
	/** Given a feature vector, decide whether it meets the standards
	 *  set by the user filter or not. 
	 */
	public boolean filter(FeatureVector fv) {
		// Check to make sure the item type is correct.
		ItemType type = ItemType.values()[(int)fv.get(Feature.ItemType)];
		if (!itemTypes.contains(type)) return false;
		// Check to make sure each feature is correct.
		for (Feature f : Feature.values()) {
			if (minVals.containsKey(f) && fv.get(f) < minVals.get(f)) return false;
			if (maxVals.containsKey(f) && fv.get(f) > maxVals.get(f)) return false;
		}
		return true;
	}
	
	/** Gets a set of all the item types to consider. */
	public Set<ItemType> itemTypes() {
		return itemTypes;
	}
	
	/** What is the minimum value of feature f to consider?
	 * (Double.MIN_VALUE if no minimum specified)
	 */
	public double minAllowed(Feature f) {
		if (minVals.containsKey(f)) return minVals.get(f);
		else return Double.MIN_VALUE;
	}
	
	/** What is the maximum value of feature f to consider?
	 * (Double.MAX_VALUE if no minimum specified)
	 */
	public double maxAllowed(Feature f) {
		if (maxVals.containsKey(f)) return maxVals.get(f);
		else return Double.MAX_VALUE;
	}
	
	/** Given a database of all items to consider (including history) and a snapshot
	 *  of the current state of the TP, look through all items in the DB
	 *  and return a list of the feature vectors corresponding to those allowed
	 *  by the filter. Note items should have at least as much history as requested to be 
	 *  considered by prefs.
	 */
	public List<FeatureVector> filterAll(ItemDB items, TPSnapshot snapshot) {
		System.out.println();
		System.out.println("*******************");
		System.out.println("Running filter algorithm...");
		List<FeatureVector> result = new ArrayList<FeatureVector>();
		for (int id : items.validIDS()) {
			// If there's no history, we can't predict anything.
			if (items.getItemInfo(id).getHistory().size() == 0) continue;
			// Build the feature vector for this item.
			FeatureVector fv = new FeatureVector(id, items, snapshot, 
					historyDays(), buyMode(), sellMode());
			// Add information only if good deal.
			if (filter(fv)) {
				result.add(fv);
			}
		}
		// Sort results.
		Collections.sort(result, new Comparator<FeatureVector>() {
			@Override
			public int compare(FeatureVector fv1, FeatureVector fv2) {
				double val1 = fv1.get(sortFeature);
				double val2 = fv2.get(sortFeature);
				double diff = sortAsc ? val1 - val2 : val2 - val1;
				if (Math.abs(diff) < 0.0001) return 0;
				else if (diff < 0) return -1;
				else return 1;
			}
		});
		
		System.out.println("Deal prediction algorithm complete.");
		return result;
	}
	
	@Override
	public String toString() {
		String res = "\n";
		res += "*******************\n";
		res += "User Preferences\n";
		res += "Days of history considered: " + historyDays + "\n";
		res += "Buy mode: " + buyMode.name() + "\n";
		res += "Sell mode: " + sellMode.name() + "\n";
		res += "Item types: " + Arrays.toString(itemTypes.toArray()) + "\n";
		for (Feature f : Feature.values()) {
			if (minVals.containsKey(f) || maxVals.containsKey(f)) {
				res += f.name();
				if (minVals.containsKey(f)) {
					res += " Min: " + minVals.get(f);
				}
				if (maxVals.containsKey(f)) {
					res += " Max: " + maxVals.get(f);
				}
				res += "\n";
			}
		}
		return res;
	}
}
