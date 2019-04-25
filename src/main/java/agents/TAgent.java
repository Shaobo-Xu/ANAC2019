/* *************************************************************************************************************************************
	   TAgent: using the optimal stopping rule (cutoffs) for bidding. 
	    		   Estimates rounds based on own/opponent actions.	
	    		   Uses all the cutoffs (all_osr_cuttofs=true)
	          
	    TODO tournament pb, Java heap space..
	
		@author rafik hadfi <rafik@itolab.nitech.ac.jp>
 * ***********************************************************************************************************************************/

package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import genius.core.Agent;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.ActionWithBid;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.timeline.Timeline;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

public class TAgent extends Agent {
	private Action actionOfPartner = null;
	private static int counter, index, idx, m, d, n;

	private static boolean flag, all_osr_cuttofs, two_osr_subsets, unused;

	private static double rvB, totalTime, timeLeftBeforeAction,
			timeLeftAfterAction, OpponentMaxDuration, OwnMaxDuration;

	private static Stack<HashMap<Value, Double>> stack_h = null,
			stack_aux = null, stack_hc = null;
	private static ArrayList<ValueDiscrete> max_values = null;

	// init is called when a next session starts with the same opponent

	// #### cutoffs
	// #################################################################################
	public double bids(int j) {
		return (j == 1) ? 0.5 * rvB / n + 0.5 : 0.5 * sq(bids(j - 1)) + 0.5; // adding
																				// scaling.
	}

	// ###############################################################################################
	public double bids_(double Bj) {
		return Math.sqrt(2 * Bj - 1);
	}

