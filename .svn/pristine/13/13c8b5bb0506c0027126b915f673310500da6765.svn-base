package agents.qoagent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;


/*
 * Created on 11/09/2004
 *
 */

/**
 * @author raz
 * @version 1.0
 *
 * AutomatedAgentsCore class: 
 * In charge of handling the different agent's types.
 * In charge for returning the agreements for the desired type.
 * 
 * @see AutomatedAgent
 * @see AutomatedAgentType
 */

public class AutomatedAgentsCore {
    //General constants for reading the utility data from the utilities fils
	public static final String COMMENT_CHAR_STR = "#";
	public static final String ISSUE_HEADER_STR = "!";
	public static final String ISSUE_SEPARATOR_STR = "*";	
	public static final String VALUES_UTILITY_SEPARATOR_STR = " ";
	public static final String VALUES_NAMES_SEPARATOR_STR = "~";
	public static final String GENERAL_DATA_SEPARATOR_STR = "@";
	public static final String TIME_EFFECT_STR = "Time-Effect";
	public static final String OPT_OUT_STR = "Opt-Out";
	public static final String STATUS_QUO_STR = "Status-Quo";
	public static final int TIME_EFFECT_IND = 0;
	public static final int OPT_OUT_IND = 1;
	public static final int STATUS_QUO_IND = 2;
	public static final int GENERAL_VALUES_NUM = 3;
	
    /*
     * @@ This version supports 3 types of utilities:
     * Long term, short term and compromise orientation type
     */
	public static final int LONG_TERM_TYPE_IDX = 0;
	public static final int SHORT_TERM_TYPE_IDX = 1;
	public static final int COMPROMISE_TYPE_IDX = 2;
	public static final int AGENT_TYPES_NUM = 3;
    private static final String SIDE_B_COMPROMISE_UTILITY_FILE = "utilitySide_BCompromise.txt";
    private static final String SIDE_B_SHORT_TERM_UTILITY_FILE = "utilitySide_BShortTerm.txt";
    private static final String SIDE_B_LONG_TERM_UTILITY_FILE = "utilitySide_BLongTerm.txt";
    private static final String SIDE_A_COMPROMISE_UTILITY_FILE = "utilitySide_ACompromise.txt";
    private static final String SIDE_A_SHORT_TERM_UTILITY_FILE = "utilitySide_AShortTerm.txt";
    private static final String SIDE_A_LONG_TERM_UTILITY_FILE = "utilitySide_ALongTerm.txt";
    private static final String SIDE_B_COMPROMISE_NAME = "SIDE_B_COMPROMISE";
    private static final String SIDE_B_LONG_TERM_NAME = "SIDE_B_LONG_TERM";
    private static final String SIDE_B_SHORT_TERM_NAME = "SIDE_B_SHORT_TERM";
    private static final String SIDE_A_COMPROMISE_NAME = "SIDE_A_COMPROMISE";
    private static final String SIDE_A_LONG_TERM_NAME = "SIDE_A_LONG_TERM";
    private static final String SIDE_A_SHORT_TERM_NAME = "SIDE_A_SHORT_TERM";
        
	
	// list of all possible Side A agent types (Eng/Emp)
    // each value is FullUtility
	private ArrayList<AutomatedAgentType> m_SideA_AgentTypesList; 
	//	list of all possible Side B types (Zim/Job Can)
    //  each value is FullUtility
	private ArrayList<AutomatedAgentType> m_SideB_AgentTypesList;
	
	// list of all possible Side A agent types (Eng/Emp)
    // with values of the next turn
    // each value is FullUtility
	private ArrayList<AutomatedAgentType> m_SideA_AgentTypesNextTurnList; 
	//	list of all possible Side B agent types (Zim/Job Can)
    // with values of the next turn
    // each value is FullUtility
	private ArrayList<AutomatedAgentType> m_SideB_AgentTypesNextTurnList;

	// automated agent utility values for current turn
	private AutomatedAgentType m_CurrentAgentType;
    // automated agent utility values for next turn
	private AutomatedAgentType m_CurrentAgentNextTurnType;
	// opponent type in the next turn
    private int m_nNextTurnOppType;
    
    // log file name
	private String m_sLogFileName;
	
	// inner class for calculating the agreement the
    // automated agent will offer
	private AutomatedAgentGenerateAgreement m_GenerateAgreement;
    private AgentTools agentTools = null;
    private AbstractAutomatedAgent abstractAgent = null;
	
