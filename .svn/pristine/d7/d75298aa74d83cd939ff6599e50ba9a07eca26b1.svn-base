/*
 * EnterBidDialog.java
 *
 * Created on November 16, 2006, 10:18 AM
 */

package agents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.exceptions.Warning;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;
import genius.gui.chart.UtilityPlot;

/**
 * This dialog works based on the {@link Evaluator}s and therefore only can be
 * used with {@link AdditiveUtilitySpace}. TODO can we lift this requirement?
 * 
 * @author W.Pasman
 */
@SuppressWarnings("serial")
public class EnterBidDialogExtended extends JDialog {

	private NegoInfo negoinfo; // the table model
	private genius.core.actions.Action selectedAction;
	private UIAgentExtended agent;
	private JTextArea negotiationMessages = new JTextArea("NO MESSAGES YET");
	// Wouter: we have some whitespace in the buttons,
	// that makes nicer buttons and also artificially increases the window size.
	private JButton buttonAccept = new JButton(" Accept Opponent Bid ");
	private JButton buttonEnd = new JButton("End Negotiation");
	private JButton buttonBid = new JButton("       Do Bid       ");
	private JPanel buttonPanel = new JPanel();
	private JTable BidTable;

	// alinas variables
	private JTable BidHistoryTable;
	private HistoryInfo historyinfo; // the table model
	private ChartPanel chartPanel;
	private JPanel defaultChartPanel;
	// private ScatterPlot plot;
	private UtilityPlot plot;
	private Bid lastOppBid;

	/**
	 * 
	 * @param agent
	 * @param parent
	 * @param modal
	 * @param us
	 * @param lastOpponentBid
	 *            the last opponent bid, or null if no bid has been offered. If
	 *            not null, the acceptbutton is disabled.
	 * @throws Exception
	 */
	public EnterBidDialogExtended(UIAgentExtended agent, java.awt.Frame parent,
			boolean modal, AdditiveUtilitySpace us, Bid lastOpponentBid)
			throws Exception {
		super(parent, modal);
		this.agent = agent;
		negoinfo = new NegoInfo(null, null, us);
		historyinfo = new HistoryInfo(agent, null, null, us);
		lastOppBid = lastOpponentBid;
		initThePanel();
	}

	// quick hack.. we can't refer to the Agent's utilitySpace because
	// the field is protected and there is no getUtilitySpace function either.
	// therefore the Agent has to inform us when utilspace changes.
	public void setUtilitySpace(AdditiveUtilitySpace us) {
		negoinfo.utilitySpace = us;
		historyinfo.utilitySpace = us;
	}

