package genius.core.uncertainty;

import java.util.List;

import java.util.ArrayList;
import java.util.Iterator;

import agents.org.apache.commons.lang.StringUtils;
import genius.core.Bid;

/**
 * Provides a (total) ranking of bids: b1 <= b2 <= ... <= bn
 */
public class BidRanking implements Iterable<Bid> {
	/** Ordered from low to high */
	private final List<Bid> bidOrder;
	private final double lowUtility;
	private final double highUtility;

	/**
	 * 
	 * @param bidOrder
	 *            bids, Ordered from low to high utility for me. Must not be
	 *            empty.
	 * @param lowUtil
	 *            A suggestion for the utility of the first (worst) bid in the
	 *            list. Must be in [0,1]
	 * @param highUtil
	 *            Suggested utility for me of the last (best) bid in the list.
	 *            Must be in [lowUtil,1]
	 */
	public BidRanking(List<Bid> bidOrder, double lowUtil, double highUtil) {
		if (bidOrder == null || bidOrder.isEmpty()) {
			throw new IllegalArgumentException(
					"bid order must contain at least one value.");
		}
		if (lowUtil < 0 || lowUtil > 1) {
			throw new IllegalArgumentException("low utility must be in [0,1]");
		}
		if (highUtil < lowUtil || lowUtil > 1) {
			throw new IllegalArgumentException(
					"low utility must be in [" + lowUtil + ",1]");
		}
		this.bidOrder = bidOrder;
		this.lowUtility = lowUtil;
		this.highUtility = highUtil;
	}

	public Bid getMinimalBid() {
		return bidOrder.get(0);
	}

	/**
	 * 
	 * @return The utility of the first (worst) bid in the list. 
	 * In [0,1]
	 */
	public Double getLowUtility() {
		return lowUtility;
	}

	/**
	 * 
	 * @return The utility of the last (best) bid in the list.
	 *         In [lowUtil,1].
	 */
	public Double getHighUtility() {
		return highUtility;
	}

	public Bid getMaximalBid() {
		return bidOrder.get(bidOrder.size() - 1);
	}

	public int indexOf(Bid b) {
		return bidOrder.indexOf(b);
	}

	public List<OutcomeComparison> getPairwiseComparisons() {
		ArrayList<OutcomeComparison> comparisons = new ArrayList<OutcomeComparison>();
		for (int i = 0; i < bidOrder.size() - 1; i++)
			comparisons.add(new OutcomeComparison(bidOrder.get(i),
					bidOrder.get(i + 1), -1));
		return comparisons;

	}
	

	public List<Bid> getBidOrder() 
	{
		return bidOrder;
	}

	/**
	 * The size equals 1 + the number of comparisons
	 * 
	 * @return
	 */
	public int getSize() {
		return bidOrder.size();
	}

	public int getAmountOfComparisons() {
		return getSize() - 1;
	}

	@Override
	public String toString() {
		return StringUtils.join(bidOrder.iterator(), " <= ");
	}

	/**
	 * Iterates the bids from low to high
	 */
	@Override
	public Iterator<Bid> iterator() {
		return bidOrder.iterator();
	}

}
