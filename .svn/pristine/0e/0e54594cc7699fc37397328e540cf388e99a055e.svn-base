package agents.qoagent;

import java.util.StringTokenizer;

/*
 * Created on 11/09/2004
 *
 */

/**
 * @author raz
 * @version 1.0
 * @see AutomatedAgentsCore
 * @see AutomatedAgent
 */
public class AutomatedAgentType {
	public final static String NOT_APPLICABLE_STR1 = "No agreement";
    public static final int VERY_SMALL_NUMBER = -9999;
    public static final int VERY_HIGH_NUMBER = 9999;

    public static final int MAX_ISSUES = 20;
	public static final int NO_VALUE = -1;	
	
	public FullUtility m_fullUtility;
	
	private double m_dSQValue;// status quo value
    private double m_dOptOutValue;// opting out value
	private double m_dBestAgreementValue; // best agreement value
	private double m_dWorstAgreementValue; // worst agreement value
	
	private int m_MaxIssueValues[]; // keeps the maximal values for each issue 
	private int m_BestAgreementIdx[]; // keeps the indices of the values for the best agreement for each issue
	private int m_WorstAgreementIdx[]; // keeps the indices of the values for the worst agreement for each issue
	private int m_nTotalAgreements; // total number of agreements
	
	private int m_nType;
	
	public static final int NO_TYPE = -1;
	public static final int SIDE_A_TYPE = 0; // England/Employer (Side A)
	public static final int SIDE_B_TYPE = 1; // Zimbabwe/Job Can (Side B)
	
	private String m_sAgentName;
	
    /*****************************************************************
     * Method name: AutomatedAgentType()
     * Goal: Initializes the agent
     * Input: None.
     * Output: None.
     ****************************************************************/
	public AutomatedAgentType()
	{
		m_nType = NO_TYPE;
		m_sAgentName = "";
		
		m_dBestAgreementValue = VERY_SMALL_NUMBER;
		m_dWorstAgreementValue = VERY_HIGH_NUMBER;

		m_fullUtility = new FullUtility();

		m_BestAgreementIdx = new int[MAX_ISSUES];
		m_WorstAgreementIdx = new int[MAX_ISSUES];
		m_MaxIssueValues = new int[MAX_ISSUES];
		m_nTotalAgreements = 0;
		
		for (int i = 0; i < MAX_ISSUES; ++i)
		{
			m_BestAgreementIdx[i] = NO_VALUE;
			m_WorstAgreementIdx[i] = NO_VALUE;
			m_MaxIssueValues[i] = NO_VALUE;
		}
		
		m_dSQValue = VERY_SMALL_NUMBER;
        m_dOptOutValue = VERY_SMALL_NUMBER;
	}

    /*****************************************************************
     * Method name: setName()
     * Goal: Set the agent's name
     * Input: String - name.
     * Output: None.
     ****************************************************************/
	public void setName(String sName)
	{
		m_sAgentName = sName;
	}
	
    /*****************************************************************
     * Method name: getIssuesNum()
     * Goal: Return the number of issues in the negotiation
     * Input: None.
     * Output: int - issues number.
     ****************************************************************/
	public int getIssuesNum()
	{
		int nIssuesNum = 0;
		
		for (int i = 0; i < m_fullUtility.lstUtilityDetails.size(); ++i)
		{
			UtilityDetails utilityDetails = (UtilityDetails)m_fullUtility.lstUtilityDetails.get(i);
			nIssuesNum += utilityDetails.lstUtilityIssues.size();
		}
		
		return nIssuesNum;
	}

	/**
     * Return the maximal value for a given issue
	 * @param nIssueNum - the issue number
	 * @return the maximum value for the issue
	 */
	public int getMaxIssueValue(int nIssueNum)
	{
		return m_MaxIssueValues[nIssueNum];
	}
	
	/**
     * Return the total number of agreements in the negotiation
	 * @return m_nTotalAgreements - number of total possible agreeements
	 */
	public int getTotalAgreements()
	{
		return m_nTotalAgreements;
	}
	
	/**
	 * Sets the agent type - either NO_TYPE, 
     * SIDE_A_TYPE (Eng/Emp) or SIDE_B_TYPE (Zim/Job Can)
	 * @param nType - the agent's type
	 */
	public void setAgentType(int nType)
	{
		m_nType = nType;
	}

