package negotiator.parties;

import java.util.List;

import javax.swing.JOptionPane;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.SupportedNegotiationSetting;
import genius.core.Vote;
import genius.core.actions.Action;
import genius.core.actions.InformVotingResult;
import genius.core.actions.OfferForVoting;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.Mediator;
import genius.core.parties.NegotiationInfo;
import genius.core.utility.AdditiveUtilitySpace;

/**
 * modified version of W.Pasman's modified version of Dmytro's UIAgent
 *
 * @author David Festen
 */
public class MediatorHumanNegotiationParty extends AbstractNegotiationParty implements Mediator {
	private Action opponentAction = null;
	private EnterBidDialogAcceptance ui = null;
	private Bid mostRecentAgreement = null;
	private Bid mostRecentOffer = null;

	/**
	 * One agent will be kept alive over multiple sessions. Init will be called
	 * at the start of each nego session.
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
			ui = new EnterBidDialogAcceptance(this, null, true, (AdditiveUtilitySpace) utilitySpace);
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
			mostRecentOffer = ((OfferForVoting) opponentAction).getBid();
		}

		if (opponentAction instanceof InformVotingResult
				&& ((InformVotingResult) opponentAction).getVotingResult().equals(Vote.ACCEPT)) {
			mostRecentAgreement = mostRecentOffer;
			System.out.println("mostRecentAgreement = " + mostRecentAgreement);
			JOptionPane.showMessageDialog(null, "The offer is accepted. You can continue to "
					+ "accept/reject new offers to find a better agreement.");
		}
	}

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		return ui.askUserForAction(opponentAction, mostRecentOffer, mostRecentAgreement);
	}

	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "Mediator GUI";
	}
}
