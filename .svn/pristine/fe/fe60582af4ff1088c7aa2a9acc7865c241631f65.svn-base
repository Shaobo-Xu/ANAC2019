package agents.similarity;

import java.util.ArrayList;

import genius.core.Bid;
import genius.core.Domain;
import genius.core.xml.SimpleElement;

public class Similarity {
	private Domain fDomain;
	private double fWeights[];
	private ArrayList<SimilarityFunction> fSimilarityFunctions;

	public Similarity(Domain pDomain) {
		fDomain = pDomain;
		fSimilarityFunctions = new ArrayList<SimilarityFunction>();
	}

	public final double getSimilarity(Bid pMyBid, Bid pOpponentBid) {
		double lSimilarity = 0;
		for (int i = 0; i < fSimilarityFunctions.size(); i++) {
			lSimilarity += fWeights[i]
					* fSimilarityFunctions.get(i).getSimilarityValue(pMyBid,
							pOpponentBid);
		}

		return lSimilarity;
	}

	public void loadFromXML(SimpleElement pRoot) {
		SimpleElement lXMLUtilitySpace = (SimpleElement) (pRoot
				.getChildByTagName("utility_space")[0]);
		SimpleElement lXMLObjective = (SimpleElement) (lXMLUtilitySpace
				.getChildByTagName("objective")[0]);
		Object[] lXMLIssue = lXMLObjective.getChildByTagName("issue");
		fWeights = new double[lXMLIssue.length];
		for (int j = 0; j < lXMLIssue.length; j++) {
			Object[] lXMLSimFn = ((SimpleElement) (lXMLIssue[j]))
					.getChildByTagName("similarity_function");
			if (lXMLSimFn == null || lXMLSimFn.length == 0) {
				continue;
			}
			SimilarityFunction lSimFn = new SimilarityFunction(fDomain);
			// load weights
			fWeights[j] = Double.valueOf(((SimpleElement) (lXMLSimFn[0]))
					.getAttribute("weight"));
			lSimFn.loadFromXML((SimpleElement) (lXMLSimFn[0]), Integer
					.valueOf(((SimpleElement) (lXMLIssue[j]))
							.getAttribute("index")));
			fSimilarityFunctions.add(lSimFn);
		}// for
	}
}
