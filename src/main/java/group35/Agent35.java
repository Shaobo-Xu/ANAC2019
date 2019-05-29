package group35;


import java.util.List;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.ValueDiscrete;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.AdditiveUtilitySpaceFactory;
import genius.core.uncertainty.BidRanking;
import genius.core.utility.AdditiveUtilitySpace;
import genius.core.utility.EvaluatorDiscrete;

/**
 * ExampleAgent returns the bid that maximizes its own utility for half of the negotiation session.
 * In the second half, it offers a random bid. It only accepts the bid on the table in this phase,
 * if the utility of the bid is higher than Example Agent's last bid.
 */
public class Agent35 extends AbstractNegotiationParty {
    private final String description = "Agent35";

    private Bid lastReceivedOffer; // offer on the table
    private Bid myLastOffer;
    private double myMaxUtility;
    private AdditiveUtilitySpace opponetUtilitySpace;
    private AdditiveUtilitySpaceFactory opponentFactory;
    private int totalUncertBid;
    private List<Bid> myBidOrder;
    private int firstNBid;
    private double time;
    private double concedeutil;
    private Domain domain;
    double totaltime;


    @Override
    public void init(NegotiationInfo info) {

        super.init(info);
        BidRanking bidRanking = userModel.getBidRanking();
        totalUncertBid = bidRanking.getSize();
        myBidOrder = bidRanking.getBidOrder();
        myMaxUtility = 1;
        firstNBid =(int) Math.ceil(0.01*totalUncertBid);
        // Initiate opponent's utility space
        domain = this.utilitySpace.getDomain();
        opponentFactory = new AdditiveUtilitySpaceFactory(domain);
        opponetUtilitySpace = opponentFactory.getUtilitySpace();  // Generates an simple Utility Space on the domain, with equal weights and zero values. Everything is zero-filled to already have all keys contained in the utility maps.
        totaltime = getTimeLine().getTotalTime();
    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        // According to Stacked Alternating Offers Protocol list includes
        // Accept, Offer and EndNegotiation actions only.
        time = getTimeLine().getTime(); // Gets the time, running from t = 0 (start) to t = 1 (deadline).
        // The time is normalized, so agents need not be
        // concerned with the actual internal clock.
        concedeutil = (1-time*time*time)*myMaxUtility;
        //when time<0.01 generate rand highest bids use Gaussian random, else, generate  concede bid
        if (time < 0.01) {
            if (lastReceivedOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) > concedeutil) {

                return new Accept(this.getPartyId(), lastReceivedOffer);
            } else {
                // Offering a bid
                myLastOffer = getHighBidInRank();
                return new Offer(this.getPartyId(), myLastOffer);
            }
        } else {
            if (lastReceivedOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) > concedeutil) {

                return new Accept(this.getPartyId(), lastReceivedOffer);
            } else {
                // Offering a bid
                myLastOffer = getConcedeUtilityBid(concedeutil);
                return new Offer(this.getPartyId(), myLastOffer);
            }
        }
    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param act
     */
    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;

            // storing last received offer
            lastReceivedOffer = offer.getBid();

            if (getTimeLine().getTime()< 0.1) {
                List<Issue> issues = lastReceivedOffer.getIssues();
                for (Issue i : issues) {
                    int no = i.getNumber();
                    ValueDiscrete v = (ValueDiscrete) lastReceivedOffer.getValue(no);
                    EvaluatorDiscrete evaluator = (EvaluatorDiscrete) opponetUtilitySpace.getEvaluator(i);
                    double oldUtil = evaluator.getDoubleValue(v);
                    opponentFactory.setUtility(i, v, oldUtil + 1);
                }
            }

        }
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    private Bid getMaxUtilityBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * this method return the bid based on double concedeutil
     */
    public Bid getConcedeUtilityBid(double concedeutil) {
        double maxOpponentUtility=Double.NEGATIVE_INFINITY;
        double findAmount = 0;
        double initTime = getTimeLine().getTime();
        int fixAmount = (int) Math.ceil(initTime*initTime*initTime*11000);
        Bid concedeBid = null;
        Bid thisBid = null;
        while (findAmount < fixAmount ) {
            if ((getTimeLine().getTime()-initTime) > (5/totaltime)){
                findAmount = Double.MAX_VALUE;
                if(concedeBid == null) {
                    concedeBid = getHighBidInRank();
                }
            }
            thisBid = generateRandomBid();
            double thisutil = this.utilitySpace.getUtility(thisBid);
            double opponentutil = this.opponetUtilitySpace.getUtility(thisBid);
            if (thisutil > concedeutil ) {
                findAmount = findAmount+1;
                if (maxOpponentUtility < opponentutil) {
                    maxOpponentUtility = opponentutil;
                    //System.out.println(opponentutil);
                    concedeBid = thisBid;
                }

            }
        }
        //System.out.println("findAmout: "+findAmount +" startTime: " + initTime + " Concede utility :" +concedeutil);
        return concedeBid;
    }

    /**
     * this method return the bid with highest utility in bidranking
     */
    public Bid getHighBidInRank() {
        Bid highBid = null;
        double gauRandom ;
        do {
            gauRandom = rand.nextGaussian()*0.33;
            gauRandom = Math.abs(gauRandom);
        } while (gauRandom >= 1);
        int randomNinFirstNBid = (int) Math.floor(gauRandom*firstNBid);
        highBid = myBidOrder.get(myBidOrder.size()-1-randomNinFirstNBid);
        return highBid;
    }
}