	public class AutomatedAgentGenerateAgreement
	{
		class AutomatedAgentCombinedAgreement
		{
			public double m_dAgentAgreementValue;
			public double m_dOpponentAgreementValue;
			public String m_sAgreement;
		}
		
        private double m_dAutomatedAgentValue, m_dNextTurnAutomatedAgentValue, m_dOppSelectedValue, m_dAgentSelectedNextTurnValue, m_dOppSelectedNextTurnValue;
        private String m_sAgreement, m_sNextTurnAgreement;
        
		public AutomatedAgentGenerateAgreement()
		{
			m_dAutomatedAgentValue = AutomatedAgentType.VERY_SMALL_NUMBER;
            m_dNextTurnAutomatedAgentValue = AutomatedAgentType.VERY_SMALL_NUMBER;
            m_nNextTurnOppType = AutomatedAgentType.NO_TYPE;
            m_sAgreement = "";
            m_sNextTurnAgreement = "";
		}
		
        /**
         * Calculate agreement to send for the opponent for a given agent and a given turn
         * @param agentType - the given agent
         * @param nCurrentTurn - the current turn
         * @param bCalcForNextTurn - whether to calculate based on values of the following turn 
         * PRE-CONDITION: m_CurrentAgentType should be updated for the current turn
         */      
        public void calculateAgreement(AutomatedAgentType agentType, int nCurrentTurn, boolean bCalcForNextTurn)
		{
            if (bCalcForNextTurn)
				m_CurrentAgentNextTurnType = agentType;
            else
				m_CurrentAgentType = agentType;
            
            // if the automated agent is of Side B (Zim/Job Can) need to calculate
            // offer against opponent of Side A and vice versa
			if (m_CurrentAgentType.isTypeOf(AutomatedAgentType.SIDE_B_TYPE)) 
				calculateOfferAgainstOpponent(AutomatedAgent.SIDE_A_NAME, nCurrentTurn, bCalcForNextTurn);
			else if (m_CurrentAgentType.isTypeOf(AutomatedAgentType.SIDE_A_TYPE))
				calculateOfferAgainstOpponent(AutomatedAgent.SIDE_B_NAME, nCurrentTurn, bCalcForNextTurn);
			else
			{
				System.out.println("[AA]Agent type is unknown [AutomatedAgentsCore::calculateAgreement(204)]");
				System.err.println("[AA]Agent type is unknown [AutomatedAgentsCore::calculateAgreement(204)]");
			}
		}
		
        /**
         * This is where the real logic is done to
         * calculate agreement to send for the opponent for a given agent and a given turn
         * @param agentType - the given agent
         * @param nCurrentTurn - the current turn
         * @param bCalcForNextTurn - whether to calculate based on values of the following turn 
         * PRE-CONDITION: m_CurrentAgentType should be updated for the current turn
         */              
		public void calculateOfferAgainstOpponent(String sOpponentType, int nCurrentTurn, boolean bCalcForNextTurn)
		{
            m_dAutomatedAgentValue = AutomatedAgentType.VERY_SMALL_NUMBER;
            m_dNextTurnAutomatedAgentValue = AutomatedAgentType.VERY_SMALL_NUMBER;
            m_nNextTurnOppType = AutomatedAgentType.NO_TYPE;
            
            if (bCalcForNextTurn)
                abstractAgent.calculateOfferAgainstOpponent(m_CurrentAgentNextTurnType, sOpponentType, nCurrentTurn);
            else // calc for current turn
                abstractAgent.calculateOfferAgainstOpponent(m_CurrentAgentType, sOpponentType, nCurrentTurn);
        }
		
		public String getSelectedAutomatedAgentAgreementStr()
		{
			return m_sAgreement;
		}
		
		public double getNextTurnAgentAutomatedAgentUtilityValue()
		{
			return m_dAgentSelectedNextTurnValue;
		}
		
		public String getNextTurnAutomatedAgentAgreement()
		{
			return m_sNextTurnAgreement;
		}
		
		public double getNextTurnOpponentAutomatedAgentUtilityValue()
		{
			return m_dOppSelectedNextTurnValue;
		}
		
		public int getNextTurnOpponentType()
		{
			return m_nNextTurnOppType;
		}
        
        public void setNextTurnOpponentType(int type) {
            m_nNextTurnOppType = type;        
        }

	};
	
