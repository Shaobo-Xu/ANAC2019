package negotiator.parties;

import java.util.List;

import javax.swing.JOptionPane;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.SupportedNegotiationSetting;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.utility.AdditiveUtilitySpace;

/**
 * @author W.Pasman, modified version of Dmytro's UIAgent
 */
public class UINegotiationParty extends AbstractNegotiationParty {

	private Action opponentAction = null;

	private Bid myPreviousBid = null;
	private Bid mostRecentBid;

	/**
	 * One agent will be kept alive over multiple sessions. Init will be called
	 * at the start of each nego session.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);

	}

	@Override
	public void receiveMessage(AgentID sender, Action arguments) {
		this.opponentAction = arguments;
		if (opponentAction instanceof Offer) {
			mostRecentBid = ((Offer) opponentAction).getBid();
		}

		if (opponentAction instanceof Accept) {
			JOptionPane.showMessageDialog(null, "Opponent accepted your last offer.");
		}

		if (opponentAction instanceof EndNegotiation) {
			JOptionPane.showMessageDialog(null, "Opponent canceled the negotiation session");
		}
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		Action action = null;
		try {
			EnterBidDialog2 dialog = new EnterBidDialog2(this, getPartyId(), null, true,
					(AdditiveUtilitySpace) utilitySpace, possibleActions.contains(Accept.class) ? mostRecentBid : null);

			action = dialog.askUserForAction(opponentAction, myPreviousBid, mostRecentBid);
			if ((action != null) && (action instanceof Offer)) {
				myPreviousBid = ((Offer) action).getBid();
			}
		} catch (Exception e) {
			System.out.println("Problem in UIAgent2.chooseAction:" + e.getMessage());
			e.printStackTrace();
		}

		return action;
	}

	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "UI Party";
	}
}
