package agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import genius.core.Bid;
import genius.core.Vote;
import genius.core.actions.IllegalAction;
import genius.core.actions.OfferForVoting;
import genius.core.actions.VoteForOfferAcceptance;
import genius.core.exceptions.Warning;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.utility.AdditiveUtilitySpace;

/**
 * @author David festen
 */
public class EnterBidDialogAcceptance extends JDialog implements
		EnterBidDialogInterface {

	private static final long serialVersionUID = -8582527630534972704L;
	private NegoInfo negoInfo; // the table model
	private genius.core.actions.Action selectedAction;
	private AbstractNegotiationParty party;
	private JTextArea negotiationMessages = new JTextArea("NO MESSAGES YET");
	// Wouter: we have some whitespace in the buttons,
	// that makes nicer buttons and also artificially increases the window size.
	private JButton buttonAccept = new JButton(" Accept ");
	private JButton buttonReject = new JButton(" Reject ");
	private JButton buttonExit = new JButton(" Exit Application ");

	private JPanel buttonPanel = new JPanel();
	private JTable BidTable;

	public EnterBidDialogAcceptance(AbstractNegotiationParty party,
			Frame parent, boolean modal, AdditiveUtilitySpace us) throws Exception {
		super(parent, modal);
		this.party = party;
		negoInfo = new NegoOffer(null, null, us);
		initThePanel();
	}

	// quick hack.. we can't refer to the Agent's utilitySpace because
	// the field is protected and there is no getUtilitySpace function either.
	// therefore the Agent has to inform us when utilspace changes.
	public void setUtilitySpace(AdditiveUtilitySpace us) {
		negoInfo.utilitySpace = us;
	}

	private void initThePanel() {
		if (negoInfo == null)
			throw new NullPointerException("negoInfo is null");
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Choose action for party " + party.getPartyId().toString());
		setSize(new java.awt.Dimension(600, 400));
		setBounds(0, 0, 640, 480);

		// createFrom north field: the message field
		pane.add(negotiationMessages, "North");

		// createFrom center panel: the bid table
		BidTable = new JTable(negoInfo);
		// BidTable.setModel(negoInfo); // need a model for column size etc...
		// Why doesn't this work???
		BidTable.setGridColor(Color.lightGray);
		JPanel tablepane = new JPanel(new BorderLayout());
		tablepane.add(BidTable.getTableHeader(), "North");
		tablepane.add(BidTable, "Center");
		pane.add(tablepane, "Center");
		BidTable.setRowHeight(35);

		// createFrom south panel: the buttons:
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(buttonAccept);
		buttonPanel.add(buttonReject);
		buttonPanel.add(buttonExit);
		pane.add(buttonPanel, "South");
		buttonAccept.setSelected(true);

		buttonAccept.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonAcceptActionPerformed(evt);
			}
		});

		buttonReject.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonRejectActionPerformed(evt);
			}
		});

		buttonExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonExitActionPerformed(evt);
			}
		});

		pack(); // pack will do complete layout, getting all cells etc.
	}

	private Bid getBid() {
		Bid bid = null;
		try {
			bid = negoInfo.getBid();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"There is a problem with your bid: " + e.getMessage());
		}
		return bid;
	}

	private void buttonAcceptActionPerformed(java.awt.event.ActionEvent evt) {
		Bid bid = getBid();
		if (bid != null) {
			System.out.println("Accept performed");
			selectedAction = new VoteForOfferAcceptance(party.getPartyId(),
					Vote.ACCEPT);
			setVisible(false);
		}
	}

	private void buttonRejectActionPerformed(java.awt.event.ActionEvent evt) {
		Bid bid = getBid();
		if (bid != null) {
			System.out.println("Reject performed");
			selectedAction = new VoteForOfferAcceptance(party.getPartyId(),
					Vote.REJECT);
			setVisible(false);
		}
	}

	private void buttonExitActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("Exit action performed");
		selectedAction = new IllegalAction(party.getPartyId(),
				"Exiting application");
		this.dispose();
	}

	/**
	 * This is called by UIAgent repeatedly, to ask for next action.
	 *
	 * @param opponentAction
	 *            is action done by opponent
	 * @param myPreviousBid
	 * @return our next negotionat action.
	 */
	public genius.core.actions.Action askUserForAction(
			genius.core.actions.Action opponentAction, Bid myPreviousBid) {
		return askUserForAction(opponentAction, myPreviousBid, null);
	}

	public genius.core.actions.Action askUserForAction(
			genius.core.actions.Action opponentAction, Bid currentOffer,
			Bid lastAcceptedOffer) {

		setTitle("Choose action for party " + party.getPartyId().toString());
		negoInfo.lastAccepted = null;
		if (opponentAction == null) {
			negotiationMessages.setText("Opponent did not send any action.");
		}
		if (opponentAction instanceof OfferForVoting) {
			Bid bid = ((OfferForVoting) opponentAction).getBid();
			negotiationMessages.setText("Offer:" + bid);
			negoInfo.lastAccepted = lastAcceptedOffer;
		}
		try {
			negoInfo.setOurBid(currentOffer);
		} catch (Exception e) {
			new Warning("error in askUserForAction:", e, true, 2);
		}

		BidTable.setDefaultRenderer(BidTable.getColumnClass(0),
				new MyCellRenderer1(negoInfo));
		BidTable.setDefaultEditor(BidTable.getColumnClass(0), new MyCellEditor(
				negoInfo));

		pack();
		setVisible(true); // this returns only after the panel closes.
		// Wouter: this WILL return normally if Thread is killed, and the
		// ThreadDeath exception will disappear.
		return selectedAction;
	}
}

/********************************************************/