    /**
     * @param nAgentType - the agent's type 
     * (either NO_TYPE, SIDE_A_TYPE (Eng/Emp) or SIDE_B_TYPE (Zim/Job Can)
     * @return true if the agent is of type nAgentType. Otherwise, returns false.
     */
    public boolean isTypeOf(int nAgentType)
    {
        if (nAgentType == m_nType)
            return true;
        else
            return false;  
    }
    
    /**
     * Return the status quo value
     * @return m_dSQValue - SQ value
     */
	public double getSQValue() {
		return m_dSQValue;
	}
    
    public double getOptOutValue() {
        return m_dOptOutValue;
    }
	
	/**
     * Returns the value of the best agreement in the current turn
	 * @param nCurrentTurn - current turn
     * @return best agreement value
	 */
	public double getBestAgreementValue(int nCurrentTurn)
	{
		return getAgreementValue(m_BestAgreementIdx, nCurrentTurn);	
	}
	
	/**
     * Returns the best agreement as String value
	 * @return best agreement as string
	 */
	public String getBestAgreementStr()
	{
		return getAgreementStr(m_BestAgreementIdx);	
	}

	/**
     * Returns the value of the worst agreement in the current turn
     * @param nCurrentTurn - current turn
	 * @return worst agreement value
	 */
	public double getWorstAgreementValue(int nCurrentTurn)
	{
		return getAgreementValue(m_WorstAgreementIdx, nCurrentTurn);	
	}

	/**
     * Returns the worst agreement as String value
	 * @return worst agreement as string
	 */
	public String getWorstAgreementStr()
	{
		return getAgreementStr(m_WorstAgreementIdx);	
	}

	/**
     * Get value of a given agreement by issues indices for
     * a given turn
	 * @param CurrentAgreementIdx - indices of the agreement
     * @param nCurrentTurn - the current turn
	 * @return the agreement value
	 */
	public double getAgreementValue(int CurrentAgreementIdx[], int nCurrentTurn)
	{
		double dAttributeWeight = 0;
		double dUtility = 0;
		double dCurrentIssueValue = 0;
		double dAgreementValue = 0;
		
		boolean bAgreementHasValues = false;
			
		// generate the agreement
		int nIssueNum = 0;
		for (int i = 0; i < m_fullUtility.lstUtilityDetails.size(); ++i)
		{
			UtilityDetails utilityDetails = (UtilityDetails)m_fullUtility.lstUtilityDetails.get(i);
			
			for (int j = 0; j < utilityDetails.lstUtilityIssues.size(); ++j)
			{
				UtilityIssue utilityIssue = (UtilityIssue)utilityDetails.lstUtilityIssues.get(j);
				
				if (CurrentAgreementIdx[nIssueNum] != NO_VALUE)
				{
					bAgreementHasValues = true;
					
					UtilityValue utilityValue = (UtilityValue)utilityIssue.lstUtilityValues.get(CurrentAgreementIdx[nIssueNum]);
	
					dUtility = utilityValue.dUtility;
					
					// add time effect for this issue
					// currently, dTimeEffect() is 0.
					//adding the issue's utility to the total score
					dUtility += ( nCurrentTurn - 1) * utilityValue.dTimeEffect;
				}
				else
					dUtility = 0;
				
				dAttributeWeight = utilityIssue.dAttributeWeight;
				dCurrentIssueValue = (double)(dUtility * dAttributeWeight)/(double)100;
				dAgreementValue += dCurrentIssueValue;
				
				nIssueNum++;
			}
		}
	
		// add time effect for entire agreement
		double dTimeEffect = m_fullUtility.dTimeEffect;
		dAgreementValue = dAgreementValue + ((dTimeEffect * (nCurrentTurn - 1)) / (double)100);

		if (!bAgreementHasValues) // agreement is empty - all issues has no value
			dAgreementValue = VERY_SMALL_NUMBER;
		
		return dAgreementValue;
	}

