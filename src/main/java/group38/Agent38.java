package group38;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.*;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.Evaluator;
import genius.core.utility.EvaluatorDiscrete;

import java.util.*;

public class Agent38 extends AbstractNegotiationParty {
    private final String description = "Agent38";

    private Bid lastReceivedOffer; // offer on the table
    private Bid myLastOffer;
    private AdditiveUtilitySpace additiveUtilitySpace;
    private AbstractUtilitySpace utilitySpace;
    private ArrayList<ArrayList<Integer>> freqTable;
    private Integer sumOfRows;
    private Double maxUtility;
    private Double minUtility;
    private Double distMaxMinUtil; // distance between max, and min utility
    private ArrayList<Double> weights;
    private HashMap<AgentID, ArrayList<ArrayList<Integer>>> partyFrequencyTables;
    private HashMap<AgentID, ArrayList<Double>> partyWeights;
    private HashMap<AgentID, ArrayList<ArrayList<Double>>> partyValueTables;
    private HashMap<AgentID, ArrayList<Integer>> valueIndexes;
    private Double epsilon;
    private HashMap<Integer, ArrayList<Value>> values = new HashMap<>();
    private int noOfPossibleBids;
    private Boolean evoBid;
    private Double beta;
    private Integer n;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        this.partyFrequencyTables = new HashMap<>();
        this.partyWeights = new HashMap<>();
        this.partyValueTables = new HashMap<>();
        this.valueIndexes = new HashMap<>();
        this.epsilon = 0.2;
        this.beta = 2.0;

        this.utilitySpace = this.estimateUtilitySpace();
        AdditiveUtilitySpace additiveUtilitySpace = (AdditiveUtilitySpace) utilitySpace;
        this.additiveUtilitySpace = additiveUtilitySpace;

        List<Issue> issues = additiveUtilitySpace.getDomain().getIssues();
        this.weights = new ArrayList<>();

        ArrayList<ArrayList<Integer>> freqTable = new ArrayList<>();
        noOfPossibleBids = 1;
        for (Issue issue : issues) {
            ArrayList<Integer> row = new ArrayList<>();

            int issueNumber = issue.getNumber();

            this.values.put(issueNumber, new ArrayList<>());

            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;
            EvaluatorDiscrete evaluatorDiscrete = (EvaluatorDiscrete) additiveUtilitySpace.getEvaluator(issueNumber);

            List<ValueDiscrete> values = issueDiscrete.getValues();

            int noOfValues = 0;
            for (ValueDiscrete v : values) {
                row.add(0);
                this.values.get(issueNumber).add(v);
                noOfValues++;
            }
            noOfPossibleBids *= noOfValues;
            freqTable.add(row);
        }
        this.freqTable = freqTable;
        this.sumOfRows = 0;
        for (int i = 0; i < freqTable.size(); i++) {
            this.weights.add(0.0);
        }

        Bid maxBid = getMaxUtilityBid();
        Bid minBid = getMinUtilityBid();
        Double maxUtility = utilitySpace.getUtility(maxBid);
        Double minUtility = utilitySpace.getUtility(minBid);
        Double distMaxMinUtil = maxUtility - minUtility; // distance between max utility, and minimum utility

        this.distMaxMinUtil = distMaxMinUtil;
        this.maxUtility = maxUtility;
        this.minUtility = minUtility;

