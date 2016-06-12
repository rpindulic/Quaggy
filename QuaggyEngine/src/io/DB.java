package io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import core.ItemDB;
import core.ItemInfo;
import core.TPItemInfo;
import core.TPSnapshot;


/** Acts as an interface into the MySQL DB used to store long-term information.
 * Can either read from or write to the DB.
 * @author Ryan Pindulic
 */
public class DB {
	
	//Stores a connection to the MySQL DB. Null until connect() is called.
	Connection conn;
	
	//Stores some information for accessing the DB.
	private static final String USER = "root";
	private static final String PASSWORD = "root";
	private static final String SERVER_NAME = "localhost";
	private static final int PORT_NUMBER = 3306;
	private static final String DB_NAME = "quaggy";
	
	//The item table, which stores information about every item in GW2.
	public static final String ITEM_TABLE = "items";
	//The listings table, which stores information about the history of TP sales in GW2.
	public static final String LISTINGS_TABLE = "listings";
	
	/** Create a new DB, and connect to it. */
	public DB() {
		connect();
	}
	
	/** Using the item table in the DB, return an ItemDB of
	 *  the currently known items. 
	 *  Include history only if history flag is set to true.
	 *  
	 *  Throws IllegalArgumentException in case of failure.
	 */
	public ItemDB getItemDB(boolean history) {
		System.out.println();
		System.out.println("******************");
		System.out.println("Loading item database...");
		Statement stmt = null;
	    try {
			Map<Integer, ItemInfo> rval = new HashMap<Integer, ItemInfo>();
			//Query the items table to find all item information
	        stmt = conn.createStatement();
	        stmt.execute("SELECT * FROM " + ITEM_TABLE); 
	        ResultSet rs = stmt.getResultSet();
	        while (rs.next()) {
	        	//Parse the attributes returned by the SQL query into attributes
	        	String[] attrs = new String[ItemInfo.Attribute.values().length];
	        	int idVal = rs.getInt("ID");
	        	for (ItemInfo.Attribute attr : ItemInfo.Attribute.values()) {
	        		String sqlName = attr.name().toUpperCase();
	        		attrs[attr.ordinal()] = rs.getString(sqlName);
	        	}
	        	//Create the new iteminfo
	        	ItemInfo info = new ItemInfo(idVal, attrs);
	        	//Pull history if needed
	        	if (history) {
	        		info.setHistory(getHistory(idVal));
	        	}
	        	//Map from the item ID to its attributes
	        	rval.put(idVal, info);
	        }
	        stmt.close();
	        System.out.println("Loading item database complete.");
	        return new ItemDB(rval);
	    } 
	    catch (SQLException e) {
	    	throw new IllegalArgumentException("Error loading Item DB From MySQL : " + e.toString());
	    }
	}
	
	/** Given an item's ID, return a list of all listings
	 * we have stored in its history.
	 * 
	 * Throws IllegalArgumentException in case of failure.
	 */
	public List<TPItemInfo> getHistory(int itemID) {
		Statement stmt = null;
	    try {
	    	List<TPItemInfo> rval = new ArrayList<TPItemInfo>();
	        stmt = conn.createStatement();
	        stmt.execute("SELECT * FROM " + LISTINGS_TABLE + " L WHERE L.ID = " + itemID + " ORDER BY timestamp"); 
	        ResultSet rs = stmt.getResultSet();
	        while (rs.next()) {
	        	String timestamp = rs.getString("TIMESTAMP");
	        	
	        	int[] attrs = new int [TPItemInfo.Attribute.values().length];
	        	for (TPItemInfo.Attribute attr : TPItemInfo.Attribute.values()) {
	        		attrs[attr.ordinal()] = rs.getInt(attr.name().toUpperCase());
	        	}
	        	rval.add(new TPItemInfo(itemID, attrs, timestamp));
	        }
	        stmt.close();
	        return rval;
	    } 
	    catch (SQLException e) {
	    	throw new IllegalArgumentException("Error loading Item DB From MySQL : " + e.toString());
	    }
	}
	
	/** Given an ItemDB, saves this into the items table.
	 *  Note that this will discard any history in the ItemDB and 
	 *  save only the actual item information.
	 *  
	 *  Throws IllegalArgumentException in case of failure.
	 */
	public void saveItemDB(ItemDB db) {	
		for (int id : db.validIDS()) {
			this.addItem(db.getItemInfo(id));
		}
	}
	
	/** Given a TP snapshot, saves this to the listings table.
	 *  Note that this will not discard past history, only add to it.
	 *  
	 *  Throws IllegalArgumentException in case of failure.
	 */
	public void saveTPSnapshot(TPSnapshot snapshot) {
		for (int id : snapshot.validIDS()) {
			this.addListing(snapshot.get(id));
		}
	}
	
