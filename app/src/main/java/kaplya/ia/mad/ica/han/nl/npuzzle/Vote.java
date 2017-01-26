package kaplya.ia.mad.ica.han.nl.npuzzle;

/**
 * Created by Iv on 26-1-2017.
 */
public class Vote {
    public String actionType;
    public int voteTotal;
    public Vote(String actionType, int voteTotal){
        this.actionType = actionType;
        this.voteTotal = voteTotal;
    }
    public String getActionType(){
        return this.actionType;
    }
    public int getVoteTotal(){
        return this.voteTotal;
    }
}
