package agents.qoagent2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * The QOAgent class represents the automated agent.
 * @author Raz Lin
 * @version 1.0
 * @see QOAgentThread
 * @see QMessages
 * @see QCommunication
 * @see QAgentsCore
 * @see QAgentType
 */

/*
class QAttributeValue
{
	public String sAttribute;
	public String sValue;	
}

class QPromiseType
{
	public ArrayList agentIssueSet;
	public ArrayList opponentIssueSet;
}
*/

public class QOAgent {
	final static double OFFERS_VALUE_THRESHOLD = 0.05; // TODO: Change threshold
	public final static String NOT_APPLICABLE_STR1 = "No agreement";	
	final static String AGENT_NAME = "Automated_Agent";
	final static String AGENT_ID = "000000";
	private agents.QOAgent m_Agent;
	private String m_sAgentSide;
	private String m_sAgentName;
	private String m_sAgentId;
	private String m_sSupportMediator; // "yes", "no"
	private String m_sLogFileName;
	private String m_sProbFileName;
	
	private String m_sOppAgentId;
	
	private int m_nMaxTurns;
	private int m_nCurrentTurn;
	private int m_nMsgId;
	
//DT:	private int m_nPortNum;
	private boolean m_bHasOpponent;
	private long m_lSecondsForTurn;
	
	//private ArrayList m_offerList;
	//private QPromiseType m_promiseCombinedList;

	private QMessages m_messages;
//DT:	private QCommunication m_communication;
	
	public QGameTime m_gtStopTurn, m_gtStopNeg; 
	
	private QAgentsCore m_AgentCore;
	private QAgentType m_AgentType;
	private QAgentType m_AgentTypeNextTurn;
	
	private int m_PreviosAcceptedOffer[];
	
	private boolean m_bSendOffer;
	
	private boolean m_bEquilibriumAgent = false;
	private boolean m_bCalculateForAllAgents;
	
	/**
	 * Initialize the QOAgent
	 * @param sSide - the side of the QOAgent
	 * @param nPortNum - the port number it connects to
	 * @param sSupportMediator - whether there is a mediator
	 * @param sName - the name of the QOAgent
	 * @param sId - the id of the QOAgent
	 */
	public QOAgent(agents.QOAgent pAgent, String sSide,/*DT:  int nPortNum, */ String sSupportMediator, String sName, String sId)
	{
		m_Agent = pAgent;
		m_sLogFileName = "";
		m_bSendOffer = true;
		m_bEquilibriumAgent = false;
		m_bCalculateForAllAgents = false;
		m_PreviosAcceptedOffer = new int[QAgentType.MAX_ISSUES];
	
		for (int i = 0; i < QAgentType.MAX_ISSUES; ++i)
			m_PreviosAcceptedOffer[i] = QAgentType.NO_VALUE;
		
		if (sName.equals(""))
			sName = AGENT_NAME;
		if (sId.equals(""))
			sId = AGENT_ID;
		
		m_messages = new QMessages(this);	

		m_sAgentSide = sSide;
//DT:		m_nPortNum = nPortNum;
		m_sSupportMediator = sSupportMediator;
		m_sAgentName = sName;
		m_sAgentId = sId;
		
		setHasOpponent(false, null);
		setMaxTurns(0);
		setCurrentTurn(1);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy.H.mm.ss");
		Date now = new Date();
		String sNow = formatter.format(now);

		m_sLogFileName = "logs\\QOData" + sNow + ".log";
		m_sProbFileName = "logs\\prob" + sNow + ".";
		m_AgentCore = new QAgentsCore( m_sLogFileName, sNow, m_Agent);
		
		//	using long term type for england
		//	using short term type for zimbabwe
		if (m_sAgentSide.equals("Zimbabwe"))
			m_AgentType = m_AgentCore.getZimbabweShortTermType();  
		else if (m_sAgentSide.equals("England"))
			//@@m_AgentType =  m_AgentCore.getEnglandLongTermType();
			m_AgentType =  m_AgentCore.getEnglandShortTermType();
		
		// init agent's values
		m_AgentType.calculateValues(m_nCurrentTurn);
		m_AgentTypeNextTurn = m_AgentType;
		m_AgentTypeNextTurn.calculateValues(m_nCurrentTurn + 1);

		m_AgentCore.initGenerateAgreement(m_AgentType);
		
		// initialize connection to server
/*DT:		m_communication = new QCommunication(this, m_nPortNum);
	
		Thread CommunicationThread = new Thread(m_communication);
		CommunicationThread.start();
*/		
	/*	
		m_utility = new QUtility(this);
		
		m_threat = new QThreat(this);
		m_comment = new QComment(this);
*/	}