	private void initThePanel() {
		if (negoinfo == null)
			throw new NullPointerException("negoinfo is null");
		Container pane = getContentPane();
		// gridbag layout:
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		pane.setLayout(gridbag);

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Choose action for agent " + agent.getName());
		// setSize(new java.awt.Dimension(600, 400));
		// setBounds(0,0,640,480);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 10, 0, 10);
		// createFrom panel for history of bids
		BidHistoryTable = new JTable(historyinfo);
		BidHistoryTable.setGridColor(Color.lightGray);
		// setting the columns that contain numbers to a small width:
		BidHistoryTable.getColumnModel().getColumn(0).setMaxWidth(50);
		BidHistoryTable.getColumnModel().getColumn(2).setMaxWidth(50);
		BidHistoryTable.getColumnModel().getColumn(4).setMaxWidth(50);
		JPanel tablepaneHistory = new JPanel(new BorderLayout());
		tablepaneHistory.add(BidHistoryTable.getTableHeader(), "North");
		tablepaneHistory.add(BidHistoryTable, "Center");
		Border blackline = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(blackline,
				"History of Bids:");
		// for having the title in the center
		// title.setTitleJustification(TitledBorder.CENTER);
		tablepaneHistory.setBorder(title);
		pane.add(tablepaneHistory, c);

		c.gridwidth = 1;
		c.gridheight = 4;
		c.gridx = 0;
		c.gridy = 2;
		c.insets = new Insets(10, 10, 10, 10);
		// adding the chart
		defaultChartPanel = new JPanel();
		title = BorderFactory.createTitledBorder(blackline,
				"Utilities of Bids per round:");

		defaultChartPanel.setBorder(title);
		pane.remove(defaultChartPanel);
		pane.add(defaultChartPanel, c);

		// user input:
		JPanel userInputPanel = new JPanel();
		userInputPanel
				.setLayout(new BoxLayout(userInputPanel, BoxLayout.Y_AXIS));
		title = BorderFactory.createTitledBorder(blackline,
				"Please place your bid:");

		userInputPanel.setBorder(title);
		negotiationMessages.setBackground(Color.lightGray);
		negotiationMessages.setEditable(false);
		userInputPanel.add(negotiationMessages);

		// createFrom center panel: the bid table
		BidTable = new JTable(negoinfo);
		// BidTable.setModel(negoinfo); // need a model for column size etc...
		// Why doesn't this work???
		BidTable.setGridColor(Color.lightGray);
		BidTable.setRowHeight(18);
		JPanel tablepane = new JPanel(new BorderLayout());
		tablepane.add(BidTable.getTableHeader(), "North");
		tablepane.add(BidTable, "Center");
		userInputPanel.add(tablepane);

		if (lastOppBid == null) {
			buttonAccept.setEnabled(false);
		}

		// createFrom the buttons:
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(buttonEnd);
		buttonPanel.add(buttonAccept);
		buttonPanel.add(buttonBid);
		userInputPanel.add(buttonPanel);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = 3;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 10, 10);
		pane.add(userInputPanel, c);
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
		buttonAccept.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				buttonAcceptActionPerformed(evt);
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
			selectedAction = new Offer(agent.getAgentID(), bid);
			setVisible(false);
		}
	}

	private void buttonAcceptActionPerformed(java.awt.event.ActionEvent evt) {
		Bid bid = getBid();
		if (bid != null) {
			System.out.println("Accept performed");
			selectedAction = new Accept(agent.getAgentID(), lastOppBid);
			setVisible(false);
		}
	}

	private void buttonEndActionPerformed(java.awt.event.ActionEvent evt) {
		System.out.println("End Negotiation performed");
		selectedAction = new EndNegotiation(agent.getAgentID());
		setVisible(false);
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
		historyinfo.nrOfBids = agent.historyOfBids.size();
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
			negoinfo.setOurBid(myPreviousBid);
		} catch (Exception e) {
			System.out.println("error in askUserForAction:" + e.getMessage());
			e.printStackTrace();
		}

		BidTable.setDefaultRenderer(BidTable.getColumnClass(0),
				new MyCellRenderer(negoinfo));
		BidHistoryTable.setDefaultRenderer(BidHistoryTable.getColumnClass(0),
				new MyHistoryCellRenderer(historyinfo));
		BidHistoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // needs some
																	// fixing so
																	// that the
																	// bids are
																	// visible
																	// properly
		BidTable.setDefaultEditor(BidTable.getColumnClass(0), new MyCellEditor(
				negoinfo));

		int round = agent.bidCounter;
		System.out
				.println("round# " + round + "/" + agent.historyOfBids.size());

		// createFrom a new plot of the bid utilities for each round
		double[][] myBidSeries = new double[2][round];
		double[][] oppBidSeries = new double[2][round];

		if (round > 0) {
			System.out.println(agent.historyOfBids.get(0));
			for (int i = 0; i < round; i++) {
				try {
					System.out.println("i " + i);
					Bid oppBid = agent.historyOfBids.get(i).getOppentBid();
					Bid ourBid = agent.historyOfBids.get(i).getOurBid();
					double utilOpp = 0;
					double ourUtil = 0;

					if (agent.utilitySpace != null) {
						if (oppBid != null)
							utilOpp = agent.utilitySpace.getUtility(oppBid);
						ourUtil = agent.utilitySpace.getUtility(ourBid);
					} else {
						System.out.println("agent.utilSpace=null");
					}

					myBidSeries[0][i] = i + 1;
					myBidSeries[1][i] = ourUtil;

					oppBidSeries[0][i] = i + 1;
					oppBidSeries[1][i] = utilOpp;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		// if there is a chart already, remove and draw new one
		if (defaultChartPanel.getComponents().length > 0)
			defaultChartPanel.remove(chartPanel);
		// plot = new ScatterPlot(myBidSeries, oppBidSeries);
		plot = new UtilityPlot(myBidSeries, oppBidSeries);
		JFreeChart chart = plot.getChart();
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(350, 350));
		defaultChartPanel.add(chartPanel);

		pack();
		repaint();
		setVisible(true); // this returns only after the panel closes.
		negoinfo.comboBoxes.get(0).requestFocusInWindow();
		return selectedAction;
	}
}

