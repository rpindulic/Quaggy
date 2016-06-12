package io;

import java.text.DecimalFormat;

import quaggy.ItemDB;
import quaggy.TPSnapshot;

/** Abstract class defining the interface to be used for any API
 * we wish to incorporate into our code. 
 * @author Ryan Pindulic
 */
public abstract class API {
	
	protected String BASE_URL;				//Root URL for the api
	
	/** Creates a new API with the given base URL. */
	public API(String url) {
		this.BASE_URL = url;
	}
	
	/** Query the API and return a database of all item information
	 *  WITHOUT trading post history.
	 */
	public abstract ItemDB getItemDB();
	
	/** Takes a snapshot of the current state of the TP according 
	 *  to the specific API and returns this snapshot.
	 *  This may take a significant amount of time to complete
	 *  depending on the API being used.
	 */
	public abstract TPSnapshot snapshot();
	
	/** Queries the entire database and TP history from the API,
	 *  and adds this history to the provided database.
	 *  This function accomplishes this dynamically, so that 
	 *  information is added individually as it is loaded.
	 *  Depending on the API implementation, this may take a 
	 *  tremendous amount of time.
	 *  
	 *  If fresh is set to true, the DB will be wiped before the sync,
	 *  otherwise we will simply add the data on top of the old DB.
	 *  
	 *  This will load all items with IDs greater than or equal to start.
	 *  The start parameter is useful for starting your sync where you 
	 *  may have left off in case you must stop prematurely.
	 */
	public abstract void resyncHistory(DB database, boolean fresh, int start);

	/** Same as resyncHistory above but will start at 0 by default. */
	public void resyncHistory(DB database, boolean fresh) {
		resyncHistory(database, fresh, 0);
	}
	
	/** Given a number and a total, format a String
	 * representing this number as a percent of the total.
	 * Useful for keeping users up-to-date on progress
	 * of operations that may take a long time to complete.
	 */
	public static String formatPercent(int num, int total) {
		double result = ((double)num / total) * 100;
		DecimalFormat formatter = new DecimalFormat("#0.000");
		return formatter.format(result) + "%";
	}
}
