package agents.qoagent;

/**
 * @author raz
 * This class should serve as an auxiliary class
 * The methods should not be changed/revised
 *
 */ 
public class AgentTools {
    AutomatedAgent agent = null;
    
    /**
     * Constructor
     * Save a pointer to the AutomatedAgent class
     * @param agent - pointer to the AutomatedAgent class
     */
    public AgentTools(AutomatedAgent agent) {
        this.agent = agent;
    }

    /***********************************************
     * @@ Logic for sending messages
     * Below are messages the automated agent sends to the opponent
     * Call them from the AbstractAutomatedAgent class, and also
     * add any logic you need in that class, just before calling them
     * Do not add the logic in this class!
     ***********************************************/  
    
    /**
     * Called when you want to accept a message
     * @param sOriginalMessage - the message to be accepted
     */
	public void acceptMessage(String sOriginalMessage) {
        String sAcceptMsg = agent.formatMessage(AutomatedAgentMessages.ACCEPT, sOriginalMessage);
        
        //createFrom a thread to send delayed message
        // You can choose how much time the agent should wait before accepting the opponent's offer.
        // The delay time is defined the AutomatedAgentDelayedMessageThread class.
         AutomatedAgentDelayedMessageThread delayedMessageThread = new AutomatedAgentDelayedMessageThread(agent, sAcceptMsg);
        delayedMessageThread.start();           
    }

    /**
     * Called when you want to reject a message
     * @param sOriginalMessage - the message to be rejected
     */
    public void rejectMessage(String sOriginalMessage) {
        String sRejectMsg = agent.formatMessage(AutomatedAgentMessages.REJECT, sOriginalMessage);
        
        //createFrom a thread to send delayed message
        // You can choose how much time the agent should wait before sending the rejection to the opponent.
        // The delay time is defined the AutomatedAgentDelayedMessageThread class.
        AutomatedAgentDelayedMessageThread delayedMessageThread = new AutomatedAgentDelayedMessageThread(agent, sRejectMsg);
        delayedMessageThread.start();      
    }

    /**
     * Called when you want to send a message of type offer, counter offer, promise or query
     * @param currentAgreementIdx - the indices of the message
     */
    public void sendMessage(int nMessageType, int currentAgreementIdx[]) {
        String sMessage = getMessageByIndices(currentAgreementIdx);
        sMessage = agent.formatMessage(nMessageType, sMessage);
 
        //createFrom a thread to send delayed message
        // You can choose how much time the agent should wait before sending the message to the opponent.
        // The delay time is defined the AutomatedAgentDelayedMessageThread class.
        AutomatedAgentDelayedMessageThread delayedMessageThread = new AutomatedAgentDelayedMessageThread(agent, sMessage, agent.getCurrentTurn());
        delayedMessageThread.start();
    }
    
    public void optOut() {
        String sMessage = agent.formatMessage(AutomatedAgentMessages.OPT_OUT, "");
        agent.printMessageToServer(sMessage);
    }
    
    /**
     * Called when you want to send a message of type offer, counter offer, promise or query
     * @param sMessage - the message as a String
     */
   public void sendMessage(int nMessageType, String sMessage) {
        sMessage = agent.formatMessage(nMessageType, sMessage);
        
        //createFrom a thread to send delayed message
        // You can choose how much time the agent should wait before sending the message to the opponent.
        // The delay time is defined the AutomatedAgentDelayedMessageThread class.
        AutomatedAgentDelayedMessageThread delayedMessageThread = new AutomatedAgentDelayedMessageThread(agent, sMessage, agent.getCurrentTurn());
        delayedMessageThread.start();
    }

    
    /**
     * Called when you want to send an offer
     * @param sOffer - the offer to be sent
     */
    public void sendOffer(String sOffer) {
        setSendOfferFlag(true);
        sendMessage(AutomatedAgentMessages.OFFER, sOffer);
    }

    /**
     * Called when you want to send a query
     * @param currentAgreementIdx - the indices of the query
     */
 	public void sendQuery(int currentAgreementIdx[]) {
       setSendOfferFlag(true);
	    sendMessage(AutomatedAgentMessages.QUERY, currentAgreementIdx);
    }
    
    /**
     * Called when you want to send a promise
     * @param currentAgreementIdx - the indices of the promise
     */
 	public void sendPromise(int currentAgreementIdx[]) {
       setSendOfferFlag(true);
        sendMessage(AutomatedAgentMessages.PROMISE, currentAgreementIdx);        
    }
    
    /**
     * Called when you want to send a counter offer
     * @param currentAgreementIdx - the indices of the counter offer
     */
 	public void sendCounterOffers(int currentAgreementIdx[]) {
       setSendOfferFlag(true);
        sendMessage(AutomatedAgentMessages.COUNTER_OFFER, currentAgreementIdx);        
    }

