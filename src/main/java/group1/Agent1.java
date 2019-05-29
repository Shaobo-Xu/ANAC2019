package group1;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.uncertainty.ExperimentalUserModel;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;
import genius.core.utility.UncertainAdditiveUtilitySpace;
import genius.core.utility.UtilitySpace;



@SuppressWarnings("serial")
public class Agent1 extends AbstractNegotiationParty {
	private final String DESCRIPTION = "*angry rap noises*";
	
	private final boolean DEBUG_MODE = false; 
	private final boolean SUPER_DEBUG_MODE = false; 
	private final boolean LAST_DITCH_MODE = true; // TODO: Check if this is used now.
	private final double RANDOM_FACTOR = 0.1; // How often we return a weighted random 
											  // bid as opposed to the best estimated bid
											  // for the opponent.
											  // TODO: Make this dynamic (decrease as time goes on).
	
	private Bid latestOffer;
	private double thresholdUtility;
	private double targetUtility;
	
	private int offerCount = 0;
	
	// The last round in which we sent each specific bid. 
	private HashMap<Bid, Integer> sentBidRound = new HashMap<>();
	
	// Concession parameters.
	private double ka = 0;
	private double beta = 0.1;
	
	// Opponent modelling.
	private Map<Issue, Map<Value, Double>> issueValueScores = new HashMap<>();
	private Map<Issue, Map<Value, Integer>> issueValueCounts = new HashMap<>();
	private Map<Issue, Double> issueWeights = new HashMap<>();
	
	// Best bid received.
	private Bid bestReceivedBid;
	private Double bestReceivedUtility = -1.0;
	
	// Concession information.
	private LinkedList<Double> utilitiesOffered = new LinkedList<Double>();
	// consider only the last x utilities
	private int UTILITY_HISTORY = 30;
	private double estimatedNextUtility= 0.0;
	
	// Round information.
	private LinkedList<Double> roundTimes = new LinkedList<Double>();
	// consider only the last x round times
	private int ROUND_TIME_HISTORY = 30;
	// something large so we don't go into last ditch mode straight up
	private int estimatedRoundsRemaining = 99999;
	private double lastRoundEnd = -1;
	
	private int roundCount = 0;
	
//	private FutureUtilityEstimator fue;
	AdditiveUtilitySpaceFactory bigFactory; // Class variable here to prevent scoping issues. 
											// Can remove later if necessary.
	
	ExperimentalUserModel e;
	UncertainAdditiveUtilitySpace realSpace;
			
	
	@Override
    public void init(NegotiationInfo info) {
        super.init(info);
        
        //long initStartTime = System.currentTimeMillis();
        
        // int numRounds = 50;
        //fue = new FutureUtilityEstimator(numRounds);
        
        thresholdUtility = getOwnUtility(getMinUtilityBid());
        targetUtility = 1;
        
        initOpponentModel();
        
        if (SUPER_DEBUG_MODE) {
            new PreferenceExperiments(info);
        }
        
        if (DEBUG_MODE) {
        	e = (ExperimentalUserModel) userModel;
            realSpace = e.getRealUtilitySpace();
            bidUtilityDistance();
        }
        
        try {
            utilitySpace = fbEstimate(userModel.getBidRanking().getBidOrder());
        } catch (Exception e) {
        	// TODO: I assume this will revert back to counting utility space if fbEstimate fails.
        	e.printStackTrace();
        }

        if (DEBUG_MODE) {
    		bidOrderingHammingDistance(); // NOTE: This is an evaluation step done here instead of 
    									  // with the rest of the code. This is because it relies on 
    									  // the userModel, which we only instantiate above (to fix 
    								      // those weird class errors when importing).
    		bidUtilityDistance();
        }
        
        //System.out.println("init() time: " + (System.currentTimeMillis() - initStartTime));
	}
	
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {		
		try {
			roundCount++;
			
			// Update time information.
			updateRoundEstimation();
			
			lastRoundEnd = getTimeLine().getTime();
			
			if (latestOffer == null) {
				return new Offer(getPartyId(), getMaxUtilityBid());
			}
			
			if (shouldAcceptOffer(latestOffer)) {
				return new Accept(getPartyId(), latestOffer);
			}
			
			// Update best received bid.
			double offerUtility = getOwnUtility(latestOffer);
			if (offerUtility >= bestReceivedUtility) {
				bestReceivedUtility = offerUtility;
				bestReceivedBid = latestOffer;
			}
			
			updateDeltaUtility(offerUtility);
			updateAcceptanceCritera();
			updateOpponentModel();
			
			Bid counterBid = generateCounterBid();
			
			
			if (DEBUG_MODE) {
				//System.out.println("Real Counter-offer utility:" + getOwnRealUtility(counterBid));
			}
			
			sentBidRound.put(counterBid, roundCount);
			return new Offer(getPartyId(), counterBid);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) {
        	offerCount++;
        	latestOffer = ((Offer) act).getBid();
        	
//    		double offerUtility = getOwnUtility(latestOffer);
//        	fue.addNewOffer(roundCount, offerUtility);
        }
    }
	
