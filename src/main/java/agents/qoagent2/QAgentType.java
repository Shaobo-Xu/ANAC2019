package agents.qoagent2;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.StringTokenizer;

/*
 * Created on 11/09/2004
 *
 */

/**
 * @author raz
 * @version 1.0
 * @see QAgentsCore
 * @see QOAgent
 */
public class QAgentType {
	public static final double PRECISION_VALUE = 0.3; // TODO: Change precision value
	
	public static final int VERY_SMALL_NUMBER = -9999;
	public static final int VERY_HIGH_NUMBER = 9999;
	public static final int LOWER_THAN_SQ_RET_VAL = VERY_SMALL_NUMBER * 10;
	public static final int MAX_ISSUES = 20;
	public static final int NO_VALUE = -1;	
	
	public FullUtility m_fullUtility;
	
	private double m_dTypeProbability;
	private double m_dTotalAgreementsPoints;
	private double m_dTotalAgreementsPointsAboveSQ;//25-09-05
	private double m_dSQValue;//25-09-05
	private double m_dMaxValue;//06-05-06
	
	private double m_dBestAgreementValue;
	private double m_dWorstAgreementValue;
	
	private int m_MaxIssueValues[];
	private int m_BestAgreementIdx[];
	private int m_WorstAgreementIdx[];
	private int m_nTotalAgreements;
	
	private int m_nType;
	
	public static final int NO_TYPE = -1;
	public static final int ENGLAND_TYPE = 0;
	public static final int ZIMBABWE_TYPE = 1;

	private Hashtable<String, Integer> m_mapAgreementToRanking;
	
	private boolean m_bEquilibriumAgent = false;

    private static final int BEST_OFFER_REMOVAL = 5;
	
	private class IdxToValue
	{
		public int AgreementIdx[];
		public double dAgreementValue;
		
		IdxToValue()
		{
			dAgreementValue = 0;
			AgreementIdx = new int[MAX_ISSUES];
		}
	}
	
	/**
	 * Initializes the agent.
	 */
	public QAgentType(boolean bEquilibriumAgent)
	{
		m_bEquilibriumAgent = bEquilibriumAgent;
		m_mapAgreementToRanking = new Hashtable();
		m_nType = NO_TYPE;
		
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
		
		m_dTotalAgreementsPoints = 0;
		m_dTotalAgreementsPointsAboveSQ = 0;
		m_dSQValue = VERY_SMALL_NUMBER;
		m_dMaxValue = VERY_HIGH_NUMBER; //06-05-06
		
		m_dTypeProbability = (double)1 / (double)QAgentsCore.AGENT_TYPES_NUM;
	}
	
	/**
	 * @return total number of issues
	 */
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
	 * @param nIssueNum - the issue number
	 * @return the maximum value for the issue
	 */
	public int getMaxIssueValue(int nIssueNum)
	{
		return m_MaxIssueValues[nIssueNum];
	}
	
	/**
	 * @return m_nTotalAgreements - number of total possible agreeements
	 */
	public int getTotalAgreements()
	{
		return m_nTotalAgreements;
	}
	
	/**
	 * Sets the agent type - either NO_TYPE, ENGLAND_TYPE or ZIMBABWE_TYPE
	 * @param nType - the agent's type
	 */
	public void setAgentType(int nType)
	{
		m_nType = nType;
	}

	//25-09-05
	public double getSQValue() {
		return m_dSQValue;
	}
	
