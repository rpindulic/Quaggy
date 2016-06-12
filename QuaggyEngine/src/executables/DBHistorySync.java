package executables;

import io.DB;
import io.SpidyAPI;

/** Wipes the database's history and resyncs
 *  from SpidyAPI. Will take a very long time.
 * @author Ryan
 *
 */
public class DBHistorySync {
	public static void main(String[]args) {
		new SpidyAPI().resyncHistory(new DB(), true);
	}
}
