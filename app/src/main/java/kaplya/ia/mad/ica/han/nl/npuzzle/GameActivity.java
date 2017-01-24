package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 3-5-2015.
 */
public class GameActivity extends ActionBarActivity {
    //private ArrayList<Bitmap> chunked = null;
    private ArrayList<Bitmap> newChunked = null;

    private static Context mContext;
    private PuzzleAdapter adapter = null;
    public static String imageName;
    public static ImageView view;
    public int difficulty;

    public static TextView turnIndicator;
    public static Button sendHint;
    public Button resolve;
    public static int hintValue;
    public static boolean hintGiven = false;
    public static String hostName;
    public String playerType;
    private GridView grid;
    private ValueEventListener statusEventListener;

    private FirebaseDatabase database;
    //adding reference to table
    private DatabaseReference myRef;

    @Override
    //this was totally changed, instead of using extern firebase room listener i`m using listener in this class
    //so now we both need to connect and THEN we going to init the game
    //it must be working on the same principle as the multiplayer start screen list because we have
    //also an adapter and an gridview(listview in multiplayer screen)
    //and while host is waiting at guest i`ll place an image like placeholder at the gridview
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        setContentView(R.layout.activity_game);
        mContext = this.getApplicationContext();
        turnIndicator = (TextView)findViewById(R.id.turnIndicator);
        sendHint = (Button)findViewById(R.id.sendHintButton);

        sendHint.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hintGiven) {
                    adapter.notifyOpponent();
                } else {
                    Log.d("GameActivity", "No hint was chosen, choose hint first");
                }
            }
        });
        Intent intent = getIntent();
        playerType = intent.hasExtra("host")? "host":"guest";
        hostName = intent.getStringExtra("selectedHostName");
        Log.d("GameActivity", "Host name is: " + hostName);
        initDatabase();

        resolve = (Button)findViewById(R.id.resolveButton);
        resolve.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.resolvePuzzle();
            }
        });
        sendHint.setVisibility(View.GONE);
        resolve.setVisibility(View.GONE);
    }
    private void initGame(){
        view = getCustomImageView(imageName);
        newChunked = splitImage(view, difficulty);

        ArrayList<Tile> tiles = new ArrayList<Tile>();
        setBlankTile(newChunked, tiles);
        grid = (GridView)findViewById(R.id.gridView);

        adapter = new PuzzleAdapter(this,difficulty, tiles, playerType);

        grid.setAdapter(adapter);
        grid.setNumColumns((int) Math.sqrt(newChunked.size()));
        resolve.setVisibility(View.VISIBLE);
    }
