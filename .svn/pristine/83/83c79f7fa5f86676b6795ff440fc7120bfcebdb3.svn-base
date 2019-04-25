package agents.qoagent2;

/*****************************************************************
 * Class name: GameTime
 * Goal: Creating the stop-watch and displaying it using threads.
 * Description: None.
 * Input: None.
 * Output: None.
 ****************************************************************/
public class QGameTime implements Runnable
{
	private QOAgent m_agent;
	private int m_nMaxTurn;
	private boolean m_bIsTurn;
	private long m_nTime; // the timer
	private boolean m_bRun; // should the clock continue to run?
	private boolean m_bCountUp;
	private int m_nStartSeconds;
	private int m_nStartMinutes;
	private int m_nStartHours;

/*****************************************************************
 * Method name: GameTime()
 * Goal: Constructor.
 * Description: Initialize the class variables.
 * Input: None.
 * Output: None.
 ****************************************************************/
	public QGameTime(boolean bCountUp,int nHours,int nMinutes, int nSeconds,QOAgent agent, boolean TurnOrNeg)
	{
		m_bIsTurn=TurnOrNeg;
		m_agent = agent;
		m_nMaxTurn = m_agent.getMaxTurns();
		m_bCountUp = bCountUp;
		m_bRun = false;

		if (!bCountUp)
		{
			m_nStartMinutes = nMinutes;
			m_nStartSeconds = nSeconds;
			m_nStartHours = nHours;

			m_nTime = nHours*3600+ nMinutes*60 + nSeconds;
		}
		else
		{
			m_nStartSeconds = 0;
			m_nStartMinutes = 0;
			m_nStartHours = 0;
			m_nTime = 0;
		}
	}

	public void SetTime(boolean bCountUp,int nHours,int nMinutes, int nSeconds,boolean TurnOrNeg)
	{
		m_bIsTurn=TurnOrNeg;
		m_bCountUp = bCountUp;
		m_bRun = false;

		if (!bCountUp)
		{
			m_nStartMinutes = nMinutes;
			m_nStartSeconds = nSeconds;
			m_nStartHours = nHours;

			m_nTime = nHours*3600+ nMinutes*60 + nSeconds;
		}
		else
		{
			m_nStartSeconds = 0;
			m_nStartMinutes = 0;
			m_nStartHours = 0;
			m_nTime = 0;
		}
	}

/*****************************************************************
 * Method name: GameTime()
 * Goal: Constructor.
 * Description: Initialize the class variables.
 * Input: None.
 * Output: None.
 ****************************************************************/
	public QGameTime()
	{
		m_bCountUp = true;
		m_bRun = false;
		m_nTime = 0;
	}

/*****************************************************************
 * Method name: run()
 * Goal: Display and run the stop-watch.
 * Description: Increasing the time every second while the game is in progress.
 * Input: None.
 * Output: None.
 ****************************************************************/
	public void run()
	{
		while(m_bRun)
		{
			try{
				Thread.sleep(1000);
				if (m_bCountUp)
					m_nTime++; // increasing the time each 1 sec.
				else
					m_nTime--; // decreasing the time each 1 sec.
			} catch(Exception e){}

			if (m_nTime == 0)
				stopRunning();
		}
	}

/*****************************************************************
 * Method name: stopRunning()
 * Goal: Stop the stop-watch.
 * Description: None.
 * Input: None.
 * Output: None.
 ****************************************************************/
	public void stopRunning()
	{
		m_bRun = false;
		
		int nCurrentTurn = m_agent.getCurrentTurn();

		if((m_bIsTurn)&&(nCurrentTurn < m_nMaxTurn))
		{
			newGame(); //restart timer
			m_bRun = true;
			//m_agent.incrementCurrentTurn(); // incremented by the agent itself
		}
	}

/*****************************************************************
 * Method name: newGame()
 * Goal: Start a new stop-watch.
 * Description: None.
 * Input: None.
 * Output: None.
 ****************************************************************/
	public void newGame()
	{
		m_bRun = true;
		m_nTime = m_nStartHours*3600 + m_nStartMinutes * 60 + m_nStartSeconds;
	}

	public void setRunMethod(boolean bCountUp)
	{
		m_bCountUp = bCountUp;
	}
	
	public void setRun(boolean bRun)
	{
		m_bRun = bRun;
	}
} // end class - GameTime