	/** Adds an item to the item table in the DB. 
	 * 
	 *  Throws IllegalArgumentException in case of failure.*/
	public void addItem(ItemInfo newItem) {
		try{
			String insertString = "INSERT INTO " + ITEM_TABLE + " VALUES (" + 
					newItem.getId() + ",";
			for (ItemInfo.Attribute attr : ItemInfo.Attribute.values()) {
				String val = newItem.get(attr);
				val = val.replaceAll("'", "''"); //double quote for SQL escape
				insertString+="'" + val + "',";
			}
			// Remove trailing comma.
			insertString = insertString.substring(0, insertString.length()-1);
			insertString+=")";
			
			executeUpdate(insertString);
		}
		catch (SQLException e) {
			throw new IllegalArgumentException("Error inserting item " + newItem + " into DB : " + e.toString());
		}
	}
	
	/** Adds a TP listing to the listings table in the DB.
	 * 
	 *  Throws IllegalArgumentException in case of failure.*/
	public void addListing(TPItemInfo newListing) {
		try{
			String insertString = "INSERT INTO " + LISTINGS_TABLE + " VALUES (" + 
					newListing.getID() + "," + 
					"'" + newListing.time() +  "',";
			for (TPItemInfo.Attribute attr : TPItemInfo.Attribute.values()) {
				int val = newListing.get(attr);
				insertString += "'" + val + "',";
			}
			// Remove trailing comma.
			insertString = insertString.substring(0, insertString.length()-1);
			insertString+=")";
			
			executeUpdate(insertString);
		}
		catch (SQLException e) {
			throw new IllegalArgumentException("Error inserting item " + newListing + " into DB : " + e.toString());
		}
	}
	
	/** Create the items table, using attribute information from ItemInfo.
	 * 
	 *  Throws IllegalArgumentException in case of failure. */
	public void createItemsTable() {
		try{
			String createString = 
					"CREATE TABLE " + ITEM_TABLE + " ( " +
					"ID INTEGER NOT NULL, ";
			for (ItemInfo.Attribute attr : ItemInfo.Attribute.values()) {
				String name = attr.name().toUpperCase();
				createString+= name + " varchar(160) NOT NULL, ";
			}
			createString+="PRIMARY KEY (ID))";
			
			executeUpdate(createString);
		}
		catch (SQLException e) {
			throw new IllegalArgumentException("Error, could not create item table : " + e.toString());
		}
	}
	
	/** Create the listings table, where each tuple has the id of the item being sold,
	 *  the timestamp of the listing, and the listing info. 
	 * 
	 *  Throws IllegalArgumentException in case of failure.*/
	public void createListingsTable() {
		try{
			String createString = 
					"CREATE TABLE " + LISTINGS_TABLE + " ( " +
					"ID INTEGER NOT NULL, " +
					"TIMESTAMP varchar(160) NOT NULL, ";
			for (TPItemInfo.Attribute attr : TPItemInfo.Attribute.values()) {
				String name = attr.name().toUpperCase();
				createString += name + " varchar(160) NOT NULL, ";
			}
			createString += "PRIMARY KEY (ID, TIMESTAMP))";
			
			executeUpdate(createString);
		}
		catch (SQLException e) {
			throw new IllegalArgumentException("Error, could not create listings table : " + e.toString());
		}
	}
	
	/** Drop the items table, in case we need to refresh item info from API.
	 * 
	 *  Throws IllegalArgumentException in case of failure.*/
	public void dropItemsTable() {
		try{
			String dropString = "DROP TABLE " + ITEM_TABLE;
			executeUpdate(dropString);
		}
		catch (SQLException e) {
			throw new IllegalArgumentException("Error, could not drop item table : " + e.toString());
		}
	}
	
	/** Drop the listings table. Since listings table is historical, this should not be called
	 *  except when you really want to erase all of history (ie to resync with the API).
	 * 
	 *  Throws IllegalArgumentException in case of failure.*/
	public void dropListingsTable() {
		try{
			String dropString = "DROP TABLE " + LISTINGS_TABLE;
			executeUpdate(dropString);
		}
		catch (SQLException e) {
			throw new IllegalArgumentException("Error, could not drop listings table : " + e.toString());
		}
	}
	
	/**
	 * Run a SQL command which does not return a recordset:
	 * CREATE/INSERT/UPDATE/DELETE/DROP/etc.
	 * 
	 * @throws SQLException If something goes wrong
	 */
	private boolean executeUpdate(String command) throws SQLException {
	    Statement stmt = null;
	    try {
	        stmt = conn.createStatement();
	        stmt.executeUpdate(command); 
	        return true;
	    } 
	    finally {
	        if (stmt != null) { 
	        	stmt.close(); 
	        }
	    }
	}
	
	/** Connect to the Quaggy DB. Must succeed before read/write queries can be made. 
	 	In case of failure, throw IllegalArgumentException. */
	private void connect() {
		try{
			Properties connectionProps = new Properties();
			connectionProps.put("user", USER);
			connectionProps.put("password", PASSWORD);
	
			conn = DriverManager.getConnection("jdbc:mysql://"
					+ SERVER_NAME + ":" + PORT_NUMBER + "/" + DB_NAME,
					connectionProps);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Error connecting to db " + DB_NAME + "\n" + e);
		}

	}
}