	public QOAgent(agents.QOAgent pAgent, boolean bIsEquilibriumAgent, String sSide, /*DT: int nPortNum,*/ String sSupportMediator, String sName, String sId)
	{
		m_Agent = pAgent;
		m_sLogFileName = "";
		m_bSendOffer = true;
		m_bEquilibriumAgent = bIsEquilibriumAgent;
		m_bCalculateForAllAgents = false;
		m_PreviosAcceptedOffer = new int[QAgentType.MAX_ISSUES];
	
		for (int i = 0; i < QAgentType.MAX_ISSUES; ++i)
			m_PreviosAcceptedOffer[i] = QAgentType.NO_VALUE;
		
		if (sName.equals(""))
			sName = AGENT_NAME;
		if (sId.equals(""))
			sId = AGENT_ID;
		
		m_messages = new QMessages(this);	

		m_sAgentSide = sSide;
		//DT:		m_nPortNum = nPortNum;
		m_sSupportMediator = sSupportMediator;
		m_sAgentName = sName;
		m_sAgentId = sId;
		
		setHasOpponent(false, null);
		setMaxTurns(0);
		setCurrentTurn(1);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy.H.mm.ss");
		Date now = new Date();
		String sNow = formatter.format(now);

		m_sLogFileName = "logs\\QOData" + sNow + ".log";
		m_sProbFileName = "logs\\prob" + sNow + ".";
		m_AgentCore = new QAgentsCore(m_sLogFileName, sNow, m_bEquilibriumAgent, m_Agent);
		
		//	using long term type for england
		//	using short term type for zimbabwe
		if (m_sAgentSide.equals("Zimbabwe"))
			m_AgentType = m_AgentCore.getZimbabweShortTermType();  
		else if (m_sAgentSide.equals("England"))
			//@@m_AgentType =  m_AgentCore.getEnglandLongTermType();
			m_AgentType =  m_AgentCore.getEnglandShortTermType();
		
		// init agent's values
		m_AgentType.calculateValues(m_nCurrentTurn);
		m_AgentTypeNextTurn = m_AgentType;
		m_AgentTypeNextTurn.calculateValues(m_nCurrentTurn + 1);

		m_AgentCore.initGenerateAgreement(m_AgentType);
		
		// initialize connection to server
/* DT:		m_communication = new QCommunication(this, m_nPortNum);
	
		Thread CommunicationThread = new Thread(m_communication);
		CommunicationThread.start();
*/		
	}

	
	/**
	 * Main function of the agent.
	 * Used if the agent is run outside the Server
	 * Run using the following options:
	 * % java QOAgent <side_name> <port_num> <support_mediator> [<agent_name> <agent_id>] 
	 * 
	 */
/* DT:	public static void main(String[] args)
	{
		if (args.length < 5)
		{
			System.err.println("Error: Usage - java QOAgent <agent name> <port_num> <support_mediator> <equilibrium_agent> <calc_for_all_agents> [<name> <id>]");
			System.exit(1);
		}
		
		// createFrom instance of class
		// includes first connection to server
		String sSideName = args[0];
		String sPortNum = args[1];
		String sEquilibriumAgent = args[3];
		String sCalculateForAllAgents = args[4];
		int nPortNum = new Integer(sPortNum).intValue();
		
		String sSupportMediator = args[2];
		
		String sName = "";
		String sId = "";
		if (args.length > 5)
		{
			sName = args[5];
			
			if (args.length > 6)
				sId = args[6];
		}
		
		boolean bEquilibriumAgent;

		if (sEquilibriumAgent.equals("Yes"))
			bEquilibriumAgent = true;
		else
			bEquilibriumAgent = false;

		QOAgent agent = new QOAgent(bEquilibriumAgent, sSideName, nPortNum, sSupportMediator, sName, sId);
		
		if (sEquilibriumAgent.equals("Yes"))
			agent.setEquilibriumAgent(true);
		else
			agent.setEquilibriumAgent(false);

		if (sCalculateForAllAgents.equals("Yes"))
			agent.setCalculateEquilibriumForAllAgents(true);
		else
			agent.setCalculateEquilibriumForAllAgents(false);
		// register with the server
		agent.register();
	}
*/	
	public void setEquilibriumAgent(boolean bIsEquilibriumAgent)
	{
		m_bEquilibriumAgent = bIsEquilibriumAgent;
	}
	
	public void setCalculateEquilibriumForAllAgents(boolean bCalculateForAllAgents)
	{
		m_bCalculateForAllAgents = bCalculateForAllAgents;
	}
	
