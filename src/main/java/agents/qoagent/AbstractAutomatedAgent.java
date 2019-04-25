package agents.qoagent;

/**
 * @author raz
 * This class should hold all your logic for your automated agent
 * Examples are provided inline and marked as examples
 *
 */ 
public class AbstractAutomatedAgent {
    AgentTools agentTools = null;
    
    public AbstractAutomatedAgent() {}
    
    /**
     * Constructor
     * Save a pointer to the AgentTools class
     * @param agentTools - pointer to the AgentTools class
     */
    public AbstractAutomatedAgent(AgentTools agentTools) {
        this.agentTools = agentTools;
    }
    
	/**
	 * Called before the the nagotiation starts.
	 * Add any logic you need here.
     * For example, calculate the very first offer you'll
     * offer the opponent 
     * @param agentType - the automated agent
	 */
	public void initialize(AutomatedAgentType agentType, String sOpponentType) {
        /* Negotiation is about to start
         * You can add logic if needed to receiveMessage your agent
         * @@EXAMPLE@@
         * For example: calculate the first offer the 
         * automated agent offers the opponent and send it
         */
        
        /********************************
         * Start example code
         ********************************/
        // calculate Automated Agent first offer
        calculateOfferAgainstOpponent(agentType, sOpponentType, 1);
        /********************************
         * End example code
         ********************************/
    }
    
    /** 
     * Called when a message of type:
     * QUERY, COUNTER_OFFER, OFFER or PROMISE 
     * is received
     * Note that if you accept a message, the accepted message is saved in the 
     * appropriate structure, so no need to add logic for this.
     * @param nMessageType - the message type
     * @param CurrentAgreementIdx - the agreement indices
     * @param sOriginalMessage - the message itself as string
     */
    public void calculateResponse(int nMessageType, int CurrentAgreementIdx[], String sOriginalMessage) {
        //Calculating the response
        //You can decide on your actions for that turn
        //You can decide on different logic based on the message type
        //In case you accept an offer, you might decide NOT to
        //send an offer you calculated before and just waited for
        //it to be sent. To do so, use the "send flag" as in
        //the example below
        //@@EXAMPLE@@
        //For example: 
        //1 - if the newly offer has lower utility values than already
        //accepted agreement, reject it;
        //2 - if the automated agent is going to propose an offer with lower utility values to
        //it in the next turn, accept the opponent's offer and
        //don't send any offer of your own
        //3 - else, always accept

        /********************************
         * Start example code
         ********************************/
        // decide whether to accept the message or reject it:
        double dOppOfferValueForAgent = AutomatedAgentType.VERY_SMALL_NUMBER;
        double dAutomatedAgentNextOfferValueForAgent = AutomatedAgentType.VERY_SMALL_NUMBER;

        // Check the utility value of the opponent's offer
        dOppOfferValueForAgent = agentTools.getAgreementValue(CurrentAgreementIdx); 
            
        // 1. check whether previous accepted agreement is better - if so, reject
        double dAcceptedAgreementValue = agentTools.getAcceptedAgreementsValue(); 
        
        if (dAcceptedAgreementValue >= dOppOfferValueForAgent)
        {
            // reject offer
            agentTools.rejectMessage(sOriginalMessage);
            return;
        }
        
        // 2. check the value of the automated agent in the next turn
        agentTools.calculateNextTurnOffer();
        dAutomatedAgentNextOfferValueForAgent = agentTools.getNextTurnOfferValue();

        if (dOppOfferValueForAgent >= dAutomatedAgentNextOfferValueForAgent)
        {
            // accept offer
            agentTools.acceptMessage(sOriginalMessage);
            
            //prevent sending future offer in this turn
            agentTools.setSendOfferFlag(false);

            /* @@ You accepted opponent's message
             * The automated agent accepts a messsage here
             * You can add logic if needed to receiveMessage your agent
             * Note: The accepted message is saved in the 
             * appropriate structure. No need to add logic for this 
             */
        }
        else
        {
            // accept offer
            agentTools.acceptMessage(sOriginalMessage);
            
            //prevent sending future offer in this turn
            agentTools.setSendOfferFlag(false);
            
            /* @@ You accepted opponent's message
             * The automated agent accepts a messsage here
             * You can add logic if needed to receiveMessage your agent
             * Note: The accepted message is saved in the 
             * appropriate structure. No need to add logic for this 
             */
        }
        /********************************
         * End example code
         ********************************/
    }
        
    /***********************************************
     * @@ Logic for receiving messages
     * Below are messages the opponent sends to the automated agent
     * You can add logic if needed to receiveMessage your agent per message type
     ***********************************************/
    
    /**
	 * called whenever we get a comment from the opponent
     * You can add logic to receiveMessage your agent
     * @param sComment -the received comment
	 */
	public void commentReceived(String sComment) {
        /* @@ Received a comment from the opponent
         * You can add logic if needed to receiveMessage your agent
         */
    }

	/**
	 * called whenever we get a threat from the opponent
     * You can add logic to receiveMessage your agent
     * @param sThreat - the received threat
	 */
	public void threatReceived(String sThreat) {
        /* @@ Received a threat from the opponent
         * You can add logic if needed to receiveMessage your agent
         */
    }
	
