package executables;

import io.API;
import io.DB;
import io.SpidyAPI;
import quaggy.TPSnapshot;

/** Updates the DB with modern information. 
 *  Should be run routinely at even intervals to keep DB
 *  up to date. */
public class DBUpdate {
	
	public static void main(String[]args) {
		update();
	}
	
	public static TPSnapshot update() {
		DB db = new DB();
		API api = new SpidyAPI();
		TPSnapshot snapshot = api.snapshot();
		db.saveTPSnapshot(snapshot);
		System.out.println();
		System.out.println("Updating DB with snapshot complete.");
		return snapshot;
	}
}
