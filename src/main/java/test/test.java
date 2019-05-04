package test;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.utility.AbstractUtilitySpace;
import genius.core.utility.UtilitySpace;
import org.junit.Test;

import java.util.*;


public class test extends AbstractNegotiationParty {
    //初始化Agent
    @Override
    public void init(NegotiationInfo info) {

    }

    //报价 or 接受 策略
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        double aaa = utilitySpace.getReservationValue();

        System.out.println(aaa);


        Bid mb = generateRandomBid();
        return new Offer(this.getPartyId(), mb);
    }

    @Override
    public void receiveMessage(AgentID sender, Action act) {
    }

    @Override
    public String getDescription() {
        return "Well Played";
    }
}