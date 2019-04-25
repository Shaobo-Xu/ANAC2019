package agents.similarity;

import java.util.HashMap;

import genius.core.Bid;
import genius.core.issue.ValueDiscrete;
import genius.core.xml.SimpleElement;



public class CriteriaDiscrete implements Criteria {
	private int fIssueIndex;
	private HashMap<ValueDiscrete, Double> fCriteriaValues;

	public CriteriaDiscrete(int pIssueIndex) {
		fIssueIndex = pIssueIndex;
		fCriteriaValues = new HashMap<ValueDiscrete, Double> ();
	}
	public double getValue(Bid pBid) {
		ValueDiscrete lValue = null;
		try {
			lValue = (ValueDiscrete)(pBid.getValue(fIssueIndex));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Double lTmp = fCriteriaValues.get(lValue);
		if (lTmp!=null) {
			return fCriteriaValues.get(lValue);
		} else {
			System.out.println("Can't find criteria value for " +lValue.toString() + "(issue index = " +String.valueOf(fIssueIndex)+")");
			return -1;
		}
			
	}

	public void loadFromXML(SimpleElement pRoot) {

		Object[] xml_items = (pRoot).getChildByTagName("item");
		int nrOfValues = xml_items.length;
		ValueDiscrete value;
		for(int j=0;j<nrOfValues;j++) {
            value = new ValueDiscrete(((SimpleElement)xml_items[j]).getAttribute("value"));
            this.fCriteriaValues.put(value, Double.valueOf(((SimpleElement)xml_items[j]).getAttribute("evaluation")));
        }
	}
	

}
