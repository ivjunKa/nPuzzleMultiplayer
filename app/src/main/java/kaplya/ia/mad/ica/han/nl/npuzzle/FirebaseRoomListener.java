package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Iv on 12-1-2017.
 */
public class FirebaseRoomListener{
    public static String guestName;
    public Intent intent;
    private Context context;
    private boolean playing = false;
    FirebaseRoomListener(String hostName, String guestName){
        Log.d("FirebaseRoomListener", "Firebase room listener has been created");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //adding reference to table
        final DatabaseReference myRef = database.getInstance().getReference();
        myRef.child(hostName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

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
//        myRef.child("users").child("siv").addChildEventListener(new ChildEventListener() {
//
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                //Log.d("FirebaseRoomListener", "On child added:  " + dataSnapshot.toString());
//                //Log.d("FirebaseRoomListener", "Total childrens:" + dataSnapshot.getChildrenCount());
//                //for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//
//                //}
//                //intent.putExtra("chunksTotal", 16);
//                //intent.putExtra("drawableName", "dog");
//                //Log.d("FirebaseRoomListener", "Child image name is " + dataSnapshot.child("imgname").getValue().toString());
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                //Log.d("FirebaseRoomListener", "On child changed: " + dataSnapshot.toString());
////                if (dataSnapshot.getKey().toString().equalsIgnoreCase("status") &&
////                        dataSnapshot.getValue().toString().equalsIgnoreCase("ready_to_play")) {
////                    Log.d("FirebaseRoomListener", "Something was changed" + s);
////                    context.startActivity(intent);
////                }
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });


        myRef.child("users").child("siv").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if(dataSnapshot.hasChild("status") && dataSnapshot.child("status").getValue().toString().equalsIgnoreCase("ready_to_play")){
                        //here we can init the game for quest
                    //GameActivity.initThisGame((Integer) dataSnapshot.child("difficulty").getValue(), dataSnapshot.child("imgname").getValue().toString());
                }
//                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Log.d("FirebaseRoomListener", snapshot.getKey().toString());
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setContext(Context context, Intent intent){
        this.intent = intent;
        this.context = context;
    }
}
