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
import genius.core.actions.Offer;
import genius.core.actions.OfferForVoting;
import genius.core.exceptions.Warning;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.utility.AdditiveUtilitySpace;

/**
 *
 * @author W.Pasman
 */
public class EnterBidDialogOfferForVoting extends JDialog implements
		EnterBidDialogInterface {

	private static final long serialVersionUID = -8582527630534972706L;
	private NegoInfo negoinfo; // the table model
	private genius.core.actions.Action selectedAction;
	private AbstractNegotiationParty party;
	private JTextArea negotiationMessages = new JTextArea("NO MESSAGES YET");
	// Wouter: we have some whitespace in the buttons,
	// that makes nicer buttons and also artificially increases the window size.
	private JButton buttonEnd = new JButton("End Negotiation");
	private JButton buttonBid = new JButton("       Propose       ");
	private JPanel buttonPanel = new JPanel();
	private JTable BidTable;

	public EnterBidDialogOfferForVoting(AbstractNegotiationParty party,
			Frame parent, boolean modal, AdditiveUtilitySpace us) throws Exception {
		super(parent, modal);
		this.party = party;
		negoinfo = new NegoProposeOffer(null, null, us);
		initThePanel();
	}

	// quick hack.. we can't refer to the Agent's utilitySpace because
	// the field is protected and there is no getUtilitySpace function either.
	// therefore the Agent has to inform us when utilspace changes.
	public void setUtilitySpace(AdditiveUtilitySpace us) {
		negoinfo.utilitySpace = us;
	}

	private void initThePanel() {
		if (negoinfo == null)
			throw new NullPointerException("negoinfo is null");
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Choose action for party " + party.getPartyId().toString());
		// setSize(new java.awt.Dimension(600, 400));
		// setBounds(0,0,640,480);

		// createFrom north field: the message field
		pane.add(negotiationMessages, "North");

		// createFrom center panel: the bid table
		BidTable = new JTable(negoinfo);
		// BidTable.setModel(negoinfo); // need a model for column size etc...
		// Why doesn't this work???
		BidTable.setGridColor(Color.lightGray);
		JPanel tablepane = new JPanel(new BorderLayout());
		tablepane.add(BidTable.getTableHeader(), "North");
		tablepane.add(BidTable, "Center");
		pane.add(tablepane, "Center");
		BidTable.setRowHeight(35);
		// createFrom south panel: the buttons:
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(buttonEnd);
		// buttonPanel.add(buttonSkip);
		buttonPanel.add(buttonBid);
		pane.add(buttonPanel, "South");
		buttonBid.setSelected(true);

		// set action listeners for the buttons
		buttonBid.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonBidActionPerformed(evt);
			}
		});
		// buttonSkip.addActionListener(new java.awt.event.ActionListener() {
		// public void actionPerformed(java.awt.event.ActionEvent evt) {
		// buttonSkipActionPerformed(evt);
		// }
		// });
		buttonEnd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonEndActionPerformed(evt);
			}
		});
		pack(); // pack will do complete layout, getting all cells etc.
	}

	private Bid getBid() {
		Bid bid = null;
		try {
			bid = negoinfo.getBid();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"There is a problem with your bid: " + e.getMessage());
		}
		return bid;
	}

	private void buttonBidActionPerformed(java.awt.event.ActionEvent evt) {

		Bid bid = getBid();
		if (bid != null) {
			selectedAction = new OfferForVoting(party.getPartyId(), bid);
			setVisible(false);
		}
	}

	private void buttonEndActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("End Negotiation performed");
		selectedAction = new EndNegotiation(party.getPartyId());
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

		setTitle("Choose action for party " + party.getPartyId().toString());
		negoinfo.lastAccepted = null;
		if (opponentAction == null) {
			negotiationMessages.setText("Opponent did not send any action.");
		}
		if (opponentAction instanceof Accept) {
			negotiationMessages.setText("Opponent accepted your last bid!");
			negoinfo.lastAccepted = myPreviousBid;
		}
		if (opponentAction instanceof EndNegotiation) {
			negotiationMessages.setText("Opponent cancels the negotiation.");
		}
		if (opponentAction instanceof Offer) {
			negotiationMessages.setText("Opponent proposes the following bid:");
			negoinfo.lastAccepted = ((Offer) opponentAction).getBid();
		}
		try {
			negoinfo.setOurBid(null);
		} catch (Exception e) {
			new Warning("error in askUserForAction:", e, true, 2);
		}

		BidTable.setDefaultRenderer(BidTable.getColumnClass(0),
				new MyCellRenderer1(negoinfo));
		BidTable.setDefaultEditor(BidTable.getColumnClass(0), new MyCellEditor(
				negoinfo));

		pack();
		setVisible(true); // this returns only after the panel closes.
		// Wouter: this WILL return normally if Thread is killed, and the
		// ThreadDeath exception will disappear.
		return selectedAction;
	}
}

/********************************************************/