	/**
	 * Initialises the predicted opponent model with some starting values.
	 */
	private void initOpponentModel() {
		try {
			for (Issue issue : getDomain().getIssues()) {
				IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
				
				Map<Value, Double> valueScores = new HashMap<>();
				Map<Value, Integer> valueCounts = new HashMap<>();
				for (Value value : issueDiscrete.getValues()) {
					valueScores.put(value, 1.0/issueDiscrete.getNumberOfValues());
					valueCounts.put(value, 0);
				}
				
				issueValueScores.put(issue, valueScores);
				issueValueCounts.put(issue, valueCounts);
				issueWeights.put(issue, 1.0/issueDiscrete.getNumberOfValues());
			}
		} catch (Exception e) {
			// TODO: Not sure what to do if this fails.
        	e.printStackTrace();
		}
	}
	
	
	/**
	 * Takes a bid and returns the predicted utility that bid has to us, based on the 
	 * preference elicitation from the incomplete preference profile.
	 * 
	 * @param bid 
	 * @return The predicted utility to us of the given bid.
	 */
	private double getOwnUtility(Bid bid) {
		try {
			return getUtility(bid);
		}
		catch(Exception e) {
			System.err.println("WARNING: PROBLEM WITH UTILITYSPACE");
			return 0.0;
		}	
	}
	
	private double getOwnRealUtility(Bid bid) {
		try {
			return realSpace.getUtility(bid);
		}
		catch(Exception e) {
			System.err.println("WARNING: TRIED TO ACCESS REAL FUNCTION WHEN NOT AVAILABLE");
			return getUtility(bid);
		}
	}
	
	private void updateDeltaUtility(double newUtility) {
		utilitiesOffered.addLast(newUtility);
		if (utilitiesOffered.size() > UTILITY_HISTORY) {
			utilitiesOffered.removeFirst();
		}
		double deltaUtility = 1.0;
		Double lastUtility = null;
		for (Double utility : utilitiesOffered) {
			if (lastUtility != null) {
				deltaUtility += utility - lastUtility;
			} 
			lastUtility = utility;
		}
		estimatedNextUtility = newUtility + deltaUtility / utilitiesOffered.size();
	}
	
	private void updateRoundEstimation() {
		try {
			if (lastRoundEnd > 0) {
				double roundTime = getTimeLine().getTime() - lastRoundEnd;
				roundTimes.addLast(roundTime);
				if (roundTimes.size() > ROUND_TIME_HISTORY) {
					roundTimes.removeFirst();
				}
				double averageRoundTime = 0.0;
				for (Double time : roundTimes) {
					averageRoundTime += time;
				}
				averageRoundTime /= roundTimes.size();
				estimatedRoundsRemaining = (int) ((1 - getTimeLine().getTime()) / averageRoundTime) - 1;
			}
		} catch (Exception e) {
			// TODO: Not sure what happens if this fails.
			e.printStackTrace();
		}
	}
	