    /**
     * Called when you want to send a comment
     * @param sMessage - the comment to be sent
     */
    public void sendComment(String sMessage) {
        sMessage = agent.formatMessage(AutomatedAgentMessages.COMMENT, sMessage);
        agent.printMessageToServer(sMessage);  
    }
    
    /**
     * Called when you want to send a threat
     * @param sMessage - the threat to be sent
     */
 	public void sendThreat(String sMessage) {
       sMessage = agent.formatMessage(AutomatedAgentMessages.THREAT, sMessage);
       agent.printMessageToServer(sMessage);  
    }
    /***********************************************
     * @@ End of methods for sending message
     ***********************************************/
    
	// helper function
    /**
     * Get the total number of turns in the negotiation
     * @return total number of turns
     */	
    public int getTurnsNumber() {
     return agent.getMaxTurns();   
    }
    
    /**
     * Get the current turn number
     * @return the current turn
     */
    public int getCurrentTurn() {
     return agent.getCurrentTurn();   
    }
	
	// utility functions
    /**
     * @param agentType - the agent's type
     * @param CurrentAgreementIdx - the agreement indices
     * @param nCurrentTurn - the current turn for calculations
     * @return the value of a given agreement for the agent at a given turn
     */
    public double getAgreementValue(AutomatedAgentType agentType, int[] CurrentAgreementIdx, int nCurrentTurn) {
        return agentType.getAgreementValue(CurrentAgreementIdx, nCurrentTurn);
    }
    
    
    /**
     * Return the best agreement as string
     * @param agentType - the agent's type
     * @return the best agreement as String
     */
    public String getBestAgreementStr(AutomatedAgentType agentType) {
        return agentType.getBestAgreementStr();
    }

    /**
     * Return the best agreement value for a given agent
     * @param agentType - the agent's type
     * @return the best agreement value computed for the current turn
     */
    public double getBestAgreementValue(AutomatedAgentType agentType) {
        return agentType.getBestAgreementValue();
    }
    
    /**
     * Return the worst agreement as a String
     * @param agentType - the agent's type
     * @return the worst agreement as String
     */
    public String getWorstAgreementStr(AutomatedAgentType agentType) {
        return agentType.getWorstAgreementStr();
    }

    /**
     * Return the worst agreement for a given agent
     * @param agentType - the agent's type
     * @return the worst agreement value computed for the current turn
     */
    public double getWorstAgreementValue(AutomatedAgentType agentType) {
        return agentType.getWorstAgreementValue();
    }

    /**
     * Sets the best agreement value for a given agent
     * @param agentType - the agent's type
     * @param value - the value
     */
    public void setBestAgreementValue(AutomatedAgentType agentType, double value) {
        agentType.setBestAgreementValue(value);
    }

    /**
     * Sets the best agreement indices for a given agent
     * @param agentType - the agent's type
     * @param currentAgreementIdx - the agreement indices
     */
    public void setBestAgreementIndices(AutomatedAgentType agentType, int[] currentAgreementIdx) {
        agentType.setBestAgreementIndices(currentAgreementIdx);
    }
    
    /**
     * Sets the worst agreement value for a given agent
     * @param agentType - the agent's type
     * @param value - the value
     */
    public void setWorstAgreementValue(AutomatedAgentType agentType, double value) {
        agentType.setWorstAgreementValue(value);
    }

    /**
     * Sets the worst agreement indices for a given agent
     * @param agentType - the agent's type
     * @param currentAgreementIdx - the agreement indices
     */
    public void setWorstAgreementIndices(AutomatedAgentType agentType, int[] currentAgreementIdx) {
        agentType.setWorstAgreementIndices(currentAgreementIdx);        
    }
    /**
     * Initializes the best agreement - 
     * inits the indices and sets minimal value
     * @param agentType - the agent's type
     */
    public void initializeBestAgreement(AutomatedAgentType agentType) {
        agentType.setBestAgreementValue(AutomatedAgentType.VERY_SMALL_NUMBER);
        agentType.initializeBestAgreementIndices();
    }

    /**
     * Initializes the worst agreement - 
     * inits the indices and sets maximal value
     * @param agentType - the agent's type
     */
    public void initializeWorstAgreement(AutomatedAgentType agentType) {
        agentType.setWorstAgreementValue(AutomatedAgentType.VERY_HIGH_NUMBER);
        agentType.initializeWorstAgreementIndices();
        
    }

    /**
     * Return the time effect for the entire agreement
     * @param agentType - the agent's type
     * @return the time effect for the entire agreement
     */
   public double getAgreementTimeEffect(AutomatedAgentType agentType) {
        return agentType.getAgreementTypeEffect();
    }

