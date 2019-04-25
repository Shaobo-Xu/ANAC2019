package agents.nastyagent;

import java.util.List;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import genius.core.Agent;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.parties.NegotiationInfo;
import genius.core.parties.NegotiationParty;
import genius.core.persistent.PersistentDataContainer;
import genius.core.protocol.DefaultMultilateralProtocol;
import genius.core.protocol.StackedAlternatingOffersProtocol;
import genius.core.utility.AbstractUtilitySpace;

/**
 * NastyAgent is an agent with nasty behaviour: throws, returns silly actions,
 * goes to sleep for long times. This is for testing if Genius is robust for
 * such cases. If not nasty, this agent just places bids ordered by decreasing
 * utility.
 * <p>
 * This is an abstract class without any actual nastyness. This is because
 * {@link NegotiationParty} does not support parameterization anymore (unlike
 * {@link Agent}. The actual (non-abstract) implementations do the nastyness.
 * 
 * @author W.Pasman.
 * 
 */
public abstract class NastyAgent implements NegotiationParty {

	ArrayList<Bid> bids = new ArrayList<Bid>(); // the bids that we MAY place.
	Iterator<Bid> bidIterator; // next bid that we can place. Iterator over
	// bids.
	protected AgentID id;
	protected Action lastReceivedAction;
	protected boolean ended = false; // used to check if the nego was ended
										// properly
	protected PersistentDataContainer data;
	protected AbstractUtilitySpace utilitySpace;

	@Override
	public void init(NegotiationInfo info) 
	{
		this.id = info.getAgentID();
		this.data = info.getPersistentData();
		this.utilitySpace = info.getUtilitySpace();

		BidIterator biter = new BidIterator(utilitySpace.getDomain());
		while (biter.hasNext())
			bids.add(biter.next());
		Collections.sort(bids, new BidComparator(utilitySpace));
		bidIterator = bids.iterator();

	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		if (bidIterator.hasNext()) {
			return new Offer(id, bidIterator.next());
		}
		return null;
	}

	@Override
	public void receiveMessage(AgentID sender, Action action) {
		this.lastReceivedAction = action;
	}

	@Override
	public String getDescription() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return StackedAlternatingOffersProtocol.class;
	}

	@Override
	public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
		ended = true;
		return null;
	}

	public boolean isEnded() {
		return ended;
	}

}
