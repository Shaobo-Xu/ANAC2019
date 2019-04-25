package agents.qoagent;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * The AutomatedAgent class represents the automated agent.
 * @author Raz Lin
 * @version 1.0
 * @see AutomatedAgentThread
 * @see AutomatedAgentMessages
 * @see AutomatedAgentCommunication
 * @see AutomatedAgentsCore
 * @see AutomatedAgentType
 */

public class AutomatedAgent {
    final public static String SIDE_A_NAME = "SIDE_A"; //England/Employer
    final public static String SIDE_B_NAME = "SIDE_B"; //Zimbabwe/Job Candidate
    
	final static String AGENT_NAME = "Automated_Agent";
	final static String AGENT_ID = "000000";
    
	private String m_sAgentSide;
	private String m_sAgentName;
	private String m_sAgentId;
	private String m_sLogFileName;
	private String m_sOppAgentId;
	
	private int m_nMaxTurns;
	private int m_nCurrentTurn;
	private int m_nMsgId;
	
	private int m_nPortNum;
	private boolean m_bHasOpponent;
	private long m_lSecondsForTurn;
    
    private AgentTools m_agentTools = null;
    private AbstractAutomatedAgent m_abstractAgent = null;
	
	private AutomatedAgentMessages m_messages;
	//DT: private AutomatedAgentCommunication m_communication;
	
	public AutomatedAgentGameTime m_gtStopTurn, m_gtStopNeg; // timers till end of turn and end of negotiation
	
	private AutomatedAgentsCore m_AgentCore;
	private AutomatedAgentType m_AgentType; // to obtain agreement values of current turn
	private AutomatedAgentType m_AgentTypeNextTurn; // to obtain agreement values for next turn
    
	private int m_PreviosAcceptedOffer[]; // indices of attributes that were agreed upon previously
    private boolean m_bSendOffer = true;
	
    public AutomatedAgent() {}
    
	/**
	 * Initialize the Automated Agent
	 * @param sSide - the side of the Automated Agent
	 * @param nPortNum - the port number it connects to
	 * @param sName - the name of the Automated Agent
	 * @param sId - the id of the Automated Agent
	 */
	public AutomatedAgent(String sSide, int nPortNum, String sName, String sId)
	{
        m_bSendOffer = true; // flag used to decide whetehr to send offers or not
        m_agentTools = new AgentTools(this);
        m_abstractAgent = new AbstractAutomatedAgent(m_agentTools);
        
        m_sLogFileName = "";
		
		m_PreviosAcceptedOffer = new int[AutomatedAgentType.MAX_ISSUES];
	
		for (int i = 0; i < AutomatedAgentType.MAX_ISSUES; ++i)
			m_PreviosAcceptedOffer[i] = AutomatedAgentType.NO_VALUE;
		
		if (sName.equals(""))
			sName = AGENT_NAME;
		if (sId.equals(""))
			sId = AGENT_ID;
		
		m_messages = new AutomatedAgentMessages(this, m_agentTools, m_abstractAgent);	

		m_sAgentSide = sSide;
		m_nPortNum = nPortNum;
		m_sAgentName = sName;
		m_sAgentId = sId;
		
		setHasOpponent(false, null);
		setMaxTurns(0);
		setCurrentTurn(1);
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy.H.mm.ss");
		Date now = new Date();
		String sNow = formatter.format(now);

		m_sLogFileName = "logs\\AutomatedAgentData" + sNow + ".log";
		
        m_AgentCore = new AutomatedAgentsCore(m_sLogFileName, sNow, m_agentTools, m_abstractAgent);
//        m_AgentCore.setAgentTools(m_agentTools);
  //      m_AgentCore.setAbstractAgent(m_abstractAgent);
		
        // Choose the agent type
        m_agentTools.setAutomatedAgentType(m_sAgentSide);
        
		// init agent's values
        // both for the first turn (1) and for the following turn (2)
		m_AgentType.calculateValues(m_abstractAgent, m_nCurrentTurn);
		m_AgentTypeNextTurn = m_AgentType;
		m_AgentTypeNextTurn.calculateValues(m_abstractAgent, m_nCurrentTurn + 1);

		m_AgentCore.initGenerateAgreement(m_AgentType);
		
		// initialize connection to server
		//DT: m_communication = new AutomatedAgentCommunication(this, m_nPortNum);
	
		//DT: Thread CommunicationThread = new Thread(m_communication);
		//DT: CommunicationThread.start();
	}