    /**
     * Return the SQ value for a given agent
     * @param agentType - the agent's type
     * @return the status quo value computed for the current turn
     * for a given agent type
     */
    public double getSQValue(AutomatedAgentType agentType) {
        return agentType.getSQValue();
    }

    /**
     * Return the opting out value for a given agent
     * @param agentType - the agent's type
     * @return the opting out value computed for the current turn
     */
    public double getOptOutValue(AutomatedAgentType agentType) {
        return agentType.getOptOutValue();
    }
    
    /**
     * @return the total number of agreement
     */
    public int getTotalAgreements(AutomatedAgentType agentType) {
        return agentType.getTotalAgreements();
    }
 
    
    /**
     * Set the automated agent type
     * Possible types: COMPROMISE_TYPE, SHORT_TERM_TYPE and LONG_TERM_TYPE 
     *
     */
    public void setAutomatedAgentType(String side) {
        // @@EXAMPLE@@
        // using the short term type for the automated agent, 
        // no matter which side it plays
        agent.setAgentType(side, AutomatedAgentsCore.LONG_TERM_TYPE_IDX);
    }
    
    public String getAgentSide() {
        return agent.getAgentSide();
    }
    
    /**
     * 
     * @return the selected offer of the agent at a given turn
     */
    public String getSelectedOffer() {
        String sAutomatedAgentAgreement = agent.getAutomatedAgentAgreement();

        return sAutomatedAgentAgreement;
    }

    /**
     * 
     * @return the value of the selected offer of the agent at a given turn
     */
    public double getSelectedOfferValue() {
        String sAutomatedAgentAgreement = agent.getAutomatedAgentAgreement();

        int nextAgreementIndices[] = new int[AutomatedAgentType.MAX_ISSUES];
        nextAgreementIndices = agent.getAgreementIndices(sAutomatedAgentAgreement);
        
        double dNextAgreementValue = agent.getAgreementValue(nextAgreementIndices);
        
        return dNextAgreementValue;
    }
    
    /**
     * 
     * @return the value of previously accepted agreement
     */
    public double getAcceptedAgreementsValue() {
        // The accepted agreement is saved in agent.m_PreviosAcceptedOffer
        // Note: agreements can be incremental. The m_PreviosAcceptedOffer saves the whole agreement
        int previousAcceptedAgreementsIndices[] = new int[AutomatedAgentType.MAX_ISSUES];
        previousAcceptedAgreementsIndices = agent.getPreviousAcceptedAgreementsIndices();
     
        double dAcceptedAgreementValue = agent.getAgreementValue(previousAcceptedAgreementsIndices);
        
        return dAcceptedAgreementValue;

    }
    
    public int[] getAcceptedAgreementIdx() {
        // The accepted agreement is saved in agent.m_PreviosAcceptedOffer
        // Note: agreements can be incremental. The m_PreviosAcceptedOffer saves the whole agreement
        int previousAcceptedAgreementsIndices[] = new int[AutomatedAgentType.MAX_ISSUES];
        previousAcceptedAgreementsIndices = agent.getPreviousAcceptedAgreementsIndices();
        
        return previousAcceptedAgreementsIndices;
        
    }

    /**
     * calculate the selected offer the agent will propose
     * in the following turn 
     *
     */
    public void calculateNextTurnOffer() {
        agent.calculateNextTurnOffer();
    }

    /**
     * 
     * @return the value of the selected offer the agent will propose
     * in the following turn 
     */
    public double getNextTurnOfferValue() {
        double dAutomatedAgentNextOfferValueForAgent = agent.getNextTurnAutomatedAgentOfferValue();
        return dAutomatedAgentNextOfferValueForAgent;
    }
    
    /**
     * Iterator for going over all possible agreements
     * @param totalIssuesNum - the total number of issues in the negotiatoin
     * @param currentAgreementIdx - the current agreement indices
     * @param maxIssueValues - the maximal issue value
     */
    public void getNextAgreement(int totalIssuesNum, int[] currentAgreementIdx, int[] maxIssueValues) {
        //TODO:DEBUG THIS
        // receiveMessage issue values indices for evaluating the next agreement
        boolean bFinishUpdate = false;
        for (int k = totalIssuesNum-1; k >= 0 && !bFinishUpdate; --k)
        {
            if (currentAgreementIdx[k]+1 >= maxIssueValues[k])
            {
                currentAgreementIdx[k] = 0;
            }
            else
            {
                currentAgreementIdx[k]++;
                bFinishUpdate = true;
            }                                   
        }
    }

    /**
     * Get the opponent's side
     * @param sideName - the type of side (A or B)
     * @param type - the type (compromise, short, long)
     * @return
     */
    public AutomatedAgentType getNextTurnSideAgentType(String sideName, int type) {
        AutomatedAgentType agentType = null;
        agentType = agent.getNextTurnSideAgentType(sideName, type);
        return agentType;
    }
    