	//06-05-06
	public double getMaxValue() {
	    return m_dMaxValue;
	}
	/**
	 * Calculate luce numbers, best agreement and worse agreement at time nTimePeriod.
	 * A recursive function for calculating all combination of issues.
	 * @param nTimePeriod - the time period for the agreement
	 */
	public void calculateValues(int nCurrentTurn)
	{
		m_dTotalAgreementsPoints = 0;
		m_dTotalAgreementsPointsAboveSQ = 0;
		
		double dAgreementValue = 0;
		int nValuesNum = 0;
		
		// initialization
		m_dBestAgreementValue = VERY_SMALL_NUMBER;
		m_dWorstAgreementValue = VERY_HIGH_NUMBER;

		//??m_fullUtility = new FullUtility();

		int nIssuesNum = getIssuesNum();
		
		int CurrentAgreementIdx[] = new int[nIssuesNum];
		
		for (int i = 0; i < nIssuesNum; ++i)
		{
			m_BestAgreementIdx[i] = 0;
			m_WorstAgreementIdx[i] = 0;
			CurrentAgreementIdx[i] = 0;
		}
		
		// calculate this only once
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
	
		IdxToValue AgreementRankings[] = new IdxToValue[m_nTotalAgreements];
		
		for (int i = 0; i < m_nTotalAgreements; ++i)
		{
			AgreementRankings[i] = new IdxToValue();
			
			dAgreementValue = getAgreementValue(CurrentAgreementIdx, nCurrentTurn);
			
			// calculate the total possible agreements points
		
			// the luce number is the value of a specific agreement divided by
			// the total possible agreements points

			// check for best agreement
			if (dAgreementValue > m_dBestAgreementValue)
			{
				m_dBestAgreementValue = dAgreementValue;

				// save agreement
				for (int k = 0; k < nIssuesNum; ++k)
				{
					m_BestAgreementIdx[k] = CurrentAgreementIdx[k];
				}
			}						
						
			// check for worst agreement
			if (dAgreementValue < m_dWorstAgreementValue)
			{
				m_dWorstAgreementValue = dAgreementValue;
				// save agreement
				for (int k = 0; k < nIssuesNum; ++k)
				{
					m_WorstAgreementIdx[k] = CurrentAgreementIdx[k];
				}
			}						
						
			// @@ - CHECK //@@ remove next line, uncomment the line next
			m_dTotalAgreementsPoints += dAgreementValue;
			//m_dTotalAgreementsPoints += Math.exp(dAgreementValue * PRECISION_VALUE);
		
			
			//25-09-05
			//---------------
			double dTimeEffectForSQ = m_fullUtility.dTimeEffect;
			m_dSQValue = m_fullUtility.dStatusQuoValue;
			m_dSQValue = ( m_dSQValue + (dTimeEffectForSQ * (nCurrentTurn - 1))) / (double)100;
			double originalAgreementValue = Math.log(dAgreementValue) / PRECISION_VALUE;
			if (originalAgreementValue > m_dSQValue)
				m_dTotalAgreementsPointsAboveSQ += dAgreementValue;
			//---------------
			
			// insert agreement value and indices to the ranking array
			// use insertion sort
			boolean bFound = false;
			for (int nRankIdx = 0; nRankIdx < i && !bFound; ++nRankIdx)
			{
				if (AgreementRankings[nRankIdx].dAgreementValue > dAgreementValue)
				{
					// found insertion point
					for (int nInsertIdx = i; nInsertIdx > nRankIdx; --nInsertIdx)
					{
						for (int ind = 0; ind < CurrentAgreementIdx.length; ++ind)
							AgreementRankings[nInsertIdx].AgreementIdx[ind] = AgreementRankings[nInsertIdx - 1].AgreementIdx[ind];
						AgreementRankings[nInsertIdx].dAgreementValue = AgreementRankings[nInsertIdx - 1].dAgreementValue; 
					}
					
					for (int ind = 0; ind < CurrentAgreementIdx.length; ++ind)
						AgreementRankings[nRankIdx].AgreementIdx[ind] = CurrentAgreementIdx[ind];
					AgreementRankings[nRankIdx].dAgreementValue = dAgreementValue;
					
					bFound = true;
				}
			}
			
			if (!bFound)
			{
				for (int ind = 0; ind < CurrentAgreementIdx.length; ++ind)
					AgreementRankings[i].AgreementIdx[ind] = CurrentAgreementIdx[ind];
				AgreementRankings[i].dAgreementValue = dAgreementValue;
			}
			
			// receiveMessage issue values indices - for next loop of new indices
			boolean bFinishUpdate = false;
			for (int k = nIssuesNum-1; k >= 0 && !bFinishUpdate; --k)
			{
				if (CurrentAgreementIdx[k]+1 >= m_MaxIssueValues[k])
				{
					CurrentAgreementIdx[k] = 0;
				}
				else
				{
					CurrentAgreementIdx[k]++;
					bFinishUpdate = true;
				}									
			}
		}
		
		// set the rankings in the map
		for (int i = 0; i < m_nTotalAgreements; ++i)
		{
			String sAgreement = getAgreementStr(AgreementRankings[i].AgreementIdx);
			m_mapAgreementToRanking.put(sAgreement, new Integer(i+1));
		}
		
		//06-05-06
		// find the max value as for the current turn
		// each turn, remove X best offers from being offered
		int ind = AgreementRankings.length - ( (BEST_OFFER_REMOVAL) * nCurrentTurn );
		m_dMaxValue = AgreementRankings[ind].dAgreementValue;
		
		//printRankings();//@@		
	}

