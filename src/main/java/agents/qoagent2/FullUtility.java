package agents.qoagent2;

import java.util.ArrayList;
import agents.qoagent2.UtilityDetails;

public class FullUtility
{
	public double dTimeEffect;
	public double dStatusQuoValue;
	public double dOptOutValue;
	public ArrayList<UtilityDetails> lstUtilityDetails; // list of UtilityDetails
	
	public FullUtility()
	{
		dTimeEffect = 0;
		dStatusQuoValue = 0;
		dOptOutValue = 0;
		lstUtilityDetails = new ArrayList<UtilityDetails>();
	}
}