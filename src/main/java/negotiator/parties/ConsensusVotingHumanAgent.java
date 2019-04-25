package negotiator.parties;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JOptionPane;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.SupportedNegotiationSetting;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.actions.OfferForVoting;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.protocol.AlternatingMultipleOffersProtocol;
import genius.core.protocol.DefaultMultilateralProtocol;
import genius.core.utility.AdditiveUtilitySpace;

/**
 * @author W.Pasman, modified version of Dmytro's UIAgent
 */
public class ConsensusVotingHumanAgent extends AbstractNegotiationParty {
	private Action opponentAction = null;
	private EnterBidDialogInterface ui = null;
	private Bid myPreviousBid = null;
	private Queue<Bid> offers = new LinkedList<Bid>();

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
		System.out.println("old  dialog closed. Trying to open new dialog. ");
		try {
			ui = new EnterBidDialogOfferForVoting(this, null, true, (AdditiveUtilitySpace) utilitySpace);
		} catch (Exception e) {
			System.out.println("Problem in UIAgent2.init:" + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("finished init of UIAgent2");
	}

	@Override
	public void receiveMessage(AgentID sender, Action arguments) {
		this.opponentAction = arguments;

		if (opponentAction instanceof OfferForVoting) {
			offers.offer(((OfferForVoting) opponentAction).getBid());
		}

		// if (opponentAction instanceof Accept && sender != this) {
		// JOptionPane.showMessageDialog(null,
		// "" + sender + " accepted your last offer.");
		// }

		if (opponentAction instanceof EndNegotiation) {
			JOptionPane.showMessageDialog(null, "" + sender + " cancelled the negotiation session");
		}
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		if (ui != null) {
			ui.dispose();
			ui = null;
		}
		try {
			if (possibleActions.contains(Accept.class)) {
				Bid topic = offers.poll();
				ui = new EnterBidDialogAcceptReject(this, null, true, (AdditiveUtilitySpace) utilitySpace, topic);
			} else {
				ui = new EnterBidDialogOfferForVoting(this, null, true, (AdditiveUtilitySpace) utilitySpace);
			}

		} catch (Exception e) {
			System.out.println("Problem in UIAgent2.init:" + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("ui.getClass().toString() = " + ui.getClass().toString());
		Action action = ui.askUserForAction(opponentAction, myPreviousBid);
		// System.out.println("action = " + action);
		if ((action != null) && (action instanceof Offer)) {
			myPreviousBid = ((Offer) action).getBid();
			offers.offer(myPreviousBid);
		}
		return action;
	}

	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public Class<? extends DefaultMultilateralProtocol> getProtocol() {
		return AlternatingMultipleOffersProtocol.class;
	}

	@Override
	public String getDescription() {
		return "Consensus Voting Human GUI";
	}
}