	/***************************************************************************************************************************/
	public void init() {
		try {
			totalTime = timeline.getTotalTime();
			timeLeftAfterAction = timeline.getCurrentTime();
			timeLeftBeforeAction = OpponentMaxDuration = OwnMaxDuration = 0;
			counter = index = idx = 0;
			rvB = 0.0;
			m = 5;
			d = -1;
			n = 1000;

			flag = true;
			all_osr_cuttofs = true;
			two_osr_subsets = false;
			unused = false;

			stack_h = new Stack<HashMap<Value, Double>>();
			stack_aux = new Stack<HashMap<Value, Double>>();
			stack_hc = new Stack<HashMap<Value, Double>>();
			max_values = new ArrayList<ValueDiscrete>();

			int i, number_of_values = -1;

			Issue pie = utilitySpace.getDomain().getIssues().get(0); // pie is
																		// the
																		// issue
			System.out.println("######   issue name = " + pie + "\n"
					+ "######   issue type = " + pie.getType());
			HashMap<Integer, Value> values = new HashMap<Integer, Value>(); // pairs
																			// <issuenumber,
																			// chosen
																			// value
																			// string>
			switch (pie.getType()) // silly way to get/set rvB...
			{
			case DISCRETE: {
				IssueDiscrete discrete_pie = (IssueDiscrete) pie;
				number_of_values = discrete_pie.getNumberOfValues();
				System.out.println("######   number_of_values = "
						+ number_of_values);

				for (i = 0; i < number_of_values; i++) {
					ValueDiscrete value = discrete_pie.getValue(i);
					values.put(pie.getNumber(), discrete_pie.getValue(i));
					// evaluation
					Evaluator eval = ((AdditiveUtilitySpace) utilitySpace)
							.getEvaluator(pie.getNumber());
					EvaluatorDiscrete evalDiscrete = (EvaluatorDiscrete) eval;
					Integer evaluation = evalDiscrete
							.getValue((ValueDiscrete) value);

					if (true)
						System.out.println("######   i="
								+ i
								+ "\t u("
								+ value.getValue()
								+ ") = "
								+ getUtility(new Bid(utilitySpace.getDomain(),
										values)) + "\t" + "  eval("
								+ value.getValue() + ") = " + evaluation);

					if (i > (number_of_values - m))
						max_values.add(value);

					if (evaluation != 0 && flag) // reaching rvB
					{
						rvB = i;
						utilitySpace.setReservationValue(rvB);
						flag = false;
					}
				}

				System.out
						.println("\n#########  generating an example of bids vector ###################################");

				// generating an example of bids vector

				for (i = 1; i < ((IssueDiscrete) pie).getNumberOfValues(); i++) {
					HashMap<Value, Double> h = new HashMap<Value, Double>();
					h.put(discrete_pie.getValue(i), new Double(n * bids(i))); // using
																				// scaling
					stack_h.push(h);
				}

				// make a copy of stack_h
				stack_hc.addAll(stack_h);

				// new stack, without redundancies.
				stack_aux.push(stack_h.pop());

				while (!stack_h.isEmpty()) {
					if (stack_h
							.get(stack_h.size() - 1)
							.values()
							.iterator()
							.next()
							.equals(stack_aux.get(stack_aux.size() - 1)
									.values().iterator().next()))
						stack_h.pop();
					else
						stack_aux.push(stack_h.pop());
				}

				for (i = 0; i < stack_aux.size(); i++)
					System.out.println("######     " + i + "  =>  "
							+ stack_aux.get(i));
				break;
			}// case
			default:
				throw new Exception("Type " + pie.getType()
						+ " not supported by TAgent.");
			} // switch
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/********************************************************************************************************************/
	private int estimated_rounds_left(boolean opponent) {
		if (opponent == true) {
			if (timeLeftBeforeAction - timeLeftAfterAction > OpponentMaxDuration)
				OpponentMaxDuration = timeLeftBeforeAction
						- timeLeftAfterAction;
		} else // own
		{
			if (timeLeftAfterAction - timeLeftBeforeAction > OwnMaxDuration)
				OwnMaxDuration = timeLeftAfterAction - timeLeftBeforeAction;
		}

		if (OpponentMaxDuration + OwnMaxDuration == 0)
			System.out.println("./0 Exception!");

		double totalDuration = OpponentMaxDuration + OwnMaxDuration, round = (totalTime - timeline
				.getCurrentTime()) / totalDuration;

		if ((int) round > d && (int) round < 1000)
			d = (int) round;

		return (int) round;
	}

	/********************************************************************************************************************/
	@Override
	public String getVersion() {
		return "1.0 (Genius 4.2)";
	}

	@Override
	public String getName() {
		return "TAgent";
	}

	/********************************************************************************************************************/

	public void ReceiveMessage(Action opponentAction) {
		actionOfPartner = opponentAction;
	}

	/********************************************************************************************************************/
	public Action chooseAction() {
		Action action = null;
		try {
			timeLeftBeforeAction = timeline.getCurrentTime();

			if (actionOfPartner == null)
				action = chooseOSRAction();

			if (actionOfPartner instanceof Offer) {
				Bid partnerBid = ((Offer) actionOfPartner).getBid();

				System.out.println("######   partnerBid === " + partnerBid);
				System.out.println("######   partnerBid.getValues() === "
						+ partnerBid.getValue(1));

				double offeredUtilFromOpponent = getUtility(partnerBid);
				// get current time
				double time = timeline.getTime();

				action = chooseOSRAction();

				Bid myBid = ((Offer) action).getBid();
				double myOfferedUtil = getUtility(myBid);
				System.out.println("######    u(myBid = " + myBid.getValue(1)
						+ ")  = " + myOfferedUtil);
				System.out
						.println("==========================================================================");

				// accept under certain circumstances

				/***
				 * # if (isAcceptable(offeredUtilFromOpponent, myOfferedUtil,
				 * time)) { action = new Accept(getAgentID());
				 * System.out.println( "==== Accepting! ===="); } #
				 ***/
			}
			if (timeline.getType().equals(Timeline.Type.Time))
				sleep(0.005); // just for fun

			timeLeftAfterAction = timeline.getCurrentTime();
			estimated_rounds_left(false); // receiveMessage the estimation for
											// own
		} catch (Exception e) {
			System.out.println("Exception in ChooseAction:" + e.getMessage());
			action = new Accept(getAgentID(),
					((ActionWithBid) actionOfPartner).getBid()); // best guess
																	// if things
																	// go wrong.
		}
		return action;
	}

	/********************************************************************************************************************/
	private boolean isAcceptable(double offeredUtilFromOpponent,
			double myOfferedUtil, double time) throws Exception {
		double P = Paccept(offeredUtilFromOpponent, time);
		if (P > Math.random())
			return true;
		return false;
	}

	/********************************************************************************************************************/

	private Action chooseOSRAction() {
		Bid nextOSRBid = null;
		try {
			nextOSRBid = getOSRBid();
		} catch (Exception e) {
			System.out.println("Problem with received bid:" + e.getMessage()
					+ ". cancelling bidding");
		}

		if (nextOSRBid == null)
			return (new Accept(getAgentID(),
					((ActionWithBid) actionOfPartner).getBid()));

		return (new Offer(getAgentID(), nextOSRBid));
	}

	/********************************************************************************************************************/
	// @return a random bid with high enough utility value.
	// @throws Exception if we can't compute the utility (eg no evaluators have
	// been set)
	// or when other evaluators than a DiscreteEvaluator are present in the util
	// space.

	private Bid getOSRBid() throws Exception {
		Bid bid = null;
		HashMap<Integer, Value> val = new HashMap<Integer, Value>(); // pairs
																		// <issuenumber,chosen
																		// value
																		// string>

		if (all_osr_cuttofs) // using all the cutoffs for bidding
		// if ( estimated_rounds_left(true) >= stack_aux.size() )
		{
			for (Value key : stack_hc.pop().keySet()) // pick only one issue !
			{
				val.put(utilitySpace.getDomain().getIssues().get(0).getNumber(),
						key);
				counter++;
				break;
			}

			System.out.println("\n\t       t  =  " + timeline.getTime()
					+ "\n\t    size  =  " + stack_hc.size()
					+ "\n\t getCurrentTime  =  " + timeline.getCurrentTime()
					* 1000 + "\n\t getTotalTime    =  "
					+ timeline.getTotalTime() * 1000
					+ "\n\t estimate rounds left    =  "
					+ estimated_rounds_left(false));
		} else // estimated_rounds_left(true) < stack_aux.size() => bidding from
				// the optimal subset
		{
			for (Value key : stack_hc.get(800 + idx).keySet())
				val.put(utilitySpace.getDomain().getIssues().get(0).getNumber(),
						key);

			System.out
					.println("###########################################################################################");
			System.out.println("###### " + stack_aux.size()
					+ " rounds before deadline ##### erl = " + idx
					+ " ############");
			System.out.println("###### val = " + val + " ############");
			System.out
					.println("###########################################################################################");

			idx++;
		}
		// If the deadline is approaching in next round

		if (unused) {
			if (estimated_rounds_left(true) < 10) {
				System.out
						.println("###########################################################################################");
				System.out
						.println("######   Approaching deadline! ############################################################");
				System.out
						.println("###########################################################################################");

				if (true) {
					for (Value key : stack_h.get(
							stack_h.size() - estimated_rounds_left(true))
							.keySet())
						// for ( Value key : stack_h.get(0).keySet() )
						val.put(utilitySpace.getDomain().getIssues().get(0)
								.getNumber(), key);
				} else // , jump to the last cutoff (best)
				{
					val.put(utilitySpace.getDomain().getIssues().get(0)
							.getNumber(), (Value) max_values.get(index));
					System.out.println("  >>>>>  max_value[" + index + "] = "
							+ max_values.get(index));
					index++;
				}

				bid = new Bid(utilitySpace.getDomain(), val);
				System.out.println("  >>>>>  val = " + val + " \t utility = "
						+ getUtility(bid));

				System.out
						.println("\n##############################################");

				System.out.println("\n");

				for (int k = 0; k < stack_h.size(); k++) {
					System.out.print("    " + k + "  =>   " + stack_h.get(k));
				}

				System.out
						.println("***************************************************************************");
				System.out.println("\t   d = " + d);
				System.out
						.println("***************************************************************************");

				System.out.println("\n");
				// TODO

			}
		}

		bid = new Bid(utilitySpace.getDomain(), val);

		System.out.println("######  offering bid = " + bid);

		return bid;
	}

	/********************************************************************************************************************/
	// This function determines the accept probability for an offer.
	// At t=0 it will prefer high-utility offers.
	// As t gets closer to 1, it will accept lower utility offers with
	// increasing probability.
	// it will never accept offers with utility 0.
	// @param u is the utility
	// @param t is the time as fraction of the total available time
	// (t=0 at start, and t=1 at end time)
	// @return the probability of an accept at time t
	// @throws Exception if you use wrong values for u or t.

	double Paccept(double u, double t1) throws Exception {
		double t = t1 * t1 * t1; // steeper increase when deadline approaches.
		if (u < 0 || u > 1.05)
			throw new Exception("utility " + u + " outside [0,1]");
		// normalization may be slightly off, therefore we have a broad boundary
		// up to 1.05
		if (t < 0 || t > 1)
			throw new Exception("time " + t + " outside [0,1]");
		if (u > 1.)
			u = 1;
		if (t == 0.5)
			return u;
		return (u - 2. * u * t + 2. * (-1. + t + Math.sqrt(sq(-1. + t) + u
				* (-1. + 2 * t))))
				/ (-1. + 2 * t);
	}

	double sq(double x) {
		return x * x;
	}
}

// End 