package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.List;

import java.util.ArrayList;
import java.util.regex.Pattern;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import genius.AgentsInstaller;
import genius.ProtocolsInstaller;
import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Deadline;
import genius.core.DeadlineType;
import genius.core.actions.ActionWithBid;
import genius.core.events.MultipartyNegoActionEvent;
import genius.core.events.NegotiationEvent;
import genius.core.events.SessionEndedNormallyEvent;
import genius.core.listener.Listener;
import genius.core.parties.NegotiationPartyInternal;
import genius.core.parties.SessionsInfo;
import genius.core.persistent.PersistentDataType;
import genius.core.protocol.MultilateralProtocol;
import genius.core.protocol.StackedAlternatingOffersProtocol;
import genius.core.repository.DomainRepItem;
import genius.core.repository.PartyRepItem;
import genius.core.repository.ProfileRepItem;
import genius.core.repository.Repository;
import genius.core.repository.RepositoryFactory;
import genius.core.session.ExecutorWithTimeout;
import genius.core.session.Session;
import genius.core.session.SessionConfiguration;
import genius.core.session.SessionManager;
import genius.domains.DomainInstaller;
import genius.gui.progress.session.ActionDocumentModel;

public class StackedAlternatingOffersProtocolE2ETest {

	private static final String BID_PATTERN = "Bid [a: .., b: .., c: .., d: .., e: .., f: .., g: .., h: .. ]";
	private static final String OFFER_PATTERN = "( Offer bid:" + BID_PATTERN
			+ " ) ";
	private static final String ACCEPT_PATTERN = "( Accept bid: " + BID_PATTERN
			+ ")";

	enum BidType {
		OFFER, ACCEPT, PLAIN_BID
	}

	private SessionsInfo info;;

	@Before
	public void before() throws IOException {
		ProtocolsInstaller.run();
		DomainInstaller.run();
		AgentsInstaller.run();

		MultilateralProtocol protocol = new StackedAlternatingOffersProtocol();
		info = new SessionsInfo(protocol, PersistentDataType.DISABLED, false);
	}

	@After
	public void after() {
		info.close();
	}

	private BidType getType(String preamble, String text) {
		if (match(preamble + OFFER_PATTERN, text)) {
			return BidType.OFFER;
		}
		if (match(preamble + ACCEPT_PATTERN, text)) {
			return BidType.ACCEPT;
		}
		if (match(preamble + BID_PATTERN, text)) {
			return BidType.PLAIN_BID;
		}
		throw new IllegalArgumentException(
				"text <" + text + "> does not contain offer or accept");

	}

	/**
	 * Asserts that the text matches the pattern
	 * 
	 * @param pattern
	 *            the format for the {@link Pattern}.
	 * @param text
	 *            the text to match
	 */
	private void assertMatch(String pattern, String text) {
		if (!match(pattern, text)) {
			assertEquals(pattern, text); // generates error text 'expected ...
											// but found ...'
		}
	}

	/**
	 * check ift the text matches the pattern
	 * 
	 * @param pattern
	 *            the format for the {@link Pattern}. This patter can contain
	 *            chars like (, [ and ' ' which will be escaped before matching
	 *            is done. '*' and '.' will not be escaped
	 * @param text
	 *            the text to match
	 * @return true iff match
	 */
	private boolean match(String pattern, String text) {
		return Pattern.matches(escape(pattern), text);
	}

	private String escape(String text) {
		text = text.replaceAll(" ", "\\\\s*");
		text = text.replaceAll("\\,", "\\\\,");
		// notice, first arg is regexp for '('
		text = text.replaceAll("\\(", "\\\\(");
		// notice, first arg is regexp for ')'
		text = text.replaceAll("\\)", "\\\\)");
		text = text.replaceAll("\\:", "\\\\:");
		text = text.replaceAll("\\[", "\\\\[");
		text = text.replaceAll("\\]", "\\]");
		return text;
	}

	/**
	 * assert that the text is a bid.
	 * 
	 * @param preamble
	 *            the expected pattern before the bid text.
	 * @param text
	 */
	private void assertBidOrAccept(String preamble, String text) {
		getType(preamble, text); // any type goes.
	}

	private class myListener implements Listener<NegotiationEvent> {

		private List<Bid> offers = new ArrayList<Bid>();
		private List<SessionEndedNormallyEvent> events = new ArrayList<SessionEndedNormallyEvent>();

		public List<SessionEndedNormallyEvent> getEvents() {
			return events;
		}

