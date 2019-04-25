package agents;

import java.util.HashMap;
import java.util.StringTokenizer;

import agents.qoagent2.QAgentsCore;
import agents.qoagent2.QMessages;
import genius.core.Agent;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;

public class QOAgent extends Agent {
	private enum ACTIONTYPE {
		START, OFFER, ACCEPT, BREAKOFF
	};

	private agents.qoagent2.QOAgent m_QOAgent;
	private boolean fFirstOffer;
	private Action fNextAction;
	private int fMessageId;
	public AdditiveUtilitySpace[] opponentModels;
	private Bid lOppntBid = null; // last opponent bid

	@Override
	public Action chooseAction() {

		if (fFirstOffer) {
			m_QOAgent.calculateFirstOffer();
			fFirstOffer = false;
		} else {
			// m_QOAgent.incrementCurrentTurn();
		}
		return fNextAction;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void init() {
		fFirstOffer = true;
		fMessageId = 1;
		opponentModels = new AdditiveUtilitySpace[3];
		try {
			opponentModels[0] = new AdditiveUtilitySpace(
					utilitySpace.getDomain(), getName() + "_long_term.xml");
			opponentModels[1] = new AdditiveUtilitySpace(
					utilitySpace.getDomain(), getName() + "_short_term.xml");
			opponentModels[2] = new AdditiveUtilitySpace(
					utilitySpace.getDomain(), getName() + "_compromise.xml");
		} catch (Exception e) {
			e.printStackTrace();
		}
		m_QOAgent = new agents.qoagent2.QOAgent(this, false, "Zimbabwe", "no",
				"QOAgent", "1");

	}

	@Override
	public void ReceiveMessage(Action opponentAction) {
		String sMessage = "";
		ACTIONTYPE lActionType;
		lActionType = getActionType(opponentAction);
		switch (lActionType) {
		case OFFER: // Offer received from opponent
			try {
				lOppntBid = ((Offer) opponentAction).getBid();
				if (fFirstOffer) {
					sMessage = "type offer source 1 target 2 tag "
							+ String.valueOf(fMessageId) + " issueSet ";
				} else {
					sMessage = "type counter_offer source 1 target 2 tag "
							+ String.valueOf(fMessageId) + " issueSet ";
				}
				for (Issue lIssue : utilitySpace.getDomain().getIssues()) {
					sMessage = sMessage
							+ lOppntBid.getValue(lIssue.getNumber()) + "*"
							+ lIssue.getName() + "*";
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case ACCEPT: // Presumably, opponent accepted last bid, but let's
						// check...
		case BREAKOFF:
			// nothing left to do. Negotiation ended, which should be checked by
			// Negotiator...
			break;
		default:
			break;
		}
		m_QOAgent.receivedMessage(sMessage);
	}

	private ACTIONTYPE getActionType(Action lAction) {
		ACTIONTYPE lActionType = ACTIONTYPE.START;
		if (lAction instanceof Offer)
			lActionType = ACTIONTYPE.OFFER;
		else if (lAction instanceof Accept)
			lActionType = ACTIONTYPE.ACCEPT;
		else if (lAction instanceof EndNegotiation)
			lActionType = ACTIONTYPE.BREAKOFF;
		return lActionType;
	}

	public void prepareAction(int pMessageType, String pMessage) {
		String sFormattedMsg = "";
		Action lAction = null;

		switch (pMessageType) {
		case QMessages.OFFER:
		case QMessages.COUNTER_OFFER: {
			/*
			 * sFormattedMsg = "type offer" + " source " + m_agent.getPartyId()
			 * + " target " + m_agent.getOpponentAgentId() + " tag " +
			 * m_agent.getMsgId() + " issueSet ";
			 * 
			 * sFormattedMsg += sMsgBody;
			 */
			HashMap<Integer, Value> lValues = new HashMap<Integer, Value>();

			String sOffer = pMessage
					.substring(pMessage.indexOf("issueSet ") + 9);
			// tokenize the agreement by issue separator
			StringTokenizer st = new StringTokenizer(sOffer,
					QAgentsCore.ISSUE_SEPARATOR_STR);

			// the agreement string has the following structure:
			// issue_value SEPARATOR issue_name SEPARATOR...
			while (st.hasMoreTokens()) {
				// get issue value
				String sCurrentIssueValue = st.nextToken();

				String sCurrentIssueName = st.nextToken();

				// find the issue name and set the index in the returned array
				Issue lIssue = null;
				for (Issue lTmp : utilitySpace.getDomain().getIssues()) {
					if (lTmp.getName().equals(sCurrentIssueName)) {
						lIssue = lTmp;
						break;
					}
				}
				IssueDiscrete lIssueDisc = (IssueDiscrete) lIssue;
				// find the value
				ValueDiscrete lValue = null;
				for (ValueDiscrete lTmp : lIssueDisc.getValues()) {
					if (lTmp.getValue().equals(sCurrentIssueValue)) {
						lValue = lTmp;
						break;
					}
				}
				lValues.put(lIssue.getNumber(), lValue);
			} // end while - has more tokens
			try {
				Bid lBid = new Bid(utilitySpace.getDomain(), lValues);
				lAction = new Offer(this.getAgentID(), lBid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			break;
		case QMessages.ACCEPT: {
			lAction = new Accept(this.getAgentID(), lOppntBid);
		}
			break;
		case QMessages.REJECT: {
			m_QOAgent.incrementCurrentTurn();

			return;
		}

		default: {
			System.out.println("[QO]ERROR: Invalid message kind: "
					+ pMessageType + " [QMessages::formatMessage(199)]");

		}
			break;
		}

		fNextAction = lAction;

	}

}