/********************************************************/

/**
 * @author Alina HistoryInfo is the class that contains the former bids and
 *         fills the JTable for the history with it.
 */
@SuppressWarnings("serial")
class HistoryInfo extends AbstractTableModel {
	public Bid ourOldBid;
	public Bid oppOldBid;
	public int nrOfBids = 0;
	private UIAgentExtended agent;
	private String[] colNames = { "Round", "Bid of your Opponent", "u(opp)",
			"Your Bid", "u(own)" };

	public String getColumnName(int col) {
		return colNames[col];
	}

	public AdditiveUtilitySpace utilitySpace;

	HistoryInfo(UIAgentExtended agent, Bid our, Bid opponent,
			AdditiveUtilitySpace us) throws Exception {
		this.agent = agent;
		utilitySpace = us;
	}

	public int getColumnCount() {
		return 5;
	}

	public int getRowCount() {
		return 10; // needs to be dynamically changed, right now we will have a
					// problem when there are more than 10 rounds
	}

	public Component getValueAt(int row, int col) {

		if (nrOfBids != 0 && row < nrOfBids) {
			// get the bids for the row-th round:
			Bid oppBid = agent.historyOfBids.get(row).getOppentBid();
			Bid ourBid = agent.historyOfBids.get(row).getOurBid();

			switch (col) {
			case 0:
				return new JLabel(Integer.toString(row + 1));// roundcount
			case 1:
				String str1 = "No Bid yet.";
				if (oppBid != null) {
					str1 = new String(oppBid.toString());
					str1 = str1.substring(4, str1.length() - 3);
				}
				return new JTextArea(str1); // opponent bid as string
			case 2:
				try {
					double utilOpp = 0.0;
					if (oppBid != null)
						utilOpp = utilitySpace.getUtility(oppBid);// utility of
																	// opponent
																	// bid

					DecimalFormat df = new DecimalFormat("0.00");
					return new JTextArea(df.format(utilOpp));
				} catch (Exception e) {
				}
				;
			case 3:
				String str2 = new String(ourBid.toString());
				str2 = str2.substring(4, str2.length() - 3);
				return new JTextArea(str2); // our bid as string
			case 4:
				try {
					double utilOur = utilitySpace.getUtility(ourBid);// utility
																		// of
																		// our
																		// bid
					DecimalFormat df = new DecimalFormat("0.00");
					return new JTextArea(df.format(utilOur));
				} catch (Exception e) {
				}
				;
			}
		}

		return null;
	}

}

/********************************************************/

/**
 * NegoInfo is the class that contains all the negotiation data, and handles the
 * GUI, updating the JTable. This is the main interface to the actual JTable.
 * This is usually called XXXModel but I dont like the 'model' in the name. We
 * implement actionlistener to hear the combo box events that require
 * re-rendering of the total cost and utility field. We are pretty hard-wired
 * for a 3-column table, with column 0 the labels, column 1 the opponent bid and
 * col2 our own bid.
 */
