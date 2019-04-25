package agents;

import javax.swing.JOptionPane;

import genius.core.Agent;
import genius.core.Bid;
import genius.core.SupportedNegotiationSetting;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;

/**
 * Uses {@link Evaluator}s and therefore requires {@link AdditiveUtilitySpace}
 * 
 * @author W.Pasman, modified version of Dmytro's UIAgent
 * 
 */
public class UIAgent extends Agent {
	private Action opponentAction = null;
	private EnterBidDialog ui = null;
	private Bid myPreviousBid = null;

	/** Creates a new instance of UIAgent */
	@Override
	public String getVersion() {
		return "1.0";
	}

	/**
	 * One agent will be kept alive over multiple sessions. Init will be called
	 * at the start of each nego session.
	 */

	@Override
	public void init() {
//		System.out.println("init UIAgent");

//		System.out.println("closing old dialog of ");
		if (ui != null) {
			ui.dispose();
			ui = null;
		}
//		System.out.println("old  dialog closed. Trying to open new dialog. ");
		try {
			ui = new EnterBidDialog(this, null, true,
					(AdditiveUtilitySpace) utilitySpace, null);
		} catch (Exception e) {
			System.out.println("Problem in UIAgent2.init:" + e.getMessage());
			e.printStackTrace();
		}
//		System.out.println("finished init of UIAgent2");
	}

	@Override
	public void ReceiveMessage(Action opponentAction) {
		this.opponentAction = opponentAction;
		if (opponentAction instanceof Accept)
			JOptionPane.showMessageDialog(null,
					"Opponent accepted your last offer.");

		else if (opponentAction instanceof EndNegotiation)
			JOptionPane.showMessageDialog(null,
					"Opponent canceled the negotiation session");
		else if (opponentAction instanceof Offer) {
			try {
				ui = new EnterBidDialog(this, null, true,
						(AdditiveUtilitySpace) utilitySpace,
						((Offer) opponentAction).getBid());
			} catch (Exception e) {
				System.out
						.println("failed to initialize the UI with new offer!");
				e.printStackTrace();
			}
		}
		return;
	}

	@Override
	public Action chooseAction() {
		Action action = ui.askUserForAction(opponentAction, myPreviousBid);
		if ((action != null) && (action instanceof Offer))
			myPreviousBid = ((Offer) action).getBid();
		return action;
	}

	public boolean isUIAgent() {
		return true;
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "Simple human user interface to place bids";
	}
}
