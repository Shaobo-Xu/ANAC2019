/** 
 * 	OptimalBidder: using the optimal stopping rule (cutoffs) for bidding. 
 * 	B_{j+1} = 1/2 + 1/2 B_j^2
 * @author rafik		
 ************************************************************************************************************************************/

package agents;

import java.util.HashMap;

import genius.core.issue.ISSUETYPE;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

public class OptimalBidderSimpleU extends OptimalBidderU {
	private static double rvB;

	public void init() {
		partitions = 1000;
		super.init();
	}

	@Override
	public String getName() {
		return "OptimalBidderSimpleU";
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
			return 0.5 + 0.5 * rvB;
		else
			return 0.5 + 0.5 * Math.pow(bid(j - 1), 2);
	}

	/**
	 * reservation value: if the reservation value is already set (<> -1.0)
	 * simply return it, otherwise get it from the utility space
	 * 
	 * @param double
	 * @return double
	 * @throws Exception
	 **/
	@Override
	public double getReservationValue(double arg) throws Exception {
		boolean flag = true;
		double rv = 0.0;

		if (arg == -1.0) // first time // TODO rmeove the -1, rv, etc.
		{
			if (pie.getType().equals(ISSUETYPE.DISCRETE)) // get/set rvB...
			{
				IssueDiscrete discrete_pie = (IssueDiscrete) pie;
				int nvalues = discrete_pie.getNumberOfValues();
				print("   nvalues = " + nvalues);
				values = new HashMap<Integer, Value>(nvalues);

				for (int i = 0; i < nvalues; i++) {
					ValueDiscrete value = discrete_pie.getValue(i);
					Evaluator evaluator = ((AdditiveUtilitySpace) utilitySpace)
							.getEvaluator(pie.getNumber());
					Integer evaluation = ((EvaluatorDiscrete) evaluator)
							.getValue((ValueDiscrete) value);
					values.put(i, value);

					if (evaluation != 0 && flag) // reaching rvB
					{
						rv = (double) i / partitions; // rvB normalized
						utilitySpace.setReservationValue(rv); // TODO no need
																// for this,
																// remove it.
						flag = false;
						print("   rvB = " + rv);
					}
				}
				return rv;
			} else {
				throw new Exception("Type " + pie.getType()
						+ " not supported by " + getName());
			}
		} else {
			return arg;
		}
	}

	/**
	 * U_{j+1} : utility cutoffs
	 * 
	 * @param int
	 * @return double
	 * @throws Exception
	 **/

	@Override
	public double utility(int j) {
		return (Math.pow(bid(j), 2) - rvB) / (1 - rvB);
	}

} // end 