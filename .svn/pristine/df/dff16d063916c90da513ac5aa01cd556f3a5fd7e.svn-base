package agents.rlboa;

import java.util.Objects;

public class LookBackState extends AbstractState implements BinnedRepresentation {

	private int myBin;
	private int oppBin;
	private int prevMyBin;
	private int prevOppBin;
	private int time;
	
	public static LookBackState TERMINAL = new LookBackState(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	public static LookBackState INITIAL = new LookBackState(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

	// TODO: Initialize these values from config file
	private static int initialActionSpace = 10;
	private static int standardActionSpace = 3;
	
	public LookBackState(int myBin, int oppBin, int prevMyBin, int prevOppBin, int time) {
		this.myBin = myBin;
		this.oppBin = oppBin;
		this.prevMyBin = prevMyBin;
		this.prevOppBin = prevOppBin;
		this.time = time;
	}
	
	public int getActionSize() {
		if (this.getMyBin() < 0) {
			return initialActionSpace;
		}
		else {
			return standardActionSpace;
		}
	}

	public int getMyBin() {
		return this.myBin;
	}
	
	public int getOppBin() {
		return this.oppBin;
	}

	public int getPrevMyBin() {
		return this.prevMyBin;
	}
	
	public int getPrevOppBin() {
		return this.prevOppBin;
	}
	
	public int getTime() {
		return this.time;
	}
	
	public boolean isInitialState() {
		return this.equals(LookBackState.INITIAL);
	}
	
	public boolean isTerminalState() {
		return this.equals(LookBackState.TERMINAL);
	}
	
	public int hash() {
		return Objects.hash(this.getMyBin(), this.getOppBin(), this.getPrevMyBin(), this.getPrevOppBin(), this.getTime());
	}
	
	@Override
	public String toString() {
		return String.format("My bin: %d, Opp bin: %d, Prev My Bin: %d, Prev Opp Bin: %d, Time: %d", this.getMyBin(), this.getOppBin(), this.getPrevMyBin(), this.getPrevOppBin(), this.getTime());
	}

}
