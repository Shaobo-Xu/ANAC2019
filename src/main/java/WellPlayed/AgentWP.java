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
import genius.core.uncertainty.UserModel;
import genius.gui.progress.session.ActionDocumentModel;
import genius.gui.session.SessionPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;


public class AgentWP extends AgentGG {


    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

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
        try {
            getConfiguration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final JFrame gui = new JFrame();
        gui.setLayout(new BorderLayout());
        gui.getContentPane().add(new SessionPanel(), BorderLayout.CENTER);
        gui.pack();
        gui.setVisible(true);
    }
}
