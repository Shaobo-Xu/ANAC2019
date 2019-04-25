package agents.qoagent2;

//file name: Agent.java
import java.util.*;
import java.io.*;
import java.net.*;

/*****************************************************************
 * Class name: Agent
 * Goal: Each object of the class represents an agent (Zimbabwe, 
 * England or a mediator). The class holds all the information 
 * of the agent along with his opponent's ID and name (and mediator's
 * ID and name - if exists). This class allows the server to set/get 
 * all necessary data of an agent in an efficient way.
 ****************************************************************/
class Agent
{
	//general variables
//DT:	private ServerThread m_st; //Allows access to the agent's thread at the server
//DT:	private MultiServer m_server; //Allows access to the server
	private Vector<Issue> m_vecIssues; //The negotiation issues of the agent and their status
	private boolean m_bHasOpponent; //Whether the agent has an opponent or not
	private boolean m_bHasMediator; //Whether the agent has a mediator or not
	private boolean m_bSupportMediator; //Whether the agent supports a mediator or not
	private int m_nCurrentTurn; //Current turn of the negotiation
	private Socket m_socket = null; //The socket which allows the agent's thread in the 
									//server to connect to the client program
	private String m_sSide; //Holds the side of the agent (Zimbabwe,England,Mediator)
	private PrintWriter m_out = null; //Allows writing to the socket
	GameTimeServer m_gtEndTurn = null; //A stop watch for each turn
	GameTimeServer m_gtEndNeg = null; //A stop watch for the entire negotiation
	private InetAddress m_ip = null; //Holds the ip address of the client
	private int m_nPort;
	private double m_dScore; //the agent's score of the negotiation
	private double m_dTimeEffect; // time effect for the entire agreement
	private double m_dOptOutValue; // opting out value
	private double m_dStatusQuoValue; // status quo value
	
	//Agent's Details
	private String m_sId; //Agent's Id
	private String m_sName; //Agent's Name
	
	//Opponent's Details, in case that we are not a mediator
	private String m_sOppId; //Opponent's Id
	private String m_sOppName; //Opponent's Name
	
	//mediator Details, in case that we are not a mediator
	private String m_sMedId; //mediator's Id
	private String m_sMedName; //mediator's Name
	
	//The two sides in case that we are a mediator
	private String m_sIdReligious,m_sIdSecular; //Ids
	private String m_sNameReligious,m_sNameSecular; //Names

	public final static String TIME_EFFECT_STR = "Time-Effect";
	public final static String OPT_OUT_STR = "Opt-Out";
	public final static String STATUS_QUO_STR = "Status-Quo";
	
	private String m_sPrefDetails;
	