    /**
     * Main algorithm for determining acceptance of an offer
     *
     * @param lastOffer The last offer made by another agent
     * @return true if should accept, false otherwise
     * @throws Exception
     */
    private boolean shouldAcceptOffer(Bid offer) throws Exception {
        try {
        	double offeredUtility = getUtility(offer);
            
            double target;
            if (estimatedRoundsRemaining > 2) {
            	target = targetUtility;
            } else {
            	target = (bestReceivedUtility + targetUtility) / 2;
            }
            
            if (estimatedRoundsRemaining <= 1) {
            	//System.out.println("LAST DITCH ACCEPT!");
            	return offeredUtility >= bestReceivedUtility;
            }
            
            if (offeredUtility >= target) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        	return false; // Perhaps should return true if we're failing on this!
        }
    }
    
	/**
	 * Takes a bid and returns an estimate of the utility it has to the opponent, based 
	 * on the opponent model.
	 * 
	 * @param bid
	 * @return The estimated utility of the bid to the opponent.
	 */
	private double estimateOpponentUtility(Bid bid) {
		try {
			double utility = 0.0;
			for (int i = 0; i < bid.getIssues().size(); i++) {
				//IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
				Issue issue = bid.getIssues().get(i);
				
				double maxIssueScore = 0.0;
				for (Double score : issueValueScores.get(issue).values()) {
					maxIssueScore = score > maxIssueScore ? score : maxIssueScore;
				}
				
				double score = issueValueScores.get(issue).get(bid.getValue(i+1));
				utility += issueWeights.get(issue) * (score / maxIssueScore);
			}
			return utility;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Better way to handle this?
			return 0.0;
		}
	}
	
	/**
	 * Updates the offer acceptance criteria. 
	 */
	private void updateAcceptanceCritera() {
		try {
			double time = getTimeLine().getTime();
			
			// increase granularity when not much time left - opponent may get more whacky
//			if (time > 0.9) {
//				fue.setWindowSize(1);
//			}
			
			Bid nashBid = estimateNashBid();
			thresholdUtility = Math.max(bestReceivedUtility, getOwnUtility(nashBid) - 
					beyondNashConcession(nashBid));
			
			// Simple conceding logic.
			double coef = ka + (1-ka)*Math.pow(1-time, beta);
			targetUtility = thresholdUtility + (coef * (1 - thresholdUtility));
			
			double concederBias = 8.0;
			targetUtility = (targetUtility*concederBias + estimatedNextUtility) / (1 + concederBias);
			
//			try {
//				double future = fue.maxFutureUtility(roundCount + 5);
//				if (future <= 1) {
					// TODO: Perhaps revert concederBias change.
//					double concederBias = 3.0;
					
//					targetUtility = (targetUtility*concederBias + future) / (1 + concederBias);
//				} else {
//					throw new Exception("darned M5P");
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: What do we do if we fail here?
		}
	}
	
	/**
	 * @param nashBid The estimated Nash bid point.
	 * @return The utility to concede beyond the Nash bid point.
	 */
	private double beyondNashConcession(Bid nashBid) {
		// TODO: Better logic to determine how much to dip below Nash bid utility.
		// TODO: Base it on time to go!
		// TODO: Only concede up to as much as the utility of the best bid received so far! 
		
		try {
//			double nashUtility = getOwnUtility(nashBid);
//			double concession = (nashUtility/15) * getTimeLine().getTime();
			
			//return concession;
			return 0; // TODO: Removed for now.
		} catch (Exception e) {
			e.printStackTrace();
			return 0; // Perhaps concede more on fail.
		}
	}