	/**
	 * called whenever the opponent agreed to one of your massages (promise, query, offer or counter offer).
     * NOTE: if an OFFER is accepted, it is saved in the appropriate structure. No need to add logic for this.
	 * @param nMessageType - the type of massage the oppnent aggreed to, can be
     * AutomatedAgentMessages.PROMISE, QUERY, OFFER, COUNTER_OFFER
	 * @param CurrentAgreementIdx - the indices of the agreement the opponent agreed to
     * @param sOriginalMessage - the original message that was accepted
	 */
	public void opponentAgreed(int nMessageType, int CurrentAgreementIdx[], String sOriginalMessage) {
        /* @@ Received a message: opponent accepted the offer/promise/query/counter offer.
        * You can add logic if needed to receiveMessage your agent
        * For example, if the message was a promise, you can now try and offer it as
        * a formal offer...
        */
    }
	
	/**
	 * called whenever the opponent rejected one of your massages (promise, query, offer or counter offer)
	 * @param nMessageType - the type of massage the oppnent rejected, can be
     * AutomatedAgentMessages.PROMISE, QUERY, OFFER, COUNTER_OFFER
	 * @param CurrentAgreementIdx - the indices of the agreement the opponent agreed to
     * @param sOriginalMessage - the original message that was rejected
	 */
	public void opponentRejected(int nMessageType, int CurrentAgreementIdx[], String sOriginalMessage) {
        /* @@ Received a message: opponent rejected the offer/promise/query/counter offer.
         * You can add logic if needed to receiveMessage your agent
         */
    }
    
    /***********************************************
     * @@ End of methods for receiving message
     ***********************************************/
 
    
    /**
     * called to decide which offer to propose the opponent at a given turn
     * This method is always called when beginning a new turn
     * You can also call it during the turn if needed
     * @param agentType - the automated agent's type
     * @param sOpponentType - the opponent's type
     * @param nCurrentTurn - the current turn
     */
    public void calculateOfferAgainstOpponent(AutomatedAgentType agentType, String sOpponentType, int nCurrentTurn) {
        //@@ Add any logic to calculate offer (or several offers)
        // and decide which to send to the opponent in a given turn
        
        /** @@EXAMPLE@@
         * In the following example, ONE offer is chosen to be
         * send to the opponent based on the following logic:
         * It will be sent only if it has a value higher than
         * an offer already accepted.
         * 
         * You can see in the example how to:
         * a) obtain the different possible types of opponent
         * b) get the total number of issues in the negotiation
         * c) get the total number of agreements in the negotiation
         * d) get the maximal value of a certain issue for each agent
         * e) go over all possible agreements and evaluate them
         * f) compare the agreement to previously accepted agreement
         * g) save one offer for later references
         * 
         * 
        /********************************
         * Start example code
         ********************************/
        // calculate Automated Agent offer
        double dCurrentAgentAgreementValue = AutomatedAgentType.VERY_SMALL_NUMBER;
        
        int totalIssuesNum = agentTools.getTotalIssues(agentType);
        int totalAgreementsNumber = agentTools.getTotalAgreements(agentType);

        int CurrentAgreementIdx[] = new int[totalIssuesNum];
        int MaxIssueValues[] = new int[totalIssuesNum];
        
        for (int i = 0; i < totalIssuesNum; ++i)
        {
            CurrentAgreementIdx[i] = 0;
            MaxIssueValues[i] = agentTools.getMaxValuePerIssue(agentType, i);
        }
        
        // the different possible agents for the opponent side
        AutomatedAgentType agentOpponentCompromise = null;
        AutomatedAgentType agentOpponentLongTerm = null;
        AutomatedAgentType agentOpponentShortTerm = null;

        agentOpponentCompromise = agentTools.getCurrentTurnSideAgentType(sOpponentType, AutomatedAgentsCore.COMPROMISE_TYPE_IDX);
        agentOpponentLongTerm = agentTools.getCurrentTurnSideAgentType(sOpponentType, AutomatedAgentsCore.LONG_TERM_TYPE_IDX);
        agentOpponentShortTerm = agentTools.getCurrentTurnSideAgentType(sOpponentType, AutomatedAgentsCore.SHORT_TERM_TYPE_IDX);                
      
        // Now, we go over all the possible agreements,
        // First, we calculate the value of each agreement for the automated agent;
        // Then, we calculate the value of each such agreement for the different possible opponent types
        // We only save the last value calculated.
        // You can change this logic, of course...        

        //In this example, we only calculate for the long term orientation
        int OpponentLongTermIdx[] = new int[totalIssuesNum];
        double dOpponentLongTermAgreementValue = AutomatedAgentType.VERY_SMALL_NUMBER;

        double dAutomatedAgentAgreementValue = AutomatedAgentType.VERY_SMALL_NUMBER;
        
        for (int i = 0; i < totalAgreementsNumber; ++i)
        {
            dAutomatedAgentAgreementValue = agentTools.getAgreementValue(agentType, CurrentAgreementIdx, nCurrentTurn);

            // @@ You can add here logic regarding how to calculate agreements
            // based on the different possible agent types (long/short/compromise orientation
            //@@EXAMPLE@@
            //Calculating the agreement values for the long term opponent
            //saving all the time the last calculated agreement (no logic...)

            dOpponentLongTermAgreementValue = agentTools.getAgreementValue(agentOpponentLongTerm, CurrentAgreementIdx, nCurrentTurn);
            // save the indices of that offer
            for (int j = 0; j < totalIssuesNum; ++j) {
              OpponentLongTermIdx[j] = CurrentAgreementIdx[j];
            }

            agentTools.getNextAgreement(totalIssuesNum, CurrentAgreementIdx, MaxIssueValues);// get the next agreement indices
        } // end for - going over all possible agreements
        
        //select which offer to propose 
        //In this example, selecting the last offer that was calculated
        if (dOpponentLongTermAgreementValue > agentTools.getCurrentTurnAutomatedAgentValue())
        {
            // you can save the values for later reference ($1)
            agentTools.setCurrentTurnAutomatedAgentValue(dOpponentLongTermAgreementValue);
            agentTools.setCurrentTurnOpponentSelectedValue(agentOpponentLongTerm.getAgreementValue(OpponentLongTermIdx, nCurrentTurn));
            agentTools.setCurrentTurnAgreementString(agentType.getAgreementStr(OpponentLongTermIdx));
        }

        // Now, the agent's core holds the new selected agreement
        
        // check the value of the offer (the one saved before, see $1...)
        double dNextAgreementValue = agentTools.getSelectedOfferValue();

        // get the value of previously accepted agreement
        double dAcceptedAgreementValue = agentTools.getAcceptedAgreementsValue(); 
        
        // Now, check whether the offer the agent intends to propose in the next turn is better
        // for it than previously accepted agreement
        
        // if the value of the offer is lower than already accepted offer, don't send it...
        if (dAcceptedAgreementValue >= dNextAgreementValue)
        {
            // default behavior is to send offer
            // however... now we don't want to send the offer
            // previously accepted offer has better score
            
            // so - don't send the offer
        }
        
        // if decided to send offer - then send the offer
        //Get the offer as string and format it as an offer
        String sOffer = agentTools.getSelectedOffer();
        agentTools.sendOffer(sOffer);
        /********************************
         * End example code
         ********************************/
    }
    