        evoBid = false;
    }

    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        double time = getTimeLine().getTime();

        double bidThreshold = this.maxUtility * Math.pow((1 - time), this.beta);
        //Using concession strategy inspired by ParsAgent Khosravimehr, Z. and Nassiri-Mofakham, F., 2017. Pars Agent:
        // Hybrid Time-Dependent, Random and Frequency-Based Bidding and Acceptance Strategies in Multilateral
        // Negotiations. In Modern Approaches to Agent-based Complex Automated Negotiation (pp. 175-183). Springer, Cham

        if(time < 0.25) {
            Double randomBidsThreshold = this.minUtility + 0.8 * this.distMaxMinUtil;
            myLastOffer = generateRandomBidWithUtility(randomBidsThreshold);
            return new Offer(this.getPartyId(), myLastOffer);
        }
        else if (time >= 0.25 && time < 0.90) {
            if (bidThreshold < this.minUtility + 0.7 * this.distMaxMinUtil) {
                bidThreshold = this.minUtility + 0.7 * this.distMaxMinUtil;
            }

            if (lastReceivedOffer != null
                    && myLastOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) >= bidThreshold) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            } else {
                if(evoBid) {
                    myLastOffer = evolutionaryGenerateBid(minUtility + 0.5 * distMaxMinUtil);
                    return new Offer(this.getPartyId(), myLastOffer);
                }
                else {
                    myLastOffer = getMaxUtilityBid();
                    return new Offer(this.getPartyId(), myLastOffer);
                }
            }
        }
        else if (time >= 0.90 && time < 0.995){
            double acceptThreshold = this.minUtility + 0.7 * this.distMaxMinUtil;
            double normalisedTime = (time - 0.9) / (1 - 0.9); // normalise time between 0 and 1
            bidThreshold = this.minUtility + 0.7 * this.distMaxMinUtil - Math.pow(0.3 * this.distMaxMinUtil*(normalisedTime),4);

            if (lastReceivedOffer != null
                && myLastOffer != null
                && this.utilitySpace.getUtility(lastReceivedOffer) > acceptThreshold) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else {
                if(evoBid) {
                    myLastOffer = evolutionaryGenerateBid(minUtility + 0.5 * distMaxMinUtil);
                    return new Offer(this.getPartyId(), myLastOffer);
                }
                else {
                    myLastOffer = getMaxUtilityBid();
                    return new Offer(this.getPartyId(), myLastOffer);
                }
            }
        }
        else {
            if(utilitySpace.getUtility(lastReceivedOffer) > 0) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else {
                myLastOffer = evolutionaryGenerateBid(0.0);
                return new Offer(this.getPartyId(), myLastOffer);
            }
        }
    }

    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);
        double time = getTimeLine().getTime();
        AgentID agentID = sender;

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;
            lastReceivedOffer = offer.getBid();
            if (this.partyFrequencyTables.keySet().contains(agentID)) {
                if(time > 0.18) {
                    this.updateFrequencyTable(agentID);
                    this.updateWeights(agentID);
                    evoBid = true;
                }
            } else {

                this.generateFrequencyTable(agentID);
                this.generateWeights(agentID);
                this.generateValueTable(agentID);
                this.generateIndexes(agentID);

            }
            calculateValues(agentID);
            //this.printFreqTable(agentID);

        }
    }

    @Override
    public String getDescription () {
        return description;
    }

    private Bid getMaxUtilityBid () {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bid getMinUtilityBid () {
        try {
            return this.utilitySpace.getMinUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printFreqTable (AgentID agentID){
        ArrayList<ArrayList<Integer>> freqTable = this.partyFrequencyTables.get(agentID);

        Integer issueNumber = 0;
        for (ArrayList<Integer> row : freqTable) {
            issueNumber += 1;

            System.out.print("Issue " + issueNumber + ": ");
            for (Integer frequency : row) {
                System.out.print(frequency.toString() + "   ");
            }
            System.out.println(" ");
        }
        ArrayList<Integer> row = freqTable.get(0);

        Integer sum = 0;
        for (Integer x : row) {
            sum += x;
        }
        System.out.println("Sum of each row: " + sum);
    }

    private Bid generateRandomBidWithUtility(double utilityThreshold) {

        Bid randomBid = null;
        double utility = 0;

        //Ensure a random bid can be found based on threshold.
        if(utilityThreshold < this.utilitySpace.getUtility(getMaxUtilityBid())) {

            while(utility < utilityThreshold) {

                randomBid = generateRandomBid();
                try {
                    utility = this.utilitySpace.getUtility(randomBid);
                }
                catch (Exception e) {
                    utility = 0;
                }
            }
        }
        //If random bid is not possible using the threshold, just give the max utility bid.
        else {
            randomBid = getMaxUtilityBid();
        }
        return randomBid;
    }

    // Initialise the frequency table for the party
    private void generateFrequencyTable (AgentID agentID){
        ArrayList<ArrayList<Integer>> partyFrequencyTable = new ArrayList<>();
        this.partyFrequencyTables.put(agentID, this.freqTable);
    }

    private void generateValueTable (AgentID agentID){
        //System.out.println("generate Value table has been reached");
        ArrayList<ArrayList<Double>> partyValueTable = new ArrayList<>();

        // Create a copy of the frequency table
        for (ArrayList<Integer> row : this.freqTable) {
            ArrayList<Double> partyValueTableRow = new ArrayList<>();
            for (Integer i : row) {
                partyValueTableRow.add(0.0);
            }
            partyValueTable.add(partyValueTableRow); // add row of zeros to the table
        }
        this.partyValueTables.put(agentID, partyValueTable);
        //System.out.println(this.partyValueTables.get(agentID).toString());

    }

    private void generateWeights (AgentID agentID){
        Integer size = this.freqTable.size();
        ArrayList<Double> weights = new ArrayList<>();

        for (Integer i = 0; i < size; i++) {
            weights.add(0.0);
        }
        this.partyWeights.put(agentID, weights);
    }

    private void generateIndexes (AgentID agentID){
        Integer size = this.freqTable.size();
        ArrayList<Integer> indexes = new ArrayList<>();

        for (Integer i = 0; i < size; i++) {
            indexes.add(-1);
        }
        this.valueIndexes.put(agentID, indexes);
    }

    // Implements opponent modelling strategy discussed in:
    // Ito, T., Zhang, M., Robu, V. and Matsuo, T. eds., 2013. Complex automated negotiations: Theories, models,
    // and software competitions. Springer Berlin Heidelberg.
    // AND:
    // Tunalı, O., Aydoğan, R. and Sanchez-Anguix, V., 2017, October. Rethinking frequency opponent modeling in
    // automated negotiation. In International Conference on Principles and Practice of Multi-Agent Systems
    // (pp. 263-279). Springer, Cham.
    private void calculateValues (AgentID agentID){
        ArrayList<ArrayList<Double>> valueTable = this.partyValueTables.get(agentID);
        ArrayList<ArrayList<Integer>> frequencyTable = this.partyFrequencyTables.get(agentID);

        for (Integer i = 0; i < valueTable.size(); i++) {
            ArrayList<Integer> row = frequencyTable.get(i);
            Integer numberOfOptions = row.size();

            // get the maximum value of the frequency table
            Integer maxFreq = 0;
            for (int k = 0 ; k < numberOfOptions ; k++) {
                if (row.get(k) >= maxFreq) {
                    maxFreq = row.get(k);
                }
            }

            for (Integer j = 0; j < numberOfOptions; j++) {
                try {
                    Double value = row.get(j).doubleValue() / maxFreq.doubleValue();
                    valueTable.get(i).set(j, value);
                } catch (Exception e) {

                }
            }
        }
    }

    private void updateWeights (AgentID agentID){
        HashMap<Integer, Value> issueValue = this.lastReceivedOffer.getValues();

        for (Integer i : issueValue.keySet()) {
            // get the value associated with i
            Value v = issueValue.get(i);

            // get the index of the issue
            Integer issueIndex = i - 1;

            // get the issue from the index
            Issue issue = this.additiveUtilitySpace.getDomain().getIssues().get(issueIndex);

            IssueDiscrete issueDiscrete = (IssueDiscrete) issue;

            // get the index of the value
            Integer valueIndex = issueDiscrete.getValues().indexOf(v);

            // now we have the valueIndex, and the issueIndex, we can check we have had two consecutive values
            if (this.valueIndexes.get(agentID).get(issueIndex).intValue() == valueIndex.intValue()) {
                Double oldWeight = this.partyWeights.get(agentID).get(issueIndex);
                // update the weights
                this.partyWeights.get(agentID).set(issueIndex, oldWeight + this.epsilon);
            }
            // update the value index
            this.valueIndexes.get(agentID).set(issueIndex, valueIndex);
        }
        // now we calculate the sum of the weights

        Double sumOfWeights = 0.0;
        for (Double weight : this.partyWeights.get(agentID)) {
            sumOfWeights += weight;
        }
        //System.out.println("Sum Of weights" + sumOfWeights);

        // now we normalise the weights if the sum of weights is not 0
        if (sumOfWeights != 0) {
            for (int i = 0; i < this.partyWeights.get(agentID).size(); i++) {
                Double weight = this.partyWeights.get(agentID).get(i);
                this.partyWeights.get(agentID).set(i, weight / sumOfWeights);
            }
        }
    }

    private void updateFrequencyTable (AgentID agentID){
        //System.out.println("Reached update frequency table");
        HashMap<Integer, Value> values = lastReceivedOffer.getValues();
        ArrayList<ArrayList<Integer>> partyFrequencyTable = this.partyFrequencyTables.get(agentID);

        // add the frequency to the frequency table
        for (Integer i : values.keySet()) {
            try {
                Integer issueNumber = i;
                Value value = lastReceivedOffer.getValue(issueNumber);

                // get the issue from the issue number
                Issue issue = this.additiveUtilitySpace.getDomain().getIssues().get(issueNumber - 1);

                IssueDiscrete issueDiscrete = (IssueDiscrete) issue;

                // get the index of the value
                Integer indexOfValue = issueDiscrete.getValues().indexOf(value);

                // the row of freqTable is issueNumber - 1, the col is indexOfValue

                // find the new frequency
                Integer newFrequency = partyFrequencyTable.get(issueNumber - 1).get(indexOfValue) + 1;
                // put the new frequency in the table
                partyFrequencyTable.get(issueNumber - 1).set(indexOfValue, newFrequency);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private double getOpponentUtility(Bid b) {

        AgentID id = null;
        double u = 0;
        for(AgentID s : partyWeights.keySet()) {
            id = s;
        }

        for(int x=0; x<b.getValues().keySet().size(); x++) {
            double weight = partyWeights.get(id).get(x);
            double vO = partyValueTables.get(id).get(x).get(valueIndexes.get(id).get(x));
            u += weight * vO;
        }

        return u;
    }

    private double evalBid(Bid b) {

        double dt = getTimeLine().getTime() * 0.2;
        double alpha1 = 0.65 - dt;
        double alpha2 = 0.35 + dt;
        //System.out.println(alpha1 + "   " + alpha2);

        return (alpha1 * this.utilitySpace.getUtility(b)) + (alpha2 * getOpponentUtility(b));
    }

    private Bid mutateBid(Bid b) {

        for(Issue i : b.getIssues()) {
            if(Math.random() < 1.0 / b.getIssues().size()) {
                int r = (int)(Math.random() * (values.get(i.getNumber()).size()));
                b.putValue(i.getNumber(), values.get(i.getNumber()).get(r));
            }
        }
        return b;
    }

    private Bid crossoverBids(Bid a, Bid b) {

        int point = (int)(Math.random() * (b.getIssues().size()));
        HashMap<Integer, Value> values = new HashMap<>();
        for(int x=0; x<a.getIssues().size(); x++) {
            if(x < point) {
                values.put(x + 1, a.getValue(x+1));
            }
            else {
                values.put(x + 1, b.getValue(x+1));
            }
        }
        Bid newBid = new Bid(getDomain(), values);
        return newBid;
    }

    private Bid pickBid(ArrayList<Bid> population, ArrayList<Double> fitnesses) {

        ArrayList<Double> wheel = new ArrayList<>();
        double sum = 0;

        for(int x=0; x<population.size(); x++) {
            sum += fitnesses.get(x);
            wheel.add(sum);
        }
        double pick = Math.random() * sum;
        int i = 0;
        while(i < population.size() && wheel.get(i) < pick) {
            i++;
        }
        return population.get(i);
    }

    // Genetic algorithm ispired by:
    // Watson, R.A., 2004, September. A simple two-module problem to exemplify building-block assembly under crossover.
    // In International Conference on Parallel Problem Solving from Nature (pp. 161-171). Springer, Berlin, Heidelberg.
    private Bid evolutionaryGenerateBid(Double utilityThreshold) {

        //Initialise population
        ArrayList<Bid> bids = new ArrayList<>();
        ArrayList<Double> fitnesses = new ArrayList<>();
        double maxUFound = 0;
        Bid maxBid = null;
        int roundFound = 0;

        int x = 0;
        while ( x < 100) {
            Bid newBid = generateRandomBid();
            x += 1;
            bids.add(newBid);
            fitnesses.add(new Double(evalBid(newBid)));
            if(evalBid(newBid) > maxUFound) {
                maxBid = newBid;
                maxUFound = evalBid(newBid);
            }
        }

        int generation = 1;
        int y = 0;
        int maxGen = noOfPossibleBids / 300;
        if(maxGen < 5) {
            maxGen = 5;
        }
        else if(maxGen > 100) {
            maxGen = 100;
        }

        while(generation < maxGen) {
            ArrayList<Bid> newPopulation = new ArrayList<>();
            ArrayList<Double> newFitnesses = new ArrayList<>();

            y = 0;
            while ( y < 99 ) {
                Bid child = mutateBid(crossoverBids(pickBid(bids, fitnesses), pickBid(bids, fitnesses)));

                newPopulation.add(child);
                newFitnesses.add(evalBid(child));
                if (evalBid(child) > maxUFound) {
                    maxBid = child;
                    maxUFound = evalBid(child);
                    roundFound = generation;
                }
                y += 1;
            }
            newPopulation.add(maxBid);
            newFitnesses.add(maxUFound);

            bids = newPopulation;
            fitnesses = newFitnesses;
            generation++;
        }

        Random rand = new Random();
        int pick = rand.nextInt(bids.size());
        while(utilitySpace.getUtility(bids.get(pick)) < this.minUtility + 0.5 * this.distMaxMinUtil) {
            pick = rand.nextInt(bids.size());
        }

        //System.out.println("Social Welfare: " + maxUFound);
        ///System.out.println("Our Utility: " + this.utilitySpace.getUtility(maxBid));
        //System.out.println("Their Utility: " + getOpponentUtility(maxBid));
        //System.out.println("Generation Found:   " + roundFound + " / " + maxGen);
        return maxBid;
    }

    @Override
    public AbstractUtilitySpace estimateUtilitySpace() {
        Domain domain = getDomain();
        AdditiveUtilitySpaceFactory_ factory = new AdditiveUtilitySpaceFactory_(domain);
        BidRanking bidranking = this.userModel.getBidRanking();
        Integer numOfBids = bidranking.getBidOrder().size();
        Double n_ = ( numOfBids.doubleValue() / 20.0 );
        int n = n_.intValue();
        factory.estimateUsingBidRanks(bidranking,n);
        return factory.getUtilitySpace();
    }

    // ===================================================================================
    //   Dealing with utility
    // ===================================================================================
    // Modified Genius AdditiveUtilitySpaceFactory from:
    // Source code available at https://tracinsy.ewi.tudelft.nl/pubtrac/Genius/browser#src/main/java/genius/core/bidding

    public class AdditiveUtilitySpaceFactory_
    {
        private AdditiveUtilitySpace u;

        /**
         * Generates an simple Utility Space on the domain, with equal weights and zero values.
         * Everything is zero-filled to already have all keys contained in the utility maps.
         */
        public AdditiveUtilitySpaceFactory_(Domain d)
        {
            List<Issue> issues = d.getIssues();
            int noIssues = issues.size();
            Map<Objective, Evaluator> evaluatorMap = new HashMap<Objective, Evaluator>();
            for (Issue i : issues) {
                IssueDiscrete issue = (IssueDiscrete) i;
                EvaluatorDiscrete evaluator = new EvaluatorDiscrete();
                evaluator.setWeight(1.0 / noIssues);
                for (ValueDiscrete value : issue.getValues()) {
                    evaluator.setEvaluationDouble(value, 0.0);
                }
                evaluatorMap.put(issue, evaluator);
            }

            u = new AdditiveUtilitySpace(d, evaluatorMap);
        }

        /**
         * Sets e_i(v) := value
         */
        public void setUtility(Issue i, ValueDiscrete v, double value)
        {
            EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
            if (evaluator == null)
            {
                evaluator = new EvaluatorDiscrete();
                u.addEvaluator(i, evaluator);
            }
            evaluator.setEvaluationDouble(v, value);
        }

        public double getUtility(Issue i, ValueDiscrete v)
        {
            EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
            return evaluator.getDoubleValue(v);
        }

        /**
         * A simple heuristic for estimating a discrete {@link AdditiveUtilitySpace} from a {@link BidRanking}.
         * Gives 0 points to all values occurring in the lowest ranked bid,
         * then 1 point to all values occurring in the second lowest bid, and so on.
         */
        public void estimateUsingBidRanks(BidRanking bidRanking, Integer n) {
            // list of bids
            List<Bid> bidOrder = bidRanking.getBidOrder();

            // find the number of issues
            Integer numberOfIssues = bidOrder.get(0).getIssues().size();
            Double coefficient = numberOfIssues.doubleValue();

            // number of bids in bidOrder
            Integer numOfBids = bidOrder.size();
            //this.numberOfBids = numberOfBids;

            Integer bidRank = 1; // initialise the rank we assign to each bid

            // Initialise the points
            Double points = coefficient * bidRank;
            for (Integer bidIndex = 0; bidIndex < numOfBids; bidIndex++) {
                ArrayList<Integer> frequencyOfValues = countValues(bidOrder, bidIndex, n);

                // find the sum of the frequencies
                Double sumOfFrequencies = 0.0;
                for (Integer f : frequencyOfValues) {
                    sumOfFrequencies += f.doubleValue();
                }

                // find the fraction of points we assign to each value
                ArrayList<Double> newPoints = new ArrayList<>(); // store the number of points for each value
                for (Integer f : frequencyOfValues) {
                    newPoints.add((f.doubleValue() / sumOfFrequencies) * points);
                }

                // Update the utility of each value
                List<Issue> issues = bidOrder.get(bidIndex).getIssues();
                Bid bid = bidOrder.get(bidIndex);
                for (Issue issue : issues) {
                    Integer issueNumber = issue.getNumber();
                    ValueDiscrete v = (ValueDiscrete) bid.getValue(issueNumber);

                    Double oldUtility = getUtility(issue, v);
                    Double newUtility = oldUtility + newPoints.get(issueNumber - 1);

                    setUtility(issue, v, newUtility);
                }

                bidRank++;
                points = coefficient * bidRank; // update the points we distribute throughout each issue
                normalizeWeightsByMaxValues();
            }
        }

        public void normalizeWeightsByMaxValues()
        {
            for (Issue i : getIssues())
            {
                EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
                evaluator.normalizeAll();
            }
            for (Issue i : getIssues())
            {
                EvaluatorDiscrete evaluator = (EvaluatorDiscrete) u.getEvaluator(i);
                evaluator.scaleAllValuesFrom0To1();
            }
            u.normalizeWeights();
        }

        /**
         * Returns the utility space that has been created.
         */
        public AdditiveUtilitySpace getUtilitySpace()
        {
            return u;
        }

        private List<Issue> getIssues()
        {
            return getDomain().getIssues();
        }

        private Domain getDomain() {
            return u.getDomain();
        }

        private ArrayList<Integer> countValues(List<Bid> bidOrder, Integer bidIndex, Integer n) {
            // this method takes each value in the bid at bidIndex in the bidOrder list, and counts how many bids have
            // the same values in the range of bids [bidIndex - n, bidIndex + n].
            Bid bid = bidOrder.get(bidIndex);
            Integer numOfBids = bidOrder.size();

            ArrayList<Integer> counterList = new ArrayList<>();

            // for bids in the range [bidIndex - n, bidIndex + n], we calculate the number of times the same values occurs
            List<Issue> issues = bid.getIssues();
            for (Issue issue : issues) {
                Integer issueNumber = issue.getNumber();

                ValueDiscrete valueDiscrete = (ValueDiscrete) bid.getValue(issueNumber);

                // initialise the counter
                int counter = 1; // start at one to include valueDiscrete

                // compareIndex is the index of the bid which we compare the values against the bid at bidIndex
                for (Integer compareIndex = bidIndex - n; compareIndex <= bidIndex + n; compareIndex += 1) {
                    if (!compareIndex.equals(bidIndex) &&
                            compareIndex >= 0 &&      // ensure compareIndex is within the size of bifOrder
                            compareIndex <= numOfBids - 1) { // ensure compareIndex is within the size of bifOrder

                        Bid compareBid = bidOrder.get(compareIndex); // get the bid which we compare the values to bid

                        ValueDiscrete compareValueDiscrete = (ValueDiscrete) compareBid.getValue(issueNumber);
                        if (valueDiscrete.equals(compareValueDiscrete)) {
                            counter += 1;
                        }
                    }
                }
                counterList.add(counter);
            }
            return counterList;
        }

    }

}

