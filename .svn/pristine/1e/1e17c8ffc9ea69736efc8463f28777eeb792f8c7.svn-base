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
public class CounterOfferHumanNegotiationParty
		extends AbstractNegotiationParty {
	private Action opponentAction = null;
	private EnterBidDialog2 ui = null;
	private Bid myPreviousBid = null;
	private Bid mostRecentOffer;

	/**
	 * One agent will be kept alive over multiple sessions. Init will be called
	 * at the start of each negotiation session.
	 */
	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		System.out.println("init UIAgent");

		System.out.println("closing old dialog of ");
		if (ui != null) {
			ui.dispose();
			ui = null;
		}
	}

	@Override
	public void receiveMessage(AgentID sender, Action arguments) {
		this.opponentAction = arguments;

		if (opponentAction instanceof Offer) {
			mostRecentOffer = ((Offer) opponentAction).getBid();
		}

		if (opponentAction instanceof Accept && sender != this.getPartyId()) {
			JOptionPane.showMessageDialog(null,
					"" + sender + " accepted the last offer.");
		}

		if (opponentAction instanceof EndNegotiation) {
			JOptionPane.showMessageDialog(null,
					"" + sender + " canceled the negotiation session");
		}
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		if (ui != null) {
			ui.dispose();
			ui = null;
		}
		try {
			ui = new EnterBidDialog2(this, getPartyId(), null, true,
					(AdditiveUtilitySpace) utilitySpace,
					possibleActions.contains(Accept.class) ? mostRecentOffer
							: null);

		} catch (Exception e) {
			System.out.println("Problem in UIAgent2.init:" + e.getMessage());
			e.printStackTrace();
		}
		Action action = ui.askUserForAction(opponentAction, myPreviousBid,
				mostRecentOffer);
		System.out.println("action = " + action);
		if ((action != null) && (action instanceof Offer)) {
			myPreviousBid = ((Offer) action).getBid();
		}
		return action;
	}

	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "Simple Human user interface to place bids";
	}
}
