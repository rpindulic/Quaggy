package executables;

import core.ItemDB;
import io.API;
import io.DB;
import io.SpidyAPI;

/** Initializes the server database, without history. */
public class DBInit {
	
	public static void main(String[]args) {
		System.out.println("***************");
		System.out.println("Initializing default DB...");
		DB db = new DB();
		API api = new SpidyAPI();
		// Create empty tables.
		db.createItemsTable();
		db.createListingsTable();
		// Pull the history-less item information from API and store it.
		ItemDB items = api.getItemDB();
		db.saveItemDB(items);
		System.out.println("Initializing default DB done.");
	}

}
