package agents;

import java.util.HashMap;
import java.util.Random;

import genius.core.Agent;
import genius.core.Bid;
import genius.core.SupportedNegotiationSetting;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Value;
import genius.core.issue.ValueReal;

public class FuzzyAgent extends Agent {

	private static int deadline = 100;
	private static int deadlineB = 100;
	private static int deadlineS = 100;
	private static double[][] BuyOffer;
	private static double[][] SellOffer;
	private static int tacticB;
	private static int tacticS;

	// private static int tacticBStretch,tacticSStretch;
	private static double K = 0.1;
	private static double MaxB = 40;
	private static double MinB = 10;
	// private static double MinB2=10;
	private static double MaxS = 40;
	// private static double MaxS2=40;
	private static double MinS = 10;

	private static boolean deal = false;
	private static double Utl = 0;
	private static double BBuy, BSell;
	// parameters for stretch 260307
	private static double BBuyStretch, BSellStretch;
	private static int LBuy, LSell;
	// private static int LBuyStretch,LSellStretch;
	private static int iteration = 1;
	// private static double constant=0.05;
	private static int MBuy, MSell;
	// private static int MBuyStretch,MSellStretch;
	// private static boolean dealB=false;
	// private static boolean dealS=false;
	private static double AcceptedValue = 0;
	// for counting rounds in resource dependat
	private static int itercount = 0;
	// The stretch constant
	private static double Scons, Bcons;
	private static boolean CBuy = false;
	private static boolean CSell = false;
	// private static double[][] Distance;
	// private static double DST;
	private static double[] dist;
	private static double percentageB1, percentageB2, percentageS1,
			percentageS2;
	// A threshhold parameter which determines when we should and should not
	// accept or propose crisp 230407
	private static double threshholdB, threshholdS;

	private static final int BUYER = 1;
	private static final int SELLER = 2;
	private int fPlayingFor;
	private int fRound;
	private Bid lastReceivedOffer;

