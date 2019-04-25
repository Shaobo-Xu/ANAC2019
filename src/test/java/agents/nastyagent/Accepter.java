package agents.nastyagent;

import java.util.List;

import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.ActionWithBid;
import genius.core.actions.Offer;

/**
 * Accepts blindly if other side made an offer. Offers first available bid
 * otherwise. throw.
 * 
 * @author W.Pasman
 *
 */
public class Accepter extends NastyAgent {
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		if (!(lastReceivedAction instanceof Offer)) {
			return new Offer(id, bids.get(0));
		}
		return new Accept(id, ((ActionWithBid) lastReceivedAction).getBid());
	}
}
