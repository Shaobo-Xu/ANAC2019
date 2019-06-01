package backup.GoodGame;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AgentGG extends AbstractNegotiationParty {

    private ImpMap impMap;
    private ImpMap opponentImpMap;
    private double offerLowerRatio = 1.0;
    private double opponentRatio;
    private double MAX_IMPORTANCE;
    private double MIN_IMPORTANCE;
    private Bid MAX_IMPORTANCE_BID;
    private double OPPONENT_MAX_IMPORTANCE;
    private double OPPONENT_MIN_IMPORTANCE;
    private Bid receivedOfferBid;
    private Bid initialOpponentBid = null;
    private double lastBidValue;
    private boolean initialTimePass = false;
    private double ratioBase;
    private Boolean collectingOpponentOffer = true;
    private int numberOfOffers = 0;
    private int numberOfIssues = 0;

    //初始化Agent
    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        // 创建 空的 我的importance map 以及对手的 value map
        this.impMap = new ImpMap(userModel);
        this.opponentImpMap = new ImpMap(userModel);

        // 我的importance map
        this.impMap.self_update(userModel);

        // 获取最大、最小bid
        this.getMaxAndMinBid();
        System.out.println("Agent " + this.getPartyId() + " has finished initialization");

        // 获取issue数量
        this.numberOfIssues = this.getDomain().getIssues().size();
    }

    //报价 or 接受 策略
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        double time = getTimeLine().getTime();

        if (getLastReceivedAction() instanceof Offer) {
            double impRatioForMe = (this.impMap.getImportance(this.receivedOfferBid) - this.MIN_IMPORTANCE) / (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE);

            // 接受报价的条件，即高于我的threshold
            if (impRatioForMe >= this.offerLowerRatio) {
                System.out.println("\n\naccepted agent: Agent" + this.getPartyId());
                System.out.println("last bid: " + this.receivedOfferBid);
                System.out.println("\ncurrent threshold: " + this.offerLowerRatio);
                System.out.println("\n\n");
                return new Accept(this.getPartyId(), this.receivedOfferBid);
            }

            // 时间小于0.32时，保持高报价，并收集对手信息
            if (time < 0.32) {
                // 一开始对方可能会一直报最高价，这个没什么用，会影响我们的map建立，因此直到对方发不重复的才开始记录
                if (initialOpponentBid == null) {
                    initialOpponentBid = receivedOfferBid;
                    this.opponentImpMap.opponent_update(this.receivedOfferBid);
                } else {
                    if (receivedOfferBid != initialOpponentBid) {
                        initialTimePass = true;
                    }
                }
                if (initialTimePass) {
                    this.opponentImpMap.opponent_update(this.receivedOfferBid);
                }

                // 前0.08时间内报最高价，为了适应一些特殊的domain
                if (time < 0.08) {
                    this.offerLowerRatio = 1.00;
                } else {
                    // 0.08~0.32时间内保持高报价
                    this.offerLowerRatio = 1. - 1.2 * (time - 0.08) * (time - 0.08) - 17. / 375. * (time - 0.08);
                }
            } else {
                // 在0.32时，运行一下计算
                if (this.collectingOpponentOffer) {
                    this.collectingOpponentOffer = false;
                    System.out.println("\n\nmy sorted importance map: ");
                    for (List<impUnit> impUnitList : this.impMap.values()) {
                        System.out.println(impUnitList);
                    }
                    System.out.println("my max importance bid: " + this.MAX_IMPORTANCE_BID);
                    System.out.println("my max importance: " + this.MAX_IMPORTANCE);

                    // 大致找出对手utility最高的Pareto边界的threshold
                    HashMap<Integer, Value> lValues = new HashMap<>();
                    for (Map.Entry<Issue, List<impUnit>> entry : this.impMap.entrySet()) {
                        Issue currentIssue = entry.getKey();
                        List<impUnit> myImpList = entry.getValue();
                        List<impUnit> opponentImpList = opponentImpMap.get(currentIssue);
                        Value value = opponentImpList.get(0).valueOfIssue;
                        boolean value_got = false;
                        for (impUnit opponentUnit : opponentImpList) {
                            if (opponentUnit.meanWeightSum / opponentImpList.get(0).meanWeightSum > 0.8) {
                                for (impUnit myUnit : myImpList) {
                                    if (myUnit.meanWeightSum / myImpList.get(0).meanWeightSum > 0.8) {
                                        if (myUnit.valueOfIssue.toString().equals(opponentUnit.valueOfIssue.toString())) {
                                            value = opponentUnit.valueOfIssue;
                                            value_got = true;
                                        }
                                    }
                                }
                            }
                            if (value_got) break;
                        }
                        int issueNumber = currentIssue.getNumber();
                        lValues.put(issueNumber, value);
                    }
                    Bid bestOpponentBid = new Bid(this.getDomain(), lValues);
                    double bestOpponentBidImportance = this.impMap.getImportance(bestOpponentBid);
                    System.out.println("\nbestOpponentBid: " + bestOpponentBid);
                    System.out.println("bestOpponentBidImportance: " + bestOpponentBidImportance);
                    System.out.println("\nopponent's sorted importance map: ");
                    for (List<impUnit> impUnitList : this.opponentImpMap.values()) {
                        System.out.println(impUnitList);
                    }

                    // 根据找到的最小的Pareto边界以及最大的值为1的边界，计算出中心偏自己位置的点的threshold
                    this.ratioBase = 0.68 + 0.32 * (bestOpponentBidImportance - this.MIN_IMPORTANCE) / (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE);
                    System.out.println("\nbestOpponentBid ratio: " + this.ratioBase);

                    // 更新对手最大、最小bid
                    this.getOpponentMaxAndMinBid();
                } else {
                    // time>0.32,正式谈判
                    if (time < 0.92) {
                        this.numberOfOffers += 1;  // 统计一下bid在0.32~0.92的时间内的发放速度，
                        this.offerLowerRatio = 0.92 - 0.5 * (time - 0.32) * (time - 0.32) + (time - 0.32) * (10. / 6. * this.ratioBase - 37. / 30.);
                    } else if (time < 1.0 - 53 * 0.6 / this.numberOfOffers) {
                        // 记下0.92~0.99* 时间内的对方给的最佳bid
                        double currentValueOfOpponent = impMap.getImportance(receivedOfferBid);
                        if (currentValueOfOpponent > this.lastBidValue) {
                            this.lastBidValue = currentValueOfOpponent;
                        }
                    } else if (time < 1.0 - 23 * 0.6 / this.numberOfOffers) {
                        // 在最后50轮内收集对手给的最好的报价并接受
                        if (impMap.getImportance(receivedOfferBid) > 1.0 * this.lastBidValue) {
                            System.out.println("compromise1");
                            return new Accept(getPartyId(), receivedOfferBid);
                        }
                    } else if (time < 1.0 - 3 * 0.6 / this.numberOfOffers) {
                        // 在最后50轮内收集对手给的最好的报价并接受
                        if (impMap.getImportance(receivedOfferBid) > 0.95 * this.lastBidValue) {
                            System.out.println("compromise2");
                            return new Accept(getPartyId(), receivedOfferBid);
                        }
                    } else {
                        // 如果没有，则妥协
                        System.out.println("compromise0");
                        return new Accept(getPartyId(), receivedOfferBid);
                    }
                    this.opponentRatio = 0.38 + 0.41 * (0.3 / 2. * (time - 0.3) * (time - 0.3) + (time - 0.3)) - 1.0 / this.numberOfIssues;
                }
            }
            double offerUpperRatio = this.offerLowerRatio + 0.08;

            Bid bid = getNeededRandomBid(this.offerLowerRatio, offerUpperRatio, this.opponentRatio);
            return new Offer(getPartyId(), bid);
        }
        return new Offer(getPartyId(), this.MAX_IMPORTANCE_BID);
    }

    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);
        if (act instanceof Offer) {
            Offer offer = (Offer) act;
            this.receivedOfferBid = offer.getBid();
        }
    }

    @Override
    public String getDescription() {
        return "Well Played";
    }

    //获取最大及最小Importance的值及对应OFFER
    private void getMaxAndMinBid() {
        HashMap<Integer, Value> lValues1 = new HashMap<>();
        HashMap<Integer, Value> lValues2 = new HashMap<>();
        for (Map.Entry<Issue, List<impUnit>> entry : this.impMap.entrySet()) {
            Value value1 = entry.getValue().get(0).valueOfIssue;
            Value value2 = entry.getValue().get(entry.getValue().size() - 1).valueOfIssue;
            int issueNumber = entry.getKey().getNumber();
            lValues1.put(issueNumber, value1);
            lValues2.put(issueNumber, value2);
        }
        this.MAX_IMPORTANCE_BID = new Bid(this.getDomain(), lValues1);
        Bid MIN_IMPORTANCE_BID = new Bid(this.getDomain(), lValues2);
        this.MAX_IMPORTANCE = this.impMap.getImportance(this.MAX_IMPORTANCE_BID);
        this.MIN_IMPORTANCE = this.impMap.getImportance(MIN_IMPORTANCE_BID);
    }

    // 获取opponent最大及最小Importance的值及对应OFFER
    private void getOpponentMaxAndMinBid() {
        HashMap<Integer, Value> lValues1 = new HashMap<>();
        HashMap<Integer, Value> lValues2 = new HashMap<>();
        for (Map.Entry<Issue, List<impUnit>> entry : this.opponentImpMap.entrySet()) {
            Value value1 = entry.getValue().get(0).valueOfIssue;
            Value value2 = entry.getValue().get(entry.getValue().size() - 1).valueOfIssue;
            int issueNumber = entry.getKey().getNumber();
            lValues1.put(issueNumber, value1);
            lValues2.put(issueNumber, value2);
        }
        Bid OPPONENT_MAX_IMPORTANCE_BID = new Bid(this.getDomain(), lValues1);
        Bid OPPONENT_MIN_IMPORTANCE_BID = new Bid(this.getDomain(), lValues2);
        this.OPPONENT_MAX_IMPORTANCE = this.opponentImpMap.getImportance(OPPONENT_MAX_IMPORTANCE_BID);
        this.OPPONENT_MIN_IMPORTANCE = this.opponentImpMap.getImportance(OPPONENT_MIN_IMPORTANCE_BID);
    }

    //随机生成高于我与对手threshold的bid
    private Bid getNeededRandomBid(double myLowerImportance, double myUpperImportance, double opponentImportance) {
        long k = this.getDomain().getNumberOfPossibleBids();
        List<Bid> myBid = new ArrayList<>();
        while (true) {
            k = k - 1;
            Bid bid = generateRandomBid();
            double lowerThreshold = myLowerImportance * (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE) + this.MIN_IMPORTANCE;
            double upperThreshold = myUpperImportance * (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE) + this.MIN_IMPORTANCE;
            double opponentThreshold = opponentImportance * (this.OPPONENT_MAX_IMPORTANCE - this.OPPONENT_MIN_IMPORTANCE) + this.OPPONENT_MIN_IMPORTANCE;
            double bidImportance = this.impMap.getImportance(bid);
            double bidOpponentImportance = this.opponentImpMap.getImportance(bid);
            //  在有限次内找寻满足条件的bid
            //  如果找不到，降低oppThreshold再找
            //  如果再找不到，就仅返回大于我的threshold的bid
            //  再找不到，就返回最大bid
            if (k > 0) {
                if (bidImportance >= lowerThreshold && bidImportance <= upperThreshold) {
                    myBid.add(bid);
                    if (bidOpponentImportance >= opponentThreshold) {
                        return bid;
                    }
                }
            } else {
                if (myBid.isEmpty()) {
                    return MAX_IMPORTANCE_BID;
                } else {  //逐步降低oppoThreshold
                    for (int i = 1; i <= 20; i++) {
                        for (Bid mb : myBid) {
                            if (bidOpponentImportance >= opponentThreshold - 0.02 * i) {
                                return mb;
                            }
                        }
                    }
                    int randIndex = rand.nextInt(myBid.size());
                    return myBid.get(randIndex);
                }
            }
        }
    }
}