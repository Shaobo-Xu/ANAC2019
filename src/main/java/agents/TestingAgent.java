package agents;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import genius.core.Agent;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.ActionWithBid;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.IssueInteger;
import genius.core.issue.IssueReal;
import genius.core.issue.Value;
import genius.core.issue.ValueInteger;
import genius.core.issue.ValueReal;

/**
 * @author W.Pasman Some improvements over the standard SimpleAgent.
 */
public class TestingAgent extends Agent {
	private Action actionOfPartner = null;
	private static double MINIMUM_BID_UTILITY = 0;

	/**
	 * init is called when a next session starts with the same opponent.
	 */
	public void init() {
		Double reservationValue = utilitySpace.getReservationValue();
		System.out.println(getName());
		System.out.println();
		System.out.println("Discount: " + utilitySpace.getDiscountFactor());
		System.out.println("RV: " + reservationValue);
		Domain domain = utilitySpace.getDomain();
		System.out.println("NumberOfPossibleBids: "
				+ domain.getNumberOfPossibleBids());
		Bid randomBid = domain.getRandomBid(null);
		try {
			System.out.println("Utitility of bid " + randomBid + " = "
					+ utilitySpace.getUtility(randomBid));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		double util = 0.24858884644383605;
		double time = 0.21064712109444447;
		double discount = 0.5;
		double discountedUtil = util * Math.pow(discount, time);
		System.out.println(discountedUtil);
	}

	@Override
	public String getName() {
		return "Testing Agent";
	}

	@Override
	public String getVersion() {
		return "1.3";
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
				// get current time
				double time = timeline.getTime();
				System.out.println("t = " + Math.round(100 * time) / 100.0
						+ ", discountedRV = "
						+ utilitySpace.getReservationValueWithDiscount(time));

				if (time > 0.1) {
					System.out.println("End, because t = " + time);
					action = new EndNegotiation(getAgentID());
				} else
					action = chooseRandomBidAction();
			}
		} catch (Exception e) {
			System.out.println("Exception in ChooseAction:" + e.getMessage());
			// best guess if things go wrong
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

		// createFrom a random bid with utility>MINIMUM_BID_UTIL.
		// note that this may never succeed if you set MINIMUM too high!!!
		// in that case we will search for a bid till the time is up (2 minutes)
		// but this is just a simple agent.
		Bid bid = null;
		double utility;
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
				case INTEGER:
					IssueInteger lIssueInteger = (IssueInteger) lIssue;
					int optionIndex2 = lIssueInteger.getLowerBound()
							+ randomnr.nextInt(lIssueInteger.getUpperBound()
									- lIssueInteger.getLowerBound());
					values.put(lIssueInteger.getNumber(), new ValueInteger(
							optionIndex2));
					break;
				default:
					throw new Exception("issue type " + lIssue.getType()
							+ " not supported by SimpleAgent2");
				}
			}
			bid = new Bid(utilitySpace.getDomain(), values);
			utility = getUtility(bid);
		} while (utility < MINIMUM_BID_UTILITY);

		// System.out.println(this.getName() + " sent " + bid);
		return bid;
	}
}
