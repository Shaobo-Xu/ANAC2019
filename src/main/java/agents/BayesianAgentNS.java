package agents;

import agents.bayesianopponentmodel.BayesianOpponentModelScalable;
import genius.core.utility.AdditiveUtilitySpace;

public class BayesianAgentNS extends BayesianAgent {

	@Override
	public String getName() {
		return "Bayesian Scalable";
	}

	@Override
	protected void prepareOpponentModel() {
		fOpponentModel = new BayesianOpponentModelScalable(
				(AdditiveUtilitySpace) utilitySpace);
	}

}