@SuppressWarnings("serial")
class NegoInfo extends AbstractTableModel implements ActionListener {
	public Bid ourOldBid; // Bid is hashmap <issueID,Value>. Our current bid is
							// only in the comboboxes,
							// use getBid().
	public Bid lastAccepted;
	public AdditiveUtilitySpace utilitySpace; // WARNING: this may be null
	public List<Issue> issues = new ArrayList<Issue>();
	// the issues, in row order as in the GUI. Init to empty, to enable
	// freshly initialized NegoInfo to give useful results to the GUI.
	public ArrayList<Integer> IDs; // the IDs/numbers of the issues, ordered to
									// row number
	public ArrayList<JComboBox> comboBoxes; // the combo boxes for the second
											// column, ordered to row number

	NegoInfo(Bid our, Bid lastAccepted, AdditiveUtilitySpace us)
			throws Exception {
		// Wouter: just discovered that assert does nothing...........
		// David@Wouter: Assert only works when assert compile flag is set to
		// true
		ourOldBid = our;
		this.lastAccepted = lastAccepted;
		utilitySpace = us;
		issues = utilitySpace.getDomain().getIssues();
		IDs = new ArrayList<Integer>();
		for (int i = 0; i < issues.size(); i++)
			IDs.add(issues.get(i).getNumber());
		makeComboBoxes();
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return issues.size() + 2;
	}

	public boolean isCellEditable(int row, int col) {
		return (col == 2 && row < issues.size());
	}

	private String[] colNames = { "Issue", "Last Accepted Bid", "Your bid" };

	public String getColumnName(int col) {
		return colNames[col];
	}

	public void setOurBid(Bid bid) throws Exception {
		ourOldBid = bid;
		if (bid == null)
			try {
				ourOldBid = utilitySpace.getMaxUtilityBid();
			} catch (Exception e) {
				System.out.println("error getting max utility first bid:"
						+ e.getMessage());
				e.printStackTrace();
			}
		makeComboBoxes(); // reset all
		setComboBoxes(ourOldBid);
	}

	void makeComboBoxes() throws Exception {
		comboBoxes = new ArrayList<JComboBox>();
		for (Issue issue : issues) {
			if (!(issue instanceof IssueDiscrete))
				System.out.println("Problem: issue " + issue
						+ " is not IssueDiscrete. ");
			List<ValueDiscrete> values = ((IssueDiscrete) issue).getValues();
			JComboBox cbox = new JComboBox();
			EvaluatorDiscrete eval = null;
			if (utilitySpace != null)
				eval = (EvaluatorDiscrete) utilitySpace.getEvaluator(issue
						.getNumber());
			for (ValueDiscrete val : values) {
				String utilinfo = "";
				if (eval != null)
					try {
						// utilinfo="("+eval.getEvaluation(val)+")";
						utilinfo = "(" + eval.getValue(val) + ")";

					} catch (Exception e) {
						System.out.println("no evaluator for " + val + "???");
					}

				cbox.addItem(val + utilinfo);
			}
			comboBoxes.add(cbox);
			for (JComboBox b : comboBoxes)
				b.addActionListener(this);
		}
	}

	/**
	 * set the initial combo box selections according to ourOldBid Note, we can
	 * only handle Discrete evaluators right now.
	 * 
	 * @throws if
	 *             there is a problem with the issues and evaluators.
	 */
	void setComboBoxes(Bid bid) throws Exception {
		for (int i = 0; i < issues.size(); i++) {
			IssueDiscrete iss = (IssueDiscrete) issues.get(i);
			ValueDiscrete val = (ValueDiscrete) bid.getValue(iss.getNumber());
			comboBoxes.get(i).setSelectedIndex(
					((IssueDiscrete) iss).getValueIndex(val));
		}
	}

	/**
	 * get the currently chosen evaluation value of given row in the table.
	 * 
	 * @param bid
	 *            : which bid (the column in the table are for ourBid and
	 *            opponentBid)
	 * @return the evaluation of the given row in the bid. returns null if the
	 *         bid has no value in that row.
	 * @throws probablly
	 *             if rownr is out of range 0...issues.size()-1
	 */
	Value getCurrentEval(Bid bid, int rownr) throws Exception {
		if (bid == null)
			return null;
		Integer ID = IDs.get(rownr); // get ID of the issue in question.
		return bid.getValue(ID); // get the current value for that issue in the
									// bid
	}

