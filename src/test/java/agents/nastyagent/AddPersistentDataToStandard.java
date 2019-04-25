package agents.nastyagent;

import java.util.List;

import genius.core.actions.Action;
import genius.core.persistent.PersistentDataType;
import genius.core.persistent.StandardInfoList;

/**
 * Tries to modify immutable persistent data
 *
 */
public class AddPersistentDataToStandard extends NastyAgent {
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		if (data.getPersistentDataType() == PersistentDataType.STANDARD) {
			StandardInfoList list = (StandardInfoList) data.get();
			list.add(null);
		}
		return super.chooseAction(possibleActions);
	}

}
