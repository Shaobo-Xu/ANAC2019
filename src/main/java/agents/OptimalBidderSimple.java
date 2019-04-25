/** 
 * 	OptimalBidder: using the optimal stopping rule (cutoffs) for bidding. 
 * 	B_{j+1} = 1/2 + 1/2 B_j^2
 * 
 * 
 * 
 * @author rafik		
 ************************************************************************************************************************************/

package agents;

import java.util.HashMap;

import genius.core.SupportedNegotiationSetting;
import genius.core.issue.ISSUETYPE;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;

public class OptimalBidderSimple extends OptimalBidder {
	@Override
	public void init() {
		partitions = 1000;
		rv = utilitySpace.getReservationValue();
		super.init();
	}

	@Override
	public String getName() {
		return "Optimal Bidder Simple";
	}

	/**
	 * computation of the bid for round j as in prop 4.3
	 * 
	 * @param round
	 *            j
	 * @return bid value
	 **/
	@Override
	public double bid(int j) {
		if (j == 1)
			return 0.5 + 0.5 * rv;
		else
			return 0.5 + 0.5 * Math.pow(bid(j - 1), 2);
	}

	/**
	 * Getting the issue's values
	 * 
	 * @param void
	 * @return void
	 * @throws Exception
	 **/
	@Override
	public void getValues() throws Exception {
		if (pie.getType().equals(ISSUETYPE.DISCRETE)) {
			IssueDiscrete discrete_pie = (IssueDiscrete) pie;
			int nvalues = discrete_pie.getNumberOfValues();
			print("   #values = " + nvalues);
			values = new HashMap<Integer, Value>(nvalues);
			for (int i = 0; i < nvalues; i++) {
				ValueDiscrete value = discrete_pie.getValue(i);
				values.put(i, value);
				// print( " values[" + i + "] = " + value);
			}
		} else {
			throw new Exception(
					"Type " + pie.getType() + " not supported by " + getName());
		}
	}

	@Override
	public String getVersion() {
		return "v1.0";
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "using the optimal stopping rule (cutoffs) for bidding";
	}

}