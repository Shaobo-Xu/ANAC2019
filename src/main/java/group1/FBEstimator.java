package group1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.utility.AbstractUtilitySpace;

public class FBEstimator {
	private Map<Issue, Map<Value, Map<Value, Double>>> scores;
	private boolean scoresNormalised = false;
	private Domain domain;
	private AdditiveUtilitySpaceFactory utilityFactory;
	
	private final double ESTIMATE_CAP = 20000;
	
	// Altenative self modelling.
	private Map<Issue, Map<Value, Double>> issueValueScores = new HashMap<>();
	private Map<Issue, Double> issueWeights = new HashMap<>();
	
	private static final int POP_LIMIT = 4000;
	
	public FBEstimator(Domain domain, AbstractUtilitySpace utilitySpace, AdditiveUtilitySpaceFactory utilityFactory) {
		this.domain = domain;
		this.utilityFactory = utilityFactory;
		
		normUtils();
		for (Issue issue : domain.getIssues()) {
			IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
			Map<Value,Double> valueScores = new HashMap<>();
			issueValueScores.put(issue, valueScores);
			
			for (ValueDiscrete value : issueDiscrete.getValues()) {
				valueScores.put(value, utilityFactory.getUtility(issue, value));
			}
			
			issueWeights.put(issue, 1.0/((double)domain.getIssues().size()));
		}
		
		scores = new HashMap<>();
		
		for (Issue issue : domain.getIssues()) {
			Map<Value, Map<Value, Double>> valueValueMap = new HashMap<>();
			List<ValueDiscrete> issueValues = ((IssueDiscrete) issue).getValues();
			for (int i = 0; i < issueValues.size(); i++) {
				//lowValue is the item we're currently on
				Value lowValue = issueValues.get(i);
				Map<Value, Double> valueScoreMap = new HashMap<>();
				//adds the item we're on to the upper level map
				valueValueMap.put(lowValue, valueScoreMap);
				
				//Each item in the issue has a map to each other item in the issue
				//Representing confidence that the item is better than the mapped item
				for (int j = 0; j < issueValues.size(); j++) {
					if (i == j) {
						//No self-mapping
						continue;
					}
					
					Value highValue = issueValues.get(j);
					//Adds the looped item to the lower level map
					valueScoreMap.put(highValue, 0.0);	
				}
			}
			
			scores.put(issue, valueValueMap);
		}
		
		// RESUME: Implement weight calculating here, in the map way, from the factory. Use that everywhere.
	}
	
	// AHHHH
	private void normUtils() {
		utilityFactory.normalizeWeightsByMaxValues2();
	}
	
	//This changes the confidence level that the lower item is better than the higher Item
	//<High Item ,<Low Item, Score> 
	//unused lol
	public void setIssueHighLowScore(Issue issue, Value highValue, Value lowValue, double score) {
		scores.get(issue).get(highValue).put(lowValue, score);
	}
	
	public double getIssueHighLowScore(Issue issue, Value highValue, Value lowValue) {
		return scores.get(issue).get(highValue).get(lowValue);
	}
	
	//See above, but adds to the score, rather than overwrite.
	//Takes a value from a higher & lower value bid, and the likelihood that that the deviation in util is NOT due to this issue
	public void addToIssueHighLowScore(Issue issue, Value highValue, Value lowValue, double score) {
		if (highValue != lowValue) {
			Map<Value, Double> valueScoreMap = scores.get(issue).get(highValue);
			double currentScore = valueScoreMap.get(lowValue);
			valueScoreMap.put(lowValue, currentScore + score);
		}

	}
	
	public ArrayList<Double> normaliseConfidences(ArrayList<Double> input) {
		ArrayList<Double> normalized = new ArrayList<Double>();
		Double adjustedMax = Math.sqrt(input.get(0));
		for (Double d : input) {
			normalized.add(Math.sqrt(d)/adjustedMax);
		}
		return normalized;
	}
	