	/**
	 * @return agent's name
	 */
	public String getAgentName()
	{
		return m_sAgentName;
	}

	/**
	 * @return agent's side
	 */
	public String getAgentSide()
	{
		return m_sAgentSide;
	}

	/**
	 * @return agent's id
	 */
	public String getAgentId()
	{
		return m_sAgentId;
	}

	/**
	 * Called by QCommunication when a message is received from the server.
	 * Parses the message using QMessages
	 * @param sMessage - the received message
	 * @see QMessages
	 * @see QCommunication
	 */
	public void receivedMessage(String sMessage)
	{
		String sParsedMsg = m_messages.parseMessage(sMessage);

		if (sParsedMsg.equals("nak")) // registration error
		{
			setMsgId(1); // first msg sent
			generateId();
			String sRegister = m_messages.formatMessage(QMessages.REGISTER, m_sAgentId);
		
			// need to send message to server
// DT:			m_communication.printMsg(sRegister);		
		}
	}
	
	public void printMessageToServer(String sMessage)
	{
// DT:		m_communication.printMsg(sMessage);
	}
	
	/**
	 * Ends the negotiation. Closes all communication
	 * @see QCommunication 
	 */
	public void endNegotiation()
	{
		m_bSendOffer = false;
// DT:		m_communication.endNegotiation();
		
		// write final probabilities to log file
		PrintWriter bw = null;
		try {
			bw = new PrintWriter(new FileWriter(m_sLogFileName, true));
			bw.println("Final Probabilities:");
			bw.println(getOpponentsProbabilitStr());
			bw.close();
			
			if (m_AgentType.isTypeOf(QAgentType.ENGLAND_TYPE))
			{
				bw = new PrintWriter(new FileWriter(m_sProbFileName + "Zim.txt", true));
				bw.println("Final Probabilities:");
				bw.println(getOpponentsProbabilitStr());
				bw.close();
			}
			else if (m_AgentType.isTypeOf(QAgentType.ZIMBABWE_TYPE))
			{
				bw = new PrintWriter(new FileWriter(m_sProbFileName + "Eng.txt", true));
				bw.println("Final Probabilities: " + getOpponentsProbabilitStr());
				bw.close();
			}
		} catch (IOException e) {
			System.out.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::endNegotiation(245)]");
			System.err.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::endNegotiation(245)]");
		}
	}
	
	/**
	 * @return the port number
	 */
