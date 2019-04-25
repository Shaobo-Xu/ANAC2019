package agents.rlboa;
import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.boaframework.BOAagentBilateral;
import genius.core.boaframework.OutcomeSpace;
import genius.core.boaframework.SortedOutcomeSpace;
import genius.core.protocol.BilateralAtomicNegotiationSession;
import negotiator.boaframework.acceptanceconditions.other.AC_Next;
import negotiator.boaframework.offeringstrategy.other.TimeDependent_Offering;
import negotiator.boaframework.omstrategy.BestBid;
import negotiator.boaframework.opponentmodel.AgentXFrequencyModel;
import negotiator.boaframework.opponentmodel.PerfectModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OppositionCalculator extends BOAagentBilateral {
    @Override
    public void agentSetup() {


        HashMap<String, Double> params = new HashMap<String, Double>();

        // TODO: implement perfect opponent model
        opponentModel = new AgentXFrequencyModel();
        opponentModel.init(this.negotiationSession, params);

        offeringStrategy = new TimeDependent_Offering();

        // Accept if the incoming offer is higher than what you would offer yourself
        acceptConditions = new AC_Next(negotiationSession, offeringStrategy, 1, 0);

        // Opponent model strategy always selects best bid it has available
        omStrategy = new BestBid();
        omStrategy.init(negotiationSession, opponentModel, params);
        setDecoupledComponents(acceptConditions, offeringStrategy, opponentModel, omStrategy);

        OutcomeSpace outcomeSpace = new OutcomeSpace(negotiationSession.getUtilitySpace());
        this.negotiationSession.setOutcomeSpace(outcomeSpace);

        System.out.print("Opposition:");
        System.out.println(this.calculateOpposition());
    }

    @Override
    public String getName() {
        return "Opposition Calculator";
    }

    private double calculateOpposition() {
        List<Bid> allOutcomes = this.negotiationSession.getOutcomeSpace().getAllBidsWithoutUtilities();
        double shortestDistanceToPerfect = Math.hypot(1, 1);

        for (Bid bid : allOutcomes) {
            double x_util = this.getUtility(bid);
            double y_util = this.opponentModel.getBidEvaluation(bid);
            double distance = Math.hypot(1 - x_util, 1 - y_util);

//            System.out.println(String.format("%s -- OppModel: %s", bid, y_util));
            if (distance < shortestDistanceToPerfect) {
                shortestDistanceToPerfect = distance;
            }
        }

        return shortestDistanceToPerfect;
    }
}
