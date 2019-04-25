package agents.qoagent;

import java.util.StringTokenizer;

/*
 * Created on 30/05/2004
 *
 */

/**
 * Responsible for the generation of formatted messages for the 
 * automated agent
 * @author raz
 * @version 1.0
 * @see AutomatedAgent
 * @see AutomatedAgentCommunication
 */
public class AutomatedAgentMessages 
{
	// constants for response of message
	public final static int MESSAGE_RECEIVED = 0;
	public final static int MESSAGE_REJECTED = 1;
	
    //type of messages
	public final static int REGISTER = 0;
	public final static int THREAT = 1;
	public final static int COMMENT = 2;
	public final static int OFFER = 3;
	public final static int PROMISE = 4;
	public final static int QUERY = 5;
	public final static int ACCEPT = 6;
	public final static int REJECT = 7;
	public final static int OPT_OUT = 8;
	public final static int COUNTER_OFFER = 9;
	
	private AutomatedAgent m_agent;
    private AgentTools agentTools = null;
    private AbstractAutomatedAgent abstractAgent = null;
	
	/**
	 * @param agent - saves the AutomatedAgent in the member variable
	 */
	public AutomatedAgentMessages(AutomatedAgent agent, AgentTools agentTools, AbstractAutomatedAgent abstractAgent)
	{
		m_agent = agent;
        this.agentTools = agentTools;
        this.abstractAgent = abstractAgent;
	}