/*	public int getPort()
	{
		return m_nPortNum;
	}*/

	/**
	 * Sets whether the agent has an opponent already or not
	 * @param bHasOpponent - whether there is an opponent
	 * @param sOppId - the id of the opponent
	 */
	public void setHasOpponent(boolean bHasOpponent, String sOppId)
	{
		m_bHasOpponent = bHasOpponent;
		
		if (m_bHasOpponent)
		{
			setOpponentAgentId(sOppId);
			
			PrintWriter bw;
			try {
				bw = new PrintWriter(new FileWriter(m_sLogFileName, true));
				bw.println("QO Side: " + m_sAgentSide);
				bw.println("Opponent ID: " + sOppId);
				bw.close();
			} catch (IOException e) {
				System.out.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::setHasOpponent(266)]");
				System.err.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::setHasOpponent(266)]");
			}
		}
		else
			setOpponentAgentId("");
	}
	
	/**
	 * Sets the opponent's id
	 * @param sOppId - the opponent's id
	 */
	public void setOpponentAgentId(String sOppId)
	{
		m_sOppAgentId = sOppId;
	}

	/**
	 * @return - the opponent's id
	 */
	public String getOpponentAgentId()
	{
		return m_sOppAgentId;
	}
	
	/**
	 * Sets the number of seconds for each turn
	 * @param lSeconds - the number of seconds
	 */
	public void setSecondsForTurn(long lSeconds)
	{
		m_lSecondsForTurn = lSeconds;
	}
	
	public long getSecondsForTurn()
	{
		return m_lSecondsForTurn;
	}
	
	/**
	 * @return "no" - the QOAgent does not support mediator
	 */
	public String getSupportMediator()
	{
		return "no";
	}
	
	/**
	 * @return max number of turns for the negotiation
	 */
	public int getMaxTurns()
	{
		return m_nMaxTurns;
	}

	/**
	 * @param nMaxTurns - max number of turns for the negotiation
	 */
	public void setMaxTurns(int nMaxTurns)
	{
		m_nMaxTurns = nMaxTurns;
	}

	/**
	 * Returns the current negotiation's turn
	 * @return m_nCurrentTurn
	 */
	public int getCurrentTurn()
	{
		return m_nCurrentTurn;
	}

	/**
	 * Sets the current negotiation's turn
	 * @param nCurrentTurn - the current turn
	 */
	public void setCurrentTurn(int nCurrentTurn)
	{
		m_nCurrentTurn = nCurrentTurn;
	}
			
	/**
	 * Increments the current turn 
	 */
	public void incrementCurrentTurn()
	{
		m_nCurrentTurn++;
		m_bSendOffer = true;
		updateAgreementsValues();
		
		double dNextAgreementValue = 0;
		double dAcceptedAgreementValue = 0;
		String sQOAgreement = "";
		String sEquilibriumAgreement = "";
		String sOffer = "";
		
		if (m_bEquilibriumAgent)
		{
			if (m_nCurrentTurn == m_nMaxTurns)
			{
				m_bSendOffer = false;
				return;
			}
			//TODO: Calculate the equilibrium offer
			m_AgentCore.calculateEquilibriumAgreement(m_AgentType, m_nMaxTurns, m_bCalculateForAllAgents, m_nCurrentTurn);
			
			sEquilibriumAgreement = m_AgentCore.getEquilibriumAgreement();
			sOffer = m_messages.formatMessage(QMessages.OFFER, sEquilibriumAgreement);
			
			// check value of next offer:
			// if it's less than previously agreed agreement - don't send it
			int nextAgreementIndices[] = new int[QAgentType.MAX_ISSUES];
			nextAgreementIndices = getAgreementIndices(sEquilibriumAgreement);
			
			dNextAgreementValue = m_AgentType.getAgreementValue(nextAgreementIndices, m_nCurrentTurn);
			dAcceptedAgreementValue = m_AgentType.getAgreementValue(m_PreviosAcceptedOffer, m_nCurrentTurn);
			
			System.err.println("~~~~~~~~~~~~~~~~~~~~~");
			System.err.println("Accepted Agreement Value for Agent: " + dAcceptedAgreementValue);//@@
			System.err.println("~~~~~~~~~~~~~~~~~~~~~");
		}
		else
		{
			// calculate QO offer
			m_AgentCore.calculateAgreement(m_AgentType, m_nCurrentTurn);
			
			// send the selected agreement
			sQOAgreement = m_AgentCore.getQOAgreement();
			sOffer = m_messages.formatMessage(QMessages.OFFER, sQOAgreement);
	
			// check value of next offer:
			// if it's less than previously agreed agreement - don't send it
			int nextAgreementIndices[] = new int[QAgentType.MAX_ISSUES];
			nextAgreementIndices = getAgreementIndices(sQOAgreement);
			
			dNextAgreementValue = m_AgentType.getAgreementValue(nextAgreementIndices, m_nCurrentTurn);
			dAcceptedAgreementValue = m_AgentType.getAgreementValue(m_PreviosAcceptedOffer, m_nCurrentTurn);
			
			System.err.println("~~~~~~~~~~~~~~~~~~~~~");
			System.err.println("Accepted Agreement Value for Agent: " + dAcceptedAgreementValue);//@@
			System.err.println("~~~~~~~~~~~~~~~~~~~~~");
		}
		
		if (dAcceptedAgreementValue >= dNextAgreementValue)
		{
			// don't send message - previously accepted offer
			// has better score
			m_bSendOffer = false;
		}
		
		if (m_bSendOffer)
		{
			// createFrom thread to send delayed message
/* DT:			QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sOffer, m_nCurrentTurn);
			delayedMessageThread.start();
*/			
			m_Agent.prepareAction(QMessages.OFFER, sOffer);
		}
	}

	public void calculateFirstOffer()
	{
		m_bSendOffer = true;
		
		String sQOAgreement = "";
		String sEquilibriumAgreement = "";
		String sOffer = "";
		
		if (m_bEquilibriumAgent)
		{
			//TODO: Calculate the equilibrium offer
			m_AgentCore.calculateEquilibriumAgreement(m_AgentType, m_nMaxTurns, m_bCalculateForAllAgents, m_nCurrentTurn);
			
			sEquilibriumAgreement = m_AgentCore.getEquilibriumAgreement();
			sOffer = m_messages.formatMessage(QMessages.OFFER, sEquilibriumAgreement);
		}
		else
		{
			// calculate QO offer
			m_AgentCore.calculateAgreement(m_AgentType, m_nCurrentTurn);
			
			// send the selected agreement
			sQOAgreement = m_AgentCore.getQOAgreement();
			sOffer = m_messages.formatMessage(QMessages.OFFER, sQOAgreement);
		}
		
		if (m_bSendOffer)
		{
			// createFrom thread to send delayed message
/* DT:			QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sOffer, m_nCurrentTurn);
			delayedMessageThread.start();
*/			
			m_Agent.prepareAction(QMessages.OFFER, sOffer);
		}
	}
	
	public boolean getIsOfferToSend()
	{
		return m_bSendOffer;
	}
	
	public void updateAgreementsValues()
	{
		m_AgentType.calculateValues(m_nCurrentTurn);
		m_AgentTypeNextTurn.calculateValues(m_nCurrentTurn + 1);
		
		m_AgentCore.updateAgreementsValues(m_nCurrentTurn);
	}
	
	/**
	 * @return m_nMsgId - the current message id
	 */
	public int getMsgId()
	{
		return m_nMsgId;
	}
	
	/**
	 * Sets a new message id
	 * @param nMsgId - the new message id
	 */
	public void setMsgId(int nMsgId)
	{
		m_nMsgId = nMsgId;
	}
	
	/**
	 * Increments the next message id.
	 * Called only after a message is sent
	 * @see QCommunication#printMsg 
	 */
	public void incrementMsgId()
	{
		m_nMsgId++;
	}
	
	/**
	 * Generates a random id for the QOAgent and saves it to m_sAgentId 
	 */
	public void generateId()
	{
		// generate random id
		Random rn = new Random(); // new random, seed to current time
		
		int nRandomAgentId = rn.nextInt();
		nRandomAgentId = Math.abs(nRandomAgentId);
		
		// call Message class with registration tag
		String sAgentId = new Integer(nRandomAgentId).toString();
		m_sAgentId = sAgentId;
	}
	
	/**
	 * Resgisters the agent with the server
	 * @see QMessages
	 * @see QCommunication 
	 */
	public void register()
	{
		setMsgId(1); // first msg sent
		String sRegister = m_messages.formatMessage(QMessages.REGISTER, m_sAgentId);
		
		// need to send message to server
//DT:		m_communication.printMsg(sRegister);		
	}
	
	/**
	 * Sends the best agreeement for the agent at the current turn 
	 */
	public void sendBestAgreement()
	{
		String sBestAgreement = m_AgentType.getBestAgreementStr();
		
/* DT:		String sAgreementMsg = m_messages.formatMessage(QMessages.OFFER, sBestAgreement);
		
		m_communication.printMsg(sAgreementMsg);
*/
		m_Agent.prepareAction(QMessages.OFFER, sBestAgreement);
	}
	
	/**
	 * 
	 * @return indices of given agreement for the current agent
	 */
	public int[] getAgreementIndices(String sAgreementStr)
	{
		return m_AgentType.getAgreementIndices(sAgreementStr);
	}
	
	/**
	 * Update opponent's probability given the message received
	 * and devicde whether to accept the message or reject it
	 * 
	 * @param nMessageType - message type
	 * @param CurrentAgreementIdx - array of agreement indices
	 */
	public void calculateResponse(int nMessageType, int CurrentAgreementIdx[], String sOriginalMessage)
	{
		System.err.println("Is Equ. Agent? " + m_bEquilibriumAgent);
		// if it's the equilibrium agent - call the equilibrium response method
		if (m_bEquilibriumAgent)
		{
			calculateEquilibriumResponse(nMessageType, CurrentAgreementIdx, sOriginalMessage);
			
			return;
		}
		
		//String sOffer = m_AgentType.getAgreementStr(CurrentAgreementIdx);
	
		// if a partial agreement was proposed in the past, 
		// the current agreement may include only partial
		// value - merge it with previous accepted agreement
		for (int i = 0; i < QAgentType.MAX_ISSUES; ++i)
		{
			// if value of current issue is "no agreement" or "no value"
			if (CurrentAgreementIdx[i] == QAgentType.NO_VALUE)
				CurrentAgreementIdx[i] = m_PreviosAcceptedOffer[i];
			else if (m_AgentType.isIssueValueNoAgreement(i, CurrentAgreementIdx[i]))
			{
				// if previous accepted agreement has values
				// for it, copy the value
				if (m_PreviosAcceptedOffer[i] != QAgentType.NO_VALUE)
					CurrentAgreementIdx[i] = m_PreviosAcceptedOffer[i];
			}
		}

		// Update probability of opponent based on message
		m_AgentCore.updateOpponentProbability(CurrentAgreementIdx, m_nCurrentTurn, nMessageType, QMessages.MESSAGE_RECEIVED);
		
		// decide whether to accept the message or reject it:
		double dOppOfferValueForAgent = QAgentType.VERY_SMALL_NUMBER;
		double dQONextOfferValueForAgent = QAgentType.VERY_SMALL_NUMBER;

		double dOppOfferValueForOpponent = QAgentType.VERY_SMALL_NUMBER;
		double dQONextOfferValueForOpponent = QAgentType.VERY_SMALL_NUMBER;
		
		// 1. Check the utility value of the opponent's offer
		// oppVal = u1(opp_offer)
		dOppOfferValueForAgent = m_AgentType.getAgreementValue(CurrentAgreementIdx, m_nCurrentTurn);
				
		// check whether previous accepted agreement is better - if so, reject
		double dAcceptedAgreementValue = m_AgentType.getAgreementValue(m_PreviosAcceptedOffer, m_nCurrentTurn);
		
		System.err.println("~~~~~~~~~~~~~~~~~~~~");
		System.err.println("Opponent Offer Value for Agent: " + dOppOfferValueForAgent);//@@
		System.err.println("Accepted Agreement Value for Agent: " + dAcceptedAgreementValue);//@@
		
		if (dAcceptedAgreementValue >= dOppOfferValueForAgent)
		{
			// reject offer
/* DT:			String sRejectMsg = m_messages.formatMessage(QMessages.REJECT, sOriginalMessage);
			
			QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sRejectMsg);
			delayedMessageThread.start();			
			//m_communication.printMsg(sRejectMsg);		
*/
			m_Agent.prepareAction(QMessages.REJECT, sOriginalMessage);
			return;
		}
		
		// 2. nextVal = Check the value of my offer in the next turn
		// nextVal = u1(next_QO_offer at time t+1)

		// calculate QO offer for next turn
		m_AgentCore.calculateNextTurnAgreement(m_AgentTypeNextTurn, m_nCurrentTurn + 1);
		dQONextOfferValueForAgent = m_AgentCore.getNextTurnAgentQOUtilityValue();

		System.err.println("Next Turn Offer Value for Agent: " + dQONextOfferValueForAgent);//@@
		System.err.println("Next Turn Offer: " + m_AgentCore.getNextTurnAgentQOAgreement());//@@
		System.err.println("~~~~~~~~~~~~~~~~~~~~");
		
		// 3. if oppVal >= nextVal --> Accept
		if (dOppOfferValueForAgent >= dQONextOfferValueForAgent)
		{
			PrintWriter bw;
			try {
				bw = new PrintWriter(new FileWriter(m_sLogFileName, true));
				bw.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				bw.println("Accepted offer: val (" + dOppOfferValueForAgent + ") >= QO_Next (" + dQONextOfferValueForAgent + ")");
				bw.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				bw.close();
			} catch (IOException e) {
				System.out.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::calculateResponse(605)]");
				System.err.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::calculateResponse(605)]");
			}
			
			// accept offer
