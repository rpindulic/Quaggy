package executables;

import core.FeatureStore;
import core.ItemDB;
import core.TPSnapshot;
import io.API;
import io.DB;
import io.SpidyAPI;

/** Repeatedly calls DPUpdate to fetch and store a new
 *  snapshot of the TP, then forwards updated feature
 *  information to the T1 servers.
 * @author Ryan
 *
 */
public class QuaggyEngine {
	
	/** The set of all items, and their histories. */
	public static ItemDB items;
	/** The updated feature vector database. */
	public static FeatureStore features;
	
	/** How many minutes to wait between each update. */
	private static final int MINUTES_WAIT = 1;
	
	public static void main(String[]args) {
		
		//Get the current state
		DB db = new DB();
		API api = new SpidyAPI();
		items = db.getItemDB(true);
		features = new FeatureStore();
		
		//Continuously update
		while (true) {
			try {
				TPSnapshot snapshot = api.snapshot();
				db.saveTPSnapshot(snapshot);
				features.load(items, snapshot);
				items.addCurrentState(snapshot);
				
				System.out.println();
				System.out.println("Database updated");
				System.out.println();
				
				Thread.sleep(1000 * 60 * MINUTES_WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
