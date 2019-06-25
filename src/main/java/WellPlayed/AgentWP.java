package WellPlayed;

import GoodGame.AgentGG;
import GoodGame.ImpMap;

import GoodGame.impUnit;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Deadline;
import genius.core.DeadlineType;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.misc.Pair;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.parties.NegotiationPartyInternal;
import genius.core.parties.SessionsInfo;
import genius.core.persistent.PersistentDataType;
import genius.core.protocol.MultilateralProtocol;
import genius.core.protocol.StackedAlternatingOffersProtocol;
import genius.core.repository.*;
import genius.core.session.*;
import genius.core.uncertainty.ExperimentalUserModel;
import genius.core.uncertainty.UserModel;
import genius.core.utility.UncertainAdditiveUtilitySpace;
import genius.gui.progress.session.ActionDocumentModel;
import genius.gui.session.SessionPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class AgentWP extends AgentGG {
    private final boolean DEBUG_MODE = true;
    private ExperimentalUserModel e;
    private UncertainAdditiveUtilitySpace realSpace;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        if (DEBUG_MODE) {
            e = (ExperimentalUserModel) userModel;
            realSpace = e.getRealUtilitySpace();
            bidUtilityDistance();
        }
        System.out.println("wp initialized");


    }

    private void getMedianBid() {
        int median = (this.userModel.getBidRanking().getSize() - 1) / 2;
        int median2 = -1;
        if (this.userModel.getBidRanking().getSize() % 2 == 0) {
            median2 = median + 1;
        }
        int current = 0;

    }

    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        return new EndNegotiation(getPartyId());
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

//        double deviation = distance/((double) bids.size());
        double deviation = distance;

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

    @Override
    public String getDescription() {
        return "Well played";
    }

    public static MultilateralSessionConfiguration getConfiguration() throws Exception {
        MultilateralProtocol protocol = new StackedAlternatingOffersProtocol();
        SessionsInfo info = new SessionsInfo(protocol, PersistentDataType.DISABLED, false);
        MultiPartyProtocolRepItem protocolRepItem = new MultiPartyProtocolRepItem(
                "Stacked Alternating Offers Protocol",
                "genius.core.protocol.StackedAlternatingOffersProtocol",
                "Each agents makes offer, counter-offer, or accepts",
                false, false);

        Deadline deadline = new Deadline(180, DeadlineType.ROUND);
        Session session = new Session(deadline, info);

        List<NegotiationPartyInternal> parties = new ArrayList<NegotiationPartyInternal>();

        DomainRepItem domain8 = RepositoryFactory
                .getDomainByName("file:etc/templates/Domain8/Domain8.xml");
        Repository<PartyRepItem> party_rep = RepositoryFactory
                .get_party_repository();

        final String[] partyclasses = {
                "test.test",
                "WellPlayed.AgentWp"};

        List<Participant> participants = new ArrayList<>();

        for (int partynr = 0; partynr < 2; partynr++) {
            ProfileRepItem profileRepItem = domain8.getProfiles().get(partynr);
            PartyRepItem partyRepItem = party_rep
                    .getPartyOfClass(partyclasses[partynr]);
            Participant prt = new Participant(new AgentID("Party " + (partynr + 1)), partyRepItem,profileRepItem);
            participants.add(prt);

            NegotiationPartyInternal negoparty = new NegotiationPartyInternal(
                    partyRepItem, profileRepItem, session, info,
                    AgentID.generateID(partyRepItem.getUniqueName()));
            parties.add(negoparty);
        }

        SessionConfiguration config = new SessionConfiguration(protocolRepItem, null, participants, deadline,
                PersistentDataType.DISABLED);

        SessionManager manager = new SessionManager(
                config, parties, session,
                new ExecutorWithTimeout(
                        1000 * deadline.getTimeOrDefaultTimeout()));
        ActionDocumentModel document = new ActionDocumentModel();
        manager.addListener(document);

        manager.runAndWait();

//        Participant mediator = null;
//        for (int n = 0; n < participantsModel.size(); n++) {
//            participants.add(participantsModel.getElementAt(n));
//        }
//
        return null;
    }

    public static void main(String[] args) {
//        try {
//            getConfiguration();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        final JFrame gui = new JFrame();
        gui.setLayout(new BorderLayout());
        gui.getContentPane().add(new SessionPanel(), BorderLayout.CENTER);
        gui.pack();
        gui.setVisible(true);
    }
}
