package agents.nastyagent;

import java.util.List;

import genius.core.actions.Action;

/**
 * returns a null action
 * 
 * @author W.Pasman
 *
 */
public class NullActionInChoose extends NastyAgent {

	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		return null;
	}
}
