package tests;

import java.util.List;

import core.FeatureVector;
import io.API;
import io.DB;
import io.SpidyAPI;

/** Executable which makes predictions on suggested items to
 *  buy, using parameters for how to determine what makes
 *  a "good deal" as input.
 *  
 *  Note: This executable file is used mostly for testing purposes, since
 *  this functionality has now been moved to the edge servers.
 * @author Ryan Pindulic
 *
 */
public class DealPredictor {
	
	// The number of days of history to store.
	private static final int HISTORY_HORIZON = 30;
	
	/** Runs the DealPredictor. Pulls historical data from
	 *  DB and queries current TP snapshot from API.
	 */
	public static void main(String[]args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Usage: DealPredictor.java <filter_name>");
		}
		DB db = new DB();
		API api = new SpidyAPI();
		FilterManager prefs = new FilterManager(args[0]);
		List<FeatureVector> matches = prefs.filterAll(db.getItemDB(HISTORY_HORIZON), api.snapshot());
		System.out.println();
		for (FeatureVector fv : matches) {
			System.out.println(fv);
		}
	}
}