    /**
     * called to calculate the values of the different possible agreements for the agent
     * @param agentType - the automated agent's type
     * @param nCurrentTurn - the current turn
     */
    public void calculateValues(AutomatedAgentType agentType, int nCurrentTurn) {
        //Calculate agreements values for a given turn

        // initialization - DO NOT CHANGE
        int nIssuesNum = agentTools.getTotalIssues(agentType);
        
        int CurrentAgreementIdx[] = new int[nIssuesNum];
        int MaxIssueValues[] = new int[nIssuesNum];

        int totalAgreementsNumber = agentTools.getTotalAgreements(agentType);

        for (int i = 0; i < nIssuesNum; ++i)
        {
            CurrentAgreementIdx[i] = 0;
            MaxIssueValues[i] = agentTools.getMaxValuePerIssue(agentType, i);
        }
        //end initialization

        // @@EXAMPLE@@
        // Currently, the method calculates the best agreement, worst agreement
        // and the utility value per agreement
        /********************************
         * Start example code
         ********************************/             
        double dAgreementValue = 0;
        
        agentTools.initializeBestAgreement(agentType);
        agentTools.initializeWorstAgreement(agentType);
        
        //To obtain infromation from the utility you can use getters from the AgentType class
        //@@EXample@@
        //Get the value of the Status Quo and Opting-Out values as time increases
        double dAgreementTimeEffect = agentTools.getAgreementTimeEffect(agentType); 
        double dStatusQuoValue = agentTools.getSQValue(agentType);
        double dOptOutValue = agentTools.getOptOutValue(agentType);
        
        // going over all agreements and calculating the best/worst agreement
        for (int i = 0; i < totalAgreementsNumber; ++i)
        {
            //Note: the agreements are saved based on their indices
            //At the end of the loop the indices are incremeneted
            dAgreementValue = agentTools.getAgreementValue(agentType, CurrentAgreementIdx, nCurrentTurn);
            
            // check for best agreement
            if (dAgreementValue > agentTools.getBestAgreementValue(agentType))
            {
                agentTools.setBestAgreementValue(agentType, dAgreementValue);

                // save agreement
                agentTools.setBestAgreementIndices(agentType, CurrentAgreementIdx);
            }                       
                        
            // check for worst agreement
            if (dAgreementValue < agentType.getWorstAgreementValue())
            {
                agentTools.setWorstAgreementValue(agentType, dAgreementValue);
                
                // save agreement
                agentTools.setWorstAgreementIndices(agentType, CurrentAgreementIdx);
            }                       

            agentTools.getNextAgreement(nIssuesNum, CurrentAgreementIdx, MaxIssueValues);// get the next agreement indices
        } // end for - going over all possible agreements
        /********************************
         * End example code
         ********************************/             
    }
}
