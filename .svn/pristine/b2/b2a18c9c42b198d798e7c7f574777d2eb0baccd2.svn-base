package agents;

import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.analysis.BidPoint;
import genius.core.analysis.ParetoFrontier;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;

/**
 * Like {@link ParetoFrontier}, but with extra functionality. This version loads
 * the frontier by itself using the given utility spaces.
 * 
 * <h1>notes</h1>We may decide to move these functions into
 * {@link ParetoFrontier}.
 * 
 * When a utility space changes, you currently just need to make a new
 * {@link ParetoFrontierPlus} from scratch using the constructor. This is
 * because we can not listen to changes in {@link AdditiveUtilitySpace}s
 * 
 * @author W.Pasman 3sep14
 *
 */
public class ParetoFrontierPlus {
	UtilitySpace mySpace, otherSpace;
	private ParetoFrontier pareto = null;

	/**
	 * Get a {@link ParetoFrontier} for this outcome space.
	 * 
	 * @throws Exception
	 */
	public ParetoFrontierPlus(UtilitySpace spaceMe, UtilitySpace spaceOther) {
		mySpace = spaceMe;
		otherSpace = spaceOther;
		try {
			computeParetoFrontier();
		} catch (Exception e) {
			throw new IllegalStateException(
					"Failed to compute Pareto Frontier", e);
		}
	}

	/**
	 * compute the pareto frontier. Warning, this may take some time and may
	 * even run out of memory if the space is really large.
	 * 
	 * @throws Exception
	 *             if getUtility fails.
	 * */
	private void computeParetoFrontier() throws Exception {
		Bid bid;

		BidIterator bids = new BidIterator(mySpace.getDomain());
		pareto = new ParetoFrontier();
		while (bids.hasNext()) {
			bid = bids.next();
			BidPoint bp = new BidPoint(bid, mySpace.getUtility(bid),
					otherSpace.getUtility(bid));
			pareto.mergeIntoFrontier(bp);
		}

		pareto.sort();
	}

	/**
	 * Project a utility onto a pareto frontier point
	 * 
	 * @param otherUtil
	 *            the utility that the projected point should be close to.
	 * @return a {@link BidPoint} on the pareto that has minimal distance to the
	 *         given otherUtil.
	 */
	public BidPoint getBidNearOpponentUtility(double otherUtil) {
		BidPoint nearest = null;
		double dist = 10; // larger than any real distance in utilspace.
		for (BidPoint bidpoint : pareto.getFrontier()) {
			double newdist = Math.abs(bidpoint.getUtilityB() - otherUtil);
			if (newdist < dist) {
				nearest = bidpoint;
				dist = newdist;
			}
		}
		return nearest;
	}

	/**
	 * get the bid on the pareto that has myUtility nearest to given target
	 * utility
	 * 
	 * @param bid
	 * @return
	 */
	public BidPoint getBidNearMyUtility(double utility) {
		BidPoint nearest = null;
		double dist = 10; // larger than any real distance in utilspace.
		for (BidPoint bidpoint : pareto.getFrontier()) {
			double newdist = Math.abs(bidpoint.getUtilityA() - utility);
			if (newdist < dist) {
				nearest = bidpoint;
				dist = newdist;
			}
		}
		return nearest;

	}

	/**
	 * get the bid on the pareto that has at least the given utility for me (as
	 * close as possible to the target)
	 * 
	 * @param utility
	 *            target utility for me.
	 * @return bid that is nearest bid above or at target utility for me. May
	 *         return null if no such bid.
	 */

	public BidPoint getBidWithMinimumUtility(double utility) {
		BidPoint nearest = null;
		double dist = 10; // larger than any real distance in utilspace.
		double newdist;
		for (BidPoint bidpoint : pareto.getFrontier()) {
			newdist = bidpoint.getUtilityA() - utility;
			if (newdist > 0 && newdist < dist) {
				nearest = bidpoint;
				dist = newdist;
			}
		}
		return nearest;
	}
}