	private String m_sEndNegReason;
	private int m_nOptOutNum;
	private int m_nResponsesNum;
	private int m_nAcceptsNum;
	private int m_nRejectionsNum;
	private int m_nCommentsNum;
	private int m_nThreatsNum;
	private int m_nOffersNum;
	private int m_nPromisesNum;
	private int m_nQueriesNum;
	/*****************************************************************
	* Method name: Agent()
	* Goal: Constructor.
	* Description: Initialize the class variables and reading the issues
	* from the utility file.
	* Input: A MultiServer object (to allow access to the server), 
	*        a ServerThread object (to allow access to the agent's 
	*		 thread at the server), and a boolean which specifies whether
	*		 the agent supports a mediator or not.
	* Output: None.
	******************************************************************/
	public Agent(/* DT: MultiServer server,ServerThread st,*/ boolean SupportMed,String sSide)
	{
		m_nOptOutNum = 0;
		m_nResponsesNum = 0;
		m_nAcceptsNum = 0;
		m_nRejectionsNum = 0;
		m_nThreatsNum = 0;
		m_nCommentsNum = 0;
		m_nOffersNum = 0;
		m_nPromisesNum = 0;
		m_nQueriesNum = 0;
		
		m_sEndNegReason = "";
		
		m_dTimeEffect = 0;
		m_dStatusQuoValue = 0;
		m_dOptOutValue = 0;
		
		m_sIdReligious=null;
		m_sIdSecular=null;
		m_sMedId=null;
		
		m_sSide=sSide;
		m_bHasMediator=false;
		m_bSupportMediator=SupportMed;
//DT:		m_st=st;
//DT:		m_server=server;
		m_bHasOpponent=false;
		m_nCurrentTurn=1;
		m_nPort = 0;
		m_dScore=0;

		m_vecIssues=new Vector<Issue>();

		//reading issues from the appropriate utility file
		try
		{
			BufferedReader br=new BufferedReader(new FileReader("utility"+m_sSide+".txt"));
			String line;
			int nOrder=1;
			Issue issue=new Issue();
			
			while((line=br.readLine())!=null)
			{
				if(!line.startsWith("#"))
				{
					if (line.startsWith("@")) // general values data
					{
						StringTokenizer stGeneral = new StringTokenizer(line);
						
						stGeneral.nextToken(); // '@'
						
						String sType = stGeneral.nextToken();

						String sValue = stGeneral.nextToken();
						Double dTemp = new Double(sValue);
						
						if (sType.equals(TIME_EFFECT_STR))
							m_dTimeEffect = dTemp.doubleValue();
						else if (sType.equals(STATUS_QUO_STR))
							m_dStatusQuoValue = dTemp.doubleValue();
						else if (sType.equals(OPT_OUT_STR))
							m_dOptOutValue = dTemp.doubleValue();
					}
					else if (!line.startsWith("!")) //a new title
					{
							switch(nOrder)
							{
							case 1: //the first line is: attribute_name*side*weight
									//the attribute's name and weight is saved in issue
									issue.setAttribute(line.substring(0,line.indexOf("*")));
									String sTemp=line.substring(line.indexOf("*")+1);
									sTemp=sTemp.substring(sTemp.indexOf("*")+1);
									issue.setWeight(sTemp);
									break;
							case 2: issue.setValues(line);
									break;
							case 3: issue.setUtilities(line);
									break;
							case 4: issue.setTimeEffect(line);
									break;
							case 5: m_vecIssues.addElement(issue);
									nOrder=0;
									issue=new Issue();
							}
							nOrder++;
					}
				}
			} //while

			br.close();
		}//try

		catch(IOException e)
		{
			System.out.println("ERROR----" + "[Agent " + m_sId + "] " + "I/O Error while reading from file: " + e.getMessage() + "[Agent::Agent(153)]");
			System.err.println("ERROR----" + "[Agent " + m_sId + "] " + "I/O Error while reading from file: " + e.getMessage() + "[Agent::Agent(153)]");
		}
	}
	
	/*****************************************************************
	* Method name: setScore()
	* Goal: Setting the agent's score.
	* Input: An integer(the score).
	* Output: None.
	****************************************************************/
	public void setScore(double score)
	{
		m_dScore=score;
	}
	
	/*****************************************************************
	* Method name: getScore()
	* Goal: Return the agent's score.
	* Input: None.
	* Output: An integer(the score).
	***************************************************************/
	public double getScore()
	{
		return m_dScore;
	}
	
	/*****************************************************************
	* Method name: setIssuesVector()
	* Goal: Setting the Issues vector.
	* Input: A vector of issues.
	* Output: None.
	****************************************************************/
	public void setIssuesVector(Vector<Issue> vec)
	{
		m_vecIssues=vec;
	}
	
	/*****************************************************************
	* Method name: getIssuesVector()
	* Goal: Return the Issues vector.
	* Input: None.
	* Output: A vector of issues.
	***************************************************************/
	public Vector<Issue> getIssuesVector()
	{
		return m_vecIssues;
	}
	
	/*****************************************************************
	* Method name: initIssuesVector()
	* Goal: Set all the issues as not agreed yet.
	* Input: None.
	* Output: None.
	***************************************************************/
	public void initIssuesVector()
	{
		if(m_vecIssues!=null)
			for(int i=0; i < getIssuesNum(); i++)
			{
				getIssueAt(i).setAgreed(false);
			}
	}
	
