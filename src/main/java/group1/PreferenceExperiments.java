package group1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.IssueReal;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;

public class PreferenceExperiments {
	private List<Double> utilErrors = new ArrayList<Double>(); // TODO :Remove
	
	private final double N_PERC = 0.1;
	
	private List<Bid> bids;
	private Domain domain; 
	private NegotiationInfo info;
	
	AbstractUtilitySpace estimatedUtilitySpace;
	
	public PreferenceExperiments(NegotiationInfo info) {
		this.domain = info.getUtilitySpace().getDomain();
		this.info = info;
		
		bids = generateSortedBidsRealOrder();
		
		int NUM_RUNS = 1;
		for (int i = 1; i <= NUM_RUNS; i++) {
			double n_perc = (1.0/((double)NUM_RUNS))*i;
			if (NUM_RUNS == 1) {
				n_perc = N_PERC;
			}
			
			
			System.out.println("n_perc: " + n_perc);
			
			List<Bid> sampleBids = randomlySelectBids(bids, (int) (bids.size() * n_perc));
			System.out.println("Sampled " + sampleBids.size()  + " bids");
			
			
			estimatedUtilitySpace = estimateUtilitySpace(sampleBids);
			System.out.println("===== BEFORE FLOWER =====");
			bidOrderingHammingDistance();
			bidOrderingHammingDistanceLewis();
			bidUtilityDistance();
			
			//flowerBoy(sampleBids);
			long time = System.currentTimeMillis();
			estimatedUtilitySpace = fbEstimate(sampleBids, estimatedUtilitySpace);
			System.out.println("fbEstimate time : " + (System.currentTimeMillis() - time));
			
			
			System.out.println("===== AFTER FLOWER =====");
			bidOrderingHammingDistance();
			bidOrderingHammingDistanceLewis();
			bidUtilityDistance();
			System.out.println();
		}
	
		System.out.println("Util Errors: " + utilErrors);
		
		throw new RuntimeException("Stop!");
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
	
	// ==================================================================================
	
	
	public AbstractUtilitySpace estimateUtilitySpace(List<Bid> bids) {
		domain.getIssues();
		AdditiveUtilitySpaceFactory factory = new AdditiveUtilitySpaceFactory(domain);
		utilityFactory = factory;
		BidRanking bidRanking = new BidRanking(bids,0,1); //userModel.getBidRanking();
		try {
			estimateUsingBidRanks(bidRanking, domain, factory);
			//estimateUsingBidRanksOld(bidRanking, domain, factory);
		}
		catch (Exception e) {}
		return factory.getUtilitySpace();
	}
	
	public void estimateUsingBidRanks(BidRanking r, Domain d, AdditiveUtilitySpaceFactory factory) {
		double points = 0;
		int bidNo = 0;
		HashMap<ValueDiscrete, Double> issueValues = new HashMap<ValueDiscrete, Double>();
		
		//Initialises all choices to 0
		for (Issue i: d.getIssues()) {
			IssueDiscrete id = (IssueDiscrete) i;
			for (ValueDiscrete v : id.getValues()) {
				issueValues.put(v, 0.0);
			}
		}
		
		for (Bid b : r.getBidOrder()) {
			bidNo++;
			points += (3*(double) bidNo/r.getBidOrder().size()); //move points change here to give first values non-zero
			//So we can differentiate from choices we don't have any information for (dangerous!)
			List<Issue> issues = b.getIssues();
			for (Issue i : issues)
			{
				int no = i.getNumber();
				ValueDiscrete v = (ValueDiscrete) b.getValue(no);
				Double oldScore = issueValues.get(v);
				//Updates value in map with new score
				issueValues.put(v, points+oldScore);
				factory.setUtility(i, v, points+oldScore);
			}
		}
		
		// TODO: Re-add shortly
		//Double totalError = evaluateEstimations(issueValues, d);
		//System.out.println("TOTAL ERROR: "+totalError);

		//normalizeWeightsByMaxValues(factory);
	}
	
	private AdditiveUtilitySpaceFactory utilityFactory;
	
	private AdditiveUtilitySpace fbEstimate(List<Bid> bids, AbstractUtilitySpace utilitySpace) {
		AdditiveUtilitySpaceFactory bigFactory = new AdditiveUtilitySpaceFactory(domain);
		
		FBEstimator fbEstimator = new FBEstimator(domain, utilitySpace, utilityFactory);
		fbEstimator.estimate(bids);
		//System.out.println(fbEstimator);
		List<Double> maxConfidences = new ArrayList<Double>();
		List<Issue> temp = new ArrayList<Issue>();
		for (Issue issue : domain.getIssues()) {
			Double maxConf = 0.0;
			List<Value> issueValues = fbEstimator.sortValuesForIssue(issue);
			Collections.reverse(issueValues);
			ArrayList<Double> confidencesList = new ArrayList<Double>();
			for(int x=0; x<issueValues.size(); x++) {
				Double sumConfidence = 0.0;
				Value v1 = issueValues.get(x);
				for(int y=x+1; y<issueValues.size(); y++) {
					Value v2 = issueValues.get(y);
					Double confidence = fbEstimator.getIssueHighLowScore(issue, v1, v2);
					sumConfidence += confidence;
				}
				confidencesList.add(sumConfidence);
				if (sumConfidence > maxConf) {
					//first will always have highest values
					maxConf = sumConfidence;
				}
			}
			// /by size to prevent issues with many items having inflated importance
			maxConfidences.add(maxConf/issueValues.size());
			temp.add(issue);
			List<Double> normalizedConfidences = fbEstimator.normaliseConfidences(confidencesList);
			
			Double confidenceWorstItem = fbEstimator.getIssueHighLowScore(issue, issueValues.get(issueValues.size()-1), issueValues.get(issueValues.size()-2));
			//2* because confidence is twin-tailed, ~0.5 (unsure) will result in same utility as 2nd worst.
			confidenceWorstItem = 2*Math.max(confidenceWorstItem, 0.1); //prevents last item being scored ridiculously low
			//Multiplies uncertainty of it being worst one by utility of 2nd worst
			Double utilityWorstItem = confidenceWorstItem * normalizedConfidences.get(normalizedConfidences.size()-2);
			normalizedConfidences.set(normalizedConfidences.size()-1, utilityWorstItem);
			
			for(int z=0; z<normalizedConfidences.size(); z++) {
				bigFactory.setUtility(issue, (ValueDiscrete)issueValues.get(z), normalizedConfidences.get(z));
			}

			//System.out.println(fbEstimator.sortValuesForIssue(issue)); //needed?
		}
		//normalizeWeightsByMaxValues(factory); needed?
		Double domainTotalMaxConfidences = 0.0;
		for(Double d: maxConfidences) {
			domainTotalMaxConfidences += d;
		}
		double[] issueWeightEstimations = new double[maxConfidences.size()];
		for(int a=0; a<maxConfidences.size(); a++) {
			issueWeightEstimations[a] = maxConfidences.get(a)/domainTotalMaxConfidences;
		}
		bigFactory.getUtilitySpace().setWeights(domain.getIssues(), issueWeightEstimations);
		AdditiveUtilitySpace space = bigFactory.getUtilitySpace();
		return space;
		
	}

	public void flowerBoy(List<Bid> bids) {
		FlowerBoy flowerBoy = new FlowerBoy(domain);
		
		System.out.println("FLR - No. bids :" + bids.size());
		long startTime = System.currentTimeMillis();
		
		Map<Issue, Map<Value, Map<Value, Double>>> scores = new HashMap<>();
		for (int i = 0; i < bids.size()-1; i++) {
			Bid lowerBid = bids.get(i);
			for (int j = i+1; j < bids.size(); j++) {
				Bid higherBid = bids.get(j);
				
				for (Issue issue : higherBid.getIssues()) {
					Value lowerBidValue = lowerBid.getValue(issue.getNumber());
					Value higherBidValue = higherBid.getValue(issue.getNumber());
					
					// Map stuff.
					/*Map<Value, Map<Value, Double>> valueValueMap = scores.get(issue);
					if (valueValueMap == null) {
						valueValueMap = new HashMap<>();
						scores.put(issue, valueValueMap);
					}
					
					// Map stuff.
					Map<Value, Double> valueScoreMap = valueValueMap.get(higherBidValue);
					if (valueScoreMap == null) {
						valueScoreMap = new HashMap<>();
						valueValueMap.put(higherBidValue, valueScoreMap);
					}
					
					// Map stuff.
					Double currentScore = valueScoreMap.get(lowerBidValue);
					if (currentScore == null) {
						currentScore = 0.0;
					}*/
					
					//currentScore += flowerBoyScore(lowerBid, higherBid);
					//valueScoreMap.put(lowerBidValue, currentScore);
					flowerBoy.addToIssueHighLowScore(issue, higherBidValue, lowerBidValue, 
							flowerBoyScore(lowerBid, higherBid, issue));
				}
			}
		}
		
		System.out.println("FLR - Time taken :" + (System.currentTimeMillis() - startTime));
		
		System.out.println(flowerBoy);
		flowerBoy.normaliseScores();
		System.out.println(flowerBoy);
		for (Issue issue : domain.getIssues()) {
			System.out.println(flowerBoy.sortValuesForIssue(issue));
			System.out.println();
		}
		//estimatedUtilitySpace.getUti
	}
	
	private double flowerBoyScore(Bid lowerBid, Bid higherBid, Issue pivotIssue) {
		double score = 0;
		
		
		// So dumb.
		HashMap<Integer, Value> valueMap = new HashMap<>();
		for (Issue issue : lowerBid.getIssues()) {
			valueMap.put(issue.getNumber(), lowerBid.getValue(issue.getNumber()));
		}
		valueMap.put(pivotIssue.getNumber(), higherBid.getValue(pivotIssue.getNumber()));	
		Bid dummyBid = new Bid(domain, valueMap);
		// DummyBid is lowerBid but with the pivotIssue changed to value of higherBid.
		Double utilDiff = Math.abs(getOwnUtility(higherBid) - getOwnUtility(dummyBid));
		
		double scoreLimit = 10000;
		if (utilDiff == 0) {
			score = scoreLimit;
		} else {
			score = Math.min(1/utilDiff, scoreLimit);
		}
		//System.out.println(utilDiff + " gives us a score of " + score);
		return score;
	}
	
	private void flowerBoyOrder(Map<Issue, Map<Value, Map<Value, Double>>> scores) {
		Map<Issue, List<Value>> issueValueOrdering = new HashMap<>();
		
		
	}
	
	class FlowerBoy {
		private Map<Issue, Map<Value, Map<Value, Double>>> scores;
		private boolean scoresNormalised = false;
		private Domain domain;
		
		FlowerBoy(Domain domain) {
			this.domain = domain;
			
			scores = new HashMap<>();
			
			for (Issue issue : domain.getIssues()) {
				Map<Value, Map<Value, Double>> valueValueMap = new HashMap<>();
				List<ValueDiscrete> issueValues = ((IssueDiscrete) issue).getValues();
				for (int i = 0; i < issueValues.size(); i++) {
					Value lowValue = issueValues.get(i);
					Map<Value, Double> valueScoreMap = new HashMap<>();
					valueValueMap.put(lowValue, valueScoreMap);
					
					for (int j = 0; j < issueValues.size(); j++) {
						if (i == j) {
							continue;
						}
						
						Value highValue = issueValues.get(j);
						valueScoreMap.put(highValue, 0.0);	
					}
				}
				
				scores.put(issue, valueValueMap);
			}
		}
		
		void setIssueHighLowScore(Issue issue, Value highValue, Value lowValue, double score) {
			scores.get(issue).get(highValue).put(lowValue, score);
		}
		
		double getIssueHighLowScore(Issue issue, Value highValue, Value lowValue, double score) {
			return scores.get(issue).get(highValue).get(lowValue);
		}
		
		void addToIssueHighLowScore(Issue issue, Value highValue, Value lowValue, double score) {
			if (highValue != lowValue) {
				Map<Value, Double> valueScoreMap = scores.get(issue).get(highValue);
				double currentScore = valueScoreMap.get(lowValue);
				valueScoreMap.put(lowValue, currentScore + score);
			}

		}
		
		void normaliseScores() {
			for (Issue issue : domain.getIssues()) {
				List<ValueDiscrete> issueValues = ((IssueDiscrete) issue).getValues();
				for (int i = 0; i < issueValues.size(); i++) {
					for (int j = i; j < issueValues.size(); j++) {
						if (i == j) {
							continue;
						}
						
						Value lowVal = issueValues.get(i);
						Value highVal = issueValues.get(j);
						
						double score = scores.get(issue).get(highVal).get(lowVal);
						double reverseScore = scores.get(issue).get(lowVal).get(highVal);
						double sumScore = score + reverseScore;
						
						double normScore = -1;
						if (sumScore != 0) {
							normScore = score/sumScore;
						}
						
						scores.get(issue).get(highVal).put(lowVal, normScore);
						scores.get(issue).get(lowVal).put(highVal, 1-normScore);
					}
				}
			}
			scoresNormalised = true;
		}
		
		List<Value> sortValuesForIssue(Issue issue) {
			if (!scoresNormalised) {
				normaliseScores();
			}
			
			List<ValueDiscrete> issueValues = ((IssueDiscrete) issue).getValues();
			Map<Value, Map<Value, Double>> valueValueMap = scores.get(issue);
			
			List<Value> sortedValues = new ArrayList<>();
			for (Value value : issueValues) {
				if (sortedValues.isEmpty()) {
					sortedValues.add(value);
					//System.out.println(sortedValues);
				} else {
					boolean added = false;
					for (int i = 0; i < sortedValues.size(); i++) {
						Value sortedValue = sortedValues.get(i);
						if (valueValueMap.get(sortedValue).get(value) > 0.5) {
							//System.out.println("\t" + sortedValue + " > " + value);
							//System.out.println(valueValueMap);
							sortedValues.add(i, value);
							//System.out.println(sortedValues);
							added = true;
							break;
						} else {
							//System.out.println("\t" + sortedValue + " !> " + value);
						}
					}
					if (!added) {
						sortedValues.add( value);
						//System.out.println(sortedValues);
					}
				}
			}
			
			return sortedValues;
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
	
	
	// ==================================================================================
	
	private List<Bid> generateSortedBids() {
		try {
			List<Bid> bids = generateAllBids();
			bids.sort(new Comparator<Bid>() {
	
				@Override
				public int compare(Bid bid0, Bid bid1) {
					return Double.compare(getOwnUtility(bid0), getOwnUtility(bid1));
				}
				
			});
			
			return bids;
		}
		catch(Exception e) {
			System.out.println("WARNING: TRIED TO ACCESS UTILITY WHEN UTILITYSPACE NOT AVAILABLE");
			return null; //returning generatsortedbidsrealorder proably wont help here & will cause loop
		}
	}
	
	private List<Bid> generateSortedBidsRealOrder() {
		try {
			List<Bid> bids = generateAllBids();
			bids.sort(new Comparator<Bid>() {
	
				@Override
				public int compare(Bid bid0, Bid bid1) {
					return Double.compare(getOwnRealUtility(bid0), getOwnRealUtility(bid1));
				}
				
			});
			
			return bids;
		}
		catch(Exception e) {
			System.out.println("WARNING: TRIED TO ACCESS REAL FUNCTION WHEN NOT AVAILABLE");
			return generateSortedBids();
		}
	}
	
	private List<Bid> generateAllBids() {
		long startTime = System.currentTimeMillis();
		
		List<Bid> bids = new ArrayList<>();
		List<Issue> issues = domain.getIssues();
		int numIssues = issues.size();

		List<Integer> issueValueNumbers = new ArrayList<Integer>();
		for (Issue issue : issues) {
			IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
			issueValueNumbers.add(-issueDiscrete.getNumberOfValues()+1);
		}
		
		while (true) {
			HashMap<Integer, Value> valueMap = new HashMap<>();
			for (int i = 0; i < numIssues; i++) {
				IssueDiscrete issueDiscrete = (IssueDiscrete) issues.get(i);
				Value value = issueDiscrete.getValue(-issueValueNumbers.get(i));
				Issue issue = issues.get(i);
				//bid.putValue(issue.getNumber(), value);
				valueMap.put(issue.getNumber(), value);
			}
			
			Bid bid = new Bid(domain, valueMap);
			bids.add(bid);
			
			boolean incremented = false;
			for (int i = 0; i < numIssues; i++) {
				if (issueValueNumbers.get(i) < 0) {
					issueValueNumbers.set(i, issueValueNumbers.get(i)+1);
					incremented = true;
					break;
				} else {
					issueValueNumbers.set(i, 
							-((IssueDiscrete) issues.get(i)).getNumberOfValues()+1);
				}
			}
			
			if (!incremented) {
				break;
			}
		}

		//System.out.println("Generated " + bids.size() + " bids");
		//System.out.println("Generate bids time: " + (System.currentTimeMillis() - startTime));
		return bids;
	}
	
	// Estimated
	private double getOwnUtility(Bid bid) {
		try {
			return estimatedUtilitySpace.getUtility(bid);
		}
		catch(Exception e) {
			System.out.println("Message: " + e.getMessage());
			e.printStackTrace();
			//System.out.println("WARNING: PROBLEM WITH UTILITYSPACE");
			return 0.0;
		}	
	}
	
	// Real
	private double getOwnRealUtility(Bid bid) {
		try {
			return info.getUtilitySpace().getUtility(bid);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("WARNING: PROBLEM WITH UTILITYSPACE");
			return 0.0;
		}	
	}
	
	private Double bidUtilityDistance() {
		Double distance = 0.0;
		List<Double> diffs = new ArrayList<>();
		for (Bid bid : bids) {
			double thisDistance = Math.abs(getOwnRealUtility(bid) - getOwnUtility(bid));
			diffs.add(thisDistance);
			distance += thisDistance;
		}
		
		double deviation = distance/((double) bids.size());
		
		double var = 0;
		for (Double diff : diffs) {
			var += Math.pow(diff, 2);
		}
		var = var / bids.size();
				
		System.out.println("Mean Utility Error: " + deviation);
		System.out.println("Variance of Utility Error: " + var);
		utilErrors.add(1-deviation);
		System.out.println("Utility Metric Performance: "+(100.0 - deviation*100)+"% Accurate.");
		return deviation;
	}
	
	
	// @LEWIS'
	private Double bidOrderingHammingDistance() {
		List<Bid> realBids = generateSortedBidsRealOrder();
		Map<Double, Set<Integer>> realUtilityPositions = new HashMap<>();
		
		for (int i = 0; i < realBids.size(); i++) {
			Bid bid = realBids.get(i);
			double util = getOwnRealUtility(bid);
			
			Set<Integer> positions = realUtilityPositions.get(util);
			if (positions == null) {
				positions = new HashSet<>();
				realUtilityPositions.put(util, positions);
			}
			positions.add(i);
		}
		
		List<Bid> estimatedBids = generateSortedBids();
		Integer aggregateDistance = 0;
		for (Bid bid : realBids) {
			Integer estimatedIndex = estimatedBids.indexOf(bid);
			Integer minOrderDistance = Integer.MAX_VALUE;
			for (Integer position : realUtilityPositions.get(getOwnRealUtility(bid))) {
				Integer orderDistance = Math.abs(position - estimatedIndex); //Number of places in the list different for this bid
				if (orderDistance < minOrderDistance) {
					minOrderDistance = orderDistance;
				}
			}
			
			aggregateDistance += minOrderDistance;
		}
		
		double worstValue = Math.floor(realBids.size() * realBids.size() / 2);
		double percentageDeviation = aggregateDistance*100/worstValue;
		System.out.println("Hamming Metric Performance: "+(100.0 - percentageDeviation)+"% Accurate.");
		return aggregateDistance*100/worstValue;
	}
	
	private Double bidOrderingHammingDistanceLewis() {
		List<Bid> estimatedBids = generateSortedBids();
		List<Bid> realBids = generateSortedBidsRealOrder();
		Integer aggregateDistance = 0;
		
		for (int realIndex = 0; realIndex < realBids.size(); realIndex++) {
			Bid b = realBids.get(realIndex);
			Integer estimatedIndex = estimatedBids.indexOf(b);
			Integer orderDistance = Math.abs(realIndex - estimatedIndex); //Number of places in the list different for this bid
			aggregateDistance += orderDistance;
		}
		
		double worstValue = (int) Math.floor(realBids.size() * realBids.size() / 2);
		double percentageDeviation = aggregateDistance*100/worstValue;
		System.out.println("Hamming Metric Performance Lewis: "+(100.0 - percentageDeviation)+"% Accurate.");
		return aggregateDistance*100/worstValue;
	}
}
