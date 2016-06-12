package core;

import java.util.List;

public class FeatureVector {
	
	/** This is the list of features that will be included in the vector. */
	public enum Feature {
		ItemID,					// Item's unique ID
		ItemType,				// Item's type, as reference to the enum in ItemType.java
		ItemRarity,				// Item's rarity level
		ItemLevel,				// Minimum level required to use item
		NumBuyOrders,			// Number of buy orders currently placed
		NumSellOrders,			// Number of sell orders currently placed
		BuyPrice,				// Current buy price
		SellPrice,				// Current sell price
		ZScoreBuyPrice,			// Z-score of item's buy price over period
		ZScoreSellPrice,		// Z-score of item's sell price over period
		MeanBuyPrice,			// Item's mean buy price over period
		MeanSellPrice,			// Item's mean sell price over period
		VarBuyPrice,			// Variance in buy price over period
		VarSellPrice,			// Variance in sell price over period
		MedianBuyPrice,			// Median of buy prices over period
		MedianSellPrice,		// Median of sell prices over period
		SlopeBuyPrice,			// Average rate of change in buy price over period
		SlopeSellPrice,			// Average rate of change in sell price over period
		CurrentFlipProfit,		// Current profit if flipped (as fraction of buy)
		MeanProfit,				// Mean profit we would make if buying now and selling at each history
		VarProfit,				// Variance in profit we would make if buying now and selling at each history
		MedianProfit,			// Median profit we would make if buying now and selling at eahc history
		OurBuyPrice,			// Price we would end up buying the item at
		NumConsidered			// How many moments in history did we end up considering?

	};
	
	/** Represents whether to buy/sell instantly or place a bid. */
	public enum Mode { 
		INSTANT, BID;
		
		@Override
		public String toString() {
			// Convert to standard caps to conform with edge API.
			switch (this) {
				case INSTANT: return "Instant";
				case BID: return "Bid";
				default: throw new IllegalArgumentException();
			}
		}
	};
	
	private double[] features;	// Stores all feature information.
	private String name;		// Store's the item's name
	
	/** Given a feature name, return the value of that feature
	 *  in this vector.
	 */
	public double get(Feature feature) {
		return features[feature.ordinal()];
	}
	
	/** Given a feature name, set the value of that feature. */
	private void set(Feature feature, double value) {
		features[feature.ordinal()] = value;
	}

