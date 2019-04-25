package agents.similarity;

import java.util.HashMap;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.issue.IssueReal;
import genius.core.issue.ValueReal;
import genius.core.utility.EVALFUNCTYPE;
import genius.core.utility.EVALUATORTYPE;
import genius.core.xml.SimpleElement;

public class CriteriaReal implements Criteria {
	// Class fields
	// double lowerBound;
	// double upperBound;
	EVALFUNCTYPE type;
	private int fIssueIndex;
	private Domain fDomain;
	HashMap<Integer, Double> fParam;

	public CriteriaReal(Domain pDomain, int pIssueIndex) {
		fIssueIndex = pIssueIndex;
		fDomain = pDomain;
		fParam = new HashMap<Integer, Double>();
	}

	public double getValue(Bid pBid) {
		double utility = 0;
		try {
			double value = ((ValueReal) pBid.getValue(fIssueIndex)).getValue();
			switch (this.type) {
			case FARATIN:
				IssueReal lIssue = (IssueReal) (fDomain.getObjectivesRoot()
						.getObjective(fIssueIndex));
				utility = EVALFUNCTYPE.evalFaratin(value,
						lIssue.getUpperBound(), lIssue.getLowerBound(),
						this.fParam.get(0), this.fParam.get(1));
				/*
				 * if (utility<0) utility = 0; else if (utility > 1) utility =
				 * 1;
				 */
				return utility;

			case LINEAR:
				utility = EVALFUNCTYPE.evalLinear(value, this.fParam.get(1),
						this.fParam.get(0));
				if (utility < 0)
					utility = 0;
				else if (utility > 1)
					utility = 1;
				return utility;
			case CONSTANT:
				return this.fParam.get(0);
			default:
				return -1.0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return utility;
	}

	public EVALUATORTYPE getType() {
		return EVALUATORTYPE.REAL;
	}

	/*
	 * public double getLowerBound() { return lowerBound; }
	 * 
	 * public double getUpperBound() { return lowerBound; }
	 */
	public void loadFromXML(SimpleElement pRoot) {
		// Object[] xml_item =
		// ((SimpleElement)pRoot).getChildByTagName("range");
		// this.lowerBound =
		// Double.valueOf(((SimpleElement)xml_item[0]).getAttribute("lowerbound"));
		// this.upperBound =
		// Double.valueOf(((SimpleElement)xml_item[0]).getAttribute("upperbound"));
		String ftype = pRoot.getAttribute("type");
		if (ftype != null)
			this.type = EVALFUNCTYPE.convertToType(ftype);
		// TODO: define exception.
		// TODO: DT: redefine this swith in more generic way
		switch (this.type) {
		case FARATIN:
			this.fParam
					.put(1, Double.valueOf(pRoot.getAttribute("parameter1")));
			this.fParam
					.put(0, Double.valueOf(pRoot.getAttribute("parameter0")));
			break;
		case LINEAR:
			this.fParam
					.put(1, Double.valueOf(pRoot.getAttribute("parameter1")));
		case CONSTANT:
			this.fParam
					.put(0, Double.valueOf(pRoot.getAttribute("parameter0")));
		}
	}
}