    public AutomatedAgentType getAgentType() {
        return m_AgentType;
    }
    
    public void setAgentType(String side, int type) {
        if (side.equals(AutomatedAgent.SIDE_B_NAME)) {
            switch (type) {
            case AutomatedAgentsCore.SHORT_TERM_TYPE_IDX:  
                m_AgentType = m_AgentCore.getSideBShortTermType();
                break;
            case AutomatedAgentsCore.LONG_TERM_TYPE_IDX:
                m_AgentType = m_AgentCore.getSideBLongTermType();
                break;
            case AutomatedAgentsCore.COMPROMISE_TYPE_IDX:
                m_AgentType = m_AgentCore.getSideBCompromiseType();
                break;
            default:
                System.err.println("[AA]ERROR----" + "Wrong type for agent: " + type + " [AutomatedAgent::setAgentType(129)]");
                break;
            }
        }
        else if (side.equals(AutomatedAgent.SIDE_A_NAME)) {
            switch (type) {
            case AutomatedAgentsCore.SHORT_TERM_TYPE_IDX:  
                m_AgentType = m_AgentCore.getSideAShortTermType();
                break;
            case AutomatedAgentsCore.LONG_TERM_TYPE_IDX:
                m_AgentType = m_AgentCore.getSideALongTermType();
                break;
            case AutomatedAgentsCore.COMPROMISE_TYPE_IDX:
                m_AgentType = m_AgentCore.getSideACompromiseType();
                break;
            default:
                System.err.println("[AA]ERROR----" + "Wrong type for agent: " + type + " [AutomatedAgent::setAgentType(129)]");
                break;
            }
        }
    }
    
	/**
	 * Main function of the agent.
	 * Used if the agent is run outside the Server
	 * Run using the following options:
	 * % java AutomatedAgent <side_name> <port_num> <equilibrium_agent> <calc_for_all_agents> [<agent_name> <agent_id>] 
	 * 
	 */
/*	DT: public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.err.println("Error: Usage - java AutomatedAgent <agent name> <port_num> [<name> <id>]");
			System.exit(1);
		}
		
		// createFrom instance of class
		// includes first connection to server
		String sSideName = args[0];
		String sPortNum = args[1];
		int nPortNum = new Integer(sPortNum).intValue();
		
		String sName = "";
		String sId = "";
		if (args.length > 2)
		{
			sName = args[2];
			
			if (args.length > 3)
				sId = args[3];
		}
		
		AutomatedAgent agent = new AutomatedAgent(sSideName, nPortNum, sName, sId);
		
        // register with the server
		agent.register();
	}
*/	
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
	 * Called by AutomatedAgentCommunication when a message is received from the server.
	 * Parses the message using AutomatedAgentMessages
	 * @param sMessage - the received message
	 * @see AutomatedAgentMessages
	 * @see AutomatedAgentCommunication
	 */
	public void receivedMessage(String sMessage)
	{
        //logic is done in parseMessage()
		String sParsedMsg = m_messages.parseMessage(sMessage);

        //if the message is nak, it means that there is a registration error
		if (sParsedMsg.equals("nak")) // registration error
		{
			setMsgId(1); // first msg sent
			generateId();
			String sRegister = m_messages.formatMessage(AutomatedAgentMessages.REGISTER, m_sAgentId);
		
			// need to send message to server
			//DT: m_communication.printMsg(sRegister);		
		}
	}