	/**
	 * Updates the predicted opponent model.
	 */
	private void updateOpponentModel() {
		try {
			double weightSum = 0;
			
			for (Entry<Integer, Value> entry : latestOffer.getValues().entrySet()) {
				Integer issueNumber = entry.getKey();
				Value issueValue = entry.getValue();
				Issue issue = latestOffer.getIssues().get(issueNumber-1);
				
				Map<Value, Integer> valueCounts = issueValueCounts.get(issue);
				valueCounts.put(issueValue, valueCounts.get(issueValue)+1);
				
				// Update predicted scores.
				Map<Value, Double> valueScores = issueValueScores.get(issue);
				Double valueScore = valueScores.get(issueValue);
				double addScore = (1.0 + (1 - getTimeLine().getTime())*5) / offerCount;
				valueScores.put(issueValue, valueScore + addScore);
				
				// Re-normalise.
				for (Entry<Value, Double> valueScoreEntry : valueScores.entrySet()) {
					valueScores.put(valueScoreEntry.getKey(), 
							valueScoreEntry.getValue()/(1+addScore));
				}
				
				// Update predicted weights.
				double w = 0.0;
				for (Entry<Value, Integer> valueCount : valueCounts.entrySet()) {
					int f = valueCount.getValue();
					w += Math.pow(f, 2) / Math.pow(offerCount, 2);
				}
				issueWeights.put(issue, w);
				weightSum += w;
			}
			
			// Normalise predicted weights.
			for (Issue issue : issueWeights.keySet()) {
				issueWeights.put(issue, issueWeights.get(issue)/weightSum);
			}
		} catch (Exception e) {
			// TODO: What to do if we fail here?
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a suitable counter-bid based on the current information.
	 * 
	 * @return A counter bid.
	 */
	private Bid generateCounterBid() {
		try {
			// Check if last minute.
			if (lastRoundEnd > 0 && estimatedRoundsRemaining <= 1 && LAST_DITCH_MODE) {
				return bestReceivedBid;
			}
			
			
			List<Bid> bids = generateBidsAboveUtility(targetUtility);
			
			double r = new Random().nextDouble();
			if (r < RANDOM_FACTOR) {
				List<Double> scores = getCounterBidScores(bids);
				BiasedSelectionSet<Bid> selectionSet = new BiasedSelectionSet<>(bids, scores);
				return selectionSet.randomSelect();
			} else {
				sortBidsByOpponentUtility(bids);
				return bids.get(bids.size()-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Have a fall back bid.
			return null;
		}
		
	}
	
	// TODO: Make efficient.
	/**
	 * Generates all possible bids in the domain (this may be a silly thing to do!)
	 * 
	 * @return List of all possible bids.
	 */
	private List<Bid> generateAllBids() {
		try {
			List<Bid> bids = new ArrayList<>();
			List<Issue> issues = getDomain().getIssues();
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
				
				Bid bid = new Bid(getDomain(), valueMap);
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
			
			return bids;
		} catch (Exception e) {
			e.printStackTrace();
			List<Bid> bids = new ArrayList<>();
			// TODO: Add fall back bid to bids list.
			return bids;
		}
	}
	
	/**
	 * Generates a list of all possible bids in the domain sorted by their predicted 
	 * utility to us.
	 * 
	 * @return Sorted list of all bids.
	 */
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
			System.err.println("WARNING: TRIED TO ACCESS UTILITY WHEN UTILITYSPACE NOT AVAILABLE");
			e.printStackTrace();
			return generateAllBids(); // Better way of handling ?
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
			System.err.println("WARNING: TRIED TO ACCESS REAL FUNCTION WHEN NOT AVAILABLE");
			return generateSortedBids();
		}
	}
	
	
	// TODO: Make efficient.
	/**
	 * Generates a list of all bids which have a utility greater than or equal to the 
	 * given utility.
	 * 
	 * @param thresholdUtilty
	 * @return A list of bids.
	 */
	private List<Bid> generateBidsAboveUtility(double thresholdUtilty) {
		try {
			thresholdUtility = thresholdUtility > 1 ? 1 : thresholdUtility;
			
			List<Bid> bids = new ArrayList<>();
			
			boolean add = false;
			for (Bid bid : generateSortedBids()) {
				if (add || getOwnUtility(bid) >= thresholdUtilty) {
					bids.add(bid);
					add = true;
				}
			}
			
			// TODO: This shouldn't happen if model is correctly normalised. 
			if (bids.isEmpty()) {
				bids.add(getMaxUtilityBid());
				System.err.println("WARN: No bids above target utility.");
			}
			
			return bids;
		} catch (Exception e) {
			e.printStackTrace();
			return generateSortedBids(); // TODO: Better way of handling this ?
		}
	}
	
	/**
	 * Sorts the given list of bids by their estimated utility to the opponent.
	 * 
	 * @param bids
	 */
	private void sortBidsByOpponentUtility(List<Bid> bids) {
		try {
			bids.sort(new Comparator<Bid>() {

				@Override
				public int compare(Bid bid0, Bid bid1) {
					return Double.compare(estimateOpponentUtility(bid0),
							estimateOpponentUtility(bid1));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// TODO: Make efficient.
	/**
	 * Estimates the Nash bid from the self and opponent models.
	 * 
	 * @return
	 */
	private Bid estimateNashBid() {
		try {
			double maxNash = -1;
			Bid nashBid = null;
			for (Bid bid : generateAllBids()) {
				double nash = getOwnUtility(bid) * estimateOpponentUtility(bid);
				if (nash > maxNash) {
					nashBid = bid;
					maxNash = nash;
				}
			}		
			return nashBid;
		} catch (Exception e) {
			e.printStackTrace();
			return null; // TODO: Return fall back bid.
			// TODO better way of handling this, e.g. have this method return Nash UTILITY and if 
			// it fails return 0.4 or something.
		}
	}
	
	/**
	 * @return The max utility bid.
	 */
	private Bid getMaxUtilityBid() {
		try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
		// TODO: Generate a fall-back bid, just in case.
        return null;
	}
	
	/**
	 * @return The min utility bid.
	 */
	private Bid getMinUtilityBid() {
		try {
            return this.utilitySpace.getMinUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
	}
	
	private final double INTER_UTIL_CARE = 5; // How much a difference in utility makes.
	private final double INTER_TIME_CARE = 2; // How much a difference in time since makes.
	private final double UTIL_CARE = 0.3; // How much opponent utility contributes to score.
	private final double TIME_CARE = 0.7; // How much time since contributes to score.
	/**
	 * Calculates a 'score' for each bid which will then be used as a probability of using
	 * that bid as the next counter-offer. This is currently a function of the bid's 
	 * utility to the opponent and the time since that same bid has been made.
	 * 
	 * @param bids Bids above target utility this round.
	 * @return A list of scores for the given bids.
	 */
	private List<Double> getCounterBidScores(List<Bid> bids) {
		try {
			double sum = 0;
			List<Double> utilScores = new ArrayList<>();
			// Raise opponent bid utility to INTER_UTIL_CARE and set as utility score.
			for (Bid bid : bids) {
				double utilScore = Math.pow(estimateOpponentUtility(bid), INTER_UTIL_CARE);
				utilScores.add(utilScore);
				sum += utilScore;
			}
			// Normalise sum of utility scores to 1.
			for (int i = 0; i < utilScores.size(); i++) {
				utilScores.set(i, utilScores.get(i)/sum);
			}
			
			sum = 0;
			double roundLimit = bids.size();
			List<Double> roundScores = new ArrayList<>();
			for (Bid bid : bids) {
				Integer lastRoundSeen = sentBidRound.get(bid);
				double roundsSince = roundLimit; // If unseen then say it was seen at the round limit.
				if (lastRoundSeen != null) {
					roundsSince = roundCount - lastRoundSeen;
				}
				double roundScore = Math.pow(Math.min(roundsSince / roundLimit, 1), 
						INTER_TIME_CARE);
				roundScores.add(roundScore);
				sum += roundScore;
			}
			for (int i = 0; i < bids.size(); i++) {
				roundScores.set(i, roundScores.get(i)/sum);
			}
			
			List<Double> scores = new ArrayList<>();
			for (int i = 0; i < bids.size(); i++) {
				scores.add(UTIL_CARE * utilScores.get(i) + TIME_CARE * roundScores.get(i));
			}
			
			return scores;
		} catch (Exception e) {
			e.printStackTrace();
			List<Double> scores = new ArrayList<Double>();
			if (bids != null) {
				for (int i = 0; i < bids.size(); i++) {
					scores.add(1.0); // TODO: Is this right?
				}
			}
			return scores;
		}
	}
	
	private AdditiveUtilitySpaceFactory utilityFactory;
	
    /*
     * This overrides the bad default method for getting a numerical representation of a partially ordered utility space
     * @see genius.core.parties.AbstractNegotiationParty#estimateUtilitySpace()
     */
	@Override
	public AbstractUtilitySpace estimateUtilitySpace() {
		try {
			Domain domain = getDomain();
			domain.getIssues();
			AdditiveUtilitySpaceFactory factory = new AdditiveUtilitySpaceFactory(domain);
			BidRanking bidRanking = userModel.getBidRanking();
			try {
				estimateUsingBidRanks(bidRanking, domain, factory);
			}
			catch (Exception e) {}
			bigFactory = factory;
			utilityFactory = factory;
			return factory.getUtilitySpace();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Not really sure how to handle this, maybe fall back on default method.
			return new AdditiveUtilitySpaceFactory(getDomain()).getUtilitySpace();
		}

	}
		
	//TODO: Consider - do we want to place unknown issues ahead of ones we rank worst?
	//Currently ranks them below even the worst.
	//TODO: Make less brittle if 2 issues with same name.
	public void estimateUsingBidRanks(BidRanking r, Domain d, AdditiveUtilitySpaceFactory factory) {
		try {
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
			
			//QUICK FIX LOOK AT MEEEEE
			
			for (Issue i: d.getIssues()) {
				IssueDiscrete id = (IssueDiscrete) i;
				Double currentLowest = 99999.0;
				for (ValueDiscrete v : id.getValues()) {
					Double score = issueValues.get(v);
					if (score < currentLowest && score != 0.0) {
						currentLowest = score;
					}
				}
				for (ValueDiscrete v : id.getValues()) {
					Double score = issueValues.get(v);
					if (score == 0.0) {
						issueValues.put(v, currentLowest);
						factory.setUtility(i, v, currentLowest);
					}
				}
			}
			
			/*for (Issue i : d.getIssues()) {
				IssueDiscrete id = (IssueDiscrete) i;
				for (ValueDiscrete v : id.getValues()) {
					factory.getUtility(i, v);
				}
			}*/
	
			if (DEBUG_MODE) {
				Double totalError = evaluateEstimations(issueValues, d);
				System.out.println("TOTAL ERROR: "+totalError);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: How best to handle this.
		}
		
		//normalizeWeightsByMaxValues(factory);
	}
	
	private Double evaluateEstimations(HashMap<ValueDiscrete, Double> issueValues, Domain d){
		
		Double totalAgentErrorIssueSizeWeightAdjusted = 0.0;
		for (Issue i: d.getIssues()) {
			IssueDiscrete id = (IssueDiscrete) i;
			HashMap<ValueDiscrete, Double> currentIssueValues  = new HashMap<ValueDiscrete, Double>();
			for (ValueDiscrete v : id.getValues()) {
				Double score = issueValues.get(v);
				currentIssueValues.put(v, score);
			}
			
			//Adapted from https://dzone.com/articles/how-to-sort-a-map-by-value-in-java-8
			//Orders the set by value
			final LinkedHashMap<ValueDiscrete, Double> sortedItems = currentIssueValues.entrySet()
	                .stream()
	                .sorted(Map.Entry.<ValueDiscrete, Double>comparingByValue().reversed())
	                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			
			totalAgentErrorIssueSizeWeightAdjusted += evaluateEstimationForIssue(i, sortedItems);
		}
		return totalAgentErrorIssueSizeWeightAdjusted;
	}
	
	private AdditiveUtilitySpace fbEstimate(List<Bid> bids) {
		try {
			UtilitySpace fallbackSpace = utilitySpace.copy();
			FBEstimator fbEstimator = new FBEstimator(getDomain(), utilitySpace, utilityFactory);
			boolean success = fbEstimator.estimate(bids);
			if (!success) {
				//return (AdditiveUtilitySpace) utilitySpace.copy();
				return (AdditiveUtilitySpace) fallbackSpace;
			}
			List<Double> maxConfidences = new ArrayList<Double>();
			List<Issue> temp = new ArrayList<Issue>();
			for (Issue issue : getDomain().getIssues()) {
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
				
				/*Double confidenceWorstItem = fbEstimator.getIssueHighLowScore(issue, issueValues.get(issueValues.size()-1), issueValues.get(issueValues.size()-2));
				//2* because confidence is twin-tailed, ~0.5 (unsure) will result in same utility as 2nd worst.
				confidenceWorstItem = 2*Math.max(confidenceWorstItem, 0.1); //prevents last item being scored ridiculously low
				//Multiplies uncertainty of it being worst one by utility of 2nd worst
				Double utilityWorstItem = confidenceWorstItem * normalizedConfidences.get(normalizedConfidences.size()-2);
				normalizedConfidences.set(normalizedConfidences.size()-1, utilityWorstItem);*/
				
				if (normalizedConfidences.size() > 1) {
					normalizedConfidences.set(normalizedConfidences.size()-1, normalizedConfidences.get(normalizedConfidences.size()-2)/2.0);
				}
				
				for(int z=0; z<normalizedConfidences.size(); z++) {
					bigFactory.setUtility(issue, (ValueDiscrete)issueValues.get(z), normalizedConfidences.get(z));
				}

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
			bigFactory.getUtilitySpace().setWeights(getDomain().getIssues(), issueWeightEstimations);
			AdditiveUtilitySpace space = bigFactory.getUtilitySpace();
			return space;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: How to handle this.
			return (AdditiveUtilitySpace) utilitySpace; // TODO: Remove cast, return abstract.
		}
		
		
	}
	
	//Takes an issue and a sorted (descending order) map of item-score pairs
	//TODO: Check that these differences produce a meaningful result
	private Double evaluateEstimationForIssue(Issue i, LinkedHashMap<ValueDiscrete, Double> issueValues) {
	
		Double max = null;
		Double aggregateError = 0.0;

		//normalizeWeightsByMaxValues(realSpace);
		
		for (Map.Entry<ValueDiscrete, Double> map : issueValues.entrySet()) {
			if (max == null) {
				max = map.getValue();
				if (max == 0.0) {
					//When no scores to report
					return 99999.9; //V. large number to show error
				}
				
			}
			//Scales score as % of max
			ValueDiscrete item = map.getKey();
			Double estimatedRelativeUtility = map.getValue()/max;
			
			//Evaluator required to access the real utility value. Takes Issue i as param
			
			if (realSpace == null) {
		        e = (ExperimentalUserModel) userModel;
		        realSpace = e.getRealUtilitySpace();
			}
			
			EvaluatorDiscrete eval = (EvaluatorDiscrete) realSpace.getEvaluator(i);
			
			try {
				Double actualRelativeUtility = eval.getEvaluation(item);
				aggregateError += Math.abs(actualRelativeUtility - estimatedRelativeUtility);
				
			} catch (Exception e1) {
				System.err.println("Error retrieving evaluation for "+item);
				e1.printStackTrace();
				return null;
			}
			//issueValues.put(map.getKey(), map.getValue()/max);
		}
		
		double result = aggregateError;
		Double adjusted = result/issueValues.size();
		Double wAdjusted = realSpace.getWeight(i) * adjusted; //Issues of relatively low weight are much harder to estimate, have less final impact, and are more affected by random chance. Multiplying by weight makes sure the final score reflects the most important issues.
		
		return wAdjusted;
	}
	
	//It's not actually hamming distance, but fight me anyway
	// 1,2,3,4 VS 4,1,3,2 has value of (1 + 2 + 0 + 3) = 6
	//Returns % of worst possible ordering
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
		
		double worstValue = (int) Math.floor(realBids.size() * realBids.size() / 2);
		double percentageDeviation = aggregateDistance*100/worstValue;
		return aggregateDistance*100/worstValue;
	}
	
	//Taken from the GENIUS class files - adjusts the values of the real utility functions to be scaled between 0 and 1 to fit with our scoring system
	public void normalizeWeightsByMaxValues(UncertainAdditiveUtilitySpace u) {
		try {
			
			for (Issue i : getDomain().getIssues())
			{
				EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
				evaluator.normalizeAll();
			}
			for (Issue i : getDomain().getIssues())
			{
				EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
				evaluator.scaleAllValuesFrom0To1();
			}
			u.normalizeWeights();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: How to handle.
		}
	}
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}
	
	// TODO: REMOVE!
	private Double bidUtilityDistance() {
		List<Bid> bids = generateAllBids();
		
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
		System.out.println("Utility Metric Performance: "+(100.0 - deviation*100)+"% Accurate.");
		return deviation;
	}
	
	/*public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
		List<Bid> bids = generateAllBids();
		sortBidsByOpponentUtility(bids);
		for (Bid bid : bids) {
			System.out.println(bid + " : " + estimateOpponentUtility(bid));
		}
		
		return null;
	}*/
	
}
