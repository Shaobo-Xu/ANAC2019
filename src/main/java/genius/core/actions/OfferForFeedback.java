package genius.core.actions;

import javax.xml.bind.annotation.XmlRootElement;

import genius.core.Agent;
import genius.core.AgentID;
import genius.core.Bid;

/**
 * immutable.
 * 
 * @author Dmytro Tykhonov
 */

@XmlRootElement
public class OfferForFeedback extends Offer {

	/** Creates a new instance of SendBid */
	public OfferForFeedback(AgentID agent, Bid bid) {
		super(agent, bid);
	}

	/** Creates a new instance of SendBid */
	public OfferForFeedback(Agent agent, Bid bid) {
		this(agent.getAgentID(), bid);
	}

	public String toString() {
		return "(Offer: " + (bid == null ? "null" : bid.toString()) + ")";
	}
}