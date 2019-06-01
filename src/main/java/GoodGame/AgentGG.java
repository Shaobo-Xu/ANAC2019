package GoodGame;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import org.junit.Test;

import java.util.*;


/**
 *
 */
public class AgentGG extends AbstractNegotiationParty {

    private ImpMap impMap;
    private ImpMap opponentImpMap;
    private double offerLowerRatio = 1.0;
    private double MAX_IMPORTANCE;
    private double MIN_IMPORTANCE;
    private double MEDIAN_IMPORTANCE;
    private Bid MAX_IMPORTANCE_BID;
    private Bid MIN_IMPORTANCE_BID;
    private double OPPONENT_MAX_IMPORTANCE;
    private double OPPONENT_MIN_IMPORTANCE;
    private Bid receivedOfferBid;
    private Bid initialOpponentBid = null;
    private double lastBidValue;
    private double reservationImportanceRatio;

    private boolean initialTimePass = false;
    private double ratioBase;
    private Boolean collectingOpponentOffer = true;
    private int numberOfOffers = 0;
    private int numberOfIssues = 0;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        // 创建 空的 我的importance map 以及对手的 value map
        this.impMap = new ImpMap(userModel);
        this.opponentImpMap = new ImpMap(userModel);

        // 我的importance map
        this.impMap.self_update(userModel);

        // 获取最大、最小、中位数bid
        this.getMaxAndMinBid();
        this.getMedianBid();

        // 获取issue数量
        this.numberOfIssues = this.getDomain().getIssues().size();

        // 获取reservation value，折算为importance的百分比
        this.reservationImportanceRatio = this.getReservationRatio();

        System.out.println("reservation ratio: " + this.reservationImportanceRatio);
        System.out.println("my max importance bid: " + this.MAX_IMPORTANCE_BID);
        System.out.println("my max importance: " + this.MAX_IMPORTANCE);
        System.out.println("my min importance bid: " + this.MIN_IMPORTANCE_BID);
        System.out.println("my min importance: " + this.MIN_IMPORTANCE);
        System.out.println("my median importance: " + this.MEDIAN_IMPORTANCE);
        System.out.println("Agent " + this.getPartyId() + " has finished initialization");
    }

    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        double time = getTimeLine().getTime();

        if (!(getLastReceivedAction() instanceof Offer)) {
            return new Offer(getPartyId(), this.MAX_IMPORTANCE_BID);
        }
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
                this.offerLowerRatio = 0.98;
            } else {
                // 0.08~0.32时间内保持高报价
                this.offerLowerRatio = 0.98 - 1.2 * (time - 0.08) * (time - 0.08) - 17. / 375. * (time - 0.08);
            }
        } else {
            // 在0.32时，运行一下计算
            if (this.collectingOpponentOffer) {
                this.collectingOpponentOffer = false;
                System.out.println("\n\nmy sorted importance map: ");
                for (List<impUnit> impUnitList : this.impMap.values()) {
                    System.out.println(impUnitList);
                }

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
                this.ratioBase = 0.6 + 0.4 * (bestOpponentBidImportance - this.MIN_IMPORTANCE) / (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE);
                System.out.println("\nbestOpponentBid ratio: " + this.ratioBase);

                // 更新对手最大、最小bid
                this.getOpponentMaxAndMinBid();
            }
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
            } else {
                // 在最后50轮内收集对手给的最好的报价并接受
                if (impMap.getImportance(receivedOfferBid) > 0.95 * this.lastBidValue) {
                    System.out.println("compromise2");
                    return new Accept(getPartyId(), receivedOfferBid);
                }
            }

        }
        double offerUpperRatio = this.offerLowerRatio + 0.08;

        Bid bid = getNeededRandomBid(this.offerLowerRatio, offerUpperRatio);
        return new Offer(getPartyId(), bid);
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

    private double getReservationRatio() {
        double medianBidRatio = (this.MEDIAN_IMPORTANCE - this.MIN_IMPORTANCE) / (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE);
        return this.utilitySpace.getReservationValue() * medianBidRatio / 0.5;
    }

    /**
     * 获取最大、最小importance的值及对应offer
     */
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
        this.MIN_IMPORTANCE_BID = new Bid(this.getDomain(), lValues2);
        this.MAX_IMPORTANCE = this.impMap.getImportance(this.MAX_IMPORTANCE_BID);
        this.MIN_IMPORTANCE = this.impMap.getImportance(this.MIN_IMPORTANCE_BID);
    }


    /**
     * 获取bid ranking 中的中位数bid对应的importance值
     */
    private void getMedianBid() {
        int median = (this.userModel.getBidRanking().getSize() - 1) / 2;
        int median2 = -1;
        if (this.userModel.getBidRanking().getSize() % 2 == 0) {
            median2 = median + 1;
        }
        int current = 0;
        for (Bid bid : this.userModel.getBidRanking()) {
            current += 1;
            if (current == median) {
                this.MEDIAN_IMPORTANCE = this.impMap.getImportance(bid);
                if (median2 == -1) break;
            }
            if (current == median2) {
                this.MEDIAN_IMPORTANCE += this.impMap.getImportance(bid);
                break;
            }
        }
        if (median2 != -1) this.MEDIAN_IMPORTANCE /= 2;
    }

    /**
     * 更新对手的最大及最小Importance的值及对应OFFER
     */
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


    /**
     * 获取符合条件的随机bid。随机生成k个bid，选取其中在阈值范围内的bids，返回其中对手importance最高的一个bid。
     *
     * @param lowerRatio 生成随机bid的importance下限
     * @param upperRatio 生成随机bid的importance上限
     * @return Bid
     */
    private Bid getNeededRandomBid(double lowerRatio, double upperRatio) {
        double lowerThreshold = lowerRatio * (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE) + this.MIN_IMPORTANCE;
        double upperThreshold = upperRatio * (this.MAX_IMPORTANCE - this.MIN_IMPORTANCE) + this.MIN_IMPORTANCE;
        for (int t = 0; t < 3; t++) {
            long k = this.getDomain().getNumberOfPossibleBids();
            double highest_opponent_importance = 0;
            Bid returnedBid = null;
            for (int i = 0; i < k; i++) {
                Bid bid = generateRandomBid();
                double bidImportance = this.impMap.getImportance(bid);
                double bidOpponentImportacne = this.opponentImpMap.getImportance(bid);
                if (bidImportance >= lowerThreshold && bidImportance <= upperThreshold) {
                    if (bidOpponentImportacne > highest_opponent_importance) {
                        highest_opponent_importance = bidOpponentImportacne;
                        returnedBid = bid;
                    }
                }
            }
            if (returnedBid != null) {
                return returnedBid;
            }
        }
        return MAX_IMPORTANCE_BID;
    }


}