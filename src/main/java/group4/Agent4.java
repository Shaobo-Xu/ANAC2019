package group4;

import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;


@SuppressWarnings("serial")
public class Agent4 extends AbstractNegotiationParty {
	
	BidBag bidBag;

	// negotiation start time used to compensate a genius bug where the initial time is less than 1
	private double negotiationStartTime = -1; 
	
	// uncertainty
	private UtilitySpaceFactory utilitySpaceFactory;
	
	// opponent modeling
	private OpponentModel opponentModel;
	
	// nash approximation
	private Bid nashBid = null;
	private double nashUtility = 0;
		

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		
		try {
			bidBag = new BidBag(getUtilitySpace(), .6);
			opponentModel = new OpponentModel(getDomain());
		} catch(Exception e) {}
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		Bid lastReceivedBid = null;
				
		try {
			double time = getBugfreeTime();
			
			double lastReceivedBidUtility = 0;
			
			if (getLastReceivedAction() instanceof Offer) {
				lastReceivedBid = ((Offer) getLastReceivedAction()).getBid();
				lastReceivedBidUtility = getUtility(lastReceivedBid);
			}
			
			// panic: accept offer in the last moment
			 
			if (timeline.getTotalTime() - timeline.getCurrentTime() <= .3 && lastReceivedBid != null ) {
				// phase 4: panic accept				
				return new Accept(getPartyId(), lastReceivedBid);
			}
			
			// select strategy according to time
			double acceptableUtility;
			
			int phase = -1;
			if (time < .2) {
				phase = 1;
			} else if (time < .95) {
				phase = 2;
			} else {
				phase = 3;
			}
			
			if (phase != 1) {
				// calculate Nash
				opponentModel.calcOpponentModel();
				nashBid = bidBag.generateBidWithMaximumUtilityProduct(opponentModel.getUtilitySpace());

				double R = 1 + .25 * (1 - TimeDependentUtility.transformTime(time, .2, .95));
				nashUtility = utilitySpace.getUtility(nashBid) * R;
												
				// accept Nash bid
				if (lastReceivedBid != null && lastReceivedBidUtility == nashUtility) {
					return new Accept(getPartyId(), lastReceivedBid);
				}
			}
			
			
			if (phase == 1) {
				
				// phase 1: boulware
				double relativeTime = TimeDependentUtility.transformTime(time, 0, .2);
				acceptableUtility = TimeDependentUtility.calcUtilityPhase1(relativeTime, utilitySpace.getDomain().getNumberOfPossibleBids());
			
			} else if (phase == 2) {
				
				// phase 2: tit for tat
				double relativeTime = TimeDependentUtility.transformTime(time, .2, .95);
				acceptableUtility = TimeDependentUtility.calcAcceptableUtilityPhase2(relativeTime, utilitySpace.getDomain().getNumberOfPossibleBids(), nashUtility);
			
			} else { // phase 4 was already handled above
			
				// phase 3: boulware between nash and .5
				double relativeTime = TimeDependentUtility.transformTime(time, .95, 1);
				acceptableUtility = TimeDependentUtility.calcUtilityPhase3(relativeTime, nashUtility);
			}
						
			// evaluate received bid
			if (lastReceivedBid != null) {
				// accept everything with the acceptable utility or better
				if (lastReceivedBidUtility >= acceptableUtility) {
					return new Accept(getPartyId(), lastReceivedBid);
				}
			}
			
			// make an offer
			Bid offeredBid;
			
			if (phase == 1) {
				
				// time-dependent offer that doesn't use the opponent model (because it is not reliable yet)
				offeredBid = bidBag.getBid(acceptableUtility);
			
			} else if (phase == 2) {
				
				// tit for tat
				double opponentUtility = opponentModel.getUtilitySpace().getUtility(lastReceivedBid);
				double utilityDistance = Math.abs(nashUtility - opponentUtility);
				double titUtility = Math.min(1.0, nashUtility + utilityDistance);

				offeredBid = bidBag.generateBidWithUtility(titUtility, opponentModel.getUtilitySpace());			
			} else { // phase 4 was already handled above
				// phase 3: boulware from nash to .5
				
				// using the opponent model to generate bids this time
				// increasing nash utility initially to make sure we propose nash in the beginning
				// else it may be that we actually start at a bid worse than nash
				offeredBid = bidBag.generateBidWithUtility(acceptableUtility  + 0.1, opponentModel.getUtilitySpace());
			}
			
			return new Offer(getPartyId(), offeredBid);
			
		} catch(Exception e) {			
			// if we run into an exception: better make a deal than crash, or make a random offer
			if (lastReceivedBid != null) {
				Bid receivedBid = ((Offer) getLastReceivedAction()).getBid();
				return new Accept(getPartyId(), receivedBid);
			}
			
			return new Offer(getPartyId(), generateRandomBid());
		}
	}
		
	
	@Override
	public void receiveMessage(AgentID sender, Action action) {
		super.receiveMessage(sender, action);
		
		try {
			if (action instanceof Offer) {										
				opponentModel.processOpponentBid(((Offer) action).getBid(), getBugfreeTime());
			}
		} catch(Exception e) {}
	}


	@Override
	public AbstractUtilitySpace estimateUtilitySpace() {
		try {
			utilitySpaceFactory = new UtilitySpaceFactory(getDomain());
			BidRanking bidRanking = userModel.getBidRanking();
			
			utilitySpaceFactory.estimateUsingBidRanks(bidRanking);
			
			return utilitySpaceFactory.getUtilitySpace();
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	public String getDescription() {
		return "gerding4president";
	}
	
	private double getBugfreeTime() {
		// compensate a genius bug: it needs some initial time to boot up (I guess because it creates the uncertainty space), but the counter progresses
		// also some agents (like us) may want to calculate a whole lot initially, so we want to delay our concession strategy by that time
		
		if (negotiationStartTime == -1 && opponentModel.getLastReceivedBid() != null) {
			negotiationStartTime = timeline.getTime();	
		}
		
		if (negotiationStartTime == -1) {
			// the opponent did not place a bid yet
			return 0;
		} else {
			// now both parties are initialized
			// so we normalize the time so that for us it starts at 0
			return TimeDependentUtility.transformTime(timeline.getTime(), negotiationStartTime, 1 - negotiationStartTime);
		}
	}
}
