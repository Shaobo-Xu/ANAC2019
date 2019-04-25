package agents;

import genius.core.AgentParam;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.tournament.VariablesAndValues.AgentParameterVariable;

public class BayesianAgentForAuctionMultiPhase extends BayesianAgentForAuction {
	protected Bid myProviderLastBid;
	protected double centerMaxOffer = 0;

	@Override
	protected Action proposeInitialBid() throws Exception {
		Bid lBid = null;
		switch (fRole) {
		case CENTER:
			switch (fPhase) {
			case FIRST_PHASE:
				CONCESSIONFACTOR = 0.07;
				lBid = getMaxUtilityBid();
				break;
			case SECOND_PHASE:
				CONCESSIONFACTOR = 0.0;
				if (fOpponentPreviousBid == null) {
					// double lSecondBest = getParameterValues().get(new
					// AgentParameterVariable(new
					// AgentParam(BayesianAgentForAuction.class.getName(),"starting_utility",0.,1.))).getValue();
					lBid = getMaxUtilityBid();
				} else
					return new Accept(getAgentID(), fOpponentPreviousBid);
			}
			break;
		case PROVIDER:
			switch (fPhase) {
			case FIRST_PHASE:
				lBid = getMaxUtilityBid();
				break;
			case SECOND_PHASE:
				// lBid = getMaxUtilityBid();
				double lSecondBest = getParameterValues().get(
						new AgentParameterVariable(new AgentParam(
								BayesianAgentForAuction.class.getName(),
								"starting_utility", 0., 1.))).getValue();
				lBid = getTradeOff(lSecondBest);
				myProviderLastBid = lBid;
				if (lBid == null)
					return new EndNegotiation(getAgentID());
				break;
			}
			break;
		}
		// Return (one of the) possible bid(s) with maximal utility.
		fSmartSteps = NUMBER_OF_SMART_STEPS;
		myLastBid = lBid;
		return new Offer(getAgentID(), lBid);
	}

	@Override
	public Action chooseAction() {
		// if((fOpponentPreviousBid!=null)&&(fRole == ROLE.CENTER)&&(fPhase ==
		// PHASE.SECOND_PHASE)) return new Accept(this);
		Action lAction = null;
		ACTIONTYPE lActionType;
		Bid lOppntBid = null;

		try {
			lActionType = getActionType(messageOpponent);
			switch (lActionType) {
			case OFFER: // Offer received from opponent
				lOppntBid = ((Offer) messageOpponent).getBid();
				// if (fOpponentModel.haveSeenBefore(lOppntBid)) {
				// lAction=myLastAction; break; }
				// double lDistance = calculateEuclideanDistanceUtilitySpace();
				// if(myLastAction==null) dumpDistancesToLog(0);
				System.out.print("Updating beliefs ...");
				if (myPreviousBids.size() < 8)
					fOpponentModel.updateBeliefs(lOppntBid);
				// dumpDistancesToLog(fRound++);
				System.out.println("Done!");
				if (fRole == ROLE.CENTER) {
					if (utilitySpace.getUtility(lOppntBid) > centerMaxOffer) {
						centerMaxOffer = utilitySpace.getUtility(lOppntBid);
						if (fPhase == PHASE.SECOND_PHASE)
							return new Accept(getAgentID(), lOppntBid);
					}
				}
				if (myLastAction == null)
					// Other agent started, lets propose my initial bid.
					lAction = proposeInitialBid();
				else {
					// log("time="+time+" offeredutil="+offeredutil+" accept probability P="+P);
					if (utilitySpace.getUtility(lOppntBid) * 1.05 >= utilitySpace
							.getUtility(myLastBid)
					/* || .05*P>Math.random() */) {
						// Opponent bids equally, or outbids my previous bid, so
						// lets accept
						lAction = new Accept(getAgentID(), lOppntBid);
						// log("randomly accepted");
					} else {
						Bid lnextBid = proposeNextBid(lOppntBid);
						if (lnextBid == null) {
							lAction = new EndNegotiation(getAgentID());
						} else {

							lAction = new Offer(getAgentID(), lnextBid);

							myProviderLastBid = lnextBid;
							// Propose counteroffer. Get next bid.
							// Check if utility of the new bid is lower than
							// utility of the opponent's last bid
							// if yes then accept last bid of the opponent.
							if (utilitySpace.getUtility(lOppntBid) * 1.05 >= utilitySpace
									.getUtility(lnextBid)) {
								// Opponent bids equally, or outbids my previous
								// bid, so lets accept
								lAction = new Accept(getAgentID(), lOppntBid);
								// log("opponent's bid higher than util of my last bid! accepted");
							}
						}

					}
					// remember current bid of the opponent as its previous bid
					fOpponentPreviousBid = lOppntBid;
				}
				break;
			case ACCEPT:
			case BREAKOFF:
				// nothing left to do. Negotiation ended, which should be
				// checked by
				// Negotiator...
				break;
			default:
				// I am starting, but not sure whether Negotiator checks this,
				// so
				// lets check also myLastAction...
				if (myLastAction == null) {
					// dumpDistancesToLog(fRound++);
					lAction = proposeInitialBid();
				} else
					// simply repeat last action
					lAction = myLastAction;
				break;
			}
		} catch (Exception e) {
			// log("Exception in chooseAction:"+e.getMessage());
			e.printStackTrace();
			lAction = new Offer(getAgentID(), myLastBid);
		}
		myLastAction = lAction;
		if (myLastAction instanceof Offer) {
			myPreviousBids.add(((Offer) myLastAction).getBid());
			myLastBid = ((Offer) myLastAction).getBid();
		}
		return lAction;

	}

	@Override
	protected Bid getNextBidAuction(Bid pOppntBid) throws Exception {
		if (pOppntBid == null)
			throw new NullPointerException("pOpptBid=null");
		if (myLastBid == null)
			throw new Exception("myLastBid==null");
		// log("Get next bid ...");
		Bid lBid = null;

		switch (fRole) {
		case CENTER:
			switch (fPhase) {
			case FIRST_PHASE:
				lBid = getNextBidSmart(pOppntBid);
				break;
			case SECOND_PHASE:
				// double lSecondBest = getParameterValues().get(new
				// AgentParameterVariable(new
				// AgentParam(BayesianAgentForAuction.class.getName(),"reservation",0.,1.))).getValue();
				// lBid = getTradeOff(lSecondBest);
				// return new Accept(this);
				lBid = getNextBidSmart(pOppntBid);
				break;
			}
			break;
		case PROVIDER:
			switch (fPhase) {
			case FIRST_PHASE:
				lBid = getNextBidSmart(pOppntBid);
				break;
			case SECOND_PHASE:
				lBid = getNextBidSmart(pOppntBid);
				break;
			}
			break;
		}
		return lBid;
	}

}