	public void normaliseScores() {
		for (Issue issue : domain.getIssues()) {
			List<ValueDiscrete> issueValues = ((IssueDiscrete) issue).getValues();
			for (int i = 0; i < issueValues.size(); i++) {
				for (int j = i; j < issueValues.size(); j++) {
					if (i == j) {
						continue;
					}
					
					Value lowVal = issueValues.get(i);
					Value highVal = issueValues.get(j);
					
					//Aggregates the low -> high and high -> low scoring
					double score = scores.get(issue).get(highVal).get(lowVal);
					double reverseScore = scores.get(issue).get(lowVal).get(highVal);
					double sumScore = score + reverseScore;
					
					double normScore = -1;
					if (sumScore != 0.0) {
						normScore = score/sumScore;
					}
					else {
						normScore = estimateConfidenceBasedOnNearestNeighbours(issue, issueValues, i, j);
					}
					
					scores.get(issue).get(highVal).put(lowVal, normScore);
					scores.get(issue).get(lowVal).put(highVal, 1-normScore);
				}
			}
		}
		scoresNormalised = true;
	}
	
	//TODO: What if item doesn't appear at all?
	private double estimateConfidenceBasedOnNearestNeighbours(Issue i, List<ValueDiscrete> issueValues, int lowIndex, int highIndex) {
		Value lowVal = issueValues.get(lowIndex);
		if ((highIndex - lowIndex) > 1) {
			double crossCertainty = 0.5; //originally thought 0.0 but 0.5 means unsure, 0.0 means 100% sure it's not.
			Value prevVal = lowVal;
			for (int x = lowIndex+1; x <= highIndex; x++) {
				Value intermediateVal = issueValues.get(x);
				double prevScore = scores.get(i).get(prevVal).get(intermediateVal);

				//If an error occurs, ignore this pairing
				if(prevScore <= 0.5) {
					prevScore = 0.5; //tempting to use recursive function here but might cause problems in real setting
				}
				
				//This is the % (e.g. 0.2 -> 20%) certainty above neutral for this pairing
				double certaintyContribution = (prevScore - 0.5)*2;				
				double remainingUncertainty = 1.0 - crossCertainty;				
				crossCertainty = crossCertainty + (remainingUncertainty * certaintyContribution);
				
				prevVal = intermediateVal;
			}
			return crossCertainty;
		}
		else { //IN PROGRESS
			return 0.5;
//			Value lowerVal;
//			Value intermediateVal;
//			Value higherVal;
//			if(lowIndex == 0) {
//				lowerVal = lowVal;
//				intermediateVal = issueValues.get(lowIndex+1)
//				higherVal = issueValues.get(lowIndex+2);
//			}
//			else if(highIndex == issueValues.size()-1) {
//				higherVal = issueValues.get(highIndex);
//			}
//			else {
//				lowerVal = issueValues.get(lowIndex-1);
//				higherVal = issueValues.get(highIndex+1);
//			}
//			double bigStep = scores.get(i).get(lowerVal).get(higherVal);
//			double smallStep = scores.get(i).get(intermediateVal).get(higherVal);

		}
	}
	
	public List<Value> sortValuesForIssue(Issue issue) {
		if (!scoresNormalised) {
			normaliseScores();
		}
		
		List<ValueDiscrete> issueValues = ((IssueDiscrete) issue).getValues();
		Map<Value, Map<Value, Double>> valueValueMap = scores.get(issue);
		
		List<Value> sortedValues = new ArrayList<>();
		for (Value value : issueValues) {
			if (sortedValues.isEmpty()) {
				sortedValues.add(value);
			} else {
				boolean added = false;
				//Compares items already added to the list to the current item
				//As index 0 is best, traverses from best to worst
				for (int i = 0; i < sortedValues.size(); i++) {
					Value sortedValue = sortedValues.get(i);
					//If the incumbent item's map says the current item is better, add to sortedValues index
					//As best -> worst, adds in at first place where the current is better
					if (valueValueMap.get(sortedValue).get(value) > 0.5) {
						//best is at index 0
						sortedValues.add(i, value);
						added = true;
						break;
					} else {
					}
				}
				if (!added) {
					//adds to end (is worst in list so far)
					sortedValues.add( value);
				}
			}
		}
		
		return sortedValues;
	}
	
