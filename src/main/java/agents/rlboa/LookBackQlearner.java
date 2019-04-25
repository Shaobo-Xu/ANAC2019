package agents.rlboa;

import genius.core.Bid;
import genius.core.BidHistory;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.events.MultipartyNegoActionEvent;

@SuppressWarnings("serial")
public class LookBackQlearner extends Qlearner {
	
	@Override
	public AbstractState getStateRepresentation(MultipartyNegoActionEvent negoEvent) {
		
		Bid oppLastBid = negotiationSession.getOpponentBidHistory().getLastBid();
		Bid myLastBid = negotiationSession.getOwnBidHistory().getLastBid();
		
		Bid myPreviousBid = this.getPreviousBid(negotiationSession.getOwnBidHistory());
		Bid oppPreviousBid = this.getPreviousBid(negotiationSession.getOpponentBidHistory());
		
		Bid agreement = negoEvent.getAgreement();
		Action currentAction = negoEvent.getAction();

		if (agreement != null || currentAction.getClass() == EndNegotiation.class) {
			return LookBackState.TERMINAL;
		}

		int myBin = this.getBinIndex(myLastBid);
		int myPrevBin = this.getBinIndex(myPreviousBid);
		int oppBin = this.getBinIndex(oppLastBid);
		int oppPrevBin = this.getBinIndex(oppPreviousBid);
		
		double time = negotiationSession.getTime();

		LookBackState state = new LookBackState(myBin, oppBin, myPrevBin, oppPrevBin, this.getTimeBinIndex(time));

		return state;
	}
	
	private Bid getPreviousBid(BidHistory history) {
		Bid bid = null;
		
		if (history.size() > 1) {
			return history.getHistory().get(history.size() - 1).getBid();
		}
		
		return bid;
	}
	
	@Override
	public String getName() {
		return "LookBackQlearner";
	}
}