    /**
     * Get the opponent's side
     * @param sideName - the type of side (A or B)
     * @param type - the type (compromise, short, long)
     * @return
     */
    public AutomatedAgentType getCurrentTurnSideAgentType(String sideName, int type) {
        AutomatedAgentType agentType = null;
        agentType = agent.getCurrentTurnSideAgentType(sideName, type);
        return agentType;
    }

    /**
     * 
     * @return the value of the selected offer for the next turn
     */
    public double getNextTurnAutomatedAgentValue() {
        return agent.getNextTurnAutomatedAgentValue();
    }

    /**
     * 
     * @return the value of the selected offer for the current turn
     */
    public double getCurrentTurnAutomatedAgentValue() {
        return agent.getCurrentTurnAutomatedAgentValue();
    }

   
    
    /**
     * Sets the value of the selected offer for the current turn
     * @param agreementValue - the agreement's value
     */
    public void setCurrentTurnAutomatedAgentValue(double agreementValue) {
        agent.setCurrentTurnAutomatedAgentValue(agreementValue);
    }

    /**
     * Sets the value of the selected offer for the following turn
     * @param agreementValue - the agreement's value
     */
    public void setNextTurnAutomatedAgentSelectedValue(double agreementValue) {
        agent.setNextTurnAutomatedAgentSelectedValue(agreementValue);
    }
    
    /**
     * Sets the value of the selected offer for the following turn
     * for the opponent
     * @param agreementValue - the agreement's value
     */
    public void setNextTurnOpponentSelectedValue(double agreementValue) {
        agent.setNextTurnOpponentSelectedValue(agreementValue);
    }
    
    /**
     * Sets the value of the selected offer for the current turn
     * for the opponent
     * @param agreementValue - the agreement's value
     */
    public void setCurrentTurnOpponentSelectedValue(double agreementValue) {
        agent.setCurrentTurnOpponentSelectedValue(agreementValue);
    }

    /**
     * Sets the String of the selected offer for the following turn
     * @param agreementStr - the agreement as String
     */
    public void setNextTurnAgreementString(String agreementStr) {
        agent.setNextTurnAgreementString(agreementStr);        
    }
    
    /**
     * Sets the String of the selected offer for the current turn
     * @param agreementStr - the agreement as String
     */
    public void setCurrentTurnAgreementString(String agreementStr) {
        agent.setCurrentTurnAgreementString(agreementStr);        
    }

    /**
     * Sets the side of the opponent (Side A or B)
     * @param agreementStr - the agreement as String
     */
    public void setNextTurnOpponentType(int type) {
        agent.setNextTurnOpponentType(type);        
    }

    /**
     * Calculating the response to a given proposal.
     * This method eventually calls AbstractAutomatedAgent.calculateResponse()
     * @see AbstractAutomatedAgent#calculateResponse
     *
     */
    public void calculateResponse(int messageType, int[] currentAgreementIdx, String message) {
        agent.calculateResponse(messageType, currentAgreementIdx, message);
    }
    
    /**
     * @return string of a given agreement for the current agent
     */
    public String getMessageByIndices(int[] currentAgreementIdx) {
        return agent.getAgreementStr(currentAgreementIdx);
    }
    
    /**
     * @return indices of a given agreement for the current agent
     */
    public int[] getMessageIndicesByMessage(String currentAgreementStr) {
        return agent.getAgreementIndices(currentAgreementStr);
    }

    /**
     * @return value of a given agreement for the current agent
     */
    public double getAgreementValue(int[] currentAgreementIdx) {
        double dAgreementValue = agent.getAgreementValue(currentAgreementIdx);
        return dAgreementValue;
    }
    
    /**
     * 
     * @return the total number of seconds per each turn
     */
    public double getSecondPerTurn() {
        return agent.getSecondsForTurn();
    }

    /**
     * 
     * @return the total number of issues for negotiation
     */
    public int getTotalIssues(AutomatedAgentType agentType) {
        return agentType.getIssuesNum();
    }

    /**
     * return the maximal value for the agent for issue i
     * @param agentType - the agent's type
     * @param issueNum - the issue number
     * @return the maximal value per that issue
     */
    public int getMaxValuePerIssue(AutomatedAgentType agentType, int issueNum) {
        return agentType.getMaxIssueValue(issueNum);
    }
    
    /**
     * 
     * @return whether the flag for sending offers/queries/promises
     * is true or not
     */
    public boolean getSendOfferFlag() {
        return agent.getSendOfferFlag();
    }

    /**
     * Sets the boolean flag of sending offers/queries/promises
     * @param flag - true if wanting to send message, false - o/w
     */
    public void setSendOfferFlag(boolean flag) {
        agent.setSendOfferFlag(flag);
    }
}
