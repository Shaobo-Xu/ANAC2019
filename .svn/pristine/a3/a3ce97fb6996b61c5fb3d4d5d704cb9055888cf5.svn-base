package agents.similarity;

import java.util.ArrayList;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.xml.SimpleElement;

public class SimilarityFunction {

	private double fWeights[];
	private ArrayList<Criteria> fCriteria;
	private Domain fDomain;
	private SIMILARITYTYPE fType;
	private int fIssueIndex;

	public SimilarityFunction(Domain pDomain) {
		fDomain = pDomain;
		fCriteria = new ArrayList<Criteria>();
	}

	public double getSimilarityValue(Bid pMyBid, Bid pOpponentBid) {
		double lResult = 0;
		switch (fType) {
		case BINARY:
			try {
				if (pMyBid.getValue(fIssueIndex).equals(
						pOpponentBid.getValue(fIssueIndex)))
					lResult = 1;
				else
					lResult = 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case CUSTOM_DEFINED:
			for (int i = 0; i < fCriteria.size(); i++) {
				Criteria lCriteria = fCriteria.get(i);
				lResult += fWeights[i]
						* (1 - Math.abs(lCriteria.getValue(pMyBid)
								- lCriteria.getValue(pOpponentBid)));
			}
			break;
		} // switch
		return lResult;
	}

	public void loadFromXML(SimpleElement pRoot, int pIssueIndex) {
		fIssueIndex = pIssueIndex;
		switch (SIMILARITYTYPE.convertToType(pRoot.getAttribute("type"))) {
		case BINARY:
			fType = SIMILARITYTYPE.BINARY;
			break;
		case CUSTOM_DEFINED:
			fType = SIMILARITYTYPE.CUSTOM_DEFINED;
			Object[] lXMLCriteriaFn = pRoot
					.getChildByTagName("criteria_function");
			fWeights = new double[lXMLCriteriaFn.length];
			// read similarity functions
			for (int i = 0; i < lXMLCriteriaFn.length; i++) {
				// TODO: DT: finish loading from XML for CriteriaDiscrete
				// load weights
				fWeights[i] = Double
						.valueOf(((SimpleElement) (lXMLCriteriaFn[i]))
								.getAttribute("weight"));
				Criteria lCriteria = null;
				switch (fDomain.getObjectivesRoot().getObjective(pIssueIndex)
						.getType()) {
				case REAL:
					lCriteria = new CriteriaReal(fDomain, pIssueIndex);
					lCriteria.loadFromXML((SimpleElement) (lXMLCriteriaFn[i]));
					fCriteria.add(lCriteria);
					break;
				case DISCRETE:
					lCriteria = new CriteriaDiscrete(pIssueIndex);
					lCriteria.loadFromXML((SimpleElement) (lXMLCriteriaFn[i]));
					fCriteria.add(lCriteria);
					break;
				}// switch
			}// for
			break;
		}// switch
	}

}
