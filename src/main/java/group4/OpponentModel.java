package group4;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.utility.AdditiveUtilitySpace;

public class OpponentModel {
	public UtilitySpaceFactory opponentUtilitySpaceFactory;
	private Set<Bid> uniqueBids = new HashSet<>();
	private HashMap<Double, Bid> history = new HashMap<>();
		
	private int numberOfBids = 0;
	private Bid lastReceivedBid;
	
	OpponentModel(Domain domain) {
		this.opponentUtilitySpaceFactory = new UtilitySpaceFactory(domain);
	}
	
	AdditiveUtilitySpace getUtilitySpace() {
		return this.opponentUtilitySpaceFactory.getUtilitySpace();
	}
	
	void processOpponentBid(Bid bid, double time) {
		numberOfBids++;
		lastReceivedBid = bid;
		
		if (!uniqueBids.contains(bid)) {
			uniqueBids.add(bid);
			history.put(time, bid);
		}
	}
	
	
	// note: assumes additive utility space and discrete values
	void calcOpponentModel() {
		opponentUtilitySpaceFactory.estimateUsingHistory(history);
		// logger.log(opponentUtilitySpaceFactory.getUtilitySpace().toString());
	}
	
	public int getNumberOfBids() {
		return numberOfBids;
	}

	Bid getLastReceivedBid() {
		return lastReceivedBid;
	}

	public HashMap<Double, Bid> getHistory() {
		// TODO Auto-generated method stub
		return null;
	}
}