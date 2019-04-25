package agents.nastyagent;

import java.util.ArrayList;
import java.util.HashMap;

import genius.core.Bid;

/**
 * mangles the map
 *
 */
public class StoreUnserializableThing extends NastyAgent {
	@Override
	public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
		// we actually got here. Report it.
		super.negotiationEnded(acceptedBid);
		ArrayList<Object> map = new ArrayList<Object>();
		map.add(new Object());
		data.put(map);
		return null;
	}

}
