package agents.rlboa;

import genius.core.Bid;
import genius.core.events.MultipartyNegoActionEvent;
import genius.core.events.NegotiationEvent;
import genius.core.listener.Listener;

public interface RLBOA extends Listener<NegotiationEvent> {

	/**
	 * This method should pass the reward and newState trough to its component
	 * that uses an RL-strategy
	 *
	 * @param reward
	 * @param newState
	 */
	public void observeEnvironment(double reward, AbstractState newState);

	/**
	 * This method should instantiate a new AbstractState object that represents
	 * a distinct state in the environment.
	 * @return
	 */
	public AbstractState getStateRepresentation(MultipartyNegoActionEvent negoEvent);

	/**
	 * This method shoud implement the reward function of the Agent, based
	 * on an incomming bid.
	 *
	 * @param agreement
	 * @return
	 */
	public double getReward(Bid agreement);
}
