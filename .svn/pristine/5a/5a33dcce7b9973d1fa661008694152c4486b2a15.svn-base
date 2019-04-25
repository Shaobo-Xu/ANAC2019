package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import genius.core.Agent;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.issue.ValueReal;

/**
 * 
 * @author TB Always bids the highest for himself
 * 
 */
public class SimpleTFTAgent extends Agent {
	private static int round = 0;
	private Bid myLastBid = null;
	private Action opponentAction = null;
	private List<Bid> opponentPreviousBids;

	public void init() {
		opponentPreviousBids = new ArrayList<Bid>();
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	public void ReceiveMessage(Action opponentAction) {
		this.opponentAction = opponentAction;
	}

	public Action chooseAction() {
		Action myAction = null;

		if (round == 0)
			myAction = chooseOpeningAction();
		else if (round == 1)
			myAction = chooseOffer2();
		else if (round == 2)
			myAction = chooseOffer3();
		else if (round == 3)
			myAction = chooseOffer4();
		else if (opponentAction instanceof Offer) {
			myAction = chooseCounterOffer();
		}

		// We start
		// if (opponentAction == null)
		// myAction = chooseOpeningAction();
		// Opponent started, now it is our turn
		// else if (myLastBid == null)
		// myAction = chooseOffer2();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// Remember
		if (myAction instanceof Offer)
			myLastBid = ((Offer) myAction).getBid();
		if (opponentAction instanceof Offer)
			opponentPreviousBids.add(((Offer) opponentAction).getBid());

		System.out.println("Round " + round + ", " + getName() + " offers "
				+ myAction);
		round++;

		return myAction;
	}

	private Action chooseCounterOffer() {
		Bid opponentBid = ((Offer) opponentAction).getBid();
		double opponentOffer = toOffer(opponentBid);
		Bid opponentPreviousBid = opponentPreviousBids.get(opponentPreviousBids
				.size() - 1);
		double previousOpponentOffer = toOffer(opponentPreviousBid);

		double myPreviousOffer = toOffer(myLastBid);

		// double myOffer = (previousOpponentOffer / opponentOffer) *
		// myPreviousOffer;
		double myOffer = (previousOpponentOffer - opponentOffer)
				+ myPreviousOffer;

		if (getName().equals("Agent B"))
			myOffer = 0.3 - (5.0 / round) * 0.1;
		Domain domain = utilitySpace.getDomain();
		Issue pieForOne = domain.getIssues().get(0);
		HashMap<Integer, Value> myOfferedPackage = new HashMap<Integer, Value>();
		ValueReal value = new ValueReal(myOffer);
		myOfferedPackage.put(pieForOne.getNumber(), value);
		Bid firstBid = null;
		try {
			firstBid = new Bid(domain, myOfferedPackage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(getName() + " previously got "
				+ previousOpponentOffer + " and now gets offer "
				+ opponentOffer + " and counter-offers " + myOffer);

		// if (offeredutil > 0.5)
		// return new Accept(getAgentID());

		return new Offer(getAgentID(), firstBid);
	}

	private double toOffer(Bid bid) {
		Domain domain = utilitySpace.getDomain();
		Issue pieForOne = domain.getIssues().get(0);
		try {
			return ((ValueReal) bid.getValue(pieForOne.getNumber())).getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * pie voor mij -> pie voor agent A
	 */
	private ValueReal personalValue2IssueValue(double personalValue) {
		ValueReal value;
		if (getName().equals("Agent A"))
			value = new ValueReal(personalValue);
		else
			value = new ValueReal(1 - personalValue);
		return value;
	}

	private Action chooseOpeningAction() {
		Domain domain = utilitySpace.getDomain();
		Issue pie = domain.getIssues().get(0);
		HashMap<Integer, Value> myOfferedPackage = new HashMap<Integer, Value>();
		myOfferedPackage.put(pie.getNumber(), personalValue2IssueValue(0.9));
		Bid firstBid = null;
		try {
			firstBid = new Bid(domain, myOfferedPackage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Offer(getAgentID(), firstBid);
	}

	private Action chooseOffer2() {
		Domain domain = utilitySpace.getDomain();
		Issue pie = domain.getIssues().get(0);
		HashMap<Integer, Value> myOfferedPackage = new HashMap<Integer, Value>();
		myOfferedPackage.put(pie.getNumber(), personalValue2IssueValue(0.9));
		Bid firstBid = null;
		try {
			firstBid = new Bid(domain, myOfferedPackage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Offer(getAgentID(), firstBid);
	}

	private Action chooseOffer3() {
		Domain domain = utilitySpace.getDomain();
		Issue pie = domain.getIssues().get(0);
		HashMap<Integer, Value> myOfferedPackage = new HashMap<Integer, Value>();
		myOfferedPackage.put(pie.getNumber(), personalValue2IssueValue(0.8));
		Bid firstBid = null;
		try {
			firstBid = new Bid(domain, myOfferedPackage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Offer(getAgentID(), firstBid);
	}

	private Action chooseOffer4() {
		Domain domain = utilitySpace.getDomain();
		Issue pie = domain.getIssues().get(0);
		HashMap<Integer, Value> myOfferedPackage = new HashMap<Integer, Value>();
		myOfferedPackage.put(pie.getNumber(), personalValue2IssueValue(0.85));
		Bid firstBid = null;
		try {
			firstBid = new Bid(domain, myOfferedPackage);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new Offer(getAgentID(), firstBid);
	}
}
