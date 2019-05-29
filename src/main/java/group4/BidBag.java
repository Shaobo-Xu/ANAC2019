package group4;

import java.util.TreeMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.utility.UtilitySpace;

// holds bids with their utilities for a utility space
public class BidBag {
	private TreeMap<Double, HashSet<Bid>> bidCache;
	
	// creates a BidMap. If utility is less than minUtility, it won't be cached
	BidBag(UtilitySpace utilitySpace, double minUtility) {		
		bidCache = new TreeMap<>();
		
		BidIterator i = new BidIterator(utilitySpace.getDomain());

		while (i.hasNext()) {
			Bid bid = i.next();
			double utility = utilitySpace.getUtility(bid);
			
			if (utility >= minUtility) {
				HashSet<Bid> bidsWithUtility = bidCache.getOrDefault(utility, new HashSet<Bid>()); 
				bidsWithUtility.add(bid);
				bidCache.put(utility, bidsWithUtility);
			}
		}
	}
	
	// generates a bid that is closest to a given utility, from the perspective of our agent
	// will not take opponent model into account, so it is only used in the beginning
	Bid getBid(double targetUtility) {
		// try to find a bid that has exactly the given utility
		HashSet<Bid> matchedBidSet = bidCache.get(targetUtility);
		
		if (matchedBidSet == null) {
			// No exact bid set was found => take the next one with minimum distance
			// we will take all bid sets that are slightly greater and all bids that are slightly smaller than the target util
			// we evaluate which set is is closer and return any bid from that set 
			
			Entry<Double, HashSet<Bid>> bidsWithSmallerUtil = bidCache.floorEntry(targetUtility);
			Entry<Double, HashSet<Bid>> bidsWithBiggerUtil = bidCache.floorEntry(targetUtility);
			
			// evaluate which set is closer to the target utility (still in terms of our own utity)
			
			if (bidsWithSmallerUtil == null) {
				// there are only bids with utilities that are bigger, so evaluate those
				matchedBidSet = bidsWithBiggerUtil.getValue();
			} else if (bidsWithBiggerUtil == null) {
				// there are only bids with utilities that are smaller, so evaluate those
				matchedBidSet = bidsWithSmallerUtil.getValue();
			} else {
				// take the one that is closer to our target utility (still in terms of our own utility)
				double utilDiffForBiggerUtils = Math.abs(bidsWithBiggerUtil.getKey() - targetUtility);
				double utilDiffForSmallerUtils = Math.abs(bidsWithSmallerUtil.getKey() - targetUtility);
				
				matchedBidSet = (utilDiffForBiggerUtils < utilDiffForSmallerUtils ? bidsWithBiggerUtil : bidsWithSmallerUtil).getValue();
			}
		}
		
		// since we are not taking opponent utils into account: return a random bid from the set
		return getBid(matchedBidSet);
	}
	
	// returns a random bid from a set
	private Bid getBid(Set<Bid> set) {
		// we will return a random bid from the set to increase diversity
		
		int randomIndex = (int) Math.random() * set.size();
		
		int currentIndex = 0;
		for (Bid i : set) {
			if (currentIndex == randomIndex) {
				return i;
			}
			currentIndex ++;
		}
		
		// something screwed up		
		// just return the first index to go on
		return set.iterator().next();
	}
	
	// returns a bid from the set that maximizes the given utility space result
	// does not calc nash product, looks only on the opponentUtilitySpace
	private static Bid getBestBidForOpponent(Set<Entry<Double, HashSet<Bid>>> set, UtilitySpace utilitySpace) {
		Bid bestBid = null;
		double maxUtil = -1;
		
		
		for (Entry<Double, HashSet<Bid>> entry : set) {		
			for (Bid bid : entry.getValue()) {
				double utility = utilitySpace.getUtility(bid);
								
				if (utility > maxUtil || utility == maxUtil && Math.random() > .5) {
					maxUtil = utility;
					bestBid = bid;
				}
			}
		}
		
		return bestBid;
	}
	
	// returns if this cache (subset) has at least N bids
	private static boolean areNBidsInSubset(NavigableMap<Double, HashSet<Bid>> window, int numberOfBidsRequired) {		
		for (HashSet<Bid> i : window.values()) {
			numberOfBidsRequired -= i.size();
			if (numberOfBidsRequired <= 0) {
				return true;
			}
		}
		
		return false;
	}
	
	
	
	// generates a nash bid between this bag and another utility space
	Bid generateBidWithMaximumUtilityProduct(UtilitySpace otherUtilitySpace) {	
		double nashProduct = -1;
		Bid nashBid = null;
		
		// iterate through our own utilities sets
		for (Entry<Double, HashSet<Bid>> sets : bidCache.entrySet()) {
			
			// iterate over all possible bid with that utility
			for (Bid bid : sets.getValue()) {
				// calculate the opponent utility for that bid
				double otherUtility = otherUtilitySpace.getUtility(bid);
				double product = otherUtility * sets.getKey();
			
				if (product > nashProduct || product == nashProduct && Math.random() > .5) {
					nashProduct = product;
					nashBid = bid;
				}
			}
		}
		
		if (nashBid == null) {
			// something went horribly wrong!!!
			// this means that nash is less is incredibly small
			// this should never happen
			// so just return a Bid that is around .4 so we don't crash
						
			return getBid(.5);
		}
		
		return nashBid;
	}
	
	
	
	
	// generates a bid close to a given utility for this bag by opening a small window around that utility
	// within this window, it tries to maximize the utility for player 2
	// if nothing was found, the window will be made wider gradually
	Bid generateBidWithUtility(double targetUtility, UtilitySpace otherUtilitySpace) {
		
		double windowSize = 0.02; // we allow us to sacrifice this utility in order to find a good bid for the opponent. if it is too small we will increase it
		boolean atLeastOneBidFound = false;
		
		NavigableMap<Double, HashSet<Bid>> window = null;
		
		while (!atLeastOneBidFound && windowSize < .3) {
			window = bidCache.subMap(targetUtility - windowSize, true, targetUtility + windowSize, true);
			atLeastOneBidFound = areNBidsInSubset(window, 1); 
			
			if (!atLeastOneBidFound) {
				// did not find enough in this window, so we increase it
				windowSize += .005;
			}
		}
		
		Bid resultingBid;
		
		if (windowSize == .5) {
			resultingBid = getBid(targetUtility);
		} else {
			resultingBid = getBestBidForOpponent(window.entrySet(), otherUtilitySpace);
		}
		
		// within this window, use the best for the opponent
				
		return resultingBid;
		
	}
	
}