	/**
	 * Formats the message in the predefined structure for sending it
	 * later to the server
	 * @param nMessageKind - the message kind. Can be either:
	 * 	REGISTER, THREAT, COMMENT, OFFER, PROMISE, QUERY, ACCEPT, REJECT, OPT_OUT, COUNTER_OFFER.
	 * @param sMsgBody - the message body: additional data for creating the message.
	 * sMsgBody differs for the different message types
	 * @return the formatted message
	 * @see AutomatedAgent
	 * @see AutomatedAgentCommunication
	 */
	public String formatMessage(int nMessageKind, String sMsgBody)
	{
		String sFormattedMsg = "";
		
		switch (nMessageKind)
		{
			case REGISTER:
			{
				sFormattedMsg = "type register tag " + m_agent.getMsgId() + " id " + 
					sMsgBody + " side " + m_agent.getAgentSide() + " name " +  m_agent.getAgentName() +
					" supportMediator " + m_agent.getSupportMediator() + 
					" preferenceDetails automatedAgent";
				;
			}
				break;			
			case THREAT:
			{
				sFormattedMsg = "type threat" +
				" source " + m_agent.getAgentId() +
				" target " + m_agent.getOpponentAgentId() + 
				" tag " + m_agent.getMsgId() + 
				" body "+ sMsgBody;
			}
				break;
			case COMMENT:
			{
				sFormattedMsg = "type comment" +
				" source " + m_agent.getAgentId() +
				" target " + m_agent.getOpponentAgentId() + 
				" tag " + m_agent.getMsgId() + 
				" body "+ sMsgBody;
			}
				break;
			case OFFER:
			{
				sFormattedMsg = "type offer" +
						" source " + m_agent.getAgentId() +
						" target " + m_agent.getOpponentAgentId() + 
						" tag " + m_agent.getMsgId() + 
						" issueSet ";

				sFormattedMsg += sMsgBody;
			}
				break;
			case COUNTER_OFFER:
			{
				sFormattedMsg = "type counter_offer" +
						" source " + m_agent.getAgentId() +
						" target " + m_agent.getOpponentAgentId() + 
						" tag " + m_agent.getMsgId() + 
						" issueSet ";

				sFormattedMsg += sMsgBody;
			}
				break;
			case PROMISE:
			{
				sFormattedMsg = "type promise" + 
				" source " + m_agent.getAgentId() +
				" target " + m_agent.getOpponentAgentId() + 
				" tag " + m_agent.getMsgId(); 
				
				// build the agent's issue set
				String sAgentPromise = " myIssueSet ";
				
				// NOTE: In our scenario there are no actions
				// that only for one side.
				// We do not use the option of myIssueSet and yourIssueSet
                // this explains the commented lines below
				sAgentPromise += sMsgBody;

				/*
				QPromiseType agentPromise = m_agent.getPromiseList();
 
				String sAttribute, sValue;
				ArrayList agentPromiseList = (ArrayList)agentPromise.agentIssueSet;
				for (int i = 0; i < agentPromiseList.size(); ++i)
				{
					QAttributeValue av = (QAttributeValue)agentPromiseList.get(i);
					sAttribute = av.sAttribute;
					sValue= av.sValue;

					sAgentPromise += sValue + "*" + sAttribute + "*";
				}
				*/
				sFormattedMsg += sAgentPromise + " ";

				// build the opponent's issue set
				String sOpponentPromise = "yourIssueSet ";
				
				/*
				ArrayList opponentPromiseList = (ArrayList)agentPromise.opponentIssueSet;			

				for (int i = 0; i < opponentPromiseList.size(); ++i)
				{
					QAttributeValue av = (QAttributeValue)opponentPromiseList.get(i);
					sAttribute = av.sAttribute;
					sValue= av.sValue;
	
					sOpponentPromise += sValue + "*" + sAttribute + "*";
				}
				*/
				sFormattedMsg += sOpponentPromise;
			}
				break; 
			case QUERY:
			{
				sFormattedMsg = "type query" +
						" source " + m_agent.getAgentId() +
						" target " + m_agent.getOpponentAgentId() + 
						" tag " + m_agent.getMsgId() + 
						" issueSet ";

				sFormattedMsg += sMsgBody;
			}
				break;
			case ACCEPT:
			{
				sFormattedMsg = "type response" + 
				" source " + m_agent.getAgentId() +
				" target " + m_agent.getOpponentAgentId() + 
				" tag " + m_agent.getMsgId() + 
				" answer AGREE" + 
				" message " + sMsgBody + 
				" reason "; // NOTE: No reason is supplied on accept;
				
				// save accepted msg
				String sResponse = sFormattedMsg.substring(sFormattedMsg.indexOf("answer ")+7, sFormattedMsg.indexOf("reason")-1);
				String sMessage = sResponse.substring(sResponse.indexOf("message ") + 8);
				
				// message accepted - save message
				// parse message by its type (offer, promise, query)
				String sSavedMsg = "";
				if (sMessage.startsWith("type query"))
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
				else if (sMessage.startsWith("type counter_offer"))
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
				else if (sMessage.startsWith("type offer"))
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
				else if (sMessage.startsWith("type promise"))
				{
					String sPromise = sMessage.substring(sMessage.indexOf("myIssueSet ") + 11);
					String sMyIssueSet = sPromise.substring(0, sPromise.indexOf("yourIssueSet "));
					String sYourIssueSet = sPromise.substring(sPromise.indexOf("yourIssueSet ") + 13);

					// parse to one agreement
					sSavedMsg = sMyIssueSet + sYourIssueSet;
				}
				
				// only if accepted an offer - save it
				if ( sMessage.startsWith("type counter_offer") || sMessage.startsWith("type offer"))
					m_agent.saveAcceptedMsg(sSavedMsg);
			}
				break;
			case REJECT:
			{
				sFormattedMsg = "type response" + 
				" source " + m_agent.getAgentId() +
				" target " + m_agent.getOpponentAgentId() + 
				" tag " + m_agent.getMsgId() + 
				" answer DISAGREE" + 
				" message " + sMsgBody + 
				" reason "; // NOTE: No reason is supplied
			}
				break;
			case OPT_OUT:
			{
				sFormattedMsg = "type opt-out tag " + m_agent.getMsgId();
			}
				break;
			default:
			{
				System.out.println("[AA]ERROR: Invalid message kind: " + nMessageKind + " [AutomatedAgentMessages::formatMessage(234)]");
				System.err.println("[AA]ERROR: Invalid message kind: " + nMessageKind + " [AutomatedAgentMessages::formatMessage(234)]");
			}
				break;
		}

		return sFormattedMsg;
	}