	// calculate probabilities and values upon rejection
	public double calcRejectionProbabilities(String sRejectedMsg, int nCurrentTurn)
	{
		int nIssuesNum = getIssuesNum();
		int CurrentAgreementIdx[] = new int[nIssuesNum];
		for (int i = 0; i < nIssuesNum; ++i)
			CurrentAgreementIdx[i] = 0;

		String sAgreement;
		int nRanking = 0;
		
		int nMessageRanking = getAgreementRanking(sRejectedMsg);
		double dPrevTypeProbability = getTypeProbability();
		double dOfferValue = 0;
		double dOffersSum = 0;
		double dOfferProbability = 0;

		for (int i = 0; i < m_nTotalAgreements; ++i)
		{
			sAgreement = getAgreementStr(CurrentAgreementIdx);
			
			nRanking = getAgreementRanking(sAgreement);
			
			if (nRanking > nMessageRanking)
			{
				dOfferValue = getAgreementValue(CurrentAgreementIdx, nCurrentTurn);
				dOfferProbability = getAgreementLuceValue(dOfferValue, true);
				
				dOffersSum += (dOfferProbability * dPrevTypeProbability);
			}
			
			// receiveMessage issue values indices - for next loop of new indices
			boolean bFinishUpdate = false;
			for (int k = nIssuesNum-1; k >= 0 && !bFinishUpdate; --k)
			{
				if (CurrentAgreementIdx[k]+1 >= m_MaxIssueValues[k])
				{
					CurrentAgreementIdx[k] = 0;
				}
				else
				{
					CurrentAgreementIdx[k]++;
					bFinishUpdate = true;
				}									
			}
		}
		
		return dOffersSum;
	}
	
	//@@
	
