package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stores information regarding a single item in GW2.
 *  This is information that can be found in the APIs
 *  and may be relevant in some way.
 * @author Ryan Pindulic
 *
 */
public class ItemInfo {
	
	public static final int MAX_ID = 80000;		// Maximum allowable Item ID
	
	/** Different attributes related to an item. */
	public enum Attribute { 
		Name, 					// Item's name
		Type, 					// Item's type, as defined in ItemType.java
		Rarity, 				// Item's rarity/color
		Level, 					// Level required to use this item, if available
		VendorValue, 			// Value this item can be vendored for, if available
		DefaultSkin, 			// URL for image of this item's skin, if available
		DefaultIcon 			// URL for image of this item's icon, if available
	};
	
	private int id;							// Item's id number, key into TPSnapshot
	private String[] attrs;					// Store attributes relevant to this item
	private List<TPItemInfo> history;		// TP history, sorted from newest -> oldest
	
	/** Construct a new ItemInfo. */
	public ItemInfo(int id, String name, String type, String rarity, 
			String level, String vendorValue, String defaultSkin, String urlLoc) {
		this.attrs = new String[Attribute.values().length];
		this.history = new ArrayList<TPItemInfo>();
		this.id = id;
		attrs[Attribute.Name.ordinal()] =  name;
		attrs[Attribute.Type.ordinal()] = type;
		attrs[Attribute.Rarity.ordinal()] = rarity;
		attrs[Attribute.Level.ordinal()] = level;
		attrs[Attribute.VendorValue.ordinal()] = vendorValue;
		attrs[Attribute.DefaultSkin.ordinal()] = defaultSkin;
		attrs[Attribute.DefaultIcon.ordinal()] = urlLoc;
	}
	
	/** Construct a new ItemInfo given the attribute array already constructed.
	 *  Throws IllegalArgumentException if attrs is incorrect length. */
	public ItemInfo(int id, String[] attrs) {
		if (attrs.length != Attribute.values().length) {
			throw new IllegalArgumentException("ItemInfo constructor : invalid # of attributes");
		}
		this.id = id;
		this.attrs = attrs;
		this.history = new ArrayList<TPItemInfo>();
	}
	
	/** Copy constructor. */
	public ItemInfo(ItemInfo other) {
		this.attrs = new String[Attribute.values().length];
		this.history = new ArrayList<TPItemInfo>();
		this.id = other.id;
		// Copy old data
		for (int i = 0; i < attrs.length; i++) attrs[i] = other.attrs[i];
		Collections.copy(history, other.history);
	}
	
	/** Gets the value of the attribute attr for this item. */
	public String get(Attribute attr) {
		return attrs[attr.ordinal()];
	}
	
	/** Return the history of the item in terms of TP prices. */
	public List<TPItemInfo> getHistory() {
		return history;
	}	
	
	/** Return the item's ID. */
	public int getId() {
		return id;
	}
	
	/** Purges all history from earlier than the DateTime provided.
	 *  This history will be removed from main memory, but will not
	 *  be removed from the backing MySQL store.
	 */
	public void purge(DateTime firstDate) {
		for (int i = 0; i < history.size(); i++) {
			if (history.get(i).time().compareTo(firstDate) < 0) {
				history.remove(i);
				i--;
			}
		}
	}
	
	/** Set the history to be what we want */
	public void setHistory(List<TPItemInfo> history) {
		this.history = history;
		Collections.sort(history);
	}
	
	/** Sets the value of the attribute attr for this item. */
	public void set(Attribute attr, String val) {
		attrs[attr.ordinal()] = val;
	}

	/** Set the item's ID to be what we want. */
	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return get(Attribute.Name) + " (" + id + ")";
	}
	
	/** Get the mean of a function that takes as input a TPItemInfo attribute
	 *  over its application to the most recent N history examples. 
		If we have less entries than that, throw IllegalArgumentException.
	 */
	public double mean(TPItemInfo.Attribute attr, int N, DoubleFunction func) {
		if (N > history.size() || N <= 0) {
			throw new IllegalArgumentException("Want mean of " + N + " but history"
					+ " only contains " + history.size());
		}
		double sum = 0;
		for (int i = 0; i < N; i++) {
			sum += func.apply(history.get(i).get(attr));
		}
		return sum / N;
	}
	
	/** Get the mean TPItemInfo attribute over the most recent N history entries. 
	 * If we have less entries than that, throw IllegalArgumentException
	 */
	public double mean(TPItemInfo.Attribute attr, int N) {
		return mean(attr, N, (double x) -> x);
	}
	
	/** Get the variance of a function that takes as input a TPItemInfo attribute
	 *  over its application to the most recent N history examples.
	 *  If we have less entries than that, throw IllegalArgumentException.
	 */
	public double variance(TPItemInfo.Attribute attr, int N, DoubleFunction func) {
		double mean = mean(attr, N, func);
		return mean(attr, N, (double x) -> Math.pow(func.apply(x) - mean, 2));
	}
	
	/** Get the variance of TPItemInfo attribute over the most recent N history entries.
	 * If we have less entries than that, throw IllegalArgumentException.
	 */
	public double variance(TPItemInfo.Attribute attr, int N) {
		return variance(attr, N, (double x) -> x);
	}
	
	/** Gets the median of a function as applied to the given TP attribute over
	 *  N history elements.
	 *  If we have less entries than that, throw IllegalArgumentException.
	 */
	public double median(TPItemInfo.Attribute attr, int N, DoubleFunction func) {
		if (N > history.size() || N <= 0) {
			throw new IllegalArgumentException("Want median of " + N + " but history"
					+ " only contains " + history.size());
		}
		List<Double> vals = new ArrayList<Double>();
		for (int i = 0; i < N; i++) vals.add(func.apply(history.get(i).get(attr)));
		Collections.sort(vals);
		// Even-sized list means take average of center two values.
		if (vals.size() % 2 == 0) {
			return (vals.get(vals.size()/2) + vals.get(vals.size()/2 -1))/2;
		}
		// Odd-sized list means take the center value.
		else{
			return vals.get(vals.size()/2);
		}
	}
	
	/** Gets the median of a TP attribute over N history elements. 
	 *  If we have less entries than that, throw IllegalArgumentException.
	 */
	public double median(TPItemInfo.Attribute attr, int N) {
		return median(attr, N, (double x) -> x);
	}
	
	/** Computes the average change in an attribute over the most recent N history entries.
	 * If we have less entries than that, throw IllegalArgumentException. */
	public double meanSlope(TPItemInfo.Attribute attr, int N) {
		if (N > history.size() || N <= 0) {
			throw new IllegalArgumentException("Want avg slope of " + N + " but history"
					+ " only contains " + history.size());
		}
		if (N == 1) return 0;	// Special case: only one data point means no slope.
		double sum = 0;
		for (int i = 1; i < N; i++) {
			sum += history.get(i).get(attr) - history.get(i-1).get(attr);
		}
		return sum / (N - 1);
	}
}
