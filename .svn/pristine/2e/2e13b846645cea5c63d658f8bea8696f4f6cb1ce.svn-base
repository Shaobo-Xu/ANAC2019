package agents.anac.y2013.MetaAgent.portfolio.thenegotiatorreloaded;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Baseline strategy which simply returns a random bid from the given array of
 * bid.
 * 
 * @author Mark Hendrikx
 */
public class NullStrategy extends OMStrategy {

	private Random rand;
	private double updateThreshold = 1.0;

	public NullStrategy() {
	}

	public NullStrategy(NegotiationSession negotiationSession, double time) {
		rand = new Random();
		this.negotiationSession = negotiationSession;
		updateThreshold = time;
	}

	@Override
	public void init(NegotiationSession negotiationSession, OpponentModel model, HashMap<String, Double> parameters)
			throws Exception {
		rand = new Random();
		this.negotiationSession = negotiationSession;
		if (parameters.containsKey("t")) {
			updateThreshold = parameters.get("t");
		}
	}

	@Override
	public BidDetails getBid(List<BidDetails> allBids) {
		return allBids.get(rand.nextInt(allBids.size()));
	}

	@Override
	public boolean canUpdateOM() {
		return negotiationSession.getTime() < updateThreshold;
	}
}