		@Override
		public void notifyChange(NegotiationEvent e) {
			if (e instanceof MultipartyNegoActionEvent
					&& ((MultipartyNegoActionEvent) e)
							.getAction() instanceof ActionWithBid) {
				offers.add(((ActionWithBid) ((MultipartyNegoActionEvent) e)
						.getAction()).getBid());
			} else if (e instanceof SessionEndedNormallyEvent) {
				events.add((SessionEndedNormallyEvent) e);
			}

		}
	}

	/************************************************************************************************/
	/*
	 * the actual tests are below. We can't check creation of log files, as
	 * these are not created normally.
	 */

	/**
	 * End to end test of multiparty tournament. 3 parties boulware, conceder,
	 * random. Domain8 is used.
	 */
	@Test
	public void runMultiPartyNego1() throws Exception {

		/** set up a multiparty negotiation */
		final String[] partyclasses = {
				"negotiator.parties.BoulwareNegotiationParty",
				"negotiator.parties.ConcederNegotiationParty",
				"negotiator.parties.RandomCounterOfferNegotiationParty" };
		Deadline deadline = new Deadline(60, DeadlineType.ROUND);
		Session session = new Session(deadline, info);
		List<NegotiationPartyInternal> parties = new ArrayList<NegotiationPartyInternal>();

		// bad to have absolute ref. But there's no better function in
		// Repository...
		DomainRepItem domain8 = RepositoryFactory
				.getDomainByName("file:etc/templates/Domain8/Domain8.xml");
		Repository<PartyRepItem> party_rep = RepositoryFactory
				.get_party_repository();

		for (int partynr = 0; partynr < 3; partynr++) {
			ProfileRepItem profileRepItem = domain8.getProfiles().get(partynr);
			PartyRepItem partyRepItem = party_rep
					.getPartyOfClass(partyclasses[partynr]);

			NegotiationPartyInternal negoparty = new NegotiationPartyInternal(
					partyRepItem, profileRepItem, session, info,
					AgentID.generateID(partyRepItem.getUniqueName()));
			parties.add(negoparty);
		}
		// maybe we can craete the parties directly, using this?
		// parties.add(new BoulwareNegotiationParty(utilitySpace, deadlines,
		// timeline, randomSeed));

		SessionManager manager = new SessionManager(
				mock(SessionConfiguration.class), parties, session,
				new ExecutorWithTimeout(
						1000 * deadline.getTimeOrDefaultTimeout()));

		myListener listener = new myListener();
		manager.addListener(listener);
		ActionDocumentModel document = new ActionDocumentModel();
		manager.addListener(document);

		manager.runAndWait();

		/*********** and finally check the outcome **************/
		SessionEndedNormallyEvent lastEvent = listener.getEvents()
				.get(listener.getEvents().size() - 1);
		assertNotNull(lastEvent.getAgreement());

		String[] logs = document
				.getText(0, document.getEndPosition().getOffset()).split("\\n");

		// check the logs file. It should be of this form
		// Starting negotiation session.
		// SOME NUMBER OF THIS {
		// Round N
		// Turn 1: Boulware#.* offerOrAccept
		// Turn 2: Conceder#.* offerOrAccept
		// Turn 3: Random#.* offerOrAccept
		// }
		// Round .*
		// Turn 1: .* (Accept)
		// Turn 2: .* (Accept)
		// Found an agreement: Bid[a: .., b: .., c: .., d: .., e: .., f: .., g:
		// .., h: .., ]
		// Finished negotiation session in .*s
		// SUMMARY TEXT LINE

//		for (String log : logs)
//			System.out.println(log);	
		
		int line = 0;

		assertMatch("Starting .* session.*", logs[line++]);
		assertMatch("", logs[line++]);

		int rounds = (logs.length - 18) / 4;
		
		for (int n = 0; n < rounds; n++) {
//			System.out.println("check round " + (n + 1));
			assertMatch("Round " + (n + 1), logs[line++]);
			assertBidOrAccept(" Turn 1:Boulware\\S* ", logs[line++]);
			assertBidOrAccept(" Turn 2:Conceder\\S* ", logs[line++]);
			assertBidOrAccept(" Turn 3:Random\\S* ", logs[line++]);
		}
		// then the accept round
		assertMatch("Round " + (rounds + 1), logs[line++]);
		assertEquals(BidType.ACCEPT, getType(" Turn 1: \\S* ", logs[line++]));
		assertEquals(BidType.ACCEPT, getType(" Turn 2: \\S* ", logs[line++]));
		// then "found an agreement"
		assertEquals(BidType.PLAIN_BID,
				getType(" Found an agreement: ", logs[line++]));
		assertMatch(" Finished negotiation session in \\S* s.", logs[line++]);
		// list line is the wrap-up. Not checked yet, pretty complex.
	}

}