	public void printRankings()
	{
		int nIssuesNum = getIssuesNum();
		int CurrentAgreementIdx[] = new int[nIssuesNum];
		for (int i = 0; i < nIssuesNum; ++i)
			CurrentAgreementIdx[i] = 0;

		String sAgreement;
		int nRanking = 0;
		double dRankingProb = 0;
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter("test.txt"), true);
		} catch (IOException e) {
			System.out.println("Error opening test file [QAgentsType::printRankings(308)]");
			System.err.println("Error opening test file [QAgentsType::printRankings(308)]");
			e.printStackTrace();
		}
		out.println("Rank\tProb.\tAgr.");
		for (int i = 0; i < m_nTotalAgreements; ++i)
		{
			sAgreement = getAgreementStr(CurrentAgreementIdx);
			
			nRanking = getAgreementRanking(sAgreement);
			
			dRankingProb = getAgreementRankingProbability(CurrentAgreementIdx);
			
			out.println(nRanking + "\t" + dRankingProb + "\t" + sAgreement);
		
			// receiveMessage issue values indices - for next loop of new indices
			boolean bFinishUpdate = false;
			for (int k = nIssuesNum-1; k >= 0 && !bFinishUpdate; --k)
			{
				if (CurrentAgreementIdx[k]+1 >= m_MaxIssueValues[k])
				{
					CurrentAgreementIdx[k] = 0;
				}
				else
				{
					CurrentAgreementIdx[k]++;
					bFinishUpdate = true;
				}									
			}
		}
		
		out.close();
	}
	
	/**
	 * @param nAgentType - the agent's type (either NO_TYPE, ENGLAND_TYPE or ZIMBABWE_TYPE)
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
	 * @return best agreement value
	 */
	public double getBestAgreementValue(int nCurrentTurn)
	{
		return getAgreementValue(m_BestAgreementIdx, nCurrentTurn);	
	}
	
	/**
	 * @return best agreement as string
	 */
	public String getBestAgreementStr()
	{
		return getAgreementStr(m_BestAgreementIdx);	
	}

	/**
	 * @return worst agreement value
	 */
	public double getWorstAgreementValue(int nCurrentTurn)
	{
		return getAgreementValue(m_WorstAgreementIdx, nCurrentTurn);	
	}

	/**
	 * @return worst agreement as string
	 */
	public String getWorstAgreementStr()
	{
		return getAgreementStr(m_WorstAgreementIdx);	
	}

	private int getAgreementRanking(String sAgreement)
	{
		int nRank = 0;
		
		Integer n = (Integer)m_mapAgreementToRanking.get(sAgreement);
		
		if (n != null)
			nRank = n.intValue();
		else
			System.err.println("ERR");//@@
		
		return nRank;
	}
	
	public double getAgreementRankingProbability(int CurrentAgreementIdx[])
	{
		String sAgreement = getAgreementStr(CurrentAgreementIdx);
		
		int nRank = getAgreementRanking(sAgreement);
		
		return ((double)nRank / (double)m_nTotalAgreements); 
	}
	
	/**
	 * @param dAgreementValue - the agreement value
	 * @return the luce value for the given agreement
	 */
	public double getAgreementLuceValue(double dAgreementValue)
	{
		//@@ - CHECK @@ remove next line, uncomment next line
		return dAgreementValue / m_dTotalAgreementsPointsAboveSQ;
		//return Math.exp(dAgreementValue * PRECISION_VALUE) / m_dTotalAgreementsPoints;
	}
	
	//25-09-05
	public double getAgreementLuceValue(double dAgreementValue, boolean calculateProbability)
	{
		if (calculateProbability)
			return dAgreementValue / m_dTotalAgreementsPoints;

		else // should never get here
			return LOWER_THAN_SQ_RET_VAL;
	}
	
	/**
	 * @param CurrentAgreementIdx - indices of the agreement
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
					//TODO: calculate time effect for issue in different ways:
					// (+, -, *, /)
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
		
		// m_nCurrentTurn starts from 1 but on first turn there is no time effect
		// so decrease 1 in the calculations
		
		//TODO: TIME EFFECT GOES HERE [calculate differnet ways for time effect (+, *, /)]
		dAgreementValue = dAgreementValue + ((dTimeEffect * (nCurrentTurn - 1)) / (double)100);

		if (!bAgreementHasValues) // agreement is empty - all issues has no value
			dAgreementValue = VERY_SMALL_NUMBER;
		
		//@@ - CHECK //@@ remove next line
		if (!m_bEquilibriumAgent) // if QO agent
			dAgreementValue = Math.exp(dAgreementValue * PRECISION_VALUE);
		
		return dAgreementValue;
	}

	/**
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
	
					sAgreementStr += sCurrentIssueValue + QAgentsCore.ISSUE_SEPARATOR_STR 
									+ sCurrentIssueName + QAgentsCore.ISSUE_SEPARATOR_STR; 
				}
				
				nIssueNum++;
			}
		}

		return sAgreementStr;
	}
	
	public boolean isIssueValueNoAgreement(int nIssueNum, int nIssueNumIdx)
	{
		String sIssueValue = getIssueValueStr(nIssueNum, nIssueNumIdx);
		
		if (sIssueValue.equals(QOAgent.NOT_APPLICABLE_STR1))
			return true;
		else
			return false;		
	}
	
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
				if ((nCurrentIssueNum == nIssueNum)&&(!bFound))
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
	 * Sets the probability of the type
	 * @param dProbability - the new probability
	 */
	public void setTypeProbability(double dProbability)
	{
		m_dTypeProbability = dProbability;
	}
	
	/**
	 * @return the probability of the type.
	 */
	public double getTypeProbability()
	{
		return m_dTypeProbability;
	}
	
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
		StringTokenizer st = new StringTokenizer(sAgreementStr, QAgentsCore.ISSUE_SEPARATOR_STR);
		
		// the agreement string has the following structure:
		// issue_value SEPARATOR issue_name SEPARATOR...
		while (st.hasMoreTokens())
		{
			// get issue value
			sCurrentIssueValue = st.nextToken();
			
			if (!st.hasMoreTokens())
			{
				// this is an error
				System.out.println("[QO]ERROR: Invalid agreement structure: " + sAgreementStr + " [QAgentType::getAgreementIndices(660)]");
				System.err.println("[QO]ERROR: Invalid agreement structure: " + sAgreementStr + " [QAgentType::getAgreementIndices(660)]");
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
	
	// temp function
	public void printValuesToFile(String sSourceFileName)
	{
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter("values.txt", true));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		pw.println("------------" + sSourceFileName + "----------");
		
		double dAgreementValue = 0;
		
		int nIssuesNum = getIssuesNum();
		
		int CurrentAgreementIdx[] = new int[nIssuesNum];
		
		for (int i = 0; i < nIssuesNum; ++i)
			CurrentAgreementIdx[i] = 0;
		
		for (int i = 0; i < m_nTotalAgreements; ++i)
		{
			dAgreementValue = getAgreementValue(CurrentAgreementIdx, 1);

			pw.println(dAgreementValue);
			
			// receiveMessage issue values indices - for next loop of new indices
			boolean bFinishUpdate = false;
			for (int k = nIssuesNum-1; k >= 0 && !bFinishUpdate; --k)
			{
				if (CurrentAgreementIdx[k]+1 >= m_MaxIssueValues[k])
				{
					CurrentAgreementIdx[k] = 0;
				}
				else
				{
					CurrentAgreementIdx[k]++;
					bFinishUpdate = true;
				}									
			}
		}
		
		pw.close();
	}
}
