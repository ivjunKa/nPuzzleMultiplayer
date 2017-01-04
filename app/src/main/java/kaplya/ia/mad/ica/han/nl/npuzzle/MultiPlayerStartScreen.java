package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 3-1-2017.
 */
public class MultiPlayerStartScreen extends ActionBarActivity {
    EditText userName;
    public ListView mListView;
    String[] games_array = new String[] {};
    public static List<String> games_list;
    public static ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_game_list);
        initDatabase();
        mListView = (ListView) findViewById(R.id.list_avialable_games);
        userName   = (EditText)findViewById(R.id.user_name_provider);
        // Initializing a new String Array
        // Create a List from String Array elements
        games_list = new ArrayList<String>(Arrays.asList(games_array));
        // Create an ArrayAdapter from List
        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, games_list);
        mListView.setAdapter(arrayAdapter);
        Button addNewGameButton = (Button)findViewById(R.id.button_new_game);
        addNewGameButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MultiPlayerStartScreen.this, MainActivity.class);
                intent.putExtra("multiplayer", true);
                intent.putExtra("username", userName.getText().toString());
                startActivity(intent);
            }
        });
    }
    //these needs to happends automatically with data retrieved from the database
    public static void updateList(String value){
        //Log.d("MultiplayerStartScreen", value);
        games_list.add(value);
        arrayAdapter.notifyDataSetChanged();
    }
    public static void refreshData(){

    }
    public void initDatabase(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //adding reference to table
        final DatabaseReference myRef = database.getReference("npuzzlemultiplayer");
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.hasChildren()){
                    Iterable<DataSnapshot> data = dataSnapshot.getChildren();
                    Iterator it = data.iterator();
                    while(it.hasNext()){
                        Log.d("MultiPlayerStartScreen",it.next().toString());
                    }
                }
                else{
                    Log.d("MultiPlayerStartScreen", "The dataSnapshot has no childrens");
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