	/*****************************************************************
	* Method name: setName()
	* Goal: Setting the agent's name.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setName(String sName)
	{
		m_sName = sName;
	}
	
	/*****************************************************************
	* Method name: getName()
	* Goal: Return the agent's name.
	* Input: None.
	* Output: A string.
	***************************************************************/
	public String getName()
	{
		return m_sName;
	}
	
	/*****************************************************************
	* Method name: getOpponentName()
	* Goal: Return the opponent's name.
	* Input: None.
	* Output: A string.
	***************************************************************/
	public String getOpponentName()
	{
		return m_sOppName;
	}
	
	/*****************************************************************
	* Method name: getMedName()
	* Goal: Return the mediator's name.
	* Input: None.
	* Output: A string.
	***************************************************************/
	public String getMedName()
	{
		return m_sMedName;
	}
	
	/*****************************************************************
	* Method name: getNameReligious()
	* Goal: Return the England side name (in case the agent is a mediator).
	* Input: None.
	* Output: A string.
	***************************************************************/
	public String getNameReligious()
	{
		return m_sNameReligious;
	}
	
	/*****************************************************************
	* Method name: getNameSecular()
	* Goal: Return the Zimbabwe side name (in case the agent is a mediator).
	* Input: None.
	* Output: A string.
	***************************************************************/
	public String getNameSecular()
	{
		return m_sNameSecular;
	}
	
	/*****************************************************************
	* Method name: setOpponentName()
	* Goal: Setting the opponent's name.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setOpponentName(String sOppName)
	{
		m_sOppName = sOppName;
	}
	
	/*****************************************************************
	* Method name: setMedName()
	* Goal: Setting the mediator's name.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setMedName(String sMedName)
	{
		m_sMedName = sMedName;
	}
	
	/*****************************************************************
	* Method name: setNameReligious()
	* Goal: Setting the England side name (in case the agent is a mediator).
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setNameReligious(String sName)
	{
		m_sNameReligious = sName;
	}
	
	/*****************************************************************
	* Method name: setNameSecular()
	* Goal: Setting the Zimbabwe side name (in case the agent is a mediator).
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setNameSecular(String sName)
	{
		m_sNameSecular = sName;
	}
	
	/*****************************************************************
	* Method name: setEndTurnNewGame()
	* Goal: Restarting the end-turn stop watch.
	* Input: None.
	* Output: None.
	****************************************************************/
	public void setEndTurnNewGame()
	{
		m_gtEndTurn.newGame();
	}

	/*****************************************************************
	* Method name: setEndNegNewGame()
	* Goal: Restarting the end-negotiation stop watch.
	* Input: None.
	* Output: None.
	****************************************************************/
	public void setEndNegNewGame()
	{
		m_gtEndNeg.newGame();
	}

	/*****************************************************************
	* Method name: getSide()
	* Goal: Return the agent's side.
	* Input: None.
	* Output: None.
	***************************************************************/
	public String getSide()
	{
		return m_sSide;
	}
	
	/*****************************************************************
	* Method name: setSide()
	* Goal: Setting the agent's side.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setSide(String sSide)
	{
		m_sSide = sSide;
	}

	/*****************************************************************
	* Method name: setIp()
	* Goal: Setting the agent's ip address.
	* Input: An InetAddress object.
	* Output: None.
	****************************************************************/
	public void setIp(InetAddress ip)
	{
		m_ip = ip;
	}

	/*****************************************************************
	* Method name: setIssueAt()
	* Goal: Inserting an Issue to the issues vectors at a specified index.
	* Input: An integer (the index) and an Issue.
	* Output: None.
	****************************************************************/
	public void setIssueAt(int index, Issue issue)
	{
		m_vecIssues.set(index, issue);	
	}