    /**
     * Used to send a message back to the server/opponent
     * @param sMessage - the sent message
     * @see AutomatedAgentMessages
     * @see AutomatedAgentCommunication
     */    
	public void printMessageToServer(String sMessage)
	{
		//DT: m_communication.printMsg(sMessage);
		System.out.println("Agent tries to send a message to the server:" + sMessage);
	}
	
	/**
	 * Ends the negotiation. Closes all communication
	 * @see AutomatedAgentCommunication 
	 */
	public void endNegotiation()
	{
		//DT: m_communication.endNegotiation();
	}
	
	/**
	 * @return the port number
	 */
	public int getPort()
	{
		return m_nPortNum;
	}

	/**
	 * Sets whether the agent's opponent has registered yet
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
				bw.println("AutomatedAgent Side: " + m_sAgentSide);
				bw.println("Opponent ID: " + sOppId);
				bw.close();
			} catch (IOException e) {
				System.out.println("[AA]ERROR----" + "Error opening logfile: " + e.getMessage() + " [AutomatedAgent::setHasOpponent(266)]");
				System.err.println("[AA]ERROR----" + "Error opening logfile: " + e.getMessage() + " [AutomatedAgent::setHasOpponent(266)]");
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
	
    /**
     * Get the number of seconds for each turn
     * return m_lSecondsForTurn - the number of seconds per turn
     */
	public long getSecondsForTurn()
	{
		return m_lSecondsForTurn;
	}
	
