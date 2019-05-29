package group17;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.IssueDiscrete;
import genius.core.issue.Value;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;
import genius.core.uncertainty.UserModel;

import java.util.*;

public class Agent17 extends AbstractNegotiationParty {

    private ImpMap impMap;
    private ImpMap oppoImpMap;
    double myLowerThreshold = 0.9;
    double myUpperThreshold = 1.0;
    private double MAX_IMPORTANCE;
    private double MIN_IMPORTANCE;
    private Bid MAX_IMPORTANCE_BID;
    private Bid MIN_IMPORTANCE_BID;
    private Bid receivedOfferBid;
    private double numberOfIssues;

    //初始化Agent
    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        /* -------------------importance表格-----------------------
        格式：
                        impUnit1                        impUnit2
         issue1     [value，权重之和，次数，平均权重]  [............] ...
        示例：
                        impUnit1                        impUnit2
         水果         [apple，4，2，2]               [............]   ...
        */
        this.impMap = new ImpMap(userModel);
        this.oppoImpMap = new ImpMap(userModel);

        /*------------------------------------------------------------------------
          ---------------------------我的importance表-----------------------------
          ------------------------------------------------------------------------*/
        this.impMap.update(userModel);
        //System.out.println("sorted importance map(my): " + impMap);
        //获取最大Importance的值及对应OFFER
        HashMap<Integer, Value> lValues1 = new HashMap<>();
        HashMap<Integer, Value> lValues2 = new HashMap<>();
        int issueNumber;
        for (Map.Entry<Issue, List<impUnit>> entry : impMap.entrySet()) {
            Value value1 = entry.getValue().get(0).valueOfIssue;
            Value value2 = entry.getValue().get(entry.getValue().size() - 1).valueOfIssue;
            issueNumber = entry.getKey().getNumber();
            lValues1.put(issueNumber, value1);
            lValues2.put(issueNumber, value2);
        }
        MAX_IMPORTANCE_BID = new Bid(this.getDomain(), lValues1);
        MIN_IMPORTANCE_BID = new Bid(this.getDomain(), lValues2);
        MAX_IMPORTANCE = impMap.getImportance(MAX_IMPORTANCE_BID);
        MIN_IMPORTANCE = impMap.getImportance(MIN_IMPORTANCE_BID);
        System.out.println("Agent " + this.getPartyId() + " has finished initialization");

