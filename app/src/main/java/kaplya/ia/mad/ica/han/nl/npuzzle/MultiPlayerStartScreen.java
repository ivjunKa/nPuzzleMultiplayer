package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 3-1-2017.
 */
public class MultiPlayerStartScreen extends ActionBarActivity {
    private EditText userNameField;
    public ListView mListView;
    String[] games_array = new String[] {};
    public static List<String> games_list;
    public static ArrayAdapter<String> arrayAdapter;
    private String selectedHostName = null;
    private String userName = null;
    private ChildEventListener listListener = null;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private GPSTracker gpsTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_game_list);
        gpsTracker = new GPSTracker(MultiPlayerStartScreen.this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getInstance().getReference();

        initDatabase();
        mListView = (ListView) findViewById(R.id.list_avialable_games);
        userNameField   = (EditText)findViewById(R.id.user_name_provider);
        // Initializing a new String Array
        // Create a List from String Array elements
        games_list = new ArrayList<String>(Arrays.asList(games_array));
        // Create an ArrayAdapter from List
        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, games_list);
        mListView.setAdapter(arrayAdapter);
        addGameSelectorToGameList();
        Button addNewGameButton = (Button)findViewById(R.id.button_new_game);
        Intent intent = getIntent();
        //Moet voor initDatabase() gebeuren want dan wordt listener niet 2 keer uitgevoerd
        if(intent.hasExtra("gameOver")){
            myRef.child("users").child(intent.getStringExtra("instanceName")). removeValue();
        }
        //firebaseRoomListener = new FirebaseRoomListener("siv",null);
        addNewGameButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = userNameField.getText().toString();
                Log.d("MultiplayerStartScreen", "UserName is: " + userName.length());
                if(userName.length() == 0){
                    new AlertDialog.Builder(MultiPlayerStartScreen.this)
                            .setTitle("No host name is specified")
                            .setMessage("Specify host name")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else{
                    Intent intent = new Intent(MultiPlayerStartScreen.this, MainActivity.class);
                    intent.putExtra("multiplayer", true);
                    intent.putExtra("username", userName);
                    intent.putExtra("selectedHostName", userName);
                    //finish();
                    //intent.setFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }

            }
        });
        Button joinGame = (Button)findViewById(R.id.button_join_game);
        joinGame.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedHostName == null){
                    new AlertDialog.Builder(MultiPlayerStartScreen.this)
                            .setTitle("No room specified")
                            .setMessage("Choose the room name")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else{
                    Intent intent = new Intent(MultiPlayerStartScreen.this, GameActivity.class);
                    //Intent intent = new Intent(MultiPlayerStartScreen.this, GameActivity.class);
                    //Log.d("MultiplayerStartScreen", "Context is : " + MultiPlayerStartScreen.this.toString());
                    //Log.d("MultiplayerStartScreen", "intent is : " + intent.toString());
                    //firebaseRoomListener.setContext(MultiPlayerStartScreen.this, intent);
                    intent.putExtra("username", userName);
                    intent.putExtra("selectedHostName", selectedHostName);
                    //intent.setFlags(intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    myRef.child("users").child(selectedHostName).child("status").setValue("ready_to_play");

                }
            }
        });
    }
    //these needs to happends automatically with data retrieved from the database
    public static void updateList(String value){
        //Log.d("MultiplayerStartScreen", value);
        games_list.add(value);
        arrayAdapter.notifyDataSetChanged();
    }
    public void initDatabase() {
        Log.d("MultiplayerStartScreen", "Init database");
        if(listListener == null){
            Log.d("MultiplayerStartScreen", "Creating listener");
            listListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.child("location").hasChild("lat") && dataSnapshot.child("location").hasChild("lon")) {
                        String lon = dataSnapshot.child("location").child("lon").getValue().toString();
                        String lat = dataSnapshot.child("location").child("lat").getValue().toString();
                        Double distance = Math.pow((Double.parseDouble(lon) - gpsTracker.getLongitude()), 2) + Math.pow((Double.parseDouble(lat) - gpsTracker.getLatitude()),2);
                        if(distance<10.0) {
                            updateList(dataSnapshot.getKey().toString());
                        }
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //Log.d("MultiplayerStartScreen", "Coords of known hosts:" + dataSnapshot.child("location").child("lat").getValue());
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                        for(int i = 0; i<games_list.size(); i++){
                            if(games_list.get(i).equalsIgnoreCase(dataSnapshot.getKey())){
                                games_list.remove(i);
                                arrayAdapter.notifyDataSetChanged();
                            }
                        }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            myRef.child("users").addChildEventListener(listListener);
        }
    }
    public void addGameSelectorToGameList(){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("MultiplayerStartScreen", "You choose player: " + games_list.get(i));
                Log.d("MultiplayerStartScreen", "List length is: " + adapterView.getChildCount());

                for (int j = 0; j < adapterView.getChildCount(); j++) {
                    adapterView.getChildAt(j).setBackgroundColor(Color.parseColor("#FAFAFA"));
                }
                view.setBackgroundColor(Color.parseColor("#00ff00"));
                selectedHostName = games_list.get(i);
            }
        });
    }

    public void onResume(Bundle savedInstanceState){
        super.onResume();
        Log.d("MultiplayerStartScreen", "Activity resumed");
    }
//    protected void onDestroy(){
//        super.onDestroy();
//        Log.d("MultiplayerStartScreen", "Activity destroyed");
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        //adding reference to table
//        final DatabaseReference myRef = database.getReference();
//        myRef.child("users").removeEventListener(listListener);
//    }
}