	/**
	 * get a render component
	 * 
	 * @return the Component that can be rendered to show this cell.
	 */
	public Component getValueAt(int row, int col) {
		if (row == issues.size()) {
			if (col == 0)
				return new JLabel("Utility:");
			if (utilitySpace == null)
				return new JLabel("No UtilSpace");
			Bid bid;
			if (col == 1)
				bid = lastAccepted;
			else
				try {
					bid = getBid();
				} catch (Exception e) {
					bid = null;
					System.out.println("Internal err with getBid:"
							+ e.getMessage());
				}
			;
			JProgressBar bar = new JProgressBar(0, 100);
			bar.setStringPainted(true);
			try {
				bar.setValue((int) (0.5 + 100.0 * utilitySpace.getUtility(bid)));
				bar.setIndeterminate(false);
			} catch (Exception e) {
				new Warning("Exception during cost calculation:"
						+ e.getMessage(), false, 1);
				bar.setIndeterminate(true);
			}

			return bar;
		}
		if (row == issues.size() + 1) {
			return null;
		}
		switch (col) {
		case 0:
			return new JTextArea(issues.get(row).getName());
		case 1:
			Value value = null;
			try {
				value = getCurrentEval(lastAccepted, row);
			} catch (Exception e) {
				System.out.println("Err EnterBidDialog2.getValueAt: "
						+ e.getMessage());
			}
			if (value == null)
				return new JTextArea("-");
			return new JTextArea(value.toString());
		case 2:
			return comboBoxes.get(row);
		}
		return null;
	}

	/**
	 * @return the bid as currently set in the dialog.
	 * @throws Exception
	 */
	Bid getBid() throws Exception {
		HashMap<Integer, Value> values = new HashMap<Integer, Value>();

		for (int i = 0; i < issues.size(); i++)
			values.put(IDs.get(i), ((IssueDiscrete) issues.get(i))
					.getValue(comboBoxes.get(i).getSelectedIndex()));
		// values.put(IDs.get(i), (Value)comboBoxes.get(i).getSelectedItem());
		return new Bid(utilitySpace.getDomain(), values);
	}

	public void actionPerformed(ActionEvent e) {
		// System.out.println("event d!"+e);
		// receiveMessage the cost and utility of our own bid.
		fireTableCellUpdated(issues.size(), 2);
		fireTableCellUpdated(issues.size() + 1, 2);
	}

}

class NegoShowOffer extends NegoInfo {
	private Bid topic;

	public NegoShowOffer(Bid our, Bid opponent, AdditiveUtilitySpace us,
			Bid topic) throws Exception {
		super(our, opponent, us);
		this.topic = topic;
	}

	private String[] colNames = { "Issue", "Current offer" };

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int col) {
		return colNames[col];
	}

	@Override
	public Component getValueAt(int row, int col) {
		if (row == issues.size()) {
			if (col == 0)
				return new JLabel("Utility:");
			if (utilitySpace == null)
				return new JLabel("No UtilSpace");
			Bid bid;
			if (col == 1)
				bid = lastAccepted;
			else
				try {
					bid = getBid();
				} catch (Exception e) {
					bid = null;
					System.out.println("Internal err with getBid:"
							+ e.getMessage());
				}
			;
			JProgressBar bar = new JProgressBar(0, 100);
			bar.setStringPainted(true);
			try {
				bar.setValue((int) (0.5 + 100.0 * utilitySpace.getUtility(bid)));
				bar.setIndeterminate(false);
			} catch (Exception e) {
				new Warning("Exception during cost calculation:"
						+ e.getMessage(), false, 1);
				bar.setIndeterminate(true);
			}

			return bar;
		}
		if (row == issues.size() + 1) {
			return null;
		}
		switch (col) {
		case 0:
			return new JTextArea(issues.get(row).getName());
		case 1:
			Value value = null;
			try {
				value = getCurrentEval(topic, row);
			} catch (Exception e) {
				System.out.println("Err EnterBidDialog2.getValueAt: "
						+ e.getMessage());
			}
			if (value == null)
				return new JTextArea("-");
			return new JTextArea(value.toString());

		}
		return null;
	}
}

