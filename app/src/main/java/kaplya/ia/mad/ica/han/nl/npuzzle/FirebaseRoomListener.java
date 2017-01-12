package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Iv on 12-1-2017.
 */
public class FirebaseRoomListener {
    public static String guestName;
    FirebaseRoomListener(String hostName, String guestName){
        Log.d("FirebaseRoomListener", "Firebase room listener has been created");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //adding reference to table
        final DatabaseReference myRef = database.getInstance().getReference();
        myRef.child(hostName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(FirebaseRoomListener.guestName!= null){
                    GameActivity.imageName = dataSnapshot.child("imgname").getValue().toString();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        myRef.child("users").child("siv").child("ready").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.getValue() == "true") {
                    Log.d("FirebaseRoomListener", "Guest has entered the room");
                    //GameActivity.imageName = dataSnapshot.child("imgname").getValue().toString();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}
