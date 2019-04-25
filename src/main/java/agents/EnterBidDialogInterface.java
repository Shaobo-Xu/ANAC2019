package agents;

import com.sun.media.jfxmediaimpl.MediaDisposer;

import genius.core.Bid;
import genius.core.actions.Action;

public interface EnterBidDialogInterface extends MediaDisposer.Disposable
{
    genius.core.actions.Action askUserForAction(Action opponentAction, Bid myPreviousBid);
}