	/**
	 * @return "no" - the Automated Agent does not support mediator
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
		updateAgreementsValues(); // receiveMessage values for the new turn

        calculateAgreement();
    }
    
    public void calculateAgreement() {
        // AutomatedAgentsCore.calculateAgreement method is responsible for the actual logic
        m_AgentCore.calculateAgreement(m_AgentType, m_nCurrentTurn);
    }

    /**
     * Update the agreement values when new turn begins
     * 
     */        
	public void updateAgreementsValues()
	{
		m_AgentType.calculateValues(m_abstractAgent, m_nCurrentTurn);
		m_AgentTypeNextTurn.calculateValues(m_abstractAgent, m_nCurrentTurn + 1);
		
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
	 * @see AutomatedAgentCommunication#printMsg 
	 */
	public void incrementMsgId()
	{
		m_nMsgId++;
	}
	
	/**
	 * Generates a random id for the Automated Agent and saves it to m_sAgentId 
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
	 * @see AutomatedAgentMessages
	 * @see AutomatedAgentCommunication 
	 */
/*	public void register()
	{
		setMsgId(1); // first msg sent
		String sRegister = m_messages.formatMessage(AutomatedAgentMessages.REGISTER, m_sAgentId);
		
		// need to send message to server
		m_communication.printMsg(sRegister);		
	}*/
		
	/**
	 * 
	 * @return indices of given agreement for the current agent
	 */
	public int[] getAgreementIndices(String sAgreementStr)
	{
		return m_AgentType.getAgreementIndices(sAgreementStr);
	}
	
    /**
     * If an offer is accepted, need to save it for future
     * references and comparisons
     * @param sMessage - the accepted offer
     */    
	public void saveAcceptedMsg(String sMessage)
	{
		m_PreviosAcceptedOffer = getAgreementIndices(sMessage);
		
		if (m_PreviosAcceptedOffer == null) // error occured
			m_PreviosAcceptedOffer = new int[AutomatedAgentType.MAX_ISSUES];
	}
    
   /**
     * Decide regarding a received message
     * For example, decide whether to accept or reject it, or receiveMessage other agent's data
     * 
     * @param nMessageType - message type
     * @param CurrentAgreementIdx - array of agreement indices
     */
    public void calculateResponse(int nMessageType, int CurrentAgreementIdx[], String sOriginalMessage)
    {
        // if a partial agreement was agreed in the past, 
        // the current agreement may include only partial
        // value - merge it with previous accepted agreement
        for (int i = 0; i < AutomatedAgentType.MAX_ISSUES; ++i)
        {
            // if value of current issue is "no agreement" or "no value"
            if (CurrentAgreementIdx[i] == AutomatedAgentType.NO_VALUE)
                CurrentAgreementIdx[i] = m_PreviosAcceptedOffer[i];
            else if (m_AgentType.isIssueValueNoAgreement(i, CurrentAgreementIdx[i]))
            {
                // if previous accepted agreement has values
                // for it, copy the value
                if (m_PreviosAcceptedOffer[i] != AutomatedAgentType.NO_VALUE)
                    CurrentAgreementIdx[i] = m_PreviosAcceptedOffer[i];
            }
        }

        m_abstractAgent.calculateResponse(nMessageType, CurrentAgreementIdx, sOriginalMessage);
    }

    public String getAutomatedAgentAgreement() {
        String sAgreement = m_AgentCore.getAutomatedAgentAgreement();
        return sAgreement;
    }
    
    public String formatMessage(int message, String sMessage) {
        String formattedMessage = m_messages.formatMessage(message, sMessage);
        return formattedMessage;
    }
    
    public double getAgreementValue(int[] agreementIndices) {
        double agreementValue = m_AgentType.getAgreementValue(agreementIndices, m_nCurrentTurn);
        return agreementValue;
    }
    
    public int[] getPreviousAcceptedAgreementsIndices() {
        return m_PreviosAcceptedOffer;
    }
    
    public void calculateNextTurnOffer() {
        m_AgentCore.calculateNextTurnAgreement(m_AgentTypeNextTurn, m_nCurrentTurn + 1);
    }

    public double getNextTurnAutomatedAgentOfferValue() {
        double dAutomatedAgentNextOfferValueForAgent = m_AgentCore.getNextTurnAutomatedAgentUtilityValue();
        return dAutomatedAgentNextOfferValueForAgent;
    }

    public AutomatedAgentType getNextTurnSideAgentType(String sideName, int type) {
        AutomatedAgentType agentType = null;
        if (sideName.equals(AutomatedAgent.SIDE_A_NAME)) {
            switch (type) {
            case AutomatedAgentsCore.COMPROMISE_TYPE_IDX:
                agentType = m_AgentCore.getSideACompromiseNextTurnType();
                break;
            case AutomatedAgentsCore.LONG_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideALongTermNextTurnType();
                break;
            case AutomatedAgentsCore.SHORT_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideAShortTermNextTurnType();
                break;
            default:
                System.err.println("[AA]ERROR----" + "Wrong type for agent: " + type + " [AutomatedAgent::getNextTurnSideAgentType(585)]");
                break;
            }
        } else if (sideName.equals(AutomatedAgent.SIDE_B_NAME)) {
            switch (type) {
            case AutomatedAgentsCore.COMPROMISE_TYPE_IDX:
                agentType = m_AgentCore.getSideBCompromiseNextTurnType();
                break;
            case AutomatedAgentsCore.LONG_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideBLongTermNextTurnType();
                break;
            case AutomatedAgentsCore.SHORT_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideBShortTermNextTurnType();
                break;
            default:
                System.err.println("[AA]ERROR----" + "Wrong type for agent: " + type + " [AutomatedAgent::getNextTurnSideAgentType(600)]");
                break;
            }
        } else {                   
            System.out.println("[AA]Agent type is unknown [AutomatedAgent::getNextTurnSideAgentType(604)]");
            System.err.println("[AA]Agent type is unknown [AutomatedAgent::getNextTurnSideAgentType(604)]");
            return null;
        }
        
        return agentType;
    }

    public AutomatedAgentType getCurrentTurnSideAgentType(String sideName, int type) {
        AutomatedAgentType agentType = null;
        if (sideName.equals(AutomatedAgent.SIDE_A_NAME)) {
            switch (type) {
            case AutomatedAgentsCore.COMPROMISE_TYPE_IDX:
                agentType = m_AgentCore.getSideACompromiseType();
                break;
            case AutomatedAgentsCore.LONG_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideALongTermType();
                break;
            case AutomatedAgentsCore.SHORT_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideAShortTermType();
                break;
            default:
                System.err.println("[AA]ERROR----" + "Wrong type for agent: " + type + " [AutomatedAgent::getCurrentTurnSideAgentType(622)]");
                break;
            }
        } else if (sideName.equals(AutomatedAgent.SIDE_B_NAME)) {
            switch (type) {
            case AutomatedAgentsCore.COMPROMISE_TYPE_IDX:
                agentType = m_AgentCore.getSideBCompromiseType();
                break;
            case AutomatedAgentsCore.LONG_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideBLongTermType();
                break;
            case AutomatedAgentsCore.SHORT_TERM_TYPE_IDX:
                agentType = m_AgentCore.getSideBShortTermType();
                break;
            default:
                System.err.println("[AA]ERROR----" + "Wrong type for agent: " + type + " [AutomatedAgent::getCurrentTurnSideAgentType(637)]");
                break;
            }
        } else {                   
            System.out.println("[AA]Agent type is unknown [AutomatedAgent::getCurrentTurnSideAgentType(642)]");
            System.err.println("[AA]Agent type is unknown [AutomatedAgent::getCurrentTurnSideAgentType(642)]");
            return null;
        }
        
        return agentType;
    }

    public double getNextTurnAutomatedAgentValue() {
        return m_AgentCore.getNextTurnAutomatedAgentValue();
    }

    public double getCurrentTurnAutomatedAgentValue() {
        return m_AgentCore.getCurrentTurnAutomatedAgentValue();
    }
    
    public void setNextTurnAutomatedAgentValue(double value) {
        m_AgentCore.setNextTurnAutomatedAgentValue(value);
    }
    
    public void setCurrentTurnAutomatedAgentValue(double value) {
        m_AgentCore.setCurrentTurnAutomatedAgentValue(value);
    }

    public void setNextTurnAutomatedAgentSelectedValue(double agreementValue) {
        m_AgentCore.setNextTurnAutomatedAgentSelectedValue(agreementValue);
    }

    public void setNextTurnOpponentSelectedValue(double agreementValue) {
        m_AgentCore.setNextTurnOpponentSelectedValue(agreementValue);
    }
    
    public void setCurrentTurnOpponentSelectedValue(double agreementValue) {
        m_AgentCore.setCurrentTurnOpponentSelectedValue(agreementValue);
    }

    public void setNextTurnAgreementString(String agreementStr) {
        m_AgentCore.setNextTurnAgreementString(agreementStr);        
    }
    
    public void setCurrentTurnAgreementString(String agreementStr) {
        m_AgentCore.setCurrentTurnAgreementString(agreementStr);        
    }

    public void setNextTurnOpponentType(int type) {
        m_AgentCore.setNextTurnOpponentType(type);        
    }

    public String getAgreementStr(int[] currentAgreementIdx) {
        return m_AgentType.getAgreementStr(currentAgreementIdx);
    }

    public String getBestAgreementStr() {
        return m_AgentType.getBestAgreementStr();        
    }
    
    public double getBestAgreementValue() {
        return m_AgentType.getBestAgreementValue(m_nCurrentTurn);
    }
    
    public String getWorstAgreementStr() {
        return m_AgentType.getWorstAgreementStr();        
    }
    
    public double getWorstAgreementValue() {
        return m_AgentType.getWorstAgreementValue(m_nCurrentTurn);
    }

    public double getStatusQuoValue() {
        return m_AgentType.getSQValue();
    }

    public double getOptingOutValue() {
        return m_AgentType.getOptOutValue();
    }
    
    public int getTotalAgreementsNum() {
        return m_AgentType.getTotalAgreements();
    }
    
    public int getTotalIssues() {
        return m_AgentType.getIssuesNum();
    }

    public boolean getSendOfferFlag() {
        return m_bSendOffer ;
    }
    
    public void setSendOfferFlag(boolean flag) {
        m_bSendOffer = flag;
    }
}
