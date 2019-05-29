package group4;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;

public class UtilitySpaceFactory extends AdditiveUtilitySpaceFactory {	
	
	public UtilitySpaceFactory(Domain d) {
		super(d);
	}
	
	
	/**
	 * A simple heuristic for estimating a discrete {@link AdditiveUtilitySpace} from a {@link BidRanking}.
	 * Gives 0 points to all values occurring in the lowest ranked bid, 
	 * then 1 point to all values occurring in the second lowest bid, and so on.
	 */
	@Override
	public void estimateUsingBidRanks(BidRanking r)
	{	
		double points = 1; // changed, GENIUS started with 0, but that would disregard the lowest bid ranking
				
		for (Bid b : r.getBidOrder())
		{
			List<Issue> issues = b.getIssues();
			for (Issue i : issues)
			{
				int no = i.getNumber();
				ValueDiscrete v = (ValueDiscrete) b.getValue(no);
				double oldUtil = getUtility(i, v);
				setUtility(i, v, oldUtil + points);
			}
			points += 1;
		}
		
		// we assign 1+2+3+4+...+n points in total, n being the size of our bid ranking
		// we divide the utilities by that value to rescale between 0 and 1
		// 1+2+3+...+n == n * (n+1) / 2 
		normalizeValues(r.getSize() * (r.getSize() + 1) / 2, false);
	}
	
	/**
	 * A heuristic for estimating a discrete {@link AdditiveUtilitySpace} from a opponent history of bids
	 * Gives 1 - t points to all values occurring in the lowest ranked bid, 
	 * t being the time the bid occurred first (does not count repetition)
	 */
	public void estimateUsingHistory(HashMap<Double, Bid> history) {
		for (Entry<Double, Bid> entry : history.entrySet()) {
			Bid b = entry.getValue();
			double value = 1 - entry.getKey();
			
			List<Issue> issues = b.getIssues();
			for (Issue i : issues)
			{
				int no = i.getNumber();
				ValueDiscrete v = (ValueDiscrete) b.getValue(no);
				double oldUtil = getUtility(i, v);
				setUtility(i, v, oldUtil + value);
			}
		}
		normalizeValues(history.size(), false);
	}
	
	private void normalizeValues(double utilityDivider, boolean normalizeWeightsByEntropy)
	{
		List<Issue> issues = getUtilitySpace().getDomain().getIssues();
		
		double entropiesSum = 0;
		
		// for every issue, divide the count by the number of bids so that we have a percentual weight
		for (Issue i : issues)
		{
			double squaredEntropies = 0;
			
			EvaluatorDiscrete evaluator = (EvaluatorDiscrete) getUtilitySpace().getEvaluator(i);
			// double maxValue = evaluator.getDoubleValue((ValueDiscrete) evaluator.getMaxValue());
			
			IssueDiscrete issueDiscrete = (IssueDiscrete) i;
			
			for (ValueDiscrete valueDiscrete: issueDiscrete.getValues()) {
				double oldUtility = evaluator.getDoubleValue(valueDiscrete);
				
				// johnny black just rates the values, instead we use the entropy again 
				double newUtility = oldUtility / utilityDivider;
				evaluator.setEvaluationDouble(valueDiscrete, newUtility);
				
				// sum up all squared entropies
				// this is like in johnny black
				squaredEntropies += Math.pow(oldUtility / utilityDivider, 2);
			}
		
			if (normalizeWeightsByEntropy) {
				evaluator.setWeight(squaredEntropies);
				entropiesSum += squaredEntropies;
			}
		}
		
		if (normalizeWeightsByEntropy) {
			// just as in Johnny Black: normalize the squared weights by dividing them through the sum
			
			for (Issue i : issues)
			{
				EvaluatorDiscrete evaluator = (EvaluatorDiscrete) getUtilitySpace().getEvaluator(i);
				evaluator.setWeight(evaluator.getWeight() / entropiesSum);
			}
		} else {
			// normalize weights by assigning 1 / numberOfIssues to every weight - used for uncertainty modeling
			getUtilitySpace().normalizeWeights();
		}
	}
}
