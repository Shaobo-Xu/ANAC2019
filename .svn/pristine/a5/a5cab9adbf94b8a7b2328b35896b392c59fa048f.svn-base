package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import agents.similarity.Similarity;
import genius.core.Agent;
import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.DomainImpl;
import genius.core.SupportedNegotiationSetting;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;

public class SimilarityAgent extends Agent {

	private Action messageOpponent;
	private Bid myLastBid = null;
	private Action myLastAction = null;
	private Similarity fSimilarity;

	private enum ACTIONTYPE {
		START, OFFER, ACCEPT, BREAKOFF
	};

	private enum STRATEGY {
		SMART, SERIAL, RESPONSIVE, RANDOM
	};

	private STRATEGY fStrategy = STRATEGY.SMART;
	private int fSmartSteps;
	private static final double CONCESSIONFACTOR = 0.035;
	private static final double ALLOWED_UTILITY_DEVIATION = 0.01;
	private static final int NUMBER_OF_SMART_STEPS = 0;
	private HashMap<Bid, Double> utilityCash;

	// Class constructor
	public SimilarityAgent() {
		super();
	}

	@Override
	public void init() {
		messageOpponent = null;
		myLastBid = null;
		myLastAction = null;
		fSmartSteps = 0;
		// load similarity info from the utility space
		fSimilarity = new Similarity(utilitySpace.getDomain());
		// HACK we assume DomainImpl is used
		fSimilarity.loadFromXML(
				((DomainImpl) utilitySpace.getDomain()).getXMLRoot());
		// build utility cash
		utilityCash = new HashMap<Bid, Double>();
		BidIterator lIter = new BidIterator(utilitySpace.getDomain());
		try {
			while (lIter.hasNext()) {
				Bid tmpBid = lIter.next();
				utilityCash.put(tmpBid,
						new Double(utilitySpace.getUtility(tmpBid)));
			} // while
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Class methods
	@Override
	public void ReceiveMessage(Action opponentAction) {
		messageOpponent = opponentAction;
	}

	@Override
	public String getVersion() {
		return "1.0";
	};

	private Action proposeInitialBid() {
		Bid lBid = null;
		/*
		 * Value[] values = new Value[4]; if(myName.equals("Buyer")) { values[0]
		 * = new ValueReal(0.6); values[1] = new ValueReal(0.9); values[2] = new
		 * ValueReal(0.6); values[3] = new ValueReal(1); } else { values[0] =
		 * new ValueReal(0); values[1] = new ValueReal(0.2); values[2] = new
		 * ValueReal(0); values[3] = new ValueReal(0.5); } lBid = new
		 * Bid(utilitySpace.getDomain(), values);
		 */
		// Return (one of the) possible bid(s) with maximal utility.
		try {
			lBid = utilitySpace.getMaxUtilityBid();
			Bid lBid2 = getBidRandomWalk(utilitySpace.getUtility(lBid) * 0.98,
					utilitySpace.getUtility(lBid));
			if (lBid != null) {
				lBid = lBid2;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fSmartSteps = NUMBER_OF_SMART_STEPS + 1;
		myLastBid = lBid;

		return new Offer(getAgentID(), lBid);

	}

	private Bid getNextBidSmart(Bid pOppntBid) {
		double lMyUtility = 0, lOppntUtility = 0, lTargetUtility;
		// Both parties have made an initial bid. Compute associated utilities
		// from my point of view.
		try {
			lMyUtility = utilitySpace.getUtility(myLastBid);
			lOppntUtility = utilitySpace.getUtility(pOppntBid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (fSmartSteps >= NUMBER_OF_SMART_STEPS) {
			lTargetUtility = getTargetUtility(lMyUtility, lOppntUtility);
			fSmartSteps = 0;
		} else {
			lTargetUtility = lMyUtility;
			fSmartSteps++;
		}
		Bid lMyLastBid = myLastBid;
		Bid lBid = getTradeOffExhaustive(lTargetUtility, pOppntBid);
		if (Math.abs(fSimilarity.getSimilarity(lMyLastBid, lBid)) > 0.993) {
			lTargetUtility = getTargetUtility(lMyUtility, lOppntUtility);
			fSmartSteps = 0;
			lBid = getTradeOffExhaustive(lTargetUtility, pOppntBid);
		}
		return lBid;
	}

	private Bid getTradeOffExhaustive(double pUtility, Bid pOppntBid) {
		Bid lBid = null;
		double lSim = -1;
		// BidIterator lIter = new BidIterator(utilitySpace.getDomain());
		// while(lIter.hasNext()) {
		for (Entry<Bid, Double> entry : utilityCash.entrySet()) {
			Bid tmpBid = entry.getKey();
			double lUtil = entry.getValue();
			if (Math.abs(lUtil - pUtility) < ALLOWED_UTILITY_DEVIATION) {
				double lTmpSim = fSimilarity.getSimilarity(tmpBid, pOppntBid);
				if (lTmpSim > lSim) {
					lSim = lTmpSim;
					lBid = tmpBid;
				}
			}
		} // while
		return lBid;
	}

	private Action proposeNextBid(Bid pOppntBid) {
		Bid lBid = null;
		switch (fStrategy) {
		case SMART:
			lBid = getNextBidSmart(pOppntBid);
			break;
		}
		myLastBid = lBid;
		return new Offer(getAgentID(), lBid);
	}

	@Override
	public Action chooseAction() {
		Action lAction = null;
		ACTIONTYPE lActionType;
		Bid lOppntBid = null;

		lActionType = getActionType(messageOpponent);
		try {
			switch (lActionType) {
			case OFFER: // Offer received from opponent
				lOppntBid = ((Offer) messageOpponent).getBid();
				if (myLastAction == null)
					// Other agent started, lets propose my initial bid.
					lAction = proposeInitialBid();
				else if (utilitySpace.getUtility(lOppntBid) >= utilitySpace
						.getUtility(myLastBid))
					// Opponent bids equally, or outbids my previous bid, so
					// lets
					// accept
					lAction = new Accept(getAgentID(), lOppntBid);
				else
					// Propose counteroffer. Get next bid.
					lAction = proposeNextBid(lOppntBid);
				// Check if utility of the new bid is lower than utility of the
				// opponent's last bid
				// if yes then accept last bid of the opponent.
				if (utilitySpace.getUtility(lOppntBid) >= utilitySpace
						.getUtility(myLastBid))
					// Opponent bids equally, or outbids my previous bid, so
					// lets
					// accept
					lAction = new Accept(getAgentID(), lOppntBid);
				break;
			case ACCEPT: // Presumably, opponent accepted last bid, but let's
				// check...
				break;
			case BREAKOFF:
				// nothing left to do. Negotiation ended, which should be
				// checked by
				// Negotiator...
				break;
			default:
				// I am starting, but not sure whether Negotiator checks this,
				// so
				// lets check also myLastAction...
				if (myLastAction == null)
					lAction = proposeInitialBid();
				else
					// simply repeat last action
					lAction = myLastAction;
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		myLastAction = lAction;
		return lAction;
	}

	private ACTIONTYPE getActionType(Action lAction) {
		ACTIONTYPE lActionType = ACTIONTYPE.START;
		if (lAction instanceof Offer)
			lActionType = ACTIONTYPE.OFFER;
		else if (lAction instanceof Accept)
			lActionType = ACTIONTYPE.ACCEPT;
		else if (lAction instanceof EndNegotiation)
			lActionType = ACTIONTYPE.BREAKOFF;
		return lActionType;
	}

	private Bid getBidRandomWalk(double lowerBound, double upperBoud)
			throws Exception {
		// find all suitable bids
		ArrayList<Bid> lBidsRange = new ArrayList<Bid>();
		BidIterator lIter = new BidIterator(utilitySpace.getDomain());
		while (lIter.hasNext()) {
			Bid tmpBid = lIter.next();
			double lUtil = 0;
			try {
				lUtil = utilitySpace.getUtility(tmpBid);
				if (lUtil >= lowerBound && lUtil <= upperBoud)
					lBidsRange.add(tmpBid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} // while
			// Return bid that gets closest to target utility in a "random walk"
			// search.
		/*
		 * lBestBid = utilitySpace.getDomain().getRandomBid(); while(true) {
		 * lBid = utilitySpace.getDomain().getRandomBid(); if
		 * ((utilitySpace.getUtility(lBid) > lowerBound)&&
		 * (utilitySpace.getUtility(lBestBid) < upperBoud)) { lBestBid = lBid;
		 * break; }*
		 * 
		 * }
		 */
		if (lBidsRange.size() < 1) {
			return null;
		}
		if (lBidsRange.size() < 2) {
			return lBidsRange.get(0);
		} else {
			int lIndex = (new Random()).nextInt(lBidsRange.size() - 1);
			return lBidsRange.get(lIndex);
		}
	}

	private double getTargetUtility(double myUtility, double oppntUtility) {
		return myUtility - getConcessionFactor();
	}

	private double getConcessionFactor() {
		// The more the agent is willing to concess on its aspiration value, the
		// higher this factor.
		return CONCESSIONFACTOR;
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "Tries to make bids similar to received offers";
	}

}
