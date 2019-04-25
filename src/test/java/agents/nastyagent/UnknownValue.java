package agents.nastyagent;

import java.util.List;

import genius.core.Bid;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.issue.ValueDiscrete;

/**
 * returns a deliberately miscrafted bid that contains an slightly altered value
 * that is not in the domain description. It only works if it finds an
 * issueDiscrete.
 * 
 * @author W.Pasman 2nov15
 *
 */
public class UnknownValue extends NastyAgent {
	@Override
	public Action chooseAction(List<Class<? extends Action>> possibleActions) {
		Bid bid = bids.get(0);

		int issuenr = -1;
		for (Issue issue : bid.getIssues()) {
			if (issue instanceof IssueDiscrete) {
				issuenr = issue.getNumber();
				break;
			}
		}
		if (issuenr >= 0) {
			// found an issue Discrete, modify it.
			Value value;
			try {
				value = bid.getValue(issuenr);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}

			bid = bid.putValue(issuenr, new ValueDiscrete("new" + ((ValueDiscrete) value).getValue()));
		} else {
			throw new IllegalArgumentException("UnknownValue agent needs an IssueDiscrete");
		}

		return new Offer(id, bid);
	}
}