	/** Create a feature vector for item with id, given database of item info (with history)
	 *  and current snapshot of TP. Will only look at the given
	 *  item info over the most recent <day> days. Note that these are the most recent
	 *  days that we have records for, not necessarily the absolute most recent days.
	 *  
	 *  For example, if you query a feature vector for the last 5 days and it is currently
	 *  July 10th, but we have data only through July 5th, you will get
	 *  a feature vector generated from July 1 - July 5.
	 *  
	 *	Modes determine whether you want to buy/sell instantly or place a bid.
	 *  
	 *  If no history is present, throws IllegalArgumentExcpetion.
	 */
	public FeatureVector(int id, ItemDB items, TPSnapshot snapshot, int days, Mode buyMode, Mode sellMode) {
		// Create the feature array
		features = new double[Feature.values().length];
		name = items.getItemInfo(id).get(ItemInfo.Attribute.Name);
		// Verify this item has a history on the TP
		ItemInfo item = items.getItemInfo(id);
		List<TPItemInfo> history = item.getHistory();
		if (history.size() == 0) {
			throw new IllegalArgumentException(item.get(ItemInfo.Attribute.Name) + 
					"has no history so cannot create feature vector.");
		}
		// Count how many items we want to include from this history
		DateTime newestData = history.get(0).time();
		DateTime delta = new DateTime("00-00-~"+ days + " 00:00:00");
		DateTime earliestConsider = newestData.add(delta);
		int numListings = 0;
		while (numListings < history.size() && history.get(numListings).time().compareTo(earliestConsider) >= 0) {
			numListings++;
		}
		set(Feature.NumConsidered, numListings);
		
		// Fill in item information
		int itemType = ItemType.fromString(item.get(ItemInfo.Attribute.Type)).ordinal();
		double itemRarity = Double.parseDouble(item.get(ItemInfo.Attribute.Rarity));
		double itemLevel = Double.parseDouble(item.get(ItemInfo.Attribute.Level));
		double numBuyOrders = snapshot.get(id).get(TPItemInfo.Attribute.NumBuy);
		double numSellOrders = snapshot.get(id).get(TPItemInfo.Attribute.NumSell);
		double buyPrice = snapshot.get(id).get(TPItemInfo.Attribute.BuyPrice);
		double sellPrice = snapshot.get(id).get(TPItemInfo.Attribute.SellPrice);

		set(Feature.ItemID, item.getId());
		set(Feature.ItemType, itemType);
		set(Feature.ItemRarity, itemRarity);
		set(Feature.ItemLevel, itemLevel);
		set(Feature.NumBuyOrders, numBuyOrders);
		set(Feature.NumSellOrders, numSellOrders);
		set(Feature.BuyPrice, buyPrice);
		set(Feature.SellPrice, sellPrice);
		
		// Fill in buy and sell statistical information
		double meanSellPrice = item.mean(TPItemInfo.Attribute.SellPrice, numListings);
		double meanBuyPrice = item.mean(TPItemInfo.Attribute.BuyPrice, numListings);
		double varSellPrice = item.variance(TPItemInfo.Attribute.SellPrice, numListings);
		double varBuyPrice = item.variance(TPItemInfo.Attribute.BuyPrice, numListings);
		double zBuyPrice = zScore(snapshot.get(id).get(TPItemInfo.Attribute.BuyPrice), 
				meanBuyPrice, varBuyPrice);
		double zSellPrice = zScore(snapshot.get(id).get(TPItemInfo.Attribute.SellPrice),
				meanSellPrice, varSellPrice);
		double medianBuyPrice = item.median(TPItemInfo.Attribute.BuyPrice, numListings);
		double medianSellPrice = item.median(TPItemInfo.Attribute.SellPrice, numListings);
		double slopeBuyPrice = item.meanSlope(TPItemInfo.Attribute.BuyPrice, numListings);
		double slopeSellPrice = item.meanSlope(TPItemInfo.Attribute.SellPrice, numListings);
		
		set(Feature.MeanSellPrice, meanSellPrice);
		set(Feature.MeanBuyPrice, meanBuyPrice);
		set(Feature.VarBuyPrice, varBuyPrice);
		set(Feature.VarSellPrice, varSellPrice);
		set(Feature.ZScoreBuyPrice, zBuyPrice);
		set(Feature.ZScoreSellPrice, zSellPrice);
		set(Feature.MedianBuyPrice, medianBuyPrice);
		set(Feature.MedianSellPrice, medianSellPrice);
		set(Feature.SlopeBuyPrice, slopeBuyPrice);
		set(Feature.SlopeSellPrice, slopeSellPrice);
		
		// Fill in relevant profits
		double ourBuyPrice = (buyMode == Mode.INSTANT) ? sellPrice : buyPrice;
		double flipProfit = profitFraction(buyPrice, sellPrice);
		// Come up with mean and variance in profit
		TPItemInfo.Attribute sellingPrice = (sellMode == Mode.INSTANT) ? 
				TPItemInfo.Attribute.BuyPrice : TPItemInfo.Attribute.SellPrice;
		DoubleFunction profitFunction = (double sell) -> profitFraction(ourBuyPrice, sell);
		double meanProfit = item.mean(sellingPrice, numListings, profitFunction);
		double varProfit = item.variance(sellingPrice, numListings, profitFunction);
		double medianProfit = item.median(sellingPrice, numListings, profitFunction);
		
		set(Feature.CurrentFlipProfit, flipProfit);
		set(Feature.OurBuyPrice, ourBuyPrice);
		set(Feature.MeanProfit, meanProfit);
		set(Feature.VarProfit, varProfit);	
		set(Feature.MedianProfit, medianProfit);
	}
	
	@Override
	public String toString() {
		String res = "*****************\n";
		res += "Features: " + name + "\n";
		for (Feature f : Feature.values()) {
			res += f.name() + " : " + features[f.ordinal()] + "\n";
		}
		return res;
	}
	
	/** What fraction of the buy price do we make in profit,
	 *  assuming we buy at buy and sell at sell?
	 *  If item is "free" (buy = 0), return 0.
	 */
	private double profitFraction(double buy, double sell) {
		if (buy == 0) return 0;
		return (sell * TPItemInfo.TAX_FACTOR - buy) / buy;
	}
	
	/** Compute a z-score given a value, mean, and variance.
	 *  Returns 0 if variance is 0. */
	private double zScore(double val, double mean, double var) {
		if (var == 0) return 0;
		double stddev = Math.sqrt(var);
		return (val - mean) / stddev;
	}

}
