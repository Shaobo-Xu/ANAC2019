package negotiator.boaframework.omstrategy;

import java.util.List;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import agents.anac.y2012.TheNegotiatorReloaded.TheNegotiatorReloaded;
import genius.core.bidding.BidDetails;
import genius.core.boaframework.BOAparameter;
import genius.core.boaframework.NegotiationSession;
import genius.core.boaframework.OMStrategy;
import genius.core.boaframework.OpponentModel;

/**
 * Baseline strategy which simply returns a random bid from the given array of
 * bids. Basically, the opponent model is ignored.
 * 
 * @author Mark Hendrikx
 */
public class NullStrategy extends OMStrategy {

	/**
	 * when to stop updating the opponentmodel. Note that this value is not
	 * exactly one as a match sometimes lasts slightly longer.
	 */
	private double updateThreshold = 1.1;

	/**
	 * Empty constructor used for reflexion. Note this constructor assumes that
	 * init is called next.
	 */
	public NullStrategy() {
	}

	/**
	 * Special constructor for {@link TheNegotiatorReloaded}
	 * 
	 * @param negotiationSession
	 *            nego session
	 * @param time
	 *            update time
	 */
	public NullStrategy(NegotiationSession negotiationSession, double time) {
		this.negotiationSession = negotiationSession;
		updateThreshold = time;
	}

	/**
	 * Normal constructor used to initialize the NullStrategy opponent model
	 * strategy.
	 * 
	 * @param negotiationSession
	 *            symbolizing the negotiation state.
	 */
	public NullStrategy(NegotiationSession negotiationSession) {
		this.negotiationSession = negotiationSession;
	}

	public void init(NegotiationSession negotiationSession, OpponentModel model, HashMap<String, Double> parameters)
			throws Exception {
		super.init(negotiationSession, model, parameters);
		this.negotiationSession = negotiationSession;
		if (parameters.containsKey("t")) {
			updateThreshold = parameters.get("t");
		}
	}

	/**
	 * Returns a random bid from the give array of similarly preferred bids.
	 * 
	 * @param allBids
	 *            list of similarly preferred bids.
	 * @return random bid from given array.
	 */
	@Override
	public BidDetails getBid(List<BidDetails> allBids) {
		return allBids.get(0);
	}

	/**
	 * Returns true if the opponent model be updated, which is in this case if
	 * the time is lower than the given threshold.
	 * 
	 * @return true if model may be updated.
	 */
	@Override
	public boolean canUpdateOM() {
		return negotiationSession.getTime() < updateThreshold;
	}

	@Override
	public Set<BOAparameter> getParameterSpec() {
		Set<BOAparameter> set = new HashSet<BOAparameter>();
		set.add(new BOAparameter("t", 1.1, "Time after which the OM should not be updated"));
		return set;
	}

	@Override
	public String getName() {
		return "Random";
	}
}