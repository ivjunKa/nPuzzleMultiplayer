package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Iv on 26-1-2017.
 */
public class InGameActions {
    private FirebaseDatabase database;
    //adding reference to table
    private DatabaseReference myRef;
    private String hostName;
    private int voteTotal;
    public Vote shuffleVote;
    public Vote difficultyVote;
    public HashMap<String,Integer> votes;
    public InGameActions(String hostName){
        this.hostName = hostName;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        voteTotal = 0;
        votes = new HashMap<String,Integer>();
        votes.put("shuffle", 0);
        votes.put("changeDiff", 0);
    }
    public void vote(boolean vote, String actionType) {

        int voteTotal = getVoteTotal(actionType);
        if(vote){
            voteTotal++;
        }
        else {
            voteTotal = 0;
            myRef.child("users").child(hostName).child("player_actions").setValue("none");
        }
        setVoteTotal(voteTotal,actionType);
        setActionTypeInDatabase(actionType);
    }
    public void setActionTypeInDatabase(String actionType){
        myRef.child("users").child(hostName).child("inGameActions").child(actionType).setValue(getVoteTotal(actionType));
    }
    public void setVoteTotal(int voteTotal, String actionType){
        for ( Map.Entry<String, Integer> entry : votes.entrySet()) {
            String key = entry.getKey();
            if(key.equalsIgnoreCase(actionType)){
                entry.setValue(voteTotal);
            }
        }
    }
    public int getVoteTotal(String actionType){
        for ( Map.Entry<String, Integer> entry : votes.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if(key.equalsIgnoreCase(actionType)){
                return value;
            }
        }
        return 0;
    }
    public HashMap getVotes(){
        return votes;
    }
}
