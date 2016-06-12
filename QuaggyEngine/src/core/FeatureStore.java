package core;

import java.util.HashMap;
import java.util.Map;

import core.FeatureVector.Mode;
import io.RESTClient;

/** Stores the most recent set of feature vectors that we've 
 *  parsed. Can also forward to T1 servers when received.
 * @author Ryan
 */
public class FeatureStore {
	
	/** The client can specify the number of days of history to be considered.
	 *  Since computation is done here at the backend, we have to precompute a result
	 *  for each of an allowed set of days. */
	private static final int[] HISTORY = {1,2,3,4,5,6,7,8,9,10,15,20,25,30,35,40,45,
	   50,75,100,125,150,175,200,250,300,350};
	
	/** Create a new, empty, feature store. */
	public FeatureStore() {
	}
	
	
	/** Given a digest mapping from id:history_days:buy:sell:feature -> val,
	 *  give this digest to the specified endpoint in JSON format
	 */
	private void broadcastDigest(Map<String, Double> digest, String url) {
		// First, convert the values of our dict to a json object.
		String json = "{";
		for (String key : digest.keySet()) {
			json += "\"" + key + "\":" + "\"" + digest.get(key) + "\",";
		}
		// Remove the trailing comma.
		json = json.substring(0, json.length() - 1);
		json += "}";
		// Now broadcast these values to the edge servers.
		try {
			RESTClient.post(url, json);
		}
		catch (IllegalArgumentException e) { // Endpoint / edge server not up
			System.out.println("Failed to write to edge server");
		}
	}
	
	/** Given the current item database and a snapshot of the TP, 
	 *  update our database of feature information. This will
	 *  remove all old feature information.
	 */
	public void load(ItemDB items, TPSnapshot snapshot) {
		for (int id : items.validIDS()) {
			// Maps from id:history_days:buy:sell:feature -> val.
			Map<String, Double> digest = new HashMap<String, Double>();
			// If there's no history or this isn't for sale, we can't predict anything.
			if (items.getItemInfo(id).getHistory().size() == 0 || 
					snapshot.get(id).get(TPItemInfo.Attribute.NumBuy) == 0) {
				continue;
			}
			for (int hist : HISTORY) {
				for (Mode buy : Mode.values()) {
					for (Mode sell : Mode.values()) {
						FeatureVector fv = new FeatureVector(id, items, snapshot, hist, buy, sell);
						for (FeatureVector.Feature f : FeatureVector.Feature.values()) {
							double fVal = fv.get(f);
							String key = id +":" + hist + ":" + buy + ":" + sell + ":" + f;
							digest.put(key,  fVal);
						}
					}
				}
			}
			// Broadcast our digest.
			System.out.println("Broadcasting: " + id);
			broadcastDigest(digest, "http://localhost:5000/backend/digest");
		}
	}
}
