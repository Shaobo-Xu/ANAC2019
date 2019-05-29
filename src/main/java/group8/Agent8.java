package group8;

import java.util.List;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;

import genius.core.issue.Issue;
import java.util.ArrayList;

import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

import genius.core.uncertainty.BidRanking;
import genius.core.utility.AbstractUtilitySpace;



import genius.core.uncertainty.AdditiveUtilitySpaceFactory;


public class Agent8 extends AbstractNegotiationParty {


    private final String description = "Agent8";

    private Bid lastReceivedOffer;
    private Bid myLastOffer;
    private Bid OpponentOffer;
    private static ArrayList <Bid> OpponentBidList;

    @Override
    public void init(NegotiationInfo info) {

        super.init(info);
        OpponentBidList = new ArrayList<Bid>();
        List<Bid> bids = userModel.getBidRanking().getBidOrder();
        for (Bid bid : bids) {


            List<Issue> issuesList = bid.getIssues();

            for (Issue issue : issuesList) {
                int issueNumber = issue.getNumber();

                String issueName = issue.getName();

                System.out.println("Issue Name:" + issueName + ": " + bid.getValue(issueNumber));
            }
        }

        if (isUncertain()) {
            this.utilitySpace = estimateUtilitySpace();
        }


    }


    @Override
    public Action chooseAction(List<Class<? extends Action>> possibleActions) {
        double time = getTimeLine().getTime();

        Bid myMaxUtilityBid = this.getMaxUtilityBid();

        double myMaxUtility = this.utilitySpace.getUtility(myMaxUtilityBid);


        if (time < 0.1)
        {
            double presentThreshold = 0.95;
            if (lastReceivedOffer == null)
            {
                return MakeAnOffer(myMaxUtility, presentThreshold);
            }
            else{
                double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
                if (utilityOfOpponent >= presentThreshold)
                {
                    return new Accept(this.getPartyId(), lastReceivedOffer);
                }
                else{
                    return MakeAnOffer(myMaxUtility, presentThreshold);
                }

            }

            }

        else if (time >= 0.1 && time < 0.3)
        {
            double presentThreshold = 0.9-Math.pow(time,2);
            double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
            if (lastReceivedOffer != null && utilityOfOpponent >= presentThreshold)
            {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else
                {
                    OpponentBidList.add(lastReceivedOffer);
                    for (int i=1; i<=OpponentBidList.size(); i++)
                    {

                        if (this.utilitySpace.getUtility(OpponentBidList.get(i-1))>=presentThreshold)
                        {
                            OpponentOffer = OpponentBidList.get(i-1);
                            myLastOffer = OpponentOffer;
                            return new Offer(this.getPartyId(), myLastOffer);
                        }

                    }

                 if ((time >= 0.15 && time<0.2) ||(time>=0.25 && time<0.3))
                    {
                        double utilityOfMyOffer = myMaxUtility * presentThreshold+0.09;
                        Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                        myLastOffer = bidOfMyOffer;
                        return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                    }
                else
                    {
                        double utilityOfMyOffer = myMaxUtility * presentThreshold;
                        Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                        myLastOffer = bidOfMyOffer;
                        return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                    }
                }
        }



        else if (time >= 0.3 && time < 0.5)
        {
            double presentThreshold = 0.9-3/4*Math.pow(time,2);
            double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
            if (lastReceivedOffer != null && utilityOfOpponent >= presentThreshold) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else
            {
                OpponentBidList.add(lastReceivedOffer);

                for (int i=1; i<=OpponentBidList.size(); i++)
                {
                    if (this.utilitySpace.getUtility(OpponentBidList.get(i-1))>=presentThreshold)
                    {
                        OpponentOffer = OpponentBidList.get(i-1);
                        myLastOffer = OpponentOffer;
                        return new Offer(this.getPartyId(), myLastOffer);
                    }

                }


                if ((time >= 0.35 && time<0.4) ||(time>=0.45 && time<0.5))
                {
                    double utilityOfMyOffer = myMaxUtility * presentThreshold+0.08;
                    Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                    myLastOffer = bidOfMyOffer;
                    return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                }
                else
                {
                    double utilityOfMyOffer = myMaxUtility * presentThreshold;
                    Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                    myLastOffer = bidOfMyOffer;
                    return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                }
            }
        }


        else if (time >= 0.5 && time < 0.7)
        {
            double presentThreshold = 0.9-7/8*Math.pow(time,4);
            double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
            if (lastReceivedOffer != null && utilityOfOpponent >= presentThreshold) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else
            {
                OpponentBidList.add(lastReceivedOffer);

                for (int i=1; i<=OpponentBidList.size(); i++)
                {
                    if (this.utilitySpace.getUtility(OpponentBidList.get(i-1))>=presentThreshold)
                    {
                        OpponentOffer = OpponentBidList.get(i-1);
                        myLastOffer = OpponentOffer;
                        return new Offer(this.getPartyId(), myLastOffer);
                    }

                }


                if ((time >= 0.55 && time<0.6) ||(time>=0.65 && time<0.7))
                {
                    double utilityOfMyOffer = myMaxUtility * presentThreshold+0.07;
                    Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                    myLastOffer = bidOfMyOffer;
                    return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                }
                else
                {
                    double utilityOfMyOffer = myMaxUtility * presentThreshold;
                    Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                    myLastOffer = bidOfMyOffer;
                    return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                }
            }
        }


        else if (time >= 0.7 && time < 0.9)
        {
            double presentThreshold = 0.75;
            double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
            if (lastReceivedOffer != null && utilityOfOpponent >= presentThreshold) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else
            {
                OpponentBidList.add(lastReceivedOffer);
                System.out.println(OpponentBidList.size());
                for (int i=1; i<=OpponentBidList.size(); i++)
                {
                    if (this.utilitySpace.getUtility(OpponentBidList.get(i-1))>=presentThreshold)
                    {
                        OpponentOffer = OpponentBidList.get(i-1);
                        myLastOffer = OpponentOffer;
                        return new Offer(this.getPartyId(), myLastOffer);
                    }

                }


                if ((time >= 0.75 && time<0.8) ||(time>=0.85 && time<0.9))
                {
                    double utilityOfMyOffer = myMaxUtility * presentThreshold+0.06;
                    Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                    myLastOffer = bidOfMyOffer;
                    return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                }
                else
                {
                    double utilityOfMyOffer = myMaxUtility * presentThreshold;
                    Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                    myLastOffer = bidOfMyOffer;
                    return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                }
            }
        }


        else if (time >= 0.9 && time < 0.95)
        {
            double presentThreshold = 0.72;
            double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
            if (lastReceivedOffer != null && utilityOfOpponent >= presentThreshold) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else
                {
                    OpponentBidList.add(lastReceivedOffer);

                    for (int i=1; i<=OpponentBidList.size(); i++)
                    {
                        if (this.utilitySpace.getUtility(OpponentBidList.get(i-1))>=presentThreshold)
                        {
                            OpponentOffer = OpponentBidList.get(i-1);
                            myLastOffer = OpponentOffer;
                            return new Offer(this.getPartyId(), myLastOffer);
                        }

                    }


                    double utilityOfMyOffer = myMaxUtility * presentThreshold;
                    Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                    myLastOffer = bidOfMyOffer;
                    return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
                }

        }


        else if (time >= 0.95 && time < 0.99)
        {
            double presentThreshold = 0.70;
            double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
            if (lastReceivedOffer != null && utilityOfOpponent >= presentThreshold) {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else

            {
                OpponentBidList.add(lastReceivedOffer);

                for (int i=1; i<=OpponentBidList.size(); i++)
                {
                    if (this.utilitySpace.getUtility(OpponentBidList.get(i-1))>=presentThreshold)
                    {
                        OpponentOffer = OpponentBidList.get(i-1);
                        myLastOffer = OpponentOffer;
                        return new Offer(this.getPartyId(), myLastOffer);
                    }

                }


                double utilityOfMyOffer = myMaxUtility * presentThreshold;
                Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                myLastOffer = bidOfMyOffer;
                return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
            }

        }

        else
        {
            double presentThreshold = 0.5;
            double utilityOfOpponent = this.utilitySpace.getUtility(lastReceivedOffer);
            if (lastReceivedOffer != null && utilityOfOpponent >= presentThreshold)
            {
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
            else
            {
                double utilityOfMyOffer = myMaxUtility * presentThreshold;
                Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
                myLastOffer = bidOfMyOffer;
                return MakeAnOffer(myMaxUtility, utilityOfMyOffer);
            }

        }






    }


    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) { // sender is making an offer
            Offer offer = (Offer) act;

            // storing last received offer
            lastReceivedOffer = offer.getBid();
        }

    }


    @Override
    public String getDescription() {
        return description;
    }


    @Override
    public AbstractUtilitySpace estimateUtilitySpace() {
        Domain domain = getDomain();
        AdditiveUtilitySpaceFactory factory = new AdditiveUtilitySpaceFactory(domain);

        BidRanking bidRanking = userModel.getBidRanking();

        factory.estimateUsingBidRanks(bidRanking);

        return factory.getUtilitySpace();

    }


    //@Override
    private Bid getMaxUtilityBid(){
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private Offer MakeAnOffer(double utility, double threshold) {
        double utilityOfMyOffer = utility * threshold;
        Bid bidOfMyOffer = generateMyBidWithUtility(utilityOfMyOffer);
        return new Offer(this.getPartyId(), bidOfMyOffer);
    }


    private Bid generateMyBidWithUtility(double utilityThreshold) {
        Bid randomBid;
        double utility;
        do {
            randomBid = generateRandomBid();
            try {
                utility = utilitySpace.getUtility(randomBid);
            } catch (Exception e) {
                utility = 0.0;
            }
        }
        while (utility < utilityThreshold);
        return randomBid;
    }


}