	//Parameter: POP
	public boolean estimate(List<Bid> bids) {
		if (bids.size() > POP_LIMIT) {
			bids = randomlySelectBids(bids, POP_LIMIT); // Better (even!) way of sampling.
		}
		
		List<Double> bidUtilities = new ArrayList<>();
		for (Bid bid : bids) {
			//bidUtilities.add(utilitySpace.getUtility(bid));
			bidUtilities.add(getUtility(bid));
		}
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < bids.size()-1; i++) {
			
			if ((System.currentTimeMillis() - startTime) > ESTIMATE_CAP) {
				return false;
			}
			
			Bid lowerBid = bids.get(i);
			double lowerBidUtil = bidUtilities.get(i);
			
			for (int j = i+1; j < bids.size(); j++) {
				Bid higherBid = bids.get(j);
				double higherBidUtil = bidUtilities.get(j);
				
				for (Issue issue : higherBid.getIssues()) {
					//gets values for each issue in both bids
					Value lowerBidValue = lowerBid.getValue(issue.getNumber());
					Value higherBidValue = higherBid.getValue(issue.getNumber());
					
					addToIssueHighLowScore(issue, higherBidValue, lowerBidValue, 
							flowerBoyScore(lowerBid, higherBid, lowerBidUtil, higherBidUtil, issue));
				}
			}
		}
	
		normaliseScores();
		return true;
	}
	
	private double getUtility(Bid bid, Issue excludeIssue) {
		double utility = 0.0;
		
		// TODO: resume
		for (Issue issue : bid.getIssues()) {
			if (excludeIssue != null && issue.equals(excludeIssue)) {
				continue;
			}
			
			double weight = issueWeights.get(issue);
			double valueUtil = issueValueScores.get(issue).get(bid.getValue(issue.getNumber()));
			utility += weight * valueUtil;
		}
		
		return utility;
	}
	
	private double getUtility(Bid bid) {
		return getUtility(bid, null);
	}
	
	private List<Bid> randomlySelectBids(List<Bid> bids, int n) {
		
		
		// Who cares that this is inefficient.
		Set<Bid> selectBids = new HashSet<Bid>();
		List<Bid> shuffledBids = new ArrayList<>(n);
		for (Bid bid : bids) {
			shuffledBids.add(bid);
		}
		Collections.shuffle(shuffledBids);
		
		for (int i = 0; i < n; i++) {
			selectBids.add(shuffledBids.get(i));
		}
		
		List<Bid> sampleBids = new ArrayList<>();
		for (Bid bid : bids) {
			if (selectBids.contains(bid)) {
				sampleBids.add(bid);
			}
		}
		
		return sampleBids;
	};
	
	//Higher score = lower difference in estimated utility from all issues (apart from pivot) between the 2 bids
	private double flowerBoyScore(Bid lowerBid, Bid higherBid, double lowerBidUtil, double higherBidUtil, Issue pivotIssue) {
		double score = 0;
		
		
		double issueWeight = issueWeights.get(pivotIssue);
		
		double higherWithoutIssue = higherBidUtil - 
				issueWeight * issueValueScores.get(pivotIssue).get(higherBid.getValue(pivotIssue.getNumber()));
		double lowerWithoutIssue = lowerBidUtil - 
				issueWeight * issueValueScores.get(pivotIssue).get(lowerBid.getValue(pivotIssue.getNumber()));
		
		
		// TODO: Perhaps remove abs.
		double utilDiff = Math.max(0, higherWithoutIssue - lowerWithoutIssue);
		
		double scoreLimit = 10000;
		if (utilDiff == 0) {
			score = scoreLimit;
		} else {
			score = Math.min(1/utilDiff, scoreLimit);
		}
		
		//System.out.println(utilDiff + " gives us a score of " + score); 
		return score;
	}
	
	public String toString() {
		String string = "\n==== FlowerBoy ==== \n";
		for (Issue issue : scores.keySet()) {
			string += "ISSUE: " + issue.getName() + "\n";
			for (Value highVal : scores.get(issue).keySet()) {
				for (Value lowVal : scores.get(issue).get(highVal).keySet()) {
					double score = scores.get(issue).get(highVal).get(lowVal);
					string += String.format("\t %s > %s : %f \n", highVal, lowVal, score);
				}
			}
		}
		string += "\n";
		return string;
	}
}