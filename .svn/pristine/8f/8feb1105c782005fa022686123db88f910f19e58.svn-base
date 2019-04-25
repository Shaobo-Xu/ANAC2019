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
import genius.core.actions.Accept;
import genius.core.actions.EndNegotiation;
import genius.core.actions.OfferForVoting;
import genius.core.actions.Reject;
import genius.core.exceptions.Warning;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.utility.AdditiveUtilitySpace;

/**
 *
 * @author David festen
 */
public class EnterBidDialogAcceptReject extends JDialog implements
		EnterBidDialogInterface {

	private static final long serialVersionUID = -8582527630534972701L;
	private NegoInfo negoOffer; // the table model
	private genius.core.actions.Action selectedAction;
	private AbstractNegotiationParty party;
	private Bid topic;
	private JTextArea negotiationMessages = new JTextArea("NO MESSAGES YET");
	// Wouter: we have some whitespace in the buttons,
	// that makes nicer buttons and also artificially increases the window size.
	private JButton buttonAccept = new JButton(" Accept ");
	private JButton buttonReject = new JButton(" Reject ");
	private JButton buttonExit = new JButton(" Exit Application ");

	private JPanel buttonPanel = new JPanel();
	private JTable BidTable;

	public EnterBidDialogAcceptReject(AbstractNegotiationParty party,
			Frame parent, boolean modal, AdditiveUtilitySpace us, Bid topic)
			throws Exception {

		super(parent, modal);
		this.party = party;
		this.topic = topic;
		negoOffer = new NegoShowOffer(null, null, us, topic);
		initThePanel();
	}

	// quick hack.. we can't refer to the Agent's utilitySpace because
	// the field is protected and there is no getUtilitySpace function either.
	// therefore the Agent has to inform us when utilspace changes.
	public void setUtilitySpace(AdditiveUtilitySpace us) {
		negoOffer.utilitySpace = us;
	}

	private void initThePanel() {
		if (negoOffer == null)
			throw new NullPointerException("negoOffer is null");
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Choose action for party " + party.getPartyId().toString());
		// setSize(new java.awt.Dimension(600, 400));
		// setBounds(0,0,640,480);

		// createFrom north field: the message field
		pane.add(negotiationMessages, "North");

		// createFrom center panel: the bid table
		BidTable = new JTable(negoOffer);
		// BidTable.setModel(negoOffer); // need a model for column size etc...
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
			bid = negoOffer.getBid();
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
			selectedAction = new Accept(party.getPartyId(), topic);
			setVisible(false);
		}
	}

	private void buttonRejectActionPerformed(java.awt.event.ActionEvent evt) {
		Bid bid = getBid();
		if (bid != null) {
			System.out.println("Reject performed");
			selectedAction = new Reject(party.getPartyId(), topic);
			setVisible(false);
		}
	}

	private void buttonExitActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("End negotiation action performed");
		selectedAction = new EndNegotiation(party.getPartyId());
		this.dispose();
	}

	/**
	 * This is called by UIAgent repeatedly, to ask for next action.
	 * 
	 * @param opponentAction
	 *            is action done by opponent
	 * @param votingTopic
	 * @return our next negotionat action.
	 */
	public genius.core.actions.Action askUserForAction(
			genius.core.actions.Action opponentAction, Bid votingTopic) {

		setTitle("Choose action for party " + party.getPartyId().toString());
		negoOffer.lastAccepted = null;
		if (opponentAction == null) {
			negotiationMessages.setText("Opponent did not send any action.");
		}
		if (opponentAction instanceof OfferForVoting) {
			Bid bid = ((OfferForVoting) opponentAction).getBid();
			negotiationMessages.setText("Offer:" + bid);
			negoOffer.lastAccepted = bid;
		}
		try {
			negotiationMessages.setText("Offer:" + topic);
			negoOffer.lastAccepted = topic;
			negoOffer.setOurBid(topic);
		} catch (Exception e) {
			new Warning("error in askUserForAction:", e, true, 2);
		}

		BidTable.setDefaultRenderer(BidTable.getColumnClass(0),
				new MyCellRenderer1(negoOffer));
		BidTable.setDefaultEditor(BidTable.getColumnClass(0), new MyCellEditor(
				negoOffer));

		pack();
		setVisible(true); // this returns only after the panel closes.
		// Wouter: this WILL return normally if Thread is killed, and the
		// ThreadDeath exception will disappear.
		return selectedAction;
	}
}

/********************************************************/

