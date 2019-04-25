 package agents.anac.y2014.BraveCat.OpponentModelStrategies;

 import agents.anac.y2014.BraveCat.OpponentModels.OpponentModel;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import agents.anac.y2014.BraveCat.necessaryClasses.NegotiationSession;
import genius.core.bidding.BidDetails;
 
 public class NullStrategy extends OMStrategy
 {
   private Random rand;
   private double updateThreshold = 1.1D;
 
   public NullStrategy()
   {
   }
 
   public NullStrategy(NegotiationSession negotiationSession)
   {
     this.negotiationSession = negotiationSession;
     this.rand = new Random();
   }
 
   @Override
   public void init(NegotiationSession negotiationSession, OpponentModel model, HashMap<String, Double> parameters) throws Exception {
     super.init(negotiationSession, model);
     this.rand = new Random();
     this.negotiationSession = negotiationSession;
     if (parameters.containsKey("t"))
       this.updateThreshold = ((Double)parameters.get("t")).doubleValue();
   }
 
   @Override
   public BidDetails getBid(List<BidDetails> allBids)
   {
     return (BidDetails)allBids.get(0);
   }
 
   @Override
   public boolean canUpdateOM()
   {
     return this.negotiationSession.getTime() < this.updateThreshold;
   }
 }