//    public static void initThisGame(int difficulty, String imageName){
//        ArrayList<Tile> tiles = new ArrayList<Tile>();
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_in_game, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        switch(id){
            case R.id.menu_easy:
                resetDiff(9);
                break;
            case R.id.menu_normal:
                resetDiff(16);
                break;
            case R.id.menu_hard:
                resetDiff(25);
                break;
            case R.id.action_quit:
                Intent intent = new Intent(GameActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.action_shuffle:
                Log.d("Puzzle", "SHUFFLE");
                adapter.shuffleTiles();
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
    public static void gotoWin(Context c){
        Intent intent = new Intent(c, WinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("stepsCount",PuzzleAdapter.getStepsCount());
        //intent.putExtra("imgSolved", imgResource);
        intent.putExtra("imgSolved", imageName);

        c.startActivity(intent);
    }
    public static Context getAppContext(){
        return mContext;
    }

    private void resetDiff(int difficulty){
        //this is how we can pass the resource id to the imageview
        ImageView newImg = getCustomImageView(imageName);
        //newImg.setImageResource(imgResource);
        ArrayList<Bitmap> newChunked = splitImage(newImg, difficulty);
        //initGame(difficulty,newChunked);
    }
    private void setBlankTile(ArrayList<Bitmap> chunked,ArrayList<Tile> tiles ){
        Bitmap darkTile = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.darktile);
        for(int i=0; i< chunked.size(); i++){
            Tile tile = null;
            if(i == chunked.size()-1){
                tile = new Tile(i,darkTile,true);
            }
            else {
                tile = new Tile(i, chunked.get(i), false);
            }
            tiles.add(tile);
        }
    }



    //replace existing method to chunk images from MainActivity to this GameActivity class so we can obtain image from the database
    public static ArrayList<Bitmap> splitImage(ImageView image,int chunkNumbers) {

        //For the number of rows and columns of the grid to be displayed
        int rows, cols;

        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        ArrayList<Bitmap> chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        //Getting the scaled bitmap of the source image
        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;

        //xCoord and yCoord are the pixel positions of the image chunks
        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }
        return chunkedImages;
    }
    public ImageView getCustomImageView(String filename){
        String fnm = filename; //  this is image file name
        String PACKAGE_NAME = getApplicationContext().getPackageName();
        int imgId = getResources().getIdentifier(PACKAGE_NAME + ":drawable/" + fnm, null, null);
        System.out.println("IMG ID :: "+imgId);
        System.out.println("PACKAGE_NAME :: " + PACKAGE_NAME);
//    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),imgId);
        ImageView view = new ImageView(this);
        view.setImageBitmap(BitmapFactory.decodeResource(getResources(), imgId));
        return view;
    }
    public void initDatabase(){
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        //adding reference to table
//        final DatabaseReference myRef = database.getReference();
        myRef.child("users").child(hostName).child("hintValue").setValue(0);
        statusEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("GameActivity", "This is our changed data: " + dataSnapshot.getValue().toString());
                if (dataSnapshot.hasChild("status") && dataSnapshot.child("status").getValue().toString().equalsIgnoreCase("ready_to_play")) {
                    //here we can init the game for everyone
                    imageName = dataSnapshot.child("imgname").getValue().toString();
                    Long diff = (Long) dataSnapshot.child("difficulty").getValue();
                    difficulty = diff.intValue();
                    myRef.child("users").child(hostName).child("status").setValue("playing");
                    initGame();
                }
                else if(dataSnapshot.hasChild("status") && dataSnapshot.child("status").getValue().toString().equalsIgnoreCase("game_over")){

                    new AlertDialog.Builder(GameActivity.this)
                            .setTitle("The game is over")
                            .setMessage("Game over, you will be redirected back to main page")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    myRef.child("users").child(hostName).child("status").setValue("game_over_go_back");

                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else if(dataSnapshot.hasChild("status") && dataSnapshot.child("status").getValue().toString().equalsIgnoreCase("game_over_go_back")){
                    Log.d("GameActivity", "Ending up game");
                    Intent intent = new Intent(GameActivity.this, MultiPlayerStartScreen.class);
                    intent.putExtra("gameOver","yes");
                    intent.putExtra("instanceName", hostName);
                    myRef.child("users").child(hostName).removeEventListener(statusEventListener);
                    myRef.child("users").child(hostName).child("host").removeEventListener(adapter.getOpponentActionEventListener());
                    myRef.child("users").child(hostName).child("guest").removeEventListener(adapter.getOpponentActionEventListener());
                    //myRef.removeEventListener(adapter.getDarkTileListener());
                    myRef.removeEventListener(adapter.getAllChildsListener());
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        myRef.child("users").child(hostName).addValueEventListener(statusEventListener);
//        myRef.child("users").child(hostName).child("hintValue").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Long tempValue = (Long) dataSnapshot.getValue();
//                hintValue = tempValue.intValue();
//                Log.d("GameActivity", "This is the hint value" + dataSnapshot.getValue());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }
    public static void initTurnIndicator(Boolean turn) {
        String textInTurnIndicator = turn ? "Your turn" : "Opponents turn";
        Log.d("GameActivity", "This is my turn indicator" + turn);
        turnIndicator.setText(textInTurnIndicator);
        if(turn){
            sendHint.setVisibility(View.GONE);
        }
        else {
            sendHint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(GameActivity.this)
                .setTitle("Leave game")
                .setMessage("Do you want to quit the game?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        //adding reference to table
                        final DatabaseReference myRef = database.getReference();
                        myRef.child("users").child(hostName).child("status").setValue("game_over");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