	/**
	 * Parses messages from the server.
	 * NOTE: There is no validation that this agent is the
	 * target for the message. Assuming correctness of server routing messages.
	 * @param sServerLine - the server's message
	 * @return the parsed string - relevant only if "nak"
	 * @see AutomatedAgent
	 * @see AutomatedAgentCommunication
	 */
	public String parseMessage(String sServerLine)
	{
        String sParsedString = "";
		
		if (sServerLine.startsWith("type comment"))
		{
			String sComment=sServerLine.substring(sServerLine.indexOf(" body ")+6);
			
            abstractAgent.commentReceived(sComment);
		}
		else if (sServerLine.startsWith("type threat"))
		{
			String sThreat=sServerLine.substring(sServerLine.indexOf(" body ")+6);
					
            abstractAgent.threatReceived(sThreat);
		}
		else if (sServerLine.startsWith("type endTurn"))
		{
			// turn ended
			m_agent.incrementCurrentTurn();
		}
		else if (sServerLine.startsWith("type endNegotiation"))
		{
			// negotiation ended
			m_agent.m_gtStopTurn.setRun(false);
			m_agent.m_gtStopNeg.setRun(false);
			
			System.out.println("[AA]Negotiation Ended");
			System.err.println("[AA]Negotiation Ended");
			
			m_agent.endNegotiation();
			
			// NOTE: no need to parse the end reason, agreement
			// 		and score. They are saved in the file.
		}
		else if (sServerLine.startsWith("type response"))
		{
			String sResponse = sServerLine.substring(sServerLine.indexOf("answer ")+7, sServerLine.indexOf("reason")-1);
			String sAnswerType = sResponse.substring(0,sResponse.indexOf(" "));
			String sMessage = sResponse.substring(sResponse.indexOf("message ") + 8);
			
			String sReason = sServerLine.substring(sServerLine.indexOf("reason ") + 7);

			if(sAnswerType.equals("AGREE"))
			{
				// message accepted - save message
				// parse message by its type (offer, promise, query)
				String sSavedMsg = "";
				if (sMessage.startsWith("type query")) {
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
                    abstractAgent.opponentAgreed(QUERY, m_agent.getAgreementIndices(sSavedMsg), sMessage);
                }
				else if (sMessage.startsWith("type counter_offer")) {
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
                    abstractAgent.opponentAgreed(COUNTER_OFFER, m_agent.getAgreementIndices(sSavedMsg), sMessage);
                }
				else if (sMessage.startsWith("type offer")) {
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
                    abstractAgent.opponentAgreed(OFFER, m_agent.getAgreementIndices(sSavedMsg), sMessage);
                }
				else if (sMessage.startsWith("type promise"))
				{
					String sPromise = sMessage.substring(sMessage.indexOf("myIssueSet ") + 11);
					String sMyIssueSet = sPromise.substring(0, sPromise.indexOf("yourIssueSet "));
					String sYourIssueSet = sPromise.substring(sPromise.indexOf("yourIssueSet ") + 13);

					// parse to one agreement
					sSavedMsg = sMyIssueSet + sYourIssueSet;
                    
                    abstractAgent.opponentAgreed(PROMISE, m_agent.getAgreementIndices(sSavedMsg), sMessage);
				}

				// only if accepted an offer (and not promise/query) - save it in the agreed offers
				 if ( sMessage.startsWith("type counter_offer") || sMessage.startsWith("type offer"))
				 	m_agent.saveAcceptedMsg(sSavedMsg);
			}
			else if(sAnswerType.equals("DISAGREE"))
			{
                // parse message by its type (offer, promise, query)
				String sSavedMsg = "";
				if (sMessage.startsWith("type query"))
				{
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
                    abstractAgent.opponentRejected(QUERY, m_agent.getAgreementIndices(sSavedMsg), sMessage);
				}
				else if (sMessage.startsWith("type counter_offer"))
				{
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
                    abstractAgent.opponentRejected(COUNTER_OFFER, m_agent.getAgreementIndices(sSavedMsg), sMessage);
				}
				else if (sMessage.startsWith("type offer"))
				{
					sSavedMsg = sMessage.substring(sMessage.indexOf("issueSet ") + 9);
                    abstractAgent.opponentRejected(OFFER, m_agent.getAgreementIndices(sSavedMsg), sMessage);
				}
				else if (sMessage.startsWith("type promise"))
				{
					String sPromise = sMessage.substring(sMessage.indexOf("myIssueSet ") + 11);
					String sMyIssueSet = sPromise.substring(0, sPromise.indexOf("yourIssueSet "));
					String sYourIssueSet = sPromise.substring(sPromise.indexOf("yourIssueSet ") + 13);

					// parse to one agreement
					sSavedMsg = sMyIssueSet + sYourIssueSet;
                    
                    abstractAgent.opponentRejected(PROMISE, m_agent.getAgreementIndices(sSavedMsg), sMessage);
				}
			}
		}
		else if (sServerLine.startsWith("type registered"))
		{
			String sSecsForTurn = sServerLine.substring(sServerLine.indexOf("secForTurn ")+11);
			StringTokenizer st = new StringTokenizer(sSecsForTurn);
					
			long lSecondsForTurn = Long.parseLong(st.nextToken());
			m_agent.setSecondsForTurn(lSecondsForTurn);

			String sMaxTurns=sServerLine.substring(sServerLine.indexOf("maxTurn ")+8);
			st = new StringTokenizer(sMaxTurns);
			m_agent.setMaxTurns(Integer.parseInt(st.nextToken()));

			long lTotalSec=lSecondsForTurn;
			int nHours=(int)lTotalSec/3600;

			lTotalSec -= nHours*3600;
			int nMinutes=(int)lTotalSec/60;

			lTotalSec -= nMinutes*60;

			m_agent.m_gtStopTurn = new AutomatedAgentGameTime(false,nHours,nMinutes,(int)lTotalSec,m_agent,true);
			m_agent.m_gtStopTurn.newGame(); // initializing the stop-watch

			new Thread(m_agent.m_gtStopTurn).start();

			lTotalSec=lSecondsForTurn * m_agent.getMaxTurns();
			nHours=(int)lTotalSec/3600;

			lTotalSec -= nHours*3600;
			nMinutes=(int)lTotalSec/60;

			lTotalSec -= nMinutes*60;

			m_agent.m_gtStopNeg = new AutomatedAgentGameTime(false,nHours,nMinutes,(int)lTotalSec,m_agent,false);
			m_agent.m_gtStopNeg.newGame(); // initializing the stop-watch

			new Thread(m_agent.m_gtStopNeg).start();

			String sAgentID = sServerLine.substring(sServerLine.indexOf("agentID ")+8);
			
			m_agent.setHasOpponent(true, sAgentID);
			
            String sOpponentSide = null;
            AutomatedAgentType agentType = m_agent.getAgentType();
            if (agentType.isTypeOf(AutomatedAgentType.SIDE_B_TYPE)) 
                sOpponentSide = AutomatedAgent.SIDE_A_NAME;
            else if (agentType.isTypeOf(AutomatedAgentType.SIDE_A_TYPE))
                sOpponentSide = AutomatedAgent.SIDE_B_NAME;
            
            abstractAgent.initialize(agentType, sOpponentSide);
		}
		else if (sServerLine.startsWith("type agentOptOut"))
		{
			m_agent.setHasOpponent(false, null);

			m_agent.m_gtStopTurn.setRun(false);
			m_agent.m_gtStopNeg.setRun(false);
		}
		else if (sServerLine.equals("type log request error"))
		{
			// not relevant for the Automated Agent
		}
		else if (sServerLine.startsWith("type log response"))
		{
			// not relevant for the Automated Agent
		}
		else if (sServerLine.startsWith("type query"))
		{
		    //query received
            
			// get message id
			StringTokenizer st = new StringTokenizer(sServerLine);
			
			boolean bFound = false;
			while (st.hasMoreTokens() && !bFound)
			{
				if (st.nextToken().equals("tag"))
				{
					bFound = true;
					st.nextToken();
				}
			}
	
			String sQuery=sServerLine.substring(sServerLine.indexOf("issueSet ")+9);
			
			int CurrentAgreementIdx[] = new int[AutomatedAgentType.MAX_ISSUES];
			CurrentAgreementIdx = m_agent.getAgreementIndices(sQuery);
			
            agentTools.calculateResponse(AutomatedAgentMessages.QUERY, CurrentAgreementIdx, sServerLine);
            //abstractAgent.queryReceived(CurrentAgreementIdx, sServerLine);
		}
		else if (sServerLine.startsWith("type counter_offer"))
		{
			// counter_offer received
			
			// get message id
			StringTokenizer st = new StringTokenizer(sServerLine);
			
			boolean bFound = false;
			while (st.hasMoreTokens() && !bFound)
			{
				if (st.nextToken().equals("tag"))
				{
					bFound = true;
					st.nextToken();
				}
			}
	
			String sOffer=sServerLine.substring(sServerLine.indexOf("issueSet ")+9);
			
			int CurrentAgreementIdx[] = new int[AutomatedAgentType.MAX_ISSUES];
			CurrentAgreementIdx = m_agent.getAgreementIndices(sOffer);
            
            agentTools.calculateResponse(AutomatedAgentMessages.COUNTER_OFFER, CurrentAgreementIdx, sServerLine);
            //abstractAgent.counterOfferReceived(CurrentAgreementIdx, sServerLine);
		}
		else if (sServerLine.startsWith("type offer"))
		{
			// offer received
			
			// get message id
			StringTokenizer st = new StringTokenizer(sServerLine);
			
			boolean bFound = false;
			while (st.hasMoreTokens() && !bFound)
			{
				if (st.nextToken().equals("tag"))
				{
					bFound = true;
					st.nextToken();
				}
			}
	
			String sOffer=sServerLine.substring(sServerLine.indexOf("issueSet ")+9);

			int CurrentAgreementIdx[] = new int[AutomatedAgentType.MAX_ISSUES];
			CurrentAgreementIdx = m_agent.getAgreementIndices(sOffer);
			
            agentTools.calculateResponse(AutomatedAgentMessages.OFFER, CurrentAgreementIdx, sServerLine);
            //abstractAgent.offerReceived(CurrentAgreementIdx, sServerLine);
		}
		else if (sServerLine.startsWith("type promise"))
		{
			// promise received
			
			// get message id
			StringTokenizer st = new StringTokenizer(sServerLine);
			
			boolean bFound = false;
			while (st.hasMoreTokens() && !bFound)
			{
				if (st.nextToken().equals("tag"))
				{
					bFound = true;
					st.nextToken();
				}
			}
			
			String sPromise=sServerLine.substring(sServerLine.indexOf("myIssueSet ")+11);
			String sMyIssueSet=sPromise.substring(0,sPromise.indexOf("yourIssueSet "));
			String sYourIssueSet=sPromise.substring(sPromise.indexOf("yourIssueSet ")+13);

			// parse to one agreement
			int CurrentAgreementIdxMine[] = new int[AutomatedAgentType.MAX_ISSUES];
			int CurrentAgreementIdxYours[] = new int[AutomatedAgentType.MAX_ISSUES];
			CurrentAgreementIdxMine = m_agent.getAgreementIndices(sMyIssueSet);
			CurrentAgreementIdxYours = m_agent.getAgreementIndices(sYourIssueSet);

			// combine indices
			for (int i = 0; i < AutomatedAgentType.MAX_ISSUES; ++i)
			{
				if (CurrentAgreementIdxYours[i] != AutomatedAgentType.NO_VALUE)
					CurrentAgreementIdxMine[i] = CurrentAgreementIdxYours[i]; 
			}
			
            agentTools.calculateResponse(AutomatedAgentMessages.PROMISE, CurrentAgreementIdxMine, sServerLine);
            //abstractAgent.promiseReceived(CurrentAgreementIdxMine, sServerLine);
		}
		else if (sServerLine.equals("nak") || sServerLine.equals("ack"))
		{
			sParsedString = sServerLine;
		}
		else // other unknown message
		{
			System.out.println("[AA]Unknown Message Error: " + sServerLine + " [AutomatedAgentMessages::parseMessage(590)]");
			System.err.println("[AA]Unknown Message Error: " + sServerLine + " [AutomatedAgentMessages::parseMessage(590)]");			
			
			sParsedString = sServerLine;
		}
		
		return sParsedString;
	}
}