	/**
     * Get String of agreement by issues indice
	 * @param CurrentAgreementIdx - indices of the agreement
	 * @return the agreement as string
	 */
	public String getAgreementStr(int CurrentAgreementIdx[])
	{
		String sAgreementStr = "";
		String sCurrentIssueName = "";
		String sCurrentIssueValue = "";		
			
		// generate the agreement
		int nIssueNum = 0;
		for (int i = 0; i < m_fullUtility.lstUtilityDetails.size(); ++i)
		{
			UtilityDetails utilityDetails = (UtilityDetails)m_fullUtility.lstUtilityDetails.get(i);
			
			for (int j = 0; j < utilityDetails.lstUtilityIssues.size(); ++j)
			{
				UtilityIssue utilityIssue = (UtilityIssue)utilityDetails.lstUtilityIssues.get(j);
				
				if (CurrentAgreementIdx[nIssueNum] != NO_VALUE)
				{
					UtilityValue utilityValue = (UtilityValue)utilityIssue.lstUtilityValues.get(CurrentAgreementIdx[nIssueNum]);
	
					sCurrentIssueValue = utilityValue.sValue;
					sCurrentIssueName = utilityIssue.sAttributeName;
	
					sAgreementStr += sCurrentIssueValue + AutomatedAgentsCore.ISSUE_SEPARATOR_STR 
									+ sCurrentIssueName + AutomatedAgentsCore.ISSUE_SEPARATOR_STR; 
				}
				
				nIssueNum++;
			}
		}

		return sAgreementStr;
	}
	
    /**
     * Check whether an issue in the agreement has an N/A value
     * @param nIssueNum - the issue number
     * @param nIssueIdx - the issue index
     * @return true if the issue was not agreed yet, false - o/w
     */
    public boolean isIssueValueNoAgreement(int nIssueNum, int nIssueNumIdx)
	{
		String sIssueValue = getIssueValueStr(nIssueNum, nIssueNumIdx);
		
		if (sIssueValue.equals(/* DT: ServerThread. */NOT_APPLICABLE_STR1))
			return true;
		else
			return false;		
	}

    /**
     * Get String of issue value
     * @param nIssueNum - the issue number
     * @param nIssueIdx - the issue index
     * @return the value as string
     */    
	private String getIssueValueStr(int nIssueNum, int nIssueNumIdx)
	{
		String sIssueValueStr = "";
				
		// generate the agreement
		int nCurrentIssueNum = 0;
		boolean bFound = false;
		for (int i = 0; i < m_fullUtility.lstUtilityDetails.size() && !bFound; ++i)
		{
			UtilityDetails utilityDetails = (UtilityDetails)m_fullUtility.lstUtilityDetails.get(i);
			
			for (int j = 0; j < utilityDetails.lstUtilityIssues.size(); ++j)
			{
				if (nCurrentIssueNum == nIssueNum)
				{
					UtilityIssue utilityIssue = (UtilityIssue)utilityDetails.lstUtilityIssues.get(j);
					
					UtilityValue utilityValue = (UtilityValue)utilityIssue.lstUtilityValues.get(nIssueNumIdx);

					sIssueValueStr = utilityValue.sValue;
					bFound = true;
				}
				else
					nCurrentIssueNum++;
			}
		}

		return sIssueValueStr;		
	}

    /**
     * Get indices of a given agreement
     * @param sAgreementStr - the agreement as string
     * @return an array of indices corresponding to the 
     * issues of the agreement
     */    
	public int[] getAgreementIndices(String sAgreementStr)
	{
		sAgreementStr = sAgreementStr.trim();
		
		int CurrentAgreementIdx[] = new int[MAX_ISSUES];
		
		for (int i = 0; i < MAX_ISSUES; ++i)
			CurrentAgreementIdx[i] = NO_VALUE;
		
		int nIssueNum = 0;
		boolean bFoundIssue = false, bFoundValue = false;
		String sCurrentIssueName = "";
		String sCurrentIssueValue = "";
		
		// tokenize the agreement by issue separator
		StringTokenizer st = new StringTokenizer(sAgreementStr, AutomatedAgentsCore.ISSUE_SEPARATOR_STR);
		
		// the agreement string has the following structure:
		// issue_value SEPARATOR issue_name SEPARATOR...
		while (st.hasMoreTokens())
		{
			// get issue value
			sCurrentIssueValue = st.nextToken();
			
			if (!st.hasMoreTokens())
			{
				// this is an error
				System.out.println("[AA]ERROR: Invalid agreement structure: " + sAgreementStr + " [AutomatedAgentType::getAgreementIndices(660)]");
				System.err.println("[AA]ERROR: Invalid agreement structure: " + sAgreementStr + " [AutomatedAgentType::getAgreementIndices(660)]");
				return null;
			}
			
			sCurrentIssueName = st.nextToken();
			
			// find the issue name and set the index in the returned array
			nIssueNum = 0;
			bFoundIssue = false;
			bFoundValue = false;
			
			for (int i = 0; i < m_fullUtility.lstUtilityDetails.size(); ++i)
			{
				UtilityDetails utilityDetails = (UtilityDetails)m_fullUtility.lstUtilityDetails.get(i);
				
				for (int j = 0; j < utilityDetails.lstUtilityIssues.size() && !bFoundIssue; ++j)
				{
					UtilityIssue utilityIssue = (UtilityIssue)utilityDetails.lstUtilityIssues.get(j);
					
					if (utilityIssue.sAttributeName.equals(sCurrentIssueName))
					{
						bFoundIssue = true;
						
						for (int k = 0; k < utilityIssue.lstUtilityValues.size() && !bFoundValue; ++k)
						{
							UtilityValue utilityValue = (UtilityValue)utilityIssue.lstUtilityValues.get(k);
							
							if (utilityValue.sValue.equals(sCurrentIssueValue))
							{
								bFoundValue = true;
								
								CurrentAgreementIdx[nIssueNum] = k;
							}
						}
					} // end if - found issue
					
					nIssueNum++;
				} // end for - going over all issues
			}
		} // end while - has more tokens
			
		return CurrentAgreementIdx;
	}
	
