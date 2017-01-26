package kaplya.ia.mad.ica.han.nl.npuzzle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Iv on 26-1-2017.
 */
public class VoteController {
    public String voteType;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String hostName;
    private String myType;
    public VoteController(String myType){
        database = FirebaseDatabase.getInstance();
        myRef = database.getInstance().getReference();
        this.myType = myType;
        myRef.child("users").child(GameActivity.hostName).child("inGameActions").child(myType).setValue(false);
    }
    public void vote(Boolean vote){
        //if true - set my name in vote actions to true
        if(vote){
            myRef.child("users").child(GameActivity.hostName).child("inGameActions").child(myType).setValue(true);
        }
        else{
            //myRef.child("users").child(GameActivity.hostName).child("inGameActions").child(myType).setValue(false);
            myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("none");
        }
        //if false - set my name to false and close vote
    }
}
