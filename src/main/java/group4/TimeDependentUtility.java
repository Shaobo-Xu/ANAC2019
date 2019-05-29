package group4;

public class TimeDependentUtility {	

	private static double calcUtility(double currentTime, double e, double Pmin, double Pmax) {
		double f = Math.pow(currentTime, 1.0 / e);
		double u = Pmin + (Pmax - Pmin) * (1.0 - f);
	    return u;
	}

	static double transformTime(double currentTime, double phaseStartTime, double phaseEndTime) {
		return (currentTime - phaseStartTime) / (phaseEndTime - phaseStartTime);
	}


	// we called it LD on the board
	private static double calculateDomainSpecificBound(long possibleBids) {
		// TODO: get a formula for this
	    //return possibleBids < 5000 ? .8 : .9;
		
		return .9;
	}


	static double calcUtilityPhase1(double currentTime, long possibleBids) {
		double Pmax = 1;
	    double Pmin = calculateDomainSpecificBound(possibleBids);

	    return calcUtility(currentTime, .5, Pmin, Pmax);
	}

	static double calcAcceptableUtilityPhase2(double currentTime, long possibleBids, double Pmin) {
		double Pmax = calculateDomainSpecificBound(possibleBids);

	    return calcUtility(currentTime, .5, Pmin, Pmax);
	}

	static double calcUtilityPhase3(double currentTime, double Pmax) {
		double Pmin = .5;
	    return calcUtility(currentTime, .5, Pmin, Pmax);
	}
}