	/*****************************************************************
	* Method name: setEndNeg()
	* Goal: Initializing the end-negotiation stop watch.
	* Input:  A boolean which specifies whether the stop watch counts up or not,
	* three integers (hours, minutes and seconds), another boolean which 
	* specifies whether the stop watch is of type end-turn or end-negotiation,
	* and another integer (number of turns in the negotiation).
	* Output: None.
	****************************************************************/
	public void setEndNeg(boolean bCountUp, int nHours, int nMinutes, int nSeconds, boolean bTurnOrNeg, int nMaxTurn)
	{
		m_gtEndNeg = new GameTimeServer(bCountUp,nHours,nMinutes,nSeconds,this,bTurnOrNeg,nMaxTurn /* DT: ,m_server,m_st*/ );
	}

	/*****************************************************************
	* Method name: setEndNegRun()
	* Goal: Stopping or continuing the end-negotiation stop watch.
	* Input: A boolean which specifies whether the stop watch should
	* stop (false) or continue (true).
	* Output: None.
	****************************************************************/
	public void setEndNegRun(boolean bRun)
	{
		m_gtEndNeg.setRun(bRun);
	}

	/*****************************************************************
	* Method name: setEndTurn()
	* Goal: Initializing the end-turn stop watch.
	* Input:  A boolean which specifies whether the stop watch counts up or not,
	* three integers (hours, minutes and seconds), another boolean which 
	* specifies whether the stop watch is of type end-turn or end-negotiation,
	* and another integer (number of turns in the negotiation).
	* Output: None.
	****************************************************************/
	public void setEndTurn(boolean bCountUp, int nHours, int nMinutes, int nSeconds, boolean bTurnOrNeg, int nMaxTurn)
	{
		m_gtEndTurn = new GameTimeServer(bCountUp,nHours,nMinutes,nSeconds,this,bTurnOrNeg,nMaxTurn/* DT: ,m_server,m_st */);
	}

	/*****************************************************************
	* Method name: setEndTurnRun()
	* Goal: Stopping or continuing the end-turn stop watch.
	* Input: A boolean which specifies whether the stop watch should
	* stop (false) or continue (true).
	* Output: None.
	****************************************************************/
	public void setEndTurnRun(boolean bRun)
	{
		m_gtEndTurn.setRun(bRun);
	}
	
	/*****************************************************************
	* Method name: getIssuesNum()
	* Goal: Return the number of issues of the negotiation.
	* Input: None.
	* Output: An integer.
	***************************************************************/
	public int getIssuesNum()
	{
		return m_vecIssues.size();
	}
	
	/*****************************************************************
	* Method name: getIssueAt()
	* Goal: Return the issue at the specified index.
	* Input: An integer (the index).
	* Output: An Issue.
	***************************************************************/
	public Issue getIssueAt(int index)
	{
		return (Issue)m_vecIssues.elementAt(index);
	}

	/*****************************************************************
	* Method name: setPort()
	* Goal: Setting the agent's port.
	* Input: An integer (the port).
	* Output: None.
	****************************************************************/
	public void setPort(int nPort)
	{
		m_nPort = nPort;
	}

	/*****************************************************************
	* Method name: getPort()
	* Goal: Return the agent's port.
	* Input: None.
	* Output: An integer.
	***************************************************************/
	public int getPort()
	{
		return m_nPort;
	}
	
	/*****************************************************************
	* Method name: setId()
	* Goal: Setting the agent's ID.
	* Input: A String.
	* Output: None.
	****************************************************************/
	public void setId(String sId)
	{
		m_sId = sId;
	}
	
	/*****************************************************************
	* Method name: getId()
	* Goal: Return the agent's ID.
	* Input: None.
	* Output: A String.
	***************************************************************/
	public String getId()
	{
		return m_sId;
	}
	
	/*****************************************************************
	* Method name: getOpponentId()
	* Goal: Return the opponent's ID.
	* Input: None.
	* Output: A String.
	***************************************************************/
	public String getOpponentId()
	{
		return m_sOppId;
	}
	
	/*****************************************************************
	* Method name: getMedId()
	* Goal: Return the mediator's ID.
	* Input: None.
	* Output: A String.
	***************************************************************/
	public String getMedId()
	{
		return m_sMedId;
	}
	
	/*****************************************************************
	* Method name: getIdReligious()
	* Goal: Return the England side ID (in case the agent is a mediator).
	* Input: None.
	* Output: A String.
	***************************************************************/
	public String getIdReligious()
	{
		return m_sIdReligious;
	}
	
