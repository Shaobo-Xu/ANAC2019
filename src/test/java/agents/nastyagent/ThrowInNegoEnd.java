package agents.nastyagent;

import java.util.HashMap;

import genius.core.Bid;

/**
 * throws when negotiationEnded is called.
 *
 */
public class ThrowInNegoEnd extends NastyAgent {
	@Override
	public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
		// we actually got here. Report it.
		super.negotiationEnded(acceptedBid);
		throw new RuntimeException("end with throw !");
	}
}