	/**
	 * Initializes the agent's core.
	 * Creates the different Side A agent types (Eng/Emp) and Side B agent types (Zim/Job Can). 
	 */
	public AutomatedAgentsCore(String sFileName, String sNow, AgentTools agentTools, AbstractAutomatedAgent abstractAgent)
	{
        setAgentTools(agentTools);
        setAbstractAgent(abstractAgent);
        m_CurrentAgentNextTurnType = null;
        m_SideA_AgentTypesNextTurnList = new ArrayList<AutomatedAgentType>();
        m_SideB_AgentTypesNextTurnList = new ArrayList<AutomatedAgentType>();
        
        m_sLogFileName = sFileName;
		
		m_CurrentAgentType = null;
		
		m_SideA_AgentTypesList = new ArrayList<AutomatedAgentType>();
		m_SideB_AgentTypesList = new ArrayList<AutomatedAgentType>();
		

		for (int i = 0; i < AGENT_TYPES_NUM; ++i)
		{
			m_SideA_AgentTypesList.add(i, new AutomatedAgentType());
			m_SideB_AgentTypesList.add(i, new AutomatedAgentType());

			m_SideA_AgentTypesNextTurnList.add(i, new AutomatedAgentType());
			m_SideB_AgentTypesNextTurnList.add(i, new AutomatedAgentType());
		}
		
        /*
         * @@ This version supports 3 types of utilities:
         * Long term, short term and compromise orientation type
         */        
		createSideALongTermType();
		createSideAShortTermType();
		createSideACompromiseType();
		
		createSideBLongTermType();
		createSideBShortTermType();
		createSideBCompromiseType();
	}

