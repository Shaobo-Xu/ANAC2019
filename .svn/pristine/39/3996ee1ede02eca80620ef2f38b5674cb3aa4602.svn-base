package agents.nastyagent;

import java.util.List;

import genius.core.actions.Action;
import genius.core.actions.Offer;

/**
 * Keeps repeating best bid as offer
 * 
 * @author W.Pasman
 *
 */
public class OnlyBestBid extends NastyAgent {
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		return new Offer(id, bids.get(0));
	}
}
