package executables;

import io.DB;
import io.SpidyAPI;

/** Wipes the database's history and resyncs
 *  from SpidyAPI. Will take a very long time.
 * @author Ryan
 *
 */
public class DBHistorySync {
	
	// The number of days back we want to sync to
	public static final int HISTORY_HORIZON = 30;
	
	public static void main(String[]args) {
		new SpidyAPI().resyncHistory(new DB(), true, HISTORY_HORIZON);
	}
}