	/**
	 * @return AutomatedAgentType - Side A (Eng/Emp) long term type
	 */
	public AutomatedAgentType getSideALongTermType()
	{
		return (AutomatedAgentType)m_SideA_AgentTypesList.get(LONG_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side A (Eng/Emp) short term type
	 */
	public AutomatedAgentType getSideAShortTermType()
	{
		return (AutomatedAgentType)m_SideA_AgentTypesList.get(SHORT_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side A (Eng/Emp) compromise type
	 */
	public AutomatedAgentType getSideACompromiseType()
	{
		return (AutomatedAgentType)m_SideA_AgentTypesList.get(COMPROMISE_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side B (Zim/Job Can) long term type
	 */
	public AutomatedAgentType getSideBLongTermType()
	{
		return (AutomatedAgentType)m_SideB_AgentTypesList.get(LONG_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side B (Zim/Job Can) short term type
	 */
	public AutomatedAgentType getSideBShortTermType()
	{
		return (AutomatedAgentType)m_SideB_AgentTypesList.get(SHORT_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side B (Zim/Job Can) compromise type
	 */
	public AutomatedAgentType getSideBCompromiseType()
	{
		return (AutomatedAgentType)m_SideB_AgentTypesList.get(COMPROMISE_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side A (Eng/Emp) long term type
	 */
	public AutomatedAgentType getSideALongTermNextTurnType()
	{
		return (AutomatedAgentType)m_SideA_AgentTypesNextTurnList.get(LONG_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side A (Eng/Emp) short term type
	 */
	public AutomatedAgentType getSideAShortTermNextTurnType()
	{
		return (AutomatedAgentType)m_SideA_AgentTypesNextTurnList.get(SHORT_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side A (Eng/Emp) compromise type
	 */
	public AutomatedAgentType getSideACompromiseNextTurnType()
	{
		return (AutomatedAgentType)m_SideA_AgentTypesNextTurnList.get(COMPROMISE_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side B (Zim/Job Can) long term type
	 */
	public AutomatedAgentType getSideBLongTermNextTurnType()
	{
		return (AutomatedAgentType)m_SideB_AgentTypesNextTurnList.get(LONG_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side B (Zim/Job Can) short term type
	 */
	public AutomatedAgentType getSideBShortTermNextTurnType()
	{
		return (AutomatedAgentType)m_SideB_AgentTypesNextTurnList.get(SHORT_TERM_TYPE_IDX);
	}

	/**
	 * @return AutomatedAgentType - Side B (Zim/Job Can) compromise type
	 */
	public AutomatedAgentType getSideBCompromiseNextTurnType()
	{
		return (AutomatedAgentType)m_SideB_AgentTypesNextTurnList.get(COMPROMISE_TYPE_IDX);
	}
	
	/**
	 * Creates Side B (Zim/Job Can) compromise type from the utility file.
	 * Saves the type in m_SideB_AgentTypesList	  
	 */
	private void createSideBCompromiseType()
	{
		AutomatedAgentType compromiseType = new AutomatedAgentType();
		compromiseType.setAgentType(AutomatedAgentType.SIDE_B_TYPE);
		
		String sFileName = SIDE_B_COMPROMISE_UTILITY_FILE;
		
		createAgentTypeFromFile(sFileName, compromiseType);
		
		compromiseType.setName(SIDE_B_COMPROMISE_NAME);
		m_SideB_AgentTypesList.set(COMPROMISE_TYPE_IDX, compromiseType);
		
		AutomatedAgentType agentTypeNextTurn = compromiseType;
        // since this agent contains the utility values for the next turn, at the beginning
        // it should be initialized with the second turn values
		agentTypeNextTurn.calculateValues(abstractAgent, 2);
		m_SideB_AgentTypesNextTurnList.set(COMPROMISE_TYPE_IDX, agentTypeNextTurn);
	}

	/**
	 * Creates Side B (Zim/Job Can) short term type from the utility file.
	 * Saves the type in m_SideB_AgentTypesList	  
	 */
	private void createSideBShortTermType()
	{
		AutomatedAgentType shortTermType = new AutomatedAgentType();
		shortTermType.setAgentType(AutomatedAgentType.SIDE_B_TYPE);
		
		String sFileName = SIDE_B_SHORT_TERM_UTILITY_FILE;
		
		createAgentTypeFromFile(sFileName, shortTermType);
				
		shortTermType.setName(SIDE_B_SHORT_TERM_NAME);
		m_SideB_AgentTypesList.set(SHORT_TERM_TYPE_IDX, shortTermType);
		
		AutomatedAgentType agentTypeNextTurn = shortTermType;
		// since this agent contains the utility values for the next turn, at the beginning
        // it should be initialized with the second turn values
		agentTypeNextTurn.calculateValues(abstractAgent, 2);
		m_SideB_AgentTypesNextTurnList.set(SHORT_TERM_TYPE_IDX, agentTypeNextTurn);
	}

	/**
	 * Creates Side B (Zim/Job Can) long term type from the utility file.
	 * Saves the type in m_SideB_AgentTypesList	  
	 */
	private void createSideBLongTermType()
	{
		AutomatedAgentType longTermType = new AutomatedAgentType();
		longTermType.setAgentType(AutomatedAgentType.SIDE_B_TYPE);
		
		String sFileName = SIDE_B_LONG_TERM_UTILITY_FILE;
		
		createAgentTypeFromFile(sFileName, longTermType);
		
		longTermType.setName(SIDE_B_LONG_TERM_NAME);
		m_SideB_AgentTypesList.set(LONG_TERM_TYPE_IDX, longTermType);
		
		AutomatedAgentType agentTypeNextTurn = longTermType;
        // since this agent contains the utility values for the next turn, at the beginning
        // it should be initialized with the second turn values
		agentTypeNextTurn.calculateValues(abstractAgent, 2);
		m_SideB_AgentTypesNextTurnList.set(LONG_TERM_TYPE_IDX, agentTypeNextTurn);
	}

	/**
	 * Creates Side A (Eng/Emp) comrpomise type from the utility file.
	 * Saves the type in m_SideA_AgentTypesList	  
	 */
	private void createSideACompromiseType()
	{
		AutomatedAgentType compromiseType = new AutomatedAgentType();
		compromiseType.setAgentType(AutomatedAgentType.SIDE_A_TYPE);
	
		String sFileName = SIDE_A_COMPROMISE_UTILITY_FILE;
		
		createAgentTypeFromFile(sFileName, compromiseType);
		
		compromiseType.setName(SIDE_A_COMPROMISE_NAME);
		
		m_SideA_AgentTypesList.set(COMPROMISE_TYPE_IDX, compromiseType);
		
		AutomatedAgentType agentTypeNextTurn = compromiseType;
        // since this agent contains the utility values for the next turn, at the beginning
        // it should be initialized with the second turn values
		agentTypeNextTurn.calculateValues(abstractAgent, 2);
		m_SideA_AgentTypesNextTurnList.set(COMPROMISE_TYPE_IDX, agentTypeNextTurn);
	}

	/**
	 * Creates Side A (Eng/Emp) short term type from the utility file.
	 * Saves the type in m_SideA_AgentTypesList	  
	 */
	private void createSideAShortTermType()
	{
		AutomatedAgentType shortTermType = new AutomatedAgentType();
		shortTermType.setAgentType(AutomatedAgentType.SIDE_A_TYPE);
		
		String sFileName = SIDE_A_SHORT_TERM_UTILITY_FILE;
		
		createAgentTypeFromFile(sFileName, shortTermType);
		
		shortTermType.setName(SIDE_A_SHORT_TERM_NAME);
		m_SideA_AgentTypesList.set(SHORT_TERM_TYPE_IDX, shortTermType);

		AutomatedAgentType agentTypeNextTurn = shortTermType;
        // since this agent contains the utility values for the next turn, at the beginning
        // it should be initialized with the second turn values
		agentTypeNextTurn.calculateValues(abstractAgent, 2);
		m_SideA_AgentTypesNextTurnList.set(SHORT_TERM_TYPE_IDX, agentTypeNextTurn);
	}
	
	/**
	 * Creates Side A (Eng/Emp) long term type from the utility file.
	 * Saves the type in m_SideA_AgentTypesList	  
	 */
	private void createSideALongTermType()
	{
		AutomatedAgentType longTermType = new AutomatedAgentType();
		longTermType.setAgentType(AutomatedAgentType.SIDE_A_TYPE);
		
		String sFileName = SIDE_A_LONG_TERM_UTILITY_FILE;
		
		createAgentTypeFromFile(sFileName, longTermType);
		
		longTermType.setName(SIDE_A_LONG_TERM_NAME);
		m_SideA_AgentTypesList.set(LONG_TERM_TYPE_IDX, longTermType);
		
		AutomatedAgentType agentTypeNextTurn = longTermType;
        // since this agent contains the utility values for the next turn, at the beginning
        // it should be initialized with the second turn values
		agentTypeNextTurn.calculateValues(abstractAgent, 2);
		m_SideA_AgentTypesNextTurnList.set(LONG_TERM_TYPE_IDX, agentTypeNextTurn);
	}
	
	/**
	 * Creates the specific agent type from the file name
	 * Returns the new type in agentType.
	 * @param sFileName - the file name of the agent's type
	 * @param agentType - the returned agent
	 * Note: this function is identical to readUtilityFile in the Client 	  
	 */
	private void createAgentTypeFromFile(String sFileName, AutomatedAgentType agentType)
	{
		BufferedReader br = null;
		String line;
		
		double dGeneralValues[] = new double[GENERAL_VALUES_NUM];
		
		// init values to default
		dGeneralValues[TIME_EFFECT_IND] = 0;
		dGeneralValues[STATUS_QUO_IND] = AutomatedAgentType.VERY_SMALL_NUMBER;
		dGeneralValues[OPT_OUT_IND] = AutomatedAgentType.VERY_SMALL_NUMBER;
		
		try {
			br = new BufferedReader(new FileReader(sFileName));
		
			line = br.readLine();
			while (line != null)
			{
				// if comment line - continue
				if (line.startsWith(COMMENT_CHAR_STR))
					line = br.readLine();
				else 
				{
					line = readUtilityDetails(br, line, agentType.m_fullUtility.lstUtilityDetails, dGeneralValues);
					
					agentType.m_fullUtility.dTimeEffect = dGeneralValues[TIME_EFFECT_IND];
					agentType.m_fullUtility.dStatusQuoValue = dGeneralValues[STATUS_QUO_IND];
					agentType.m_fullUtility.dOptOutValue = dGeneralValues[OPT_OUT_IND];
				} // end if-else comment line? 
			} // end while - read utility details

			// calculate values for the first turn
			agentType.calculateValues(abstractAgent, 1);
			
			br.close();
		} catch (FileNotFoundException e) {
			System.out.println("[AA]Error reading " + sFileName + ": " + e.getMessage() + " [AutomatedAgentsCore::createAgentTypeFromFile(1059)]");
			System.err.println("[AA]Error reading " + sFileName + ": " + e.getMessage() + " [AutomatedAgentsCore::createAgentTypeFromFile(1059)]");
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e1) {
			System.out.println("[AA]Error reading from " + sFileName + ": " + e1.getMessage() + " [AutomatedAgentsCore::createAgentTypeFromFile(1065)]");
			System.err.println("[AA]Error reading from " + sFileName + ": " + e1.getMessage() + " [AutomatedAgentsCore::createAgentTypeFromFile(105659)]");
			e1.printStackTrace();
			System.exit(1);
		}
	}		
	
	/**
	 * Read the utility details from the agent's file
	 * @param br - the reader of the file
	 * @param line - the read line 
	 * @param lstUtilityDetails - list of the utility details
	 * @param dGeneralValues - array of the general values
	 * @return line - the new line
	 */
	public String readUtilityDetails(BufferedReader br, String line, ArrayList<UtilityDetails> lstUtilityDetails, double dGeneralValues[])
	{
		UtilityDetails utilityDetails = null;
		
		StringTokenizer st = new StringTokenizer(line);
		
		String sTitle = st.nextToken();
		
		if (sTitle.equals(GENERAL_DATA_SEPARATOR_STR)) // general details line
		{
			String sType = st.nextToken();
			
			String sValue = st.nextToken();
			Double dTemp = new Double(sValue);
			
			if (sType.equals(TIME_EFFECT_STR))
				dGeneralValues[TIME_EFFECT_IND] = dTemp.doubleValue();
			if (sType.equals(STATUS_QUO_STR))
				dGeneralValues[STATUS_QUO_IND] = dTemp.doubleValue();
			if (sType.equals(OPT_OUT_STR))
				dGeneralValues[OPT_OUT_IND] = dTemp.doubleValue();
			
			try {
				line = br.readLine();
			} catch (IOException e) {
				System.out.println("[AA]IOException: Error reading file: " + e.getMessage() + " [AutomatedAgentsCore::readUtilityDetails(1105)]");
				System.err.println("[AA]IOException: Error reading file: " + e.getMessage() + " [AutomatedAgentsCore::readUtilityDetails(1105)]");
			}
		}
		else if (sTitle.equals(ISSUE_HEADER_STR))
		{
			utilityDetails = new UtilityDetails();
			
			// need to add new element to the utilityDetails list
			
			// get the title
			sTitle = line.substring(1);
			sTitle.trim();
			
			utilityDetails.sTitle = sTitle;

			try {
				do {
					line = br.readLine();
				} while ( (line != null) && (line.startsWith(COMMENT_CHAR_STR)));
				
				while (line != null && !line.startsWith(ISSUE_HEADER_STR))
				{
					// get the attribute name and side
					UtilityIssue utilityIssue = new UtilityIssue();
					utilityIssue.sAttributeName = line.substring(0, line.indexOf(ISSUE_SEPARATOR_STR));
					String sTemp = line.substring(line.indexOf(ISSUE_SEPARATOR_STR) + 1);
					utilityIssue.sSide = sTemp.substring(0, sTemp.indexOf(ISSUE_SEPARATOR_STR));
					sTemp = sTemp.substring(sTemp.indexOf(ISSUE_SEPARATOR_STR) + 1);
					utilityIssue.dAttributeWeight = new Double(sTemp).doubleValue();
					
					 do{ //skips comment lines
				         line=br.readLine();
				      }while((line!=null)&&(line.startsWith(COMMENT_CHAR_STR)));
					
					// read values line
					if (line != null && !line.startsWith(ISSUE_HEADER_STR))
					{
						String sUtilityLine;
						// read utility values line
						 do{
				            sUtilityLine=br.readLine();
				         }while((sUtilityLine!=null)&&(sUtilityLine.startsWith(COMMENT_CHAR_STR)));
						String sTimeEffectLine = "";
						
						StringTokenizer stUtilities = null;
						StringTokenizer stTimeEffect = null;
						
						if (sUtilityLine != null && !sUtilityLine.startsWith(ISSUE_HEADER_STR))
						{
							stUtilities = new StringTokenizer(sUtilityLine, VALUES_UTILITY_SEPARATOR_STR);
							
							// read time effect line
							 do{
				                 sTimeEffectLine=br.readLine();
				              }while((sTimeEffectLine!=null)&&(sTimeEffectLine.startsWith(COMMENT_CHAR_STR)));
														
							if (sTimeEffectLine != null && !sTimeEffectLine.startsWith(ISSUE_HEADER_STR))
								stTimeEffect = new StringTokenizer(sTimeEffectLine);
						}
						
						// get values
						StringTokenizer stValues=new StringTokenizer(line, VALUES_NAMES_SEPARATOR_STR);
						
						// go over all values
						while (stValues.hasMoreTokens())
						{
							UtilityValue utilityValue = new UtilityValue();
							
							utilityValue.sValue = stValues.nextToken();
							
							// get corresponding utility value
							if (stUtilities != null && stUtilities.hasMoreTokens())
							{
								utilityValue.dUtility = new Double(stUtilities.nextToken()).doubleValue();
								//++utilityValue.dUtility += NORMALIZE_INCREMENTOR;//TODO: Currently not using normalize incrementor
							}
								
							// get corresponding time effect value
							if (stTimeEffect != null && stTimeEffect.hasMoreTokens())
							{
								utilityValue.dTimeEffect = new Double(stTimeEffect.nextToken()).doubleValue();
							}
							
							utilityIssue.lstUtilityValues.add(utilityValue);
						}
						
						// read explanation
						 do{
				              line=br.readLine();
				         }while((line!=null)&&(line.startsWith(COMMENT_CHAR_STR)));
						
						if (line != null && !line.startsWith(ISSUE_HEADER_STR))
						{
							StringTokenizer stExp = new StringTokenizer(line);
							int i = 0;
							while (stExp.hasMoreTokens())
							{
								if (i < 6)
								{
									utilityIssue.sExplanation += stExp.nextToken() + " ";
									i++;
								}
								else
								{
									utilityIssue.sExplanation += "\n" + stExp.nextToken() + " ";
									i = 0;
								}
							}
							
							// read next line for the next iteration
						 do{
				               line=br.readLine();
				         }while((line!=null)&&(line.startsWith(COMMENT_CHAR_STR)));
						}
					} // end if - line starts with ! (ISSUE_HEADER_STR)
					
					utilityDetails.lstUtilityIssues.add(utilityIssue);
				} // end while - reading attributes
			} catch (IOException e) {
				System.out.println("[AA]IOException: Error reading file: " + e.getMessage() + " [AutomatedAgentsCore::readUtilityDetails(1225)]");
				System.err.println("[AA]IOException: Error reading file: " + e.getMessage() + " [AutomatedAgentsCore::readUtilityDetails(1225)]");
			}
					
			lstUtilityDetails.add(utilityDetails);
		} // end if - line starts new issue
		
		return line;		
	}

    /**
     * Update the agreement values based on a given turn
     * @param ntimePeriod - the specific turn
     */    
	public void updateAgreementsValues(int nTimePeriod)
	{
		AutomatedAgentType agentType = null;
		AutomatedAgentType agentTypeNextTurn = null;
		for (int i = 0; i < AGENT_TYPES_NUM; ++i)
		{
			agentType = (AutomatedAgentType)m_SideA_AgentTypesList.get(i);
			agentType.calculateValues(abstractAgent, nTimePeriod);
			m_SideA_AgentTypesList.set(i, agentType);
			
			agentTypeNextTurn = agentType;
			agentTypeNextTurn.calculateValues(abstractAgent, nTimePeriod + 1);
			m_SideA_AgentTypesNextTurnList.set(i, agentTypeNextTurn);
			
			agentType = (AutomatedAgentType)m_SideB_AgentTypesList.get(i);
			agentType.calculateValues(abstractAgent, nTimePeriod);
			m_SideB_AgentTypesList.set(i, agentType);

			agentTypeNextTurn = agentType;
			agentTypeNextTurn.calculateValues(abstractAgent, nTimePeriod + 1);
			m_SideB_AgentTypesNextTurnList.set(i, agentTypeNextTurn);
		}
	}

    /**
     * Initialize the GenerateAgreement classs by a given agent
     * @param agentType - the given agent
     */    
	public void initGenerateAgreement(AutomatedAgentType agentType)
	{
		m_CurrentAgentType = agentType;
		
		m_GenerateAgreement = new AutomatedAgentGenerateAgreement();
	}
	
    /**
     * Calculate agreement to send for the opponent for a given agent and a given turn
     * @param agentType - the given agent
     * @param nCurrentTurn - the current turn
     */      
	public void calculateAgreement(AutomatedAgentType agentType, int nCurrentTurn)
	{
		m_GenerateAgreement.calculateAgreement(agentType, nCurrentTurn, false);
	}
	
   /**
     * Return the agreement the automated agent selected to offer
     * @return String - the automated agent offer as String
     */         
	public String getAutomatedAgentAgreement()
	{
		return m_GenerateAgreement.getSelectedAutomatedAgentAgreementStr();
	}
	
    /**
     * Calculate agreement for a given agent for the following turn
     * @param agentType - the given agent
     * @param nNextTurn - the next turn
     */  	
	public void calculateNextTurnAgreement(AutomatedAgentType agentType, int nNextTurn)
	{
		m_GenerateAgreement.calculateAgreement(agentType, nNextTurn, true);
	}
	
   /**
     * Return the agreement the automated agent selected to offer based on next turn values
     * @return double - the automated agent offer's value
     */         
    public double getNextTurnAutomatedAgentUtilityValue()
	{
		return m_GenerateAgreement.getNextTurnAgentAutomatedAgentUtilityValue();
	}
	
    /**
     * Return the agreement the automated agent selected to offer based on next turn values
     * @return String - the automated agent offer as String
     */         
	public String getNextTurnAutomatedAgentAgreement()
	{
		return m_GenerateAgreement.getNextTurnAutomatedAgentAgreement();
	}
	
   /**
     * Return the opponnet's value in the next turn for the agreement the automated agent selected 
     * @return double - the opponent's agent offer's value
     */         
	public double getNextTurnOpponentAutomatedAgentUtilityValue()
	{
		return m_GenerateAgreement.getNextTurnOpponentAutomatedAgentUtilityValue();
	}

   /**
     * Return the type of the opponent 
     * @return AutomatedAgentType - the opponent's type
     */    
	public AutomatedAgentType getNextTurnOpponentType()
	{
		AutomatedAgentType opponentNextTurnType = null;
		int nOppType = m_GenerateAgreement.getNextTurnOpponentType();
		
		if (m_CurrentAgentType.isTypeOf(AutomatedAgentType.SIDE_B_TYPE))
		{
			switch (nOppType)
			{
				case COMPROMISE_TYPE_IDX:
					opponentNextTurnType = getSideACompromiseNextTurnType();
					break;
				case LONG_TERM_TYPE_IDX:
					opponentNextTurnType = getSideALongTermNextTurnType();
					break;
				case SHORT_TERM_TYPE_IDX:
					opponentNextTurnType = getSideAShortTermNextTurnType();
					break;
				default:
					System.out.println("[AA]Agent type is unknown [AutomatedAgentsCore::getNextTurnOpponentType(1310)]");
					System.err.println("[AA]Agent type is unknown [AutomatedAgentsCore::getNextTurnOpponentType(1310)]");
					break;
			}
		}
		else if (m_CurrentAgentType.isTypeOf(AutomatedAgentType.SIDE_A_TYPE))
		{
			switch (nOppType)
			{
				case COMPROMISE_TYPE_IDX:
					opponentNextTurnType = getSideBCompromiseNextTurnType();
					break;
				case LONG_TERM_TYPE_IDX:
					opponentNextTurnType = getSideBLongTermNextTurnType();
					break;
				case SHORT_TERM_TYPE_IDX:
					opponentNextTurnType = getSideBShortTermNextTurnType();
					break;
				default:
					System.out.println("[AA]Agent type is unknown [AutomatedAgentsCore::getNextTurnOpponentType(1329)]");
					System.err.println("[AA]Agent type is unknown [AutomatedAgentsCore::getNextTurnOpponentType(1329)]");
					break;
			}
		}
		
		return opponentNextTurnType;
	}

    public void setAgentTools(AgentTools agentTools) {
        this.agentTools = agentTools;
    }

    public void setAbstractAgent(AbstractAutomatedAgent abstractAgent) {
        this.abstractAgent = abstractAgent;
    }

    public double getNextTurnAutomatedAgentValue() {
       return m_GenerateAgreement.m_dNextTurnAutomatedAgentValue;
    }

    public double getCurrentTurnAutomatedAgentValue() {
        return m_GenerateAgreement.m_dAutomatedAgentValue;
    }
    
    public void setNextTurnAutomatedAgentValue(double agreementValue) {
        m_GenerateAgreement.m_dNextTurnAutomatedAgentValue = agreementValue;        
    }

    public void setCurrentTurnAutomatedAgentValue(double agreementValue) {
        m_GenerateAgreement.m_dAutomatedAgentValue = agreementValue;        
    }
    
    public void setNextTurnAutomatedAgentSelectedValue(double agreementValue) {
        m_GenerateAgreement.m_dAgentSelectedNextTurnValue = agreementValue;
    }

    public void setNextTurnOpponentSelectedValue(double agreementValue) {
        m_GenerateAgreement.m_dOppSelectedNextTurnValue = agreementValue;
    }
    
    public void setCurrentTurnOpponentSelectedValue(double agreementValue) {
        m_GenerateAgreement.m_dOppSelectedValue = agreementValue;
    }

    public void setNextTurnAgreementString(String agreementStr) {
        m_GenerateAgreement.m_sNextTurnAgreement = agreementStr;        
    }
    
    public void setCurrentTurnAgreementString(String agreementStr) {
        m_GenerateAgreement.m_sAgreement = agreementStr;        
    }

    public void setNextTurnOpponentType(int type) {
        m_GenerateAgreement.setNextTurnOpponentType(type);        
    }
}
