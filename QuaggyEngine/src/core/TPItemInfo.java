package core;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/** Trading post information for a single GW2 item, at a snapshot
 *  in time. 
 *  @author Ryan Pindulic
 */
public class TPItemInfo implements Comparable<TPItemInfo> {
	
	// A reference to the sum of all taxes the TP takes
    public static final double LISTING_TAX = 0.05;
    public static final double SELLING_TAX = 0.10;
    public static final double TAX = LISTING_TAX + SELLING_TAX;
    public static final double TAX_FACTOR = 1.0 - TAX;
	
	/** Different attributes related to an item's TP value. */
	public enum Attribute {
		NumSell,			// Number of this item offered for sale
		NumBuy,				// Number of this item offered to buy
		SellPrice,			// Current price offered by those looking to sell
		BuyPrice,			// Current price offered by those looking to buy
		SellListings,		// TODO: Figure out what this means
		BuyListings			// TODO: Figure out what this means
	};
	
	private int[] attrs;	// Store attributes relevant to this TP listing
	
	private int itemID;				// Item's unique ID
	private String timestamp;		// Timestamp at which this info was taken
	
	/** Constructor for creating a TPInfo object 
	 *  without sell or buy listings. */
	public TPItemInfo(int id, int numBuy, int buyPrice, int numSell, int sellPrice, String timestamp) {
		this.attrs = new int[Attribute.values().length];
		this.itemID = id;
		this.timestamp = timestamp;
		
		attrs[Attribute.NumBuy.ordinal()] = numBuy;
		attrs[Attribute.BuyPrice.ordinal()] = buyPrice;
		attrs[Attribute.NumSell.ordinal()] = numSell;
		attrs[Attribute.SellPrice.ordinal()] = sellPrice;
	}
	
	/**Constructor for creating TPInfo object with all needed information. */
	public TPItemInfo(int id, int numBuy, int buyPrice, int buyListings, 
			int numSell, int sellPrice, int sellListings, String timestamp) {
		this(id, numBuy, buyPrice, numSell, sellPrice, timestamp);
		attrs[Attribute.BuyListings.ordinal()] = buyListings;
		attrs[Attribute.SellListings.ordinal()] = sellListings;
	}
	
	/** Constructor for a TPInfo Object without buy
	 *  or sell listings and without timestamp. Current
	 *  time will be used instead. */
	public TPItemInfo(int id, int numBuy, int buyPrice, int numSell, int sellPrice) {
		this(id, numBuy, buyPrice, numSell, sellPrice, 
				new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()));
	}
	
	/** Constructor for creating a TPInfo object with the attributes as an array.
	 *  Throws IllegalArgumentException if array length incorrect */
	public TPItemInfo(int id, int[] attrs, String timestamp) {
		if (attrs.length != Attribute.values().length) {
			throw new IllegalArgumentException("TPItemInfo : provided attributes array wrong length");
		}
		this.itemID = id;
		this.attrs = attrs;
		this.timestamp = timestamp;
	}
	
	/** Gets the value of a given attribute. */
	public int get(Attribute attr) {
		return attrs[attr.ordinal()];
	}
	
	/** Sets the value of a given attribute. */
	public void set(Attribute attr, int value) {
		attrs[attr.ordinal()] = value;
	}
	
	@Override
	public String toString() {
		return "Item " + itemID + " at " + timestamp + " : " + get(Attribute.NumBuy) + 
				" buy offers at " + get(Attribute.BuyPrice) + " per, " + 
				get(Attribute.NumSell) + " sell offers at " + get(Attribute.SellPrice) + " per.";
	}
	
	/** Compare two TPInfo objects in order from newest timestamp ->
	 * oldest timestamp.
	 */
	@Override
	public int compareTo(TPItemInfo that) {
		DateTime ours = new DateTime(timestamp);
		DateTime theirs = new DateTime(that.timestamp);
		return -1 * ours.compareTo(theirs);
	}
	
	/** Gets the ID of the item associated with this listing. */
	public int getID() {
		return this.itemID;
	}
	
	/** Return the date/time at which this measurement was taken. */
	public DateTime time() {
		return new DateTime(timestamp);
	}
	
}
