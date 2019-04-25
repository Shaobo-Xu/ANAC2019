package agents.qoagent2;

import java.util.StringTokenizer;

//file name: Issue.java

/*****************************************************************
 * Class name: Issue
 * Goal: Saving an attribute of the negotiation along with its value,
 * weight, utility, effect of time (all strings) and whether the attribute was 
 * agreed upon (a boolen). We also save a 2 more strings: a list of 
 * all possible values (seperated by spaces) and a list of all 
 * possible utilities (seperated by spaces). Both those lists are read
 * from the utility file. 
 * When the server gets an accept message from the client, it checks
 * all the agreed attributes and their values. Then, we find the 
 * position of the agreed value in the values list, and find the
 * corresponding utility value in the same position in the utilities
 * list. We save the corresponding utility in the issue's utility field.
 ****************************************************************/
class Issue
{
	private String m_sAttribute; //the attribute's name
	private String m_sWeight; //weight of the attribute
	private String m_sValue; //the chosen value
	private String m_sUtility; //the corresponding utility
	private String m_sTimeEffect; //the time effect
	private boolean m_bAgreed; //agreed or not
	private int m_nTurn; //the turn in which the issue was agreed upon
	
	private String m_sValues; //list of possible values seperated by spaces
	private String m_sUtilities; //list of possible utilities seperated by spaces
	
	/*****************************************************************
	* Method name: Issue()
	* Goal: Constructor.
	* Description: Initialize the class variables.
	* Input: None.
	* Output: None.
	****************************************************************/
	public Issue()
	{
		m_bAgreed=false;
		m_nTurn=0;
		m_sAttribute = "";
		m_sValue = "";
		m_sUtility = "";
		m_sTimeEffect = "";
		m_sValues = "";
		m_sUtilities = "";
	}
	
	/*****************************************************************
	* Method name: getAgreed()
	* Goal: Return a boolean value which specifies whether the attribute 
	*		was agreed or not.
	* Input: None.
	* Output: A boolean value.
	***************************************************************/
	public boolean getAgreed()
	{
		return m_bAgreed;
	}
	
	/*****************************************************************
	* Method name: setAgreed()
	* Goal: Setting the boolean value which specifies whether the attribute was
	*		agreed or not.
	* Input: A boolean value.
	* Output: None.
	****************************************************************/
	public void setAgreed(boolean bAgreed)
	{
		m_bAgreed = bAgreed;
	}
	
	/*****************************************************************
	* Method name: getTurn()
	* Goal: Return the turn in which the issue was agreed upon.
	* Input: None.
	* Output: An integer.
	***************************************************************/
	public int getTurn()
	{
		return m_nTurn;
	}
	
	/*****************************************************************
	* Method name: setTurn()
	* Goal: Setting the turn in which the issue was agreed upon.
	* Input: An integer.
	* Output: None.
	****************************************************************/
	public void setTurn(int nTurn)
	{
		m_nTurn = nTurn;
	}
	
	/*****************************************************************
	* Method name: getAttribute()
	* Goal: Return the attribute's name.
	* Input: None.
	* Output: A string.
	****************************************************************/
	public String getAttribute()
	{
		return m_sAttribute;
	}
	
	/*****************************************************************
	* Method name: setAttribute()
	* Goal: Setting the attribute's name.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setAttribute(String sAttribute)
	{
		m_sAttribute = sAttribute;
	}
	
	/*****************************************************************
	* Method name: setWeight()
	* Goal: Setting the attribute's weight.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setWeight(String sWeight)
	{
		m_sWeight = sWeight;
	}
	
	/*****************************************************************
	* Method name: getWeight()
	* Goal: Return the attribute's weight.
	* Input: None.
	* Output: A string.
	****************************************************************/
	public String getWeight()
	{
		return m_sWeight;
	}
	
	/*****************************************************************
	* Method name: getValue()
	* Goal: Return the attribute's value.
	* Input: None.
	* Output: A string.
	****************************************************************/
	public String getValue()
	{
		return m_sValue;
	}
	
	/*****************************************************************
	* Method name: getValues()
	* Goal: Return the list of values.
	* Input: None.
	* Output: A string.
	****************************************************************/
	public String getValues()
	{
		return m_sValues;
	}
	
	/*****************************************************************
	* Method name: getUtility()
	* Goal: Return the attribute's utility value.
	* Input: None.
	* Output: A string.
	****************************************************************/
	public String getUtility()
	{
		return m_sUtility;
	}
	
	/*****************************************************************
	* Method name: getUtilities()
	* Goal: Return the list of utility values.
	* Input: None.
	* Output: A string.
	****************************************************************/
	public String getUtilities()
	{
		return m_sUtilities;
	}
	
	/*****************************************************************
	* Method name: getTimeEffect()
	* Goal: Return the attribute's time effect value.
	* Input: None.
	* Output: A string.
	****************************************************************/
	public String getTimeEffect()
	{
		return m_sTimeEffect;
	}
	
	/*****************************************************************
	* Method name: setValue()
	* Goal: Setting the attribute's value.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setValue(String sValue)
	{
		m_sValue = sValue;
	}
	
	/*****************************************************************
	* Method name: setValues()
	* Goal: Setting the list of values.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setValues(String sValues)
	{
		m_sValues = sValues;
	}
	
	/*****************************************************************
	* Method name: setUtility()
	* Goal: Setting the attribute's utility.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setUtility(String sUtility)
	{
		m_sUtility = sUtility;
	}
	
	/*****************************************************************
	* Method name: setUtilities()
	* Goal: Setting the list of utilities.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setUtilities(String sUtilities)
	{
		m_sUtilities = sUtilities;
	}
	
	/*****************************************************************
	* Method name: setTimeEffect()
	* Goal: Setting the attribute's effect of time.
	* Input: A string.
	* Output: None.
	****************************************************************/
	public void setTimeEffect(String sTimeEffect)
	{
		m_sTimeEffect = sTimeEffect;
	}
	
	public void setNoAgreementValue()
	{
		//finds the value's position in the values line
		//in order to find corresponding utility and effect of time
		int position=1; //this variable will hold the value's position
		String values = getValues();
		StringTokenizer stValues=new StringTokenizer(values, "~");
		while(stValues.hasMoreTokens())
		{
			String value=stValues.nextToken();
			if(value.equals(QOAgent.NOT_APPLICABLE_STR1))
			{
				break;
			}
			position++;
		}
		setValue(QOAgent.NOT_APPLICABLE_STR1); //set the value of the issue at the agent
		
		//finds and sets corresponding utility value
		String utilities = getUtilities();
		StringTokenizer stUtilities=new StringTokenizer(utilities);
		String utility="";
		for(int i=1; (i<=position)&&(stUtilities.hasMoreTokens()); i++)
		{
			utility=stUtilities.nextToken();
		}
		setUtility(utility); //set the utility value of the issue at the agent
	}
}