        numberOfIssues = (double) this.getDomain().getIssues().size();
    }

    //报价 or 接受 策略
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        double time = getTimeLine().getTime();
        double oppoBidWeight = 1 - time;
        double oppoMaxImportance;
        double oppoThreshold = 0.0;

        if (getLastReceivedAction() instanceof Offer) {
            /*--------------------------------------------------------
            ----------------------对手importance表---------------------
            ----------------------------------------------------------*/
            //更新权和、次数
            for (Issue issue : receivedOfferBid.getIssues()) {
                int no = issue.getNumber();
                List<impUnit> currentIssueList = oppoImpMap.get(issue);
                for (impUnit currentUnit : currentIssueList) {
                    if (currentUnit.valueOfIssue.toString().equals(receivedOfferBid.getValue(no).toString())) {
                        currentUnit.meanWeightSum += oppoBidWeight;
                    }
                }
            }
            //排序 TODO 可优化 priority queue
            for (List<impUnit> impUnitList : oppoImpMap.values()) {
                Collections.sort(impUnitList, new meanWeightSumComparator());
            }
            //System.out.println("sorted importance map(oppo): " + oppoImpMap);
            //获取最大Importance的值及对应OFFER
            HashMap<Integer, Value> lValues = new HashMap<>();
            int issueNumber;
            for (Map.Entry<Issue, List<impUnit>> entry : oppoImpMap.entrySet()) {
                Value value = entry.getValue().get(0).valueOfIssue;
                issueNumber = entry.getKey().getNumber();
                lValues.put(issueNumber, value);
            }
            Bid oppoMaxImportanceBid = new Bid(this.getDomain(), lValues);
            oppoMaxImportance = oppoImpMap.getImportance(oppoMaxImportanceBid);
            //System.out.println("best bid of opponent: " + oppoMaxImportanceBid);
            //System.out.println("max Importance of opponent: " + oppoMaxImportance);

            /*--------------------------------------------------------
            -------------------------正文-----------------------------
            ----------------------------------------------------------*/
            double importance = impMap.getImportance(receivedOfferBid);
            //若高于threshold，则接受
            if (importance > myLowerThreshold * (MAX_IMPORTANCE - MIN_IMPORTANCE) + MIN_IMPORTANCE) {
                System.out.println("accepted agent: Agent" + this.getPartyId());
                System.out.println("my sorted importance map: " + impMap);
                System.out.println("my best bid: " + MAX_IMPORTANCE_BID);
                System.out.println("my max importance: " + MAX_IMPORTANCE);
                System.out.println("opponent's sorted importance map: " + oppoImpMap);
                System.out.println("opponent's best bid: " + oppoMaxImportanceBid);
                System.out.println("opponent's max importance: " + oppoMaxImportance);
                System.out.println("last bid: " + receivedOfferBid);
                System.out.println(" ");
                return new Accept(this.getPartyId(), receivedOfferBid);
            } else {
                if (time < 0.3) {
                    myLowerThreshold = 0.94 - 0.04 / 0.3 * time;
                    oppoThreshold = 0;
                } else if (time < 0.93) {
                    myLowerThreshold = 0.94 - 0.443 * (time - 0.3);
                    oppoThreshold = 0.38 + 0.41 * (0.3 / 2. * (time - 0.3) * (time - 0.3) + (time - 0.3)) - 1.0 / numberOfIssues;
                } else if (time < 0.992) {
                    //回调
                    myLowerThreshold = 0.63 + 0.443 * (time - 0.3);
                    oppoThreshold = 0.8 - 1.0 / numberOfIssues;
                } else {  //妥协
                    myLowerThreshold = 0.0;
                    myUpperThreshold = 1.0;
                    oppoThreshold = 0;
                }

                if (time <= 0.9992) {
                    myUpperThreshold = myLowerThreshold + 0.1;
                }
                //随机给出一个高于两个threshold的值
                Bid bid = getNeededRandomBid(myLowerThreshold * (MAX_IMPORTANCE - MIN_IMPORTANCE) + MIN_IMPORTANCE, myUpperThreshold * (MAX_IMPORTANCE - MIN_IMPORTANCE) + MIN_IMPORTANCE, oppoThreshold * oppoMaxImportance);
                return new Offer(getPartyId(), bid);
            }
        }
        return new Offer(getPartyId(), MAX_IMPORTANCE_BID);
    }

    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);
        if (act instanceof Offer) {
            Offer offer = (Offer) act;
            receivedOfferBid = offer.getBid();
        }
    }

    @Override
    public String getDescription() {
        return "Good Game";
    }

    //importance单元
    public class impUnit {
        public Value valueOfIssue;
        public int weightSum = 0;
        public int count = 0;
        public double meanWeightSum = 0.0f;

        public impUnit(Value value) {
            this.valueOfIssue = value;
        }

        public String toString() {
            return String.format("%s %d %d %f", valueOfIssue, weightSum, count, meanWeightSum);
        }
    }

    //重写comparator接口
    static class meanWeightSumComparator implements Comparator<impUnit> {
        public int compare(impUnit o1, impUnit o2) {// 实现接口中的方法
            if (o1.meanWeightSum < o2.meanWeightSum) {
                return 1;
            } else if (o1.meanWeightSum > o2.meanWeightSum) {
                return -1;
            }
            return 0;
        }
    }

    public class ImpMap extends HashMap<Issue, List<impUnit>> {
        public ImpMap(UserModel userModel) {
            super();
            //遍历userModel中的issue，创建空importance表格
            for (Issue issue : userModel.getDomain().getIssues()) {
                IssueDiscrete temp = (IssueDiscrete) issue;
                List<impUnit> issueImpUnit = new ArrayList<>();
                int numberInIssue = temp.getNumberOfValues();
                for (int i = 0; i < numberInIssue; i++) {
                    issueImpUnit.add(new impUnit(temp.getValue(i)));
                }
                this.put(issue, issueImpUnit);
            }
        }

        public void update(UserModel userModel) {
            //遍历已知bidOrder，更新importance表格中的“权和”、“次数”
            int currentWeight = 0;
            for (Bid bid : userModel.getBidRanking().getBidOrder()) {
                currentWeight += 1;
                List<Issue> issueList = bid.getIssues();
                for (Issue issue : issueList) {
                    int no = issue.getNumber();
                    List<impUnit> currentIssueList = impMap.get(issue);
                    for (impUnit currentUnit : currentIssueList) {
                        if (currentUnit.valueOfIssue.toString().equals(bid.getValue(no).toString())) {
                            currentUnit.weightSum += currentWeight;
                            currentUnit.count += 1;
                        }
                    }
                }
            }
            //计算权重
            int i = 0;
            for (List<impUnit> impUnitList : impMap.values()) {
                for (impUnit currentUnit : impUnitList) {
                    if (currentUnit.count == 0) {
                        currentUnit.meanWeightSum = 0.0;
                    } else {
                        currentUnit.meanWeightSum = (double) currentUnit.weightSum / (double) currentUnit.count;
                    }
                }
            }
            //排序
            for (List<impUnit> impUnitList : impMap.values()) {
                impUnitList.sort(new meanWeightSumComparator());
            }
        }

        //获取某个value在List中的重要性值
        public double getValueImportance(Value value, List<impUnit> impUnits) {
            double valueImportance = 0.0;
            for (impUnit i : impUnits) {
                if (i.valueOfIssue.equals(value)) {
                    valueImportance = i.meanWeightSum;
                }
            }
            return valueImportance;
        }

        //计算某个bid对应的importance值
        public double getImportance(Bid bid) {
            double bidImportance = 0.0;
            double valueImportance = 0.0; // ? not used
            for (Issue issue : bid.getIssues()) {
                Value value = bid.getValue(issue.getNumber());
                valueImportance = getValueImportance(value, this.get(issue));
                bidImportance += valueImportance;
            }
            return bidImportance;
        }
    }


    //随机生成高于我与对手threshold的bid
    public Bid getNeededRandomBid(double myLowerThreshold, double myUpperThreshold, double oppoThreshold) {
        long k = this.getDomain().getNumberOfPossibleBids();
        List<Bid> myBid = new ArrayList<>();
        while (true) {
            k = k - 1;
            Bid bid = generateRandomBid();
            double myImp = impMap.getImportance(bid);
            double oppoImp = oppoImpMap.getImportance(bid);
            //  在有限次内找寻满足条件的bid
            //  如果找不到，降低oppThreshold再找
            //  如果再找不到，就仅返回大于我的threshold的bid
            //  再找不到，就返回最大bid
            if (k > 0) {
                if (myImp >= myLowerThreshold && myImp <= myUpperThreshold) {
                    myBid.add(bid);
                    if (oppoImp >= oppoThreshold) {
                        return bid;
                    }
                }
            } else {
                if (myBid.isEmpty()) {
                    return MAX_IMPORTANCE_BID;
                } else {  //逐步降低oppoThreshold
                    for (int i = 1; i <= 5; i++) {
                        for (Bid mb : myBid) {
                            if (oppoImp >= oppoThreshold - 0.03 * i) {
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
