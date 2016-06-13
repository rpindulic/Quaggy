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
	
	// The set of all items, and their histories.
	public static ItemDB items;
	// The updated feature vector database.
	public static FeatureStore features;
	
	// How many minutes to wait between each update.
	private static final int MINUTES_WAIT = 1;
	// The number of days of history to store in main memory.
	private static final int HISTORY_HORIZON = 30;
	// We should store the history 1 out of every X cycles
	private static final int HISTORY_CYCLES = 5;
	
	public static void main(String[]args) {
		
		//Get the current state
		DB db = new DB();
		API api = new SpidyAPI();
		items = db.getItemDB(HISTORY_HORIZON);
		features = new FeatureStore();
		
		//Continuously update
		int cycle = 0;
		while (true) {
			try {
				TPSnapshot snapshot = api.snapshot();
				features.load(items, snapshot);
				items.purge(HISTORY_HORIZON);
				if (++cycle == HISTORY_CYCLES) {
					cycle = 0;
					items.addCurrentState(snapshot);
					db.saveTPSnapshot(snapshot);
				}
				
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
