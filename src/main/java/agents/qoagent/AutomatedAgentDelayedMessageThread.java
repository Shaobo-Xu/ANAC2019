package agents.qoagent;

/**
 * The AutomatedAgentDelayedMessageThread class allows to send a message in a delay
 * @author Raz Lin
 * @version 1.0
 * @see AutomatedAgent
 */
public class AutomatedAgentDelayedMessageThread extends Thread {

	public final static long SLEEP_TIME_FACTOR = 3; // sleep half of turn time
	private String m_sOffer;
	private String m_sResponse;
	private int m_nCurrentTurn;
	private long m_lSleepTime;
	private AutomatedAgent m_agent;
	private int m_nMessageType = NO_TYPE;
	public static final int NEW_OFFER_TYPE = 0;
	public static final int RESPONSE_TYPE = 1;
	public static final int NO_TYPE = -1;
	public static final long RESPONSE_SLEEP_TIME_MILLIS = 15000; // 15 seconds
	
	AutomatedAgentDelayedMessageThread(AutomatedAgent agent, String sOffer, int nCurrentTurn)
	{
		m_nMessageType = NEW_OFFER_TYPE;
		m_agent = agent;
		m_sOffer = sOffer;
		m_nCurrentTurn = nCurrentTurn;
		
		// sleep for a while - time in milliseconds
		long lSecondsPerTurn = m_agent.getSecondsForTurn();
		long lMillisPerTurn = lSecondsPerTurn * 1000;

		m_lSleepTime = lMillisPerTurn / SLEEP_TIME_FACTOR;
	}

	AutomatedAgentDelayedMessageThread(AutomatedAgent agent, String sResponse)
	{
		m_nMessageType = RESPONSE_TYPE;
		m_agent = agent;
		m_sResponse = sResponse;
		
		// sleep for 15 seconds before answering
		m_lSleepTime = RESPONSE_SLEEP_TIME_MILLIS;
	}
	
	public void run()
	{
		try {
			sleep((long)m_lSleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("[AA]ERROR: Error during sleep" + e.getMessage() + " [AutomatedAgentDelayedMessageThread::run(35)]");
			System.err.println("[AA]ERROR: Error during sleep" + e.getMessage() + " [AutomatedAgentDelayedMessageThread::run(35)]");
		}
		
		if (m_nMessageType == NEW_OFFER_TYPE)
		{
			// check if message is still valid
			boolean bSendOffer = m_agent.getSendOfferFlag();
			int nCurrentTurn = m_agent.getCurrentTurn();
			
			if (nCurrentTurn == m_nCurrentTurn)
			{
				//	check whether to send the message or not
				if (bSendOffer)
					m_agent.printMessageToServer(m_sOffer);				
			}
		}
		else if (m_nMessageType == RESPONSE_TYPE)
		{
			m_agent.printMessageToServer(m_sResponse);			
		}
	}
}