	@Override
	public Action chooseAction() {
		double lNextBidValue = 0;
		Action lAction = null;

		switch (fPlayingFor) {
		case BUYER:
			deal = Buyer(fRound);
			lNextBidValue = BuyOffer[fRound][0];
			break;

		case SELLER:
			deal = Seller(fRound);
			lNextBidValue = SellOffer[fRound][0];
			break;
		}
		if (deal)
			lAction = new Accept(getAgentID(), lastReceivedOffer);

		else {
			HashMap<Integer, Value> lValues = new HashMap<Integer, Value>();
			ValueReal lValue = new ValueReal(lNextBidValue);
			lValues.put(utilitySpace.getDomain().getIssues().get(0).getNumber(),
					lValue);
			try {
				Bid lBid = new Bid(utilitySpace.getDomain(), lValues);
				lAction = new Offer(getAgentID(), lBid);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return lAction;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public void init() {

		fRound = 0;
		dist = new double[deadline + 1];
		BuyOffer = new double[deadlineB + 1][2];
		SellOffer = new double[deadlineS + 1][2];
		BuyOffer[0][0] = MinB;
		SellOffer[0][0] = MaxS;
		BuyOffer[0][1] = BuyOffer[0][0] + (percentageB1 * Bcons);
		SellOffer[0][1] = SellOffer[0][0] - (percentageS1 * Scons);
		if (getName().equals("Seller"))
			fPlayingFor = BUYER;
		else
			fPlayingFor = SELLER;
		tacticB = 1;
		tacticS = 1;
		BBuy = 0.01;
		LBuy = 2;
		MBuy = 2;
		BSell = 50;
		LSell = 2;
		MSell = 2;
		Bcons = 1;
		Scons = 1;
		BBuyStretch = 0.1;
		BSellStretch = 0.1;
		// Initializing the value of the threshold for the first offers
		threshholdB = ThreshFind(deadlineB, 0, BuyOffer[0][0], BuyOffer[0][1]);
		threshholdS = ThreshFind(deadlineS, 0, SellOffer[0][0],
				SellOffer[0][1]);

	}

	@Override
	public void ReceiveMessage(Action opponentAction) {
		fRound++;
		if (opponentAction instanceof Offer) {
			Offer lOffer = (Offer) opponentAction;
			lastReceivedOffer = lOffer.getBid();
			switch (fPlayingFor) {
			case BUYER:
				try {
					SellOffer[fRound][0] = ((ValueReal) (lastReceivedOffer
							.getValue(utilitySpace.getDomain().getIssues()
									.get(0).getNumber()))).getValue();
					SellOffer[fRound][1] = SellOffer[fRound][0]
							+ (percentageB1 * Bcons);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case SELLER:
				try {
					BuyOffer[fRound][0] = ((ValueReal) (lastReceivedOffer
							.getValue(utilitySpace.getDomain().getIssues()
									.get(0).getNumber()))).getValue();
					BuyOffer[fRound][1] = BuyOffer[fRound][0]
							- (percentageS1 * Scons);
				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			}// switch
		} // if
	}

	public static double CalculateCost(double constant, int NoIteration) {
		double cost;
		cost = (java.lang.Math.exp(NoIteration * constant)
				- java.lang.Math.exp(-NoIteration * constant))
				/ (java.lang.Math.pow(java.lang.Math.E, NoIteration * constant)
						+ java.lang.Math.pow(java.lang.Math.E,
								-NoIteration * constant));
		return cost;
	}

	protected static boolean Buyer(int i) {
		double A = 0;
		itercount++;
		double[] inter = {};
		switch (tacticB) {
		case 1:
			// If it has reached the deadline terminate negotiaiton
			if (i > deadlineB) {
				// System.out.println("Finished");
				return true;
			}
			// if it has not reached the deadline continue negotiation
			else {
				Utl = (MaxB - SellOffer[i - 1][0]) / (MaxB - MinB);
				if (i > 0) {
					Utl = (MaxB - SellOffer[i - 1][0]) / (MaxB - MinB);
					if (CSell == false) {
						/*
						 * if
						 * (((double)SellOffer[i-1][0]<=(double)BuyOffer[i-1][
						 * 1]) && (i<deadlineB)) {
						 * System.out.println("Accepted");
						 * AcceptedValue=SellOffer[i-1][0]; return true; }
						 */
						if ((SellOffer[i - 1][1] <= BuyOffer[i - 1][1])
								&& (i <= deadlineB)) {
							double[] xParams = { SellOffer[i - 1][1],
									SellOffer[i - 1][0], BuyOffer[i - 1][0],
									BuyOffer[i - 1][1] };
							if (BuyOffer[i - 1][1] == BuyOffer[i - 1][0]) {
								CBuy = false;
							} else {
								double[] x = { SellOffer[i - 1][1],
										SellOffer[i - 1][0], BuyOffer[i - 1][0],
										BuyOffer[i - 1][1] };
								xParams = x;
								double[] yParams = { 0.0, 1.0, 1.0, 0.0 };
								inter = Intersection(xParams, yParams);
								// Checks if the the intersection pointis lower
								// than the threshhold, if so agent will
								// propose the crisp value
								if (inter[0] <= threshholdB) {
									CBuy = true;
								} else {
									CBuy = false;
								}
							}

						}
					}
				}
				if (BBuy == 0.01 || BBuy == 0.2) {
					A = java.lang.Math.exp(java.lang.Math.pow(
							1 - ((double) java.lang.Math.min(i + 1, deadlineB)
									/ (double) deadlineB),
							(BBuy)) * java.lang.Math.log(K));
				} else {
					A = K + ((1 - K) * (java.lang.Math
							.pow(((double) java.lang.Math.min(i + 1, deadlineB)
									/ (double) deadlineB), (1 / BBuy))));
				}
				double tmp = MinB + ((MaxB - MinB) * A);
				double tmpUtl = (MaxB - tmp) / (MaxB - MinB);
				if (CSell == true && i >= 2) {
					if (SellOffer[i - 1][0] <= threshholdB) {
						AcceptedValue = SellOffer[i - 1][0];
						return true;
					} else {
						CSell = false;
					}
				}
				if ((float) tmpUtl < (float) Utl && (CBuy == false)) {
					// System.out.println("Accepted"+SellOffer[i-1][0]);
					AcceptedValue = SellOffer[i - 1][0];
					return true;
				} else {

					if (CBuy == true) {
						BuyOffer[i][0] = inter[0];
						BuyOffer[i][1] = BuyOffer[i][0];

					} else {
						BuyOffer[i][0] = tmp;
						// code added to calculate the stretch reduction
						if (BBuyStretch == 0.01 || BBuyStretch == 0.2) {
							A = java.lang.Math.exp(java.lang.Math.pow(
									1 - ((double) java.lang.Math.min(i + 1,
											deadlineB) / (double) deadlineB),
									(BBuyStretch)) * java.lang.Math.log(K));

						} else {
							A = K + ((1 - K) * (java.lang.Math.pow(
									((double) java.lang.Math.min(i + 1,
											deadlineB) / (double) deadlineB),
									(1 / BBuyStretch))));
						}
						double tmpBcons = ((percentageB2 * (Bcons))
								+ ((percentageB1 - percentageB2) * (Bcons)
										* (1 - A)));

						if (BuyOffer[i][0] + tmpBcons >= MaxB) {
							BuyOffer[i][1] = MaxB;

						} else {
							BuyOffer[i][1] = BuyOffer[i][0] + tmpBcons;
						}
					}
					threshholdB = ThreshFind(deadlineB, i + 1, BuyOffer[i][0],
							BuyOffer[i][1]);
					return false;
				}

			}

		case 2:
			if (i > deadlineB) {
				// System.out.println("Finished");
				return true;
			} else {
				if (i > 0) {
					Utl = (MaxB - SellOffer[i - 1][0]) / (MaxB - MinB);
					if (CSell == false) {
						/*
						 * if
						 * (((double)SellOffer[i-1][0]<=(double)BuyOffer[i-1][
						 * 1]) && (i<deadlineB)) {
						 * System.out.println("Accepted");
						 * AcceptedValue=SellOffer[i-1][0]; return true; }
						 */

						if ((SellOffer[i - 1][1] <= BuyOffer[i - 1][1])
								&& (i <= deadlineB)) {
							double[] xParams = new double[4];
							if (BuyOffer[i - 1][0] == BuyOffer[i - 1][1]) {
								CBuy = false;
							} else {
								double[] x = { SellOffer[i - 1][1],
										SellOffer[i - 1][0], BuyOffer[i - 1][0],
										BuyOffer[i - 1][1] };
								xParams = x;
								CBuy = true;
								double[] yParams = { 0.0, 1.0, 1.0, 0.0 };
								inter = Intersection(xParams, yParams);
								double DifUtlOurs = (MaxB - BuyOffer[i - 1][0])
										/ (MaxB - MinB);
								double MidUtl = (MaxB - inter[0])
										/ (MaxB - MinB);
								DifUtlOurs -= MidUtl;
								double DifUtlOps = (MaxB - SellOffer[i - 1][0])
										/ (MaxB - MinB);
								DifUtlOps = MidUtl - DifUtlOps;
								// if ((float)DifUtlOurs<=(float)DifUtlOps){
								CBuy = true;
								MinB = inter[0];
								// }
								// else
								// {
								// CBuy=false;
								// }

							}
							/*
							 * double[] yParams={0.0,1.0,1.0,0.0}; double[]
							 * inter={}; inter=Intersection(xParams, yParams);
							 * CBuy=true; MinB=BuyOffer[i-1][0];
							 * MaxB=SellOffer[i-1][0]; MinS=BuyOffer[i-1][0];
							 * MaxS=SellOffer[i-1][0]; BBuy=40;
							 * BuyOffer[i][0]=inter[0]; BuyOffer[i][1]=0; return
							 * false;
							 */
						}
					}
				}
				if (CSell == true && i >= 2) {
					double DifUtlOurs = (MaxB - BuyOffer[i - 1][0])
							/ (MaxB - MinB);
					DifUtlOurs -= Utl;
					double DifUtlOps = (MaxB - SellOffer[i - 1][0])
							/ (MaxB - MinB);
					DifUtlOps = Utl - DifUtlOps;
					if ((float) DifUtlOurs <= (float) DifUtlOps) {
						// System.out.println("Accepted the middle value");
						AcceptedValue = SellOffer[i - 1][0];
						return true;
					} else {
						CSell = false;
					}
				}
				if (LBuy == 3) {
					Random generator = new Random();
					if (i > LBuy) {
						// double
						// tmp3=java.lang.Math.min(java.lang.Math.max((BuyOffer[i-1][0]+(double)SellOffer[i-1-LBuy][0]-(double)SellOffer[i-1][0]+(generator.nextInt(4))),MinB),MaxB);
						double tmp3 = java.lang.Math.min(java.lang.Math.max(
								(BuyOffer[i - 1][0] + SellOffer[i - 1 - LBuy][0]
										- SellOffer[i - 1][0] + 2),
								MinB), MaxB);
						double tmpUtl3 = (MaxB - tmp3) / (MaxB - MinB);
						if (((float) tmpUtl3 <= (float) Utl)
								&& (CBuy == false)) {
							// System.out.println("Accepted");
							AcceptedValue = SellOffer[i - 1][0];
							return true;
						} else {

							if (CBuy == true) {
								BuyOffer[i][0] = inter[0];
								BuyOffer[i][1] = BuyOffer[i][0];
							} else {
								BuyOffer[i][0] = tmp3;
								if (BuyOffer[i][0] + Bcons >= MaxB) {
									BuyOffer[i][1] = MaxB;
								} else {
									BuyOffer[i][1] = BuyOffer[i][0] + Bcons;
								}
							}
							return false;
						}
					} else {
						BuyOffer[i][0] = MinB + (i * 0.5);
						BuyOffer[i][1] = MinB + (i * 0.5) + Bcons;
						return false;
					}
				} else {
					if (i > LBuy) {
						double tmp2 = java.lang.Math.min(
								java.lang.Math.max((SellOffer[i - 1 - LBuy][0]
										/ SellOffer[i - 1][0])
										* BuyOffer[i - 1][0], MinB),
								MaxB);
						// double
						// tmp3=java.lang.Math.min(java.lang.Math.max((BuyOffer[i-1][0]+(double)SellOffer[i-1-LBuy][0]-(double)SellOffer[i-1][0]+2),MinB),MaxB);
						// Utl=(double)(MaxB-SellOffer[i-1])/(double)(MaxB-MinB);
						double tmpUtl2 = (MaxB - tmp2) / (MaxB - MinB);
						if (((float) tmpUtl2 <= (float) Utl)
								&& (CBuy == false)) {
							// System.out.println("Accepted");
							AcceptedValue = SellOffer[i - 1][0];
							return true;

						} else {

							if (CBuy == true) {
								BuyOffer[i][0] = inter[0];
								BuyOffer[i][1] = BuyOffer[i][0];
							} else {
								BuyOffer[i][0] = tmp2;
								if (BuyOffer[i][0] + Bcons >= MaxB) {
									BuyOffer[i][1] = MaxB;
								} else {
									BuyOffer[i][1] = BuyOffer[i][0] + Bcons;
								}
							}
							return false;
						}
					} else {
						BuyOffer[i][0] = MinB + ((i) * 0.5);
						BuyOffer[i][1] = MinB + ((i) * 0.5) + Bcons;
						return false;
					}
				}
			}
		case 3:
			// If it has reached the termiantion condition stop negotiating
			if (i > deadlineB) {
				// System.out.println("Finished");
				return true;

			}
			// if it has not reached the deadline continue negotiation
			else {
				if (i > 0) {

					Utl = (MaxB - SellOffer[i - 1][0]) / (MaxB - MinB);
					if (CSell == false) {
						/*
						 * if
						 * (((double)SellOffer[i-1][0]<=(double)BuyOffer[i-1][
						 * 1]) && (i<deadlineB)) {
						 * System.out.println("Accepted");
						 * AcceptedValue=SellOffer[i-1][0]; return true; }
						 */

						if ((SellOffer[i - 1][1] <= BuyOffer[i - 1][1])
								&& (i <= deadlineB)) {
							double[] xParams = new double[4];
							if (BuyOffer[i - 1][0] == BuyOffer[i - 1][1]) {
								CBuy = false;
							} else {
								double[] x = { SellOffer[i - 1][1],
										SellOffer[i - 1][0], BuyOffer[i - 1][0],
										BuyOffer[i - 1][1] };
								xParams = x;
								CBuy = true;
								double[] yParams = { 0.0, 1.0, 1.0, 0.0 };
								inter = Intersection(xParams, yParams);
								double DifUtlOurs = (MaxB - BuyOffer[i - 1][0])
										/ (MaxB - MinB);
								double MidUtl = (MaxB - inter[0])
										/ (MaxB - MinB);
								DifUtlOurs -= MidUtl;
								// System.out.println(inter[0]);
								double DifUtlOps = (MaxB - SellOffer[i - 1][0])
										/ (MaxB - MinB);
								DifUtlOps = MidUtl - DifUtlOps;
								// System.out.println((float)DifUtlOps);
								// System.out.println(DifUtlOurs);
								// if ((float)DifUtlOurs<=(float)DifUtlOps){
								CBuy = true;
								MinB = inter[0];
								// }
								// else
								// {
								// CBuy=false;
								// }

							}
							/*
							 * double[]
							 * xParams={SellOffer[i-1][1],SellOffer[i-1]
							 * [0],BuyOffer[i-1][0],BuyOffer[i-1][1]}; double[]
							 * yParams={0.0,1.0,1.0,0.0}; //double[] inter={};
							 * inter=Intersection(xParams, yParams); CBuy=true;
							 * MinB=BuyOffer[i-1][0]; MaxB=SellOffer[i-1][0];
							 * MinS=BuyOffer[i-1][0]; MaxS=SellOffer[i-1][0];
							 * BBuy=40; BuyOffer[i][0]=inter[0];
							 * BuyOffer[i][1]=0; return false;
							 */
						}
					}
				}
				if (CSell == true && i >= 2) {
					double DifUtlOurs = (MaxB - BuyOffer[i - 1][0])
							/ (MaxB - MinB);
					DifUtlOurs -= Utl;
					double DifUtlOps = (MaxB - SellOffer[i - 1][0])
							/ (MaxB - MinB);
					DifUtlOps = Utl - DifUtlOps;
					if ((float) DifUtlOurs <= (float) DifUtlOps) {
						// System.out.println("Accepted the middle value");
						AcceptedValue = SellOffer[i - 1][0];
						return true;
					} else {
						CSell = false;
					}
				}
				double resource = new Double(
						MBuy * ((double) 1 / (double) (i + 1)));
				A = K + ((1 - K) * (1 / (java.lang.Math.exp(resource))));
				double tmp4 = MinB + ((MaxB - MinB) * A);
				double tmpUtl4 = (MaxB - tmp4) / (MaxB - MinB);
				if (((float) tmpUtl4 <= (float) Utl) && (CBuy == false)) {
					// System.out.println("Accepted");
					AcceptedValue = SellOffer[i - 1][0];
					return true;

				} else {

					if (CBuy == true) {
						BuyOffer[i][0] = inter[0];
						BuyOffer[i][1] = BuyOffer[i][0];
					} else {
						BuyOffer[i][0] = tmp4;
						if (BuyOffer[i][0] + Bcons >= MaxB) {
							BuyOffer[i][1] = MaxB;
						} else {
							BuyOffer[i][1] = BuyOffer[i][0] + Bcons;
						}
					}
					return false;
				}

			}
		}
		return false;
	}

	protected static boolean Seller(int i) {
		double A = 0;
		itercount++;
		double[] inter = {};
		switch (tacticS) {
		case 1:
			// If it has reached the deadline terminate negotiaiton
			if (i > deadlineS) {
				// System.out.println("Finished");
				return true;
			}
			// if it has not reached the deadline continue negotiation
			else {
				Utl = (BuyOffer[i][0] - MinS) / (MaxS - MinS);
				if (i > 0) {
					if (CBuy == false) {
						/*
						 * if
						 * (((double)SellOffer[i-1][1]<=(double)BuyOffer[i][0]
						 * )&& (i<deadlineS)) { System.out.println("Accepted");
						 * AcceptedValue=BuyOffer[i-1][0]; return true; }
						 */
						if ((SellOffer[i - 1][1] <= BuyOffer[i][1])
								&& (i <= deadlineS)) {
							double[] xParams = new double[4];
							if (SellOffer[i - 1][0] == SellOffer[i - 1][1]) {
								CSell = false;
							} else {
								double[] x = { SellOffer[i - 1][1],
										SellOffer[i - 1][0], BuyOffer[i][0],
										BuyOffer[i][1] };
								xParams = x;
								double[] yParams = { 0.0, 1.0, 1.0, 0.0 };
								inter = Intersection(xParams, yParams);
								if (inter[0] >= threshholdS) {
									CSell = true;
								} else {
									CSell = false;
								}
							}

						}
					}
				}
				if (BSell == 0.01 || BSell == 0.2) {
					A = java.lang.Math.exp(java.lang.Math.pow(
							(1 - ((double) java.lang.Math.min(i + 1, deadlineS)
									/ (double) deadlineS)),
							(BSell)) * java.lang.Math.log(K));
				} else {
					A = K + ((1 - K) * (java.lang.Math
							.pow(((double) java.lang.Math.min(i + 1, deadlineS)
									/ (double) deadlineS), (1 / BSell))));
				}
				double tmp = MinS + ((MaxS - MinS) * (1 - A));
				double tmpUtl = (tmp - MinS) / (MaxS - MinS);
				if (CBuy == true) {
					if (BuyOffer[i][0] >= threshholdS) {
						AcceptedValue = BuyOffer[i][0];
						return true;
					} else {
						CBuy = false;
					}
				}
				if (((float) tmpUtl <= (float) Utl) && (CSell == false)) {
					// System.out.println("Accepted");
					AcceptedValue = BuyOffer[i][0];
					return true;
				} else {

					if (CSell == true) {
						SellOffer[i][0] = inter[0];
						SellOffer[i][1] = SellOffer[i][0];
					} else {
						SellOffer[i][0] = tmp;
						// code added to calculate the stretch for fuzzy offers
						if (BSellStretch == 0.01 || BSellStretch == 0.2) {
							A = java.lang.Math.exp(java.lang.Math.pow(
									(1 - ((double) java.lang.Math.min(i + 1,
											deadlineS) / (double) deadlineS)),
									(BSellStretch)) * java.lang.Math.log(K));
						} else {
							A = K + ((1 - K) * (java.lang.Math.pow(
									((double) java.lang.Math.min(i + 1,
											deadlineS) / (double) deadlineS),
									(1 / BSellStretch))));
						}
						double tmpScons = ((percentageS2 * (Scons))
								+ ((percentageS1 - percentageS2) * (Scons)
										* (1 - A)));
						if ((SellOffer[i][0] - tmpScons) <= MinS) {
							SellOffer[i][1] = MinS;
						} else {
							SellOffer[i][1] = SellOffer[i][0] - tmpScons;
						}
					}
					threshholdS = ThreshFind(deadlineS, i + 1, SellOffer[i][0],
							SellOffer[i][1]);
					return false;
				}
			}
		case 2:
			if (i > deadlineS) {
				// System.out.println("Finished");
				return true;
			} else {
				Utl = (BuyOffer[i][0] - MinS) / (MaxS - MinS);
				if (i > 0) {
					if (CBuy == false) {
						/*
						 * if
						 * (((double)SellOffer[i-1][1]<=(double)BuyOffer[i][0]
						 * )&& (i<deadlineS)) { System.out.println("Accepted");
						 * AcceptedValue=BuyOffer[i-1][0]; return true; }
						 */
						if ((SellOffer[i - 1][1] <= BuyOffer[i][1])
								&& (i <= deadlineS)) {
							double[] xParams = new double[4];
							if (SellOffer[i - 1][0] == SellOffer[i - 1][1]) {
								CSell = false;
							} else {
								double[] x = { SellOffer[i - 1][1],
										SellOffer[i - 1][0], BuyOffer[i][0],
										BuyOffer[i][1] };
								xParams = x;
								CSell = true;
								double[] yParams = { 0.0, 1.0, 1.0, 0.0 };
								inter = Intersection(xParams, yParams);
								double DifUtlOurs = (SellOffer[i - 1][0] - MinS)
										/ (MaxS - MinS);
								double MinUtl = (inter[0] - MinS)
										/ (MaxS - MinS);
								DifUtlOurs -= MinUtl;
								double DifUtlOps = (BuyOffer[i - 1][0] - MinS)
										/ (MaxS - MinS);
								DifUtlOps = Utl - DifUtlOps;
								// if ((float)DifUtlOurs<=(float)DifUtlOps){
								MaxS = inter[0];
								CSell = true;
								// }
								// else
								// {
								// CSell=false;
								// }
							}
							/*
							 * double[]
							 * xParams={SellOffer[i-1][1],SellOffer[i-1]
							 * [0],BuyOffer[i][0],BuyOffer[i][1]}; double[]
							 * yParams={0.0,1.0,1.0,0.0}; //double[] inter={};
							 * inter=Intersection(xParams, yParams); CSell=true;
							 * //MinB=BuyOffer[i-1][0];
							 * //MaxB=SellOffer[i-1][0];
							 * //MinS=BuyOffer[i-1][0];
							 * //MaxS=SellOffer[i-1][0]; //BSell=40;
							 * //SellOffer[i][0]=inter[0]; //SellOffer[i][1]=0;
							 * //return false;
							 */
						}
					}
					if (CBuy == true) {
						double DifUtlOurs = (SellOffer[i - 1][0] - MinS)
								/ (MaxS - MinS);
						DifUtlOurs -= Utl;
						double DifUtlOps = (BuyOffer[i - 1][0] - MinS)
								/ (MaxS - MinS);
						DifUtlOps = Utl - DifUtlOps;
						if ((float) DifUtlOurs <= (float) DifUtlOps) {
							// System.out.println("Accepted");
							AcceptedValue = BuyOffer[i][0];
							return true;
						} else {
							CBuy = false;
						}
					}
				}
				if (LSell == 3) {
					Random generator = new Random();
					int rand = generator.nextInt(4);
					rand = 2;
					if (i >= LSell) {
						double tmp3 = java.lang.Math.min(
								java.lang.Math.max((SellOffer[i - 1][0]
										+ BuyOffer[i - LSell][0]
										- BuyOffer[i][0] - rand), MinS),
								MaxS);
						double tmpUtl3 = (tmp3 - MinS) / (MaxS - MinS);
						if (((float) tmpUtl3 <= (float) Utl)
								&& (CSell == false)) {
							// System.out.println("Accepted");
							AcceptedValue = BuyOffer[i][0];
							return true;
						} else {

							if (CSell == true) {
								SellOffer[i][0] = inter[0];
								SellOffer[i][1] = SellOffer[i][0];
							} else {
								SellOffer[i][0] = tmp3;
								if ((SellOffer[i][0] - Scons) <= MinS) {
									SellOffer[i][1] = MinS;
								} else {
									SellOffer[i][1] = SellOffer[i][0] - Scons;
								}
							}
							return false;
						}
					} else {
						SellOffer[i][0] = MaxS - ((i) * 0.5);
						SellOffer[i][1] = MaxS - ((i) * 0.5) - Scons;
						return false;
					}
				} else {
					if (i >= LSell) {

						double tmp2 = java.lang.Math.min(java.lang.Math
								.max((BuyOffer[i - LSell][0] / BuyOffer[i][0])
										* SellOffer[i - 1][0], MinS),
								MaxS);
						Utl = (BuyOffer[i][0] - MinS) / (MaxS - MinS);
						double tmpUtl2 = (tmp2 - MinS) / (MaxS - MinS);
						if (((float) tmpUtl2 <= (float) Utl)
								&& (CSell == false)) {
							// System.out.println("Accepted");
							AcceptedValue = BuyOffer[i][0];
							return true;
						} else {

							if (CSell == true) {
								SellOffer[i][0] = inter[0];
								SellOffer[i][1] = SellOffer[i][0];
							} else {
								SellOffer[i][0] = tmp2;
								if ((SellOffer[i][0] - Scons) <= MinS) {
									SellOffer[i][1] = MinS;
								} else {
									SellOffer[i][1] = SellOffer[i][0] - Scons;
								}
							}
							return false;
						}
					} else {
						SellOffer[i][0] = MaxS - ((i) * 0.5);
						SellOffer[i][1] = MaxS - ((i) * 0.5) - Scons;
						return false;
					}

				}
			}
		case 3:
			// If it has reached the deadline terminate negotiaiton
			if (i > deadlineS) {
				// System.out.println("Finished");
				return true;
			}
			// if it has not reached the deadline continue negotiation
			else {
				Utl = (BuyOffer[i][0] - MinS) / (MaxS - MinS);
				if (i > 0) {
					if (CBuy == false) {
						/*
						 * if
						 * (((double)SellOffer[i-1][1]<=(double)BuyOffer[i][0]
						 * )&& (i<deadlineS)) { System.out.println("Accepted");
						 * AcceptedValue=BuyOffer[i-1][0]; return true; }
						 */
						if ((SellOffer[i - 1][1] <= BuyOffer[i][1])
								&& (i <= deadlineS)) {
							double[] xParams = new double[4];
							if (SellOffer[i - 1][0] == SellOffer[i - 1][1]) {
								CSell = false;
							} else {
								double[] x = { SellOffer[i - 1][1],
										SellOffer[i - 1][0], BuyOffer[i][0],
										BuyOffer[i][1] };
								xParams = x;
								CSell = true;
								double[] yParams = { 0.0, 1.0, 1.0, 0.0 };
								inter = Intersection(xParams, yParams);
								double DifUtlOurs = (SellOffer[i - 1][0] - MinS)
										/ (MaxS - MinS);
								double MinUtl = (inter[0] - MinS)
										/ (MaxS - MinS);
								DifUtlOurs -= MinUtl;
								double DifUtlOps = (BuyOffer[i - 1][0] - MinS)
										/ (MaxS - MinS);
								DifUtlOps = Utl - DifUtlOps;
								// if ((float)DifUtlOurs<=(float)DifUtlOps){
								MaxS = inter[0];
								CSell = true;
								// }
								// else
								// {
								// CSell=false;
								// }
							}
							/*
							 * double[]
							 * xParams={SellOffer[i-1][1],SellOffer[i-1]
							 * [0],BuyOffer[i][0],BuyOffer[i][1]}; double[]
							 * yParams={0.0,1.0,1.0,0.0}; //double[] inter={};
							 * inter=Intersection(xParams, yParams); CSell=true;
							 * //MinB=BuyOffer[i-1][0];
							 * //MaxB=SellOffer[i-1][0];
							 * //MinS=BuyOffer[i-1][0];
							 * //MaxS=SellOffer[i-1][0]; //BSell=40;
							 * //SellOffer[i][0]=inter[0]; //SellOffer[i][1]=0;
							 * //return false;
							 */
						}
					} else if (CBuy == true) {
						double DifUtlOurs = (SellOffer[i - 1][0] - MinS)
								/ (MaxS - MinS);
						DifUtlOurs -= Utl;
						double DifUtlOps = (BuyOffer[i - 1][0] - MinS)
								/ (MaxS - MinS);
						DifUtlOps = Utl - DifUtlOps;
						if ((float) DifUtlOurs <= (float) DifUtlOps) {
							// System.out.println("Accepted");
							AcceptedValue = BuyOffer[i][0];
							return true;
						} else {
							CBuy = false;
						}
					}
				}
				double resource = new Double(
						MSell * ((double) 1 / (double) (i + 1)));
				A = K + ((1 - K) * (1 / (java.lang.Math.exp(resource))));
				double tmp4 = MinS + ((MaxS - MinS) * (1 - A));
				double tmpUtl4 = (tmp4 - MinS) / (MaxS - MinS);
				if (((float) tmpUtl4 < (float) Utl) && (CSell == false)) {
					// System.out.println("Accepted");
					AcceptedValue = BuyOffer[i][0];
					return true;
				} else {

					if (CSell == true) {
						SellOffer[i][0] = inter[0];
						SellOffer[i][1] = SellOffer[i][0];
					} else {
						SellOffer[i][0] = tmp4;
						if ((SellOffer[i][0] - Scons) <= MinS) {
							SellOffer[i][1] = MinS;
						} else {
							SellOffer[i][1] = SellOffer[i][0] - Scons;
						}
					}
					return false;
				}

			}
		}
		return false;
	}

	/**
	 * Function threshfind() which receives the current time of negotiation,
	 * pick value and stretch value of the offers and generates the threshhold
	 * for each offer
	 */
	public static double ThreshFind(int deadline, int time, double pick,
			double stretch) {
		double weightPick, weightStretch;
		weightStretch = (double) time / (double) deadline;
		weightPick = (double) (deadline - time) / (double) deadline;
		double thresh = (weightPick * pick) + (weightStretch * stretch);
		return thresh;
	}

	/**
	 * Function Intersection returns intersection of two lines
	 */
	public static double[] Intersection(double[] xPars, double[] yPars) {
		// Calculating the intersection point of the two fuzzy offers
		double ua = 0;
		ua = (((xPars[3] - xPars[2]) * (yPars[0] - yPars[2]))
				- ((yPars[3] - yPars[2]) * (xPars[0] - xPars[2])))
				/ (((yPars[3] - yPars[2]) * (xPars[1] - xPars[0]))
						- ((xPars[3] - xPars[2]) * (yPars[1] - yPars[0])));
		double[] interPoint = new double[2];
		interPoint[0] = xPars[0] + (ua * (xPars[1] - xPars[0]));
		interPoint[1] = yPars[0] + (ua * (yPars[1] - yPars[0]));
		return interPoint;
	}

	public static void FuzzyDist() {
		double midPointXB = 0;
		double midPointYB = 0;
		double midPointXS = 0;
		double midPointYS = 0;
		// midPointXB=((double)(java.lang.Math.pow(BuyOffer[iteration-1][0],2)-java.lang.Math.pow(MinB,2))/(double)(2));
		if (BuyOffer[iteration - 1][0] != BuyOffer[iteration - 1][1]) {
			midPointXB += (((3
					* (java.lang.Math.pow(BuyOffer[iteration - 1][0], 2)
							* BuyOffer[iteration - 1][1]))
					- java.lang.Math.pow(BuyOffer[iteration - 1][1], 3)
					- (2 * java.lang.Math.pow(BuyOffer[iteration - 1][0], 3)))
					/ (6 * (BuyOffer[iteration - 1][0]
							- BuyOffer[iteration - 1][1])));
			midPointXB = midPointXB / (((2 * BuyOffer[iteration - 1][1]
					* BuyOffer[iteration - 1][0])
					- java.lang.Math.pow(BuyOffer[iteration - 1][1], 2)
					- java.lang.Math.pow(BuyOffer[iteration - 1][0], 2))
					/ (2 * (BuyOffer[iteration - 1][0]
							- BuyOffer[iteration - 1][1])));
			midPointYB = 0.5;
		} else {
			midPointXB = BuyOffer[iteration - 1][0];
		}
		if (SellOffer[iteration - 1][0] != SellOffer[iteration - 1][1]) {
			// midPointXS=((double)(java.lang.Math.pow(MaxS,2)-java.lang.Math.pow(SellOffer[iteration-1][0],2))/(double)(2));
			midPointXS += (((3
					* (java.lang.Math.pow(SellOffer[iteration - 1][1], 2)
							* SellOffer[iteration - 1][0]))
					- java.lang.Math.pow(SellOffer[iteration - 1][0], 3)
					- (2 * java.lang.Math.pow(SellOffer[iteration - 1][1], 3)))
					/ (6 * (SellOffer[iteration - 1][0]
							- SellOffer[iteration - 1][1])));
			midPointXS = midPointXS / (((2 * SellOffer[iteration - 1][1]
					* SellOffer[iteration - 1][0])
					- java.lang.Math.pow(SellOffer[iteration - 1][1], 2)
					- java.lang.Math.pow(SellOffer[iteration - 1][0], 2))
					/ (2 * (SellOffer[iteration - 1][0]
							- SellOffer[iteration - 1][1])));
			midPointYS = 0.5;
		} else {
			midPointXS = SellOffer[iteration - 1][0];
		}
		double last = java.lang.Math
				.sqrt(java.lang.Math.pow((midPointXS - midPointXB), 2)
						+ java.lang.Math.pow((midPointYS - midPointYB), 2));
		dist[iteration - 1] = last;
	}

	public static double FinalDistance(double[] dist) {
		double last = 0;
		for (int i = 0; i < iteration; i++) {
			last += dist[i];
		}
		return last;
	}

	@Override
	public SupportedNegotiationSetting getSupportedNegotiationSetting() {
		return SupportedNegotiationSetting.getLinearUtilitySpaceInstance();
	}

	@Override
	public String getDescription() {
		return "Fuzzy negotiator";
	}

}