class NegoProposeOffer extends NegoInfo {
	NegoProposeOffer(Bid our, Bid opponent, AdditiveUtilitySpace us)
			throws Exception {
		super(our, opponent, us);
	}

	private String[] colNames = { "Issue", "Offer" };

	public Component getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return super.getValueAt(row, col);
		case 1:
			return super.getValueAt(row, 2);
		default:
			return null;
		}
	}

	@Override
	public String getColumnName(int col) {
		return colNames[col];
	}

	public boolean isCellEditable(int row, int col) {
		return (col == 1 && row < issues.size());
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// System.out.println("event d!"+e);
		// receiveMessage the cost and utility of our own bid.
		fireTableCellUpdated(issues.size(), 1);
		fireTableCellUpdated(issues.size() + 1, 1);
	}
}

class NegoOffer extends NegoInfo {
	NegoOffer(Bid our, Bid opponent, AdditiveUtilitySpace us) throws Exception {
		super(our, opponent, us);
	}

	private String[] colNames = { "Issue", "Most recently accepted",
			"Current offer" };

	public Component getValueAt(int row, int col) {
		if (row == issues.size()) {
			if (col == 0)
				return new JLabel("Utility:");
			if (utilitySpace == null)
				return new JLabel("No UtilSpace");
			Bid bid;
			if (col == 1)
				bid = lastAccepted;
			else
				try {
					bid = getBid();
				} catch (Exception e) {
					bid = null;
					System.out.println("Internal err with getBid:"
							+ e.getMessage());
				}
			;
			JProgressBar bar = new JProgressBar(0, 100);
			bar.setStringPainted(true);
			try {
				bar.setValue((int) (0.5 + 100.0 * utilitySpace.getUtility(bid)));
				bar.setIndeterminate(false);
			} catch (Exception e) {
				new Warning("Exception during cost calculation:"
						+ e.getMessage(), false, 1);
				bar.setIndeterminate(true);
			}

			return bar;
		}
		if (row == issues.size() + 1) {
			return null;
		}
		switch (col) {
		case 0:
			return new JTextArea(issues.get(row).getName());
		case 1:
			Value value = null;
			try {
				value = getCurrentEval(lastAccepted, row);
			} catch (Exception e) {
				System.out.println("Err EnterBidDialog2.getValueAt: "
						+ e.getMessage());
			}
			if (value == null)
				return new JTextArea("-");
			return new JTextArea(value.toString());
		case 2:
			value = null;
			try {
				value = getCurrentEval(ourOldBid, row);
			} catch (Exception e) {
				System.out.println("Err EnterBidDialog2.getValueAt: "
						+ e.getMessage());
			}
			if (value == null)
				return new JTextArea("-");
			return new JTextArea(value.toString());

		}
		return null;
	}

	@Override
	public String getColumnName(int col) {
		return colNames[col];
	}
}

/********************************************************************/

class MyCellRenderer implements TableCellRenderer {
	NegoInfo negoinfo;

	public MyCellRenderer(NegoInfo n) {
		negoinfo = n;
	}

	// the default converts everything to string...
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return negoinfo.getValueAt(row, column);
	}
}

/********************************************************************/

class MyHistoryCellRenderer implements TableCellRenderer {
	HistoryInfo historyinfo;

	public MyHistoryCellRenderer(HistoryInfo n) {
		historyinfo = n;
	}

	// the default converts everything to string...
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return historyinfo.getValueAt(row, column);
	}
}

/********************************************************/

class MyCellEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1L;
	NegoInfo negoinfo;

	public MyCellEditor(NegoInfo n) {
		super(new JTextField("vaag")); // Java wants us to call super class, who
										// cares...
		negoinfo = n;
		setClickCountToStart(1);
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		return negoinfo.getValueAt(row, column);
	}

}
