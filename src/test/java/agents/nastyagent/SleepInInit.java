package agents.nastyagent;

import genius.core.parties.NegotiationInfo;

public class SleepInInit extends NastyAgent {

	@Override
	public void init(NegotiationInfo info) {
		super.init(info);
		try {
			Thread.sleep(2000000);// sleep 2000 seconds.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
