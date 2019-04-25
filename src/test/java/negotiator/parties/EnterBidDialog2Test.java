package negotiator.parties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.Domain;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.EndNegotiation;
import genius.core.actions.Offer;
import genius.core.parties.NegotiationParty;
import genius.core.utility.AdditiveUtilitySpace;

public class EnterBidDialog2Test {

	private static final AgentID AGENTID = new AgentID("test");
	private NegotiationParty party;
	private JFrame frame;
	private AdditiveUtilitySpace utilspace;
	private Bid lastOppBid;

	@Before
	public void before() {
		party = mock(NegotiationParty.class);
		frame = new JFrame();// mock(Frame.class);
		utilspace = mock(AdditiveUtilitySpace.class);
		Domain domain = mock(Domain.class);
		when(utilspace.getDomain()).thenReturn(domain);
		lastOppBid = mock(Bid.class);

	}

	@Test
	public void testInit() throws Exception {
		// smoke test.
		new EnterBidDialog2(party, AGENTID, frame, true, utilspace, lastOppBid);
	}

	@Test
	public void testEndNego() throws Exception {

		// use non-modal dialog so that we can interact here with it
		EnterBidDialog2 dialog = new EnterBidDialog2(party, AGENTID, frame, true, utilspace, lastOppBid);
		final ControllableComponent control = new ControllableComponent(dialog);

		Action opponentAction = new Offer(AGENTID, new Bid(mock(Domain.class)));
		Bid myPreviousBid = new Bid(mock(Domain.class));

		control.delayedClickOnButton(EnterBidDialog2.END_NEGOTIATION);
		Action action = dialog.askUserForAction(opponentAction, myPreviousBid);
		assertTrue("end nego click did not result in EndNegotiation", action instanceof EndNegotiation);
	}

	@Test
	public void testEndNegoNullOpponentAction() throws Exception {

		// use non-modal dialog so that we can interact here with it
		EnterBidDialog2 dialog = new EnterBidDialog2(party, AGENTID, frame, true, utilspace, lastOppBid);
		final ControllableComponent control = new ControllableComponent(dialog);

		Bid myPreviousBid = new Bid(mock(Domain.class));

		control.delayedClickOnButton(EnterBidDialog2.END_NEGOTIATION);
		Action action = dialog.askUserForAction(null, myPreviousBid);
		assertTrue("end nego click did not result in EndNegotiation", action instanceof EndNegotiation);
	}

	@Test
	public void testEndNegoWithEndNegoOpponentAction() throws Exception {

		// use non-modal dialog so that we can interact here with it
		EnterBidDialog2 dialog = new EnterBidDialog2(party, AGENTID, frame, true, utilspace, lastOppBid);
		final ControllableComponent control = new ControllableComponent(dialog);

		Action opponentAction = new EndNegotiation(AGENTID);
		Bid myPreviousBid = new Bid(mock(Domain.class));

		control.delayedClickOnButton(EnterBidDialog2.END_NEGOTIATION);
		Action action = dialog.askUserForAction(opponentAction, myPreviousBid);
		assertTrue("end nego click did not result in EndNegotiation", action instanceof EndNegotiation);
	}

	@Test
	public void testEndNegoWithAcceptOpponentAction() throws Exception {

		// use non-modal dialog so that we can interact here with it
		EnterBidDialog2 dialog = new EnterBidDialog2(party, AGENTID, frame, true, utilspace, lastOppBid);
		final ControllableComponent control = new ControllableComponent(dialog);

		Action opponentAction = new Accept(AGENTID, lastOppBid);
		Bid myPreviousBid = new Bid(mock(Domain.class));

		control.delayedClickOnButton(EnterBidDialog2.END_NEGOTIATION);
		Action action = dialog.askUserForAction(opponentAction, myPreviousBid);
		assertTrue("end nego click did not result in EndNegotiation", action instanceof EndNegotiation);
	}

	@Test
	public void testAccept() throws Exception {

		// use non-modal dialog so that we can interact here with it
		EnterBidDialog2 dialog = new EnterBidDialog2(party, AGENTID, frame, true, utilspace, lastOppBid);
		final ControllableComponent control = new ControllableComponent(dialog);

		Action opponentAction = mock(Action.class);
		Bid myPreviousBid = new Bid(mock(Domain.class));

		control.delayedClickOnButton(EnterBidDialog2.ACCEPT);
		Action action = dialog.askUserForAction(opponentAction, myPreviousBid);
		assertTrue("end nego click did not result in accept", action instanceof Accept);
	}

	@Test
	public void testDoBid() throws Exception {

		// use non-modal dialog so that we can interact here with it
		EnterBidDialog2 dialog = new EnterBidDialog2(party, AGENTID, frame, true, utilspace, lastOppBid);
		final ControllableComponent control = new ControllableComponent(dialog);

		Action opponentAction = mock(Action.class);
		Bid myPreviousBid = new Bid(mock(Domain.class));

		control.delayedClickOnButton(EnterBidDialog2.DO_BID);
		Action action = dialog.askUserForAction(opponentAction, myPreviousBid);
		assertTrue("end nego click did not result in offer", action instanceof Offer);
		assertEquals("the placed bid is not equal to the previous bid", myPreviousBid, ((Offer) action).getBid());
	}
}