/* DT:			String sAcceptMsg = m_messages.formatMessage(QMessages.ACCEPT, sOriginalMessage);
			QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sAcceptMsg);
			delayedMessageThread.start();			

			//m_communication.printMsg(sAcceptMsg);		
*/
			m_Agent.prepareAction(QMessages.ACCEPT, sOriginalMessage);
			// set flag not to propose offer if didn't propose yet this turn
			m_bSendOffer = false;			
		}
		else
		{
			// 4. Otherwise, 
			// if |u2(next_QO_offer at time t+1) - u2(opp_offer)| <= T --> Reject
			// else, Accept with prob. lu(opp_offer).
			QAgentType opponentNextTurnAgentType = m_AgentCore.getNextTurnOpponentType();
			
			dOppOfferValueForOpponent = opponentNextTurnAgentType.getAgreementValue(CurrentAgreementIdx, m_nCurrentTurn + 1);
			dQONextOfferValueForOpponent = m_AgentCore.getNextTurnOpponentQOUtilityValue();
			
			double dValueDifference = Math.abs(dOppOfferValueForOpponent - dQONextOfferValueForOpponent);
			
			if (dValueDifference <= OFFERS_VALUE_THRESHOLD)
			{
				// reject offer
/* DT:				String sRejectMsg = m_messages.formatMessage(QMessages.REJECT, sOriginalMessage);
				QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sRejectMsg);
				delayedMessageThread.start();			
*/
				m_Agent.prepareAction(QMessages.REJECT, sOriginalMessage);
				//m_communication.printMsg(sRejectMsg);					
			}
			else 
			{
				Random generator = new Random();
				double dRandNum = generator.nextDouble();
				
				System.err.println("Rand num = " + dRandNum);//REMOVE
				
				//@@
				// accept offer using probability of agreements ranking
				double dOfferProbabilityValue = m_AgentType.getAgreementRankingProbability(CurrentAgreementIdx);
				System.err.println("Agreement ranking prob. = " + dOfferProbabilityValue);//REMOVE
				
				
				//raz 08-05-06
				boolean accept = true;
				double dSQValue = m_AgentType.getSQValue();
				double dAgreementValue = m_AgentType.getAgreementValue(CurrentAgreementIdx, m_nCurrentTurn);
				
				double originalAgreementValue = Math.log(dAgreementValue) / QAgentType.PRECISION_VALUE;
				if (originalAgreementValue < dSQValue)
					accept = false;
				
				if (!accept) {
					System.out.println("====Agreement will not be accepted, lower than SQ");
					System.err.println("====Agreement will not be accepted, lower than SQ");
				}
				
				PrintWriter bw;
				try {
					bw = new PrintWriter(new FileWriter(m_sLogFileName, true));
					bw.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
					bw.println("Using probability to decide whether to accept:");
					bw.println("Agreement ranking prob. = " + dOfferProbabilityValue);
					bw.println("Rand num = " + dRandNum);
					bw.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
					bw.close();
				} catch (IOException e) {
					System.out.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::calculateResponse(605)]");
					System.err.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::calculateResponse(605)]");
				}
								
				/*
				accept offer with a probability given by the luce number
				lu(offer_opp)
				double dOfferProbabilityValue = m_AgentType.getAgreementLuceValue(dOppOfferValueForAgent);
				*/
				
				if ( (dRandNum <= dOfferProbabilityValue) && (accept))//08-05-06
				{
					// accept offer
/* DT:					String sAcceptMsg = m_messages.formatMessage(QMessages.ACCEPT, sOriginalMessage);
					QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sAcceptMsg);
					delayedMessageThread.start();			
*/
					m_Agent.prepareAction(QMessages.ACCEPT, sOriginalMessage);
					//m_communication.printMsg(sAcceptMsg);	
					
					// set flag not to propose offer if didn't propose yet this turn
					m_bSendOffer = false;
				}
				else
				{
					// reject offer
/*DT:					String sRejectMsg = m_messages.formatMessage(QMessages.REJECT, sOriginalMessage);
					QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sRejectMsg);
					delayedMessageThread.start();
*/	
					m_Agent.prepareAction(QMessages.REJECT, sOriginalMessage);
					//m_communication.printMsg(sRejectMsg);						
				}
			} // end if-else - difference of offer and QO value for opponent
		} // end if-else - value of offer compared to next QO value
	}
	
	public void calculateEquilibriumResponse(int nMessageType, int CurrentAgreementIdx[], String sOriginalMessage)
	{
		//String sOffer = m_AgentType.getAgreementStr(CurrentAgreementIdx);
	
		// if a partial agreement was proposed in the past, 
		// the current agreement may include only partial
		// value - merge it with previous accepted agreement
		for (int i = 0; i < QAgentType.MAX_ISSUES; ++i)
		{
			// if value of current issue is "no agreement" or "no value"
			if (CurrentAgreementIdx[i] == QAgentType.NO_VALUE)
				CurrentAgreementIdx[i] = m_PreviosAcceptedOffer[i];
			else if (m_AgentType.isIssueValueNoAgreement(i, CurrentAgreementIdx[i]))
			{
				// if previous accepted agreement has values
				// for it, copy the value
				if (m_PreviosAcceptedOffer[i] != QAgentType.NO_VALUE)
					CurrentAgreementIdx[i] = m_PreviosAcceptedOffer[i];
			}
		}

		// Update probability of opponent based on message
		m_AgentCore.updateOpponentProbability(CurrentAgreementIdx, m_nCurrentTurn, nMessageType, QMessages.MESSAGE_RECEIVED);
		
		// decide whether to accept the message or reject it:
		double dOppOfferValueForAgent = QAgentType.VERY_SMALL_NUMBER;
		double dEquilibriumNextOfferValueForAgent = QAgentType.VERY_SMALL_NUMBER;

		double dOppOfferValueForOpponent = QAgentType.VERY_SMALL_NUMBER;
		double dEquilibriumNextOfferValueForOpponent = QAgentType.VERY_SMALL_NUMBER;
		
		// 1. Check the utility value of the opponent's offer
		// oppVal = u1(opp_offer)
		dOppOfferValueForAgent = m_AgentType.getAgreementValue(CurrentAgreementIdx, m_nCurrentTurn);
				
		// check whether previous accepted agreement is better - if so, reject
		double dAcceptedAgreementValue = m_AgentType.getAgreementValue(m_PreviosAcceptedOffer, m_nCurrentTurn);
		
		System.err.println("~~~~~~~~~~~~~~~~~~~~");
		System.err.println("Opponent Offer Value for Agent: " + dOppOfferValueForAgent);//@@
		System.err.println("Accepted Agreement Value for Agent: " + dAcceptedAgreementValue);//@@
		
		if (dAcceptedAgreementValue >= dOppOfferValueForAgent)
		{
			// reject offer
/* DT:			String sRejectMsg = m_messages.formatMessage(QMessages.REJECT, sOriginalMessage);
			
			QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sRejectMsg);
			delayedMessageThread.start();
*/
			m_Agent.prepareAction(QMessages.REJECT, sOriginalMessage);
			//m_communication.printMsg(sRejectMsg);		
			
			return;
		}
		
		// 2. nextVal = Check the value of my offer in the next turn
		// nextVal = u1(next_QO_offer at time t+1)

		// calculate QO offer for next turn
		m_AgentCore.calculateNextTurnEquilibriumAgreement(m_AgentTypeNextTurn, m_nMaxTurns, m_bCalculateForAllAgents, m_nCurrentTurn + 1);
		dEquilibriumNextOfferValueForAgent = m_AgentCore.getNextTurnAgentEquilibriumUtilityValue();

		System.err.println("Next Turn Offer Value for Agent: " + dEquilibriumNextOfferValueForAgent);//@@
		System.err.println("Next Turn Offer: " + m_AgentCore.getNextTurnAgentEquilibriumAgreement());//@@
		System.err.println("~~~~~~~~~~~~~~~~~~~~");
		
		// 3. if oppVal >= nextVal --> Accept
		if (dOppOfferValueForAgent >= dEquilibriumNextOfferValueForAgent)
		{
			PrintWriter bw;
			try {
				bw = new PrintWriter(new FileWriter(m_sLogFileName, true));
				bw.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				bw.println("Accepted offer: val (" + dOppOfferValueForAgent + ") >= QO_Next (" + dEquilibriumNextOfferValueForAgent + ")");
				bw.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");
				bw.close();
			} catch (IOException e) {
				System.out.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::calculateEquilibriumResponse(605)]");
				System.err.println("[QO]ERROR----" + "Error opening logfile: " + e.getMessage() + " [QOAgent::calculateEquilibriumResponse(605)]");
			}
			
			// accept offer
/* DT:			String sAcceptMsg = m_messages.formatMessage(QMessages.ACCEPT, sOriginalMessage);
			QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sAcceptMsg);
			delayedMessageThread.start();			
*/
			m_Agent.prepareAction(QMessages.ACCEPT, sOriginalMessage);
			//m_communication.printMsg(sAcceptMsg);		
			
			// set flag not to propose offer if didn't propose yet this turn
			m_bSendOffer = false;			
		}
		else
		{
			// reject offer
/* DT:			String sRejectMsg = m_messages.formatMessage(QMessages.REJECT, sOriginalMessage);
			QDelayedMessageThread delayedMessageThread = new QDelayedMessageThread(this, sRejectMsg);
			delayedMessageThread.start();
*/						
			m_Agent.prepareAction(QMessages.REJECT, sOriginalMessage);			
			//m_communication.printMsg(sRejectMsg);						
		} 
	}

	
	public void saveAcceptedMsg(String sMessage)
	{
		m_PreviosAcceptedOffer = getAgreementIndices(sMessage);
		
		if (m_PreviosAcceptedOffer == null) // error occured
			m_PreviosAcceptedOffer = new int[QAgentType.MAX_ISSUES];
	}
	
	public String getOpponentsProbabilitStr()
	{
		if (m_AgentType.isTypeOf(QAgentType.ENGLAND_TYPE))
		{
			return m_AgentCore.getZimbabweProbabilitiesStr();
		} // end if agent's type is zimbabwe
		else if (m_AgentType.isTypeOf(QAgentType.ZIMBABWE_TYPE))
		{
			return m_AgentCore.getEnglandProbabilitiesStr();
		}
		
		return "";
	}

	public void updateOpponentProbability(int CurrentAgreementIdx[], int nMessageType, int nResponseType)
	{
		// Update probability of opponent based on message
		m_AgentCore.updateOpponentProbability(CurrentAgreementIdx, m_nCurrentTurn, nMessageType, nResponseType);
	}
	
	/*public ArrayList getOfferList()
	{
		// pre-condition
		 // m_offerList contains all attributes
		 // attributes that should not be considered should
		 // have the value "N/A" (NOT_APPLICABLE_STR).
		return m_offerList;
	}
	*/

	/*public QPromiseType getPromiseList()
	{
		return m_promiseCombinedList;
	}
	*/
}
