package agents;

import genius.core.SupportedNegotiationSetting;

public class TimeDependentAgentHardliner extends TimeDependentAgent {
	@Override
	public double getE() {
		return 0;
	}

	@Override
	public String getName() {
		return "Hardliner";
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "does not concede";
	}
}