    /**
     * Calculate utility value, best agreement and worse agreement at time nTimePeriod.
     * A recursive function for calculating all combination of issues.
     * @param nTimePeriod - the time period for the agreement
     */
    public void calculateValues(AbstractAutomatedAgent abstractAgent, int nCurrentTurn)
    {
        // calculate the total number of posisble agreements
        // This is done only once
        int nValuesNum = 0;
        if (m_nTotalAgreements == 0)
        {
            m_nTotalAgreements = 1;
    
            int nIssueNum = 0;
            for (int i = 0; i < m_fullUtility.lstUtilityDetails.size(); ++i)
            {
                UtilityDetails utilityDetails = (UtilityDetails)m_fullUtility.lstUtilityDetails.get(i);
                
                for (int j = 0; j < utilityDetails.lstUtilityIssues.size(); ++j)
                {
                    UtilityIssue utilityIssue = (UtilityIssue)utilityDetails.lstUtilityIssues.get(j);
                    
                    nValuesNum = utilityIssue.lstUtilityValues.size();
    
                    m_MaxIssueValues[nIssueNum] = nValuesNum;
                    m_nTotalAgreements *= nValuesNum;
                    
                    nIssueNum++;
                }
            }
        }
        
        double dAgreementTimeEffect = m_fullUtility.dTimeEffect;
        m_dSQValue = m_fullUtility.dStatusQuoValue;
        m_dSQValue = ( m_dSQValue + (dAgreementTimeEffect * (nCurrentTurn - 1))) / (double)100;
        
        m_dOptOutValue = m_fullUtility.dOptOutValue;
        m_dOptOutValue = ( m_dOptOutValue + (dAgreementTimeEffect * (nCurrentTurn - 1))) / (double)100; 

    
        abstractAgent.calculateValues(this, nCurrentTurn);
    }
    
    public double getBestAgreementValue() {
        return m_dBestAgreementValue;
    }
    
    public void setBestAgreementValue(double value) {
        m_dBestAgreementValue = value;
    }
    
    public double getWorstAgreementValue() {
        return m_dWorstAgreementValue;
    }
    
    public void setWorstAgreementValue(double value) {
        m_dWorstAgreementValue = value;
    }

    public void initializeBestAgreementIndices() {
        int nIssuesNum = getIssuesNum();
        
        for (int i = 0; i < nIssuesNum; ++i)
        {
            m_BestAgreementIdx[i] = 0;
        }
    }

    public void initializeWorstAgreementIndices() {
        int nIssuesNum = getIssuesNum();
        
        for (int i = 0; i < nIssuesNum; ++i)
        {
            m_WorstAgreementIdx[i] = 0;
        }
    }

    public double getAgreementTypeEffect() {
        return m_fullUtility.dTimeEffect;
    }

    public void setBestAgreementIndices(int[] currentAgreementIdx) {
        int nIssuesNum = getIssuesNum();
        
        for (int k = 0; k < nIssuesNum; ++k)
        {
            m_BestAgreementIdx[k] = currentAgreementIdx[k];
        }
    }

    public void setWorstAgreementIndices(int[] currentAgreementIdx) {
        int nIssuesNum = getIssuesNum();
        
        for (int k = 0; k < nIssuesNum; ++k)
        {
            m_WorstAgreementIdx[k] = currentAgreementIdx[k];
        }
    }
}




