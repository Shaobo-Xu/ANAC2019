package agents;

import java.util.ArrayList;
import java.util.List;

import genius.core.Bid;
import genius.core.BidIterator;
import genius.core.SupportedNegotiationSetting;

/**
 * This agent does not concede, but will accept anything equal to or above the
 * reservation value. For undiscounted domain only.
 */
public class UtilityBasedAcceptor extends TimeDependentAgent {

	private List<Bid> acceptableBids;

	@Override
	public double getE() {
		return 0;
	}

	@Override
	public String getName() {
		return "Utility Based Acceptor";
	}

	@Override
	public void init() {
		super.init();
		acceptableBids = new ArrayList<Bid>();

		BidIterator iter = new BidIterator(this.utilitySpace.getDomain());
		while (iter.hasNext()) {
			Bid bid = iter.next();
			try {

				if (getUtility(bid) >= utilitySpace.getReservationValue() && (Math.random() <= getUtility(bid)))
					this.acceptableBids.add(bid);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public boolean isAcceptable(Bid plannedBid) {
		Bid opponentLastBid = getOpponentLastBid();
		if (this.acceptableBids.contains(opponentLastBid))
			return true;
		else
			return false;
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "Utility Based Acceptor";
	}
}
