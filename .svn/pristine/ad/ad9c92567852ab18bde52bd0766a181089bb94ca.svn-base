package agents;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import genius.core.Agent;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.ActionWithBid;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.IssueReal;
import genius.core.issue.Value;
import genius.core.issue.ValueReal;

/**
 * 
 * @author W.Pasman 6nov08 Some improvements over the standard SimpleAgent. We
 *         only consider random bids with utility higher than the maximum and we
 *         search at most MAX_SEARCH times in the random space for a better bid.
 *         of the opponent utilities we saw so far.
 */
public class RandomIncreasingUtilAgent extends Agent {
	private Action actionOfPartner = null;
	private static final double MINIMUM_BID_UTILITY = 0.5; // obviously we will
															// fail if there are
															// no bids with this
															// utility!
	private static final int MAX_SEARCH = 4000;

	/** the maximum opponent utility we saw so far */
	private double max_opponent_utility = 0;
	Bid max_opponent_bid = null;

	/**
	 * init is called when a next session starts with the same opponent.
	 */

	public void init() {

	}

	@Override
	public String getVersion() {
		return "1.2";
	}

	public void ReceiveMessage(Action opponentAction) {
		actionOfPartner = opponentAction;
	}

	public Action chooseAction() {
		Action action = null;
		try {
			if (actionOfPartner == null)
				action = chooseRandomBidAction();
			if (actionOfPartner instanceof Offer) {
				Bid partnerBid = ((Offer) actionOfPartner).getBid();
				double offeredutil = utilitySpace.getUtility(partnerBid);
				if (offeredutil > max_opponent_utility) {
					max_opponent_utility = offeredutil;
					max_opponent_bid = partnerBid;
				}
				double time = timeline.getTime();
				double P = Paccept(offeredutil, time);
				if (P > Math.random())
					action = new Accept(getAgentID(), partnerBid);
				else
					action = chooseRandomBidAction();
			}
			Thread.sleep(1000); // just for fun
		} catch (Exception e) {
			System.out.println("Exception in ChooseAction:" + e.getMessage());
			// best guess if things go wrong.
			action = new Accept(getAgentID(),
					((ActionWithBid) actionOfPartner).getBid());
		}
		return action;
	}

	/**
	 * Wrapper for getRandomBid, for convenience.
	 * 
	 * @return new Action(Bid(..)), with bid utility > MINIMUM_BID_UTIL. If a
	 *         problem occurs, it returns an Accept() action.
	 */
	private Action chooseRandomBidAction() {
		Bid nextBid = null;
		try {
			nextBid = getRandomBid();
		} catch (Exception e) {
			System.out.println("Problem with received bid:" + e.getMessage()
					+ ". cancelling bidding");
		}
		if (nextBid == null)
			return (new Accept(getAgentID(),
					((ActionWithBid) actionOfPartner).getBid()));
		return (new Offer(getAgentID(), nextBid));
	}

	/**
	 * @return a random bid with high enough utility value.
	 * @throws Exception
	 *             if we can't compute the utility (eg no evaluators have been
	 *             set) or when other evaluators than a DiscreteEvaluator are
	 *             present in the util space.
	 */
	private Bid getRandomBid() throws Exception {
		HashMap<Integer, Value> values = new HashMap<Integer, Value>(); // pairs
																		// <issuenumber,chosen
																		// value
																		// string>
		List<Issue> issues = utilitySpace.getDomain().getIssues();
		Random randomnr = new Random();

		// createFrom a random bid with utility>MINIMUM_BID_UTIL AND
		// utility>max_opponent_util
		// note that this may never succeed if you set MINIMUM too high!!!
		// in that case we will search for a bid till the time is up (2 minutes)
		// but this is just a simple agent.
		Bid bid = null;
		double bidutility = 0;
		int nr_tries = 0;
		do {

			for (Issue lIssue : issues) {
				switch (lIssue.getType()) {
				case DISCRETE:
					IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
					int optionIndex = randomnr.nextInt(lIssueDiscrete
							.getNumberOfValues());
					values.put(lIssue.getNumber(),
							lIssueDiscrete.getValue(optionIndex));
					break;
				case REAL:
					IssueReal lIssueReal = (IssueReal) lIssue;
					int optionInd = randomnr.nextInt(lIssueReal
							.getNumberOfDiscretizationSteps() - 1);
					values.put(
							lIssueReal.getNumber(),
							new ValueReal(lIssueReal.getLowerBound()
									+ (lIssueReal.getUpperBound() - lIssueReal
											.getLowerBound())
									* (double) (optionInd)
									/ (double) (lIssueReal
											.getNumberOfDiscretizationSteps())));
					break;
				default:
					throw new Exception("issue type " + lIssue.getType()
							+ " not supported by SimpleAgent2");
				}
			}
			bid = new Bid(utilitySpace.getDomain(), values);
			bidutility = utilitySpace.getUtility(bid);
			nr_tries++;
		} while ((bidutility < MINIMUM_BID_UTILITY || bidutility < max_opponent_utility)
				&& nr_tries < MAX_SEARCH);

		if (bidutility < MINIMUM_BID_UTILITY
				|| bidutility < max_opponent_utility) // reached search limit?
		{
			// could not find a better bid than we received, let's offer that
			// best bid so far...
			bid = max_opponent_bid;
		}

		return bid;
	}

	/**
	 * This function determines the accept probability for an offer. At t=0 it
	 * will prefer high-utility offers. As t gets closer to 1, it will accept
	 * lower utility offers with increasing probability. it will never accept
	 * offers with utility 0.
	 * 
	 * @param u
	 *            is the utility
	 * @param t
	 *            is the time as fraction of the total available time (t=0 at
	 *            start, and t=1 at end time)
	 * @return the probability of an accept at time t
	 * @throws Exception
	 *             if you use wrong values for u or t.
	 * 
	 */
	double Paccept(double u, double t1) throws Exception {
		double t = t1 * t1 * t1; // steeper increase when deadline approaches.
		if (u < 0 || u > 1.05)
			throw new Exception("utility " + u + " outside [0,1]");
		// normalization may be slightly off, therefore we have a broad boundary
		// up to 1.05
		if (t < 0 || t > 1)
			throw new Exception("time " + t + " outside [0,1]");
		if (u > 1.)
			u = 1;
		if (t == 0.5)
			return u;
		return (u - 2. * u * t + 2. * (-1. + t + Math.sqrt(sq(-1. + t) + u
				* (-1. + 2 * t))))
				/ (-1. + 2 * t);
	}

	double sq(double x) {
		return x * x;
	}

}
