package agents;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import genius.core.Agent;
import genius.core.Bid;
import genius.core.SupportedNegotiationSetting;
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
 * @author W.Pasman Some improvements over the standard SimpleAgent.
 * 
 */
public class SimpleAgt2 extends Agent {
	private Action actionOfPartner = null;
	private static final double MINIMUM_BID_UTILITY = 0.5;

	@Override
	public void ReceiveMessage(Action opponentAction) {
		actionOfPartner = opponentAction;
	}

	@Override
	public String getVersion() {
		return "3.1";
	}

	@Override
	public String getName() {
		return "Simple Agent v2";
	}

	@Override
	public Action chooseAction() {
		Action action = null;
		Bid partnerBid = null;
		try {
			if (actionOfPartner == null)
				action = chooseRandomBidAction(null); // this may throw NPE!!
			if (actionOfPartner instanceof Offer) {
				partnerBid = ((Offer) actionOfPartner).getBid();
				double offeredutil = utilitySpace.getUtility(partnerBid);
				double time = ((new Date()).getTime() - startTime.getTime())
						/ (1000. * totalTime);
				double P = Paccept(offeredutil, time);
				if (P > Math.random())
					action = new Accept(this.getAgentID(), partnerBid);
				else
					action = chooseRandomBidAction(
							((ActionWithBid) actionOfPartner).getBid());
			}
			sleep(0.005); // just for fun
		} catch (Exception e) {
			System.out.println("Exception in ChooseAction:" + e.getMessage());
			// best guess if things go
			action = new Accept(this.getAgentID(), partnerBid);
			// wrong.
		}
		return action;
	}

	/**
	 * Wrapper for getRandomBid, for convenience.
	 * 
	 * @param lastOppBid
	 *            the last opponent bid received.
	 * @return new Action(Bid(..)), with bid utility > MINIMUM_BID_UTIL. If a
	 *         problem occurs, it returns an Accept() action.
	 * 
	 */
	private Action chooseRandomBidAction(Bid lastOppBid) {
		Bid nextBid = null;
		try {
			nextBid = getRandomBid();
		} catch (Exception e) {
			System.out.println("Problem with received bid:" + e.getMessage()
					+ ". cancelling bidding");
		}
		if (nextBid == null)
			return (new Accept(this.getAgentID(), lastOppBid));
		return (new Offer(this.getAgentID(), nextBid));
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

		// createFrom a random bid with utility>MINIMUM_BID_UTIL.
		// note that this may never succeed if you set MINIMUM too high!!!
		// in that case we will search for a bid till the time is up (2 minutes)
		// but this is just a simple agent.
		Bid bid = null;
		do {
			for (Issue lIssue : issues) {
				switch (lIssue.getType()) {
				case DISCRETE:
					IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
					int optionIndex = randomnr
							.nextInt(lIssueDiscrete.getNumberOfValues());
					values.put(lIssue.getNumber(),
							lIssueDiscrete.getValue(optionIndex));
					break;
				case REAL:
					IssueReal lIssueReal = (IssueReal) lIssue;
					int lNrOfOptions = lIssueReal
							.getNumberOfDiscretizationSteps();
					double lOneStep = (lIssueReal.getUpperBound()
							- lIssueReal.getLowerBound()) / lNrOfOptions;
					int lOptionIndex = Double
							.valueOf(java.lang.Math.random() * (lNrOfOptions))
							.intValue();
					if (lOptionIndex >= lNrOfOptions)
						lOptionIndex = lNrOfOptions - 1;
					values.put(lIssueReal.getNumber(),
							new ValueReal(lIssueReal.getLowerBound()
									+ lOneStep * lOptionIndex));
					break;

				default:
					throw new Exception("issue type " + lIssue.getType()
							+ " not supported by SimpleAgent2");
				}
			}
			bid = new Bid(utilitySpace.getDomain(), values);
		} while (utilitySpace.getUtility(bid) < MINIMUM_BID_UTILITY);

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
		return (u - 2. * u * t
				+ 2. * (-1. + t + Math.sqrt(sq(-1. + t) + u * (-1. + 2 * t))))
				/ (-1. + 2 * t);
	}

	double sq(double x) {
		return x * x;
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "Random bids until good offer is received";
	}

}