	/*****************************************************************
	* Method name: getIdSecular()
	* Goal: Return the Zimbabwe side ID (in case the agent is a mediator).
	* Input: None.
	* Output: A String.
	***************************************************************/
	public String getIdSecular()
	{
		return m_sIdSecular;
	}
	
	/*****************************************************************
	* Method name: setOpponentId()
	* Goal: Setting the opponent's ID.
	* Input: A String.
	* Output: None.
	****************************************************************/
	public void setOpponentId(String sOppId)
	{
		m_sOppId = sOppId;
	}
	
	/*****************************************************************
	* Method name: setMedId()
	* Goal: Setting the mediator's ID.
	* Input: A String.
	* Output: None.
	****************************************************************/
	public void setMedId(String sMedId)
	{
		m_sMedId = sMedId;
	}
	
	/*****************************************************************
	* Method name: setIdReligious()
	* Goal: Setting the England side ID (in case the agent is a mediator).
	* Input: A String.
	* Output: None.
	****************************************************************/
	public void setIdReligious(String sId)
	{
		m_sIdReligious = sId;
	}
	
	/*****************************************************************
	* Method name: setIdSecular()
	* Goal: Setting the Zimbabwe side ID (in case the agent is a mediator).
	* Input: A String.
	* Output: None.
	****************************************************************/
	public void setIdSecular(String sId)
	{
		m_sIdSecular = sId;
	}
	
	/*****************************************************************
	* Method name: setSocket()
	* Goal: Setting the agent's socket and setting the PrintWriter to 
	* write to the socket.
	* Input: A Socket object.
	* Output: None.
	****************************************************************/
	public void setSocket(Socket socket)
	{
		m_socket = socket;
		try {
   	    	m_out = new PrintWriter(m_socket.getOutputStream(),true);
   	    }
   	    catch (IOException e)
   	    {
   	    	System.out.println("ERROR----" + "[Agent " + m_sId + "] " + "Error opening socket: " + e.getMessage() + " [Agent::setSocket(614)]");
   	    	System.err.println("ERROR----" + "[Agent " + m_sId + "] " + "Error opening socket: " + e.getMessage() + " [Agent::setSocket(614)]");
   	    }
	}
	
	/*****************************************************************
	* Method name: closeSocketStream()
	* Goal: Closing the PrintWriter which writes to the agent's socket.
	* Input: None.
	* Output: None.
	****************************************************************/
	public void closeSocketStream()
	{
		m_out.close();
	}
	
	/*****************************************************************
	* Method name: getSocket()
	* Goal: Return the agent's socket.
	* Input: None.
	* Output: A Socket object.
	***************************************************************/
	public Socket getSocket()
	{
		return m_socket;
	}
	
	/*****************************************************************
	* Method name: writeToSocket()
	* Goal: Writing a message to the agent's socket.
	* Input: A string (the message).
	* Output: None.
	***************************************************************/
	public void writeToSocket(String sMsg)
	{
		m_out.println(sMsg);
    }

	/*****************************************************************
	* Method name: incrementCurrentTurn()
	* Goal: Incrementing the current turn of the negotiation.
	* Input: None.
	* Output: None.
	***************************************************************/
	public void incrementCurrentTurn()
	{
	 	m_nCurrentTurn++;
	}
	
	/*****************************************************************
	* Method name: setCurrentTurn()
	* Goal: Setting the current turn.
	* Input: An integer.
	* Output: None.
	****************************************************************/
	public void setCurrentTurn(int nCurrentTurn)
	{
		m_nCurrentTurn = nCurrentTurn;
	}
	
	/*****************************************************************
	* Method name: getCurrentTurn()
	* Goal: Return the current turn.
	* Input: None.
	* Output: An integer.
	***************************************************************/
	public int getCurrentTurn()
	{
		return m_nCurrentTurn;
	}
	
	/*****************************************************************
	* Method name: hasOpponent()
	* Goal: Return a boolean which specifies whether the agent has an opponent.
	* Input: None.
	* Output: A boolean.
	***************************************************************/
	public boolean hasOpponent()
	{
		return m_bHasOpponent;
	}
	
	/*****************************************************************
	* Method name: setHasOpponent()
	* Goal: Setting whether the agent has an opponent.
	* Input: A boolean.
	* Output: None.
	****************************************************************/
	public void setHasOpponent(boolean bHasOpponent)
	{
		m_bHasOpponent = bHasOpponent;
	}

	/*****************************************************************
	* Method name: hasMediator()
	* Goal: Return a boolean which specifies whether the agent has a mediator.
	* Input: None.
	* Output: A boolean.
	***************************************************************/
	public boolean hasMediator()
	{
		return m_bHasMediator;
	}
	
	/*****************************************************************
	* Method name: setHasMediator()
	* Goal: Setting whether the agent has a mediator.
	* Input: A boolean.
	* Output: None.
	****************************************************************/
	public void setHasMediator(boolean bHasMediator)
	{
		m_bHasMediator = bHasMediator;
	}
	
	/*****************************************************************
	* Method name: supportMediator()
	* Goal: Return a boolean which specifies whether the agent supports
	*  a mediator.
	* Input: None.
	* Output: A boolean.
	***************************************************************/
	public boolean supportMediator()
	{
		return m_bSupportMediator;
	}
	
	/*****************************************************************
	* Method name: startEndNegThread()
	* Goal: Starting the end-negotiation stop watch's thread.
	* Input: None.
	* Output: None.
	****************************************************************/
	public void startEndNegThread()
	{
		new Thread(m_gtEndNeg).start();		
	}
	
	/*****************************************************************
	* Method name: startEndNegThread()
	* Goal: Starting the end-turn stop watch's thread.
	* Input: None.
	* Output: None.
	****************************************************************/
	public void startEndTurnThread()
	{
		new Thread(m_gtEndTurn).start();
	}
	
	public void setPrefDetails(String sPrefDetails)
	{
		m_sPrefDetails = sPrefDetails;
	}
	
	public String getPrefDetails()
	{
		return m_sPrefDetails;
	}
	
	public double getAgreementTimeEffect()
	{
		return m_dTimeEffect;
	}
	
	public double getAgreementOptOutValue()
	{
		return m_dOptOutValue;
	}
	
	public void setEndNegReason(String sEndNegReason)
	{
		m_sEndNegReason = sEndNegReason;
	}
	
	public String getEndNegReason()
	{
		return m_sEndNegReason;
	}
	
	public void setOptOutNum(int nOptOutNum)
	{
		m_nOptOutNum = nOptOutNum;
	}

	public int getOptOutNum()
	{
		return m_nOptOutNum;
	}
	
	public void incrementResponsesNum()
	{
		m_nResponsesNum++;
	}
	
	public int getResponsesNum()
	{
		return m_nResponsesNum++;
	}
	
	public void incrementAcceptsNum()
	{
		m_nAcceptsNum++;
	}
	
	public int getAcceptsNum()
	{
		return m_nAcceptsNum;
	}

	public void incrementRejectionsNum()
	{
		m_nRejectionsNum++;
	}
	
	public int getRejectionsNum()
	{
		return m_nRejectionsNum;
	}

	public void incrementThreatsNum()
	{
		m_nThreatsNum++;
	}
	
	public int getThreatsNum()
	{
		return m_nThreatsNum;
	}

	public void incrementCommentsNum()
	{
		m_nCommentsNum++;
	}
	
	public int getCommentsNum()
	{
		return m_nCommentsNum;
	}
	
	public void incrementQueriesNum()
	{
		m_nQueriesNum++;
	}
	
	public int getQueriesNum()
	{
		return m_nQueriesNum;
	}
	
	public void incrementOffersNum()
	{
		m_nOffersNum++;
	}
	
	public int getOffersNum()
	{
		return m_nOffersNum;
	}
	
	public void incrementPromisesNum()
	{
		m_nPromisesNum++;
	}
	
	public int getPromisesNum()
	{
		return m_nPromisesNum;
	}
}
