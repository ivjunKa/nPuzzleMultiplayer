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
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.KeyStore;
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
    private static String dialogTitle;
    private FirebaseDatabase database;
    //adding reference to table
    private DatabaseReference myRef;
    private ChildEventListener allChildsListener;
    public int voteTotal = 0;
    private boolean voting = false;
    @Override
    //this was totally changed, instead of using extern firebase room listener i`m using listener in this class
    //so now we both need to connect and THEN we going to init the game
    //it must be working on the same principle as the multiplayer start screen list because we have
    //also an adapter and an gridview(listview in multiplayer screen)
    //and while host is waiting at guest i`ll place an image like placeholder at the gridview
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SavedInstanceState", "The previous state is: " + savedInstanceState);
        //savedInstanceState.clear();
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
        if(playerType.equalsIgnoreCase("host")){
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0,250,0,0);
            turnIndicator.setLayoutParams(llp);
            turnIndicator.setText("Waiting for opponent.....");
        }
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
        Log.d("AdapterInGame", "Adapter is: " + adapter);
        adapter = new PuzzleAdapter(this,difficulty, tiles, playerType);

        grid.setAdapter(adapter);
        grid.setNumColumns((int) Math.sqrt(newChunked.size()));
        resolve.setVisibility(View.VISIBLE);
        createChildsListenerForSpecifiedUser(hostName, adapter);
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
                //resetDiff(9);
                myRef.child("users").child(GameActivity.hostName).child("player_actions").child("resetDiff").setValue(9);
                //myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("reset_diff_09");
                break;
            case R.id.menu_normal:
                myRef.child("users").child(GameActivity.hostName).child("player_actions").child("resetDiff").setValue(16);
                //myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("reset_diff_16");
                //resetDiff(16);
                break;
            case R.id.menu_hard:
                //resetDiff(25);
                myRef.child("users").child(GameActivity.hostName).child("player_actions").child("resetDiff").setValue(25);
                //myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("reset_diff_25");
                break;
            case R.id.action_quit:
//                Intent intent = new Intent(GameActivity.this,MainActivity.class);
//                startActivity(intent);
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
                break;
            case R.id.action_shuffle:
                Log.d("Puzzle", "SHUFFLE");
                //adapter.shuffleTiles();
                myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("shuffle");
                //adapter.shuffleWhilePlaying();
                break;
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void gotoWin(Context c){
        Intent intent = new Intent(c, WinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("stepsCount",PuzzleAdapter.getStepsCount());
        //intent.putExtra("imgSolved", imgResource);
        //intent.putExtra("imgSolved", imageName);
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
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(0, 0, 0, 0);
                    turnIndicator.setLayoutParams(llp);
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
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("gameOver", "yes");
                    intent.putExtra("instanceName", hostName);
                    cleanGame();
                    startActivity(intent);
                    finish();
                    //Moet voor initDatabase() gebeuren want dan wordt listener niet 2 keer uitgevoerd
                }
                else if(dataSnapshot.hasChild("status") && dataSnapshot.child("status").getValue().toString().equalsIgnoreCase("game_win")){
                    cleanGame();
                    gotoWin(GameActivity.this);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        myRef.child("users").child(hostName).addValueEventListener(statusEventListener);
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

//        new AlertDialog.Builder(GameActivity.this)
//                .setTitle("Leave game")
//                .setMessage("Do you want to quit the game?")
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // continue with delete
//                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//                        //adding reference to table
//                        final DatabaseReference myRef = database.getReference();
//                        myRef.child("users").child(hostName).child("status").setValue("game_over");
//                    }
//                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
//                    }
//                })
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show();
    }
    public void cleanGame(){
        Log.d("GameActivity", "Adapter is" + adapter);

        Log.d("GameActivity", "Trying to remove Status listener...");
        myRef.child("users").child(hostName).removeEventListener(statusEventListener);
        Log.d("GameActivity", "Status listener have been removed");
        if(adapter!=null){
            myRef.child("users").child(hostName).child("host").removeEventListener(adapter.getOpponentActionEventListener());
            myRef.child("users").child(hostName).child("guest").removeEventListener(adapter.getOpponentActionEventListener());
            myRef.child("users").child(hostName).removeEventListener(this.allChildsListener);
        }
        //myRef.removeEventListener(adapter.getDarkTileListener());
        myRef.child("users").child(hostName).removeValue();
    }
    public void createChildsListenerForSpecifiedUser(String userName, final PuzzleAdapter adapter){
        allChildsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                switch (dataSnapshot.getKey()) {
                    case "tileaddr":
                        Log.d("ChildListenerCase", "Tileaddr was changed");
                        Log.d("ChildListenerCase", "Adapter is" + adapter);
                        Log.d("AdapterTilesSize", "Tiles size in tileAddr listener is: " + adapter.getTilesSize() + " For player " + playerType);
                        adapter.handleDBTileArr(dataSnapshot.getValue().toString());
                        break;
                    /**
                     * This is an listener for the hints made by opponent, when hintNotifier column changes to true method notifyHint() will be called
                     * notifyHint() making a specified puzzle element appear and then changing value for of hintNotifier back to false.
                     * When hintNotifier become false method clearHints() will be called. This one clearing all the higlighted puzzles(hints) that
                     * were made.
                     */
                    case "hintNotifier":
                        Log.d("ChildListenerCase", "HintNotifier was changed");
                        if ((Boolean) dataSnapshot.getValue())
                            adapter.notifyHint(hintValue);
                        else
                            adapter.clearHints();
                        break;
                    case "darkTile":
                        Log.d("ChildListenerCase", "DarkTile was changed");
                        if (dataSnapshot.hasChild("darkTileOldPosition") && dataSnapshot.hasChild("darkTileNewPosition")) {
                            Long darkTileOPos = (Long) dataSnapshot.child("darkTileOldPosition").getValue();
                            Long darkTileNPos = (Long) dataSnapshot.child("darkTileNewPosition").getValue();
                            Log.d("AdapterTilesSize", "Tiles size in darkTile listener is: " + adapter.getTilesSize() + " For player " + playerType);
                            adapter.handleDBDarkTileSwapping(darkTileOPos, darkTileNPos);
                            //setTilesFromDatabase(dataSnapshot.child("darkTileOldPosition").getValue().toString(), dataSnapshot.child("darkTileNewPosition").getValue().toString());
                        }
                        break;
                    case "hintValue":
                        Long tempValue = (Long) dataSnapshot.getValue();
                        GameActivity.hintValue = tempValue.intValue();
                        Log.d("GameActivity", "This is the hint value" + dataSnapshot.getValue());
                        break;
                    case "inGameActions":
                        boolean everyoneVotedYes = checkIfEveryoneVotedYes(dataSnapshot);
                        if(everyoneVotedYes){
                            //do shuffle
                            if(adapter.getVoteController().getVoteType().equalsIgnoreCase("shuffle")){
                                adapter.shuffleWhilePlaying();
                            }
                            else{
                                //do change difficulty
                                Log.d("GameActivity", "Diff will be changed");
                                view = getCustomImageView(imageName);

                                newChunked = splitImage(view, difficulty);

                                ArrayList<Tile> tiles = new ArrayList<Tile>();
                                setBlankTile(newChunked, tiles);
                                adapter.reinit(difficulty, tiles);
                                grid.setNumColumns((int) Math.sqrt(newChunked.size()));
                                myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("none");
                            }

                        }
                        //here we need to check if both childs answered yes and if the vote is still active
//                        Log.d("GameActivity", "This is our changed game action: " + dataSnapshot.getKey());
//
//                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//                            Long snapshotValue = (Long) snapshot.getValue();
//                            Log.d("InGameActions", "Snapshot value is " + snapshotValue);
//                            Log.d("InGameActions", "Listener updates both players");
//                            adapter.getInGameActions().setVoteTotal(snapshotValue.intValue(),snapshot.getKey().toString());
//                            if(snapshotValue.intValue() == 2){
//                                adapter.handleDBInGameActions(snapshot.getKey());
//                                adapter.getInGameActions().setVoteTotal(0, snapshot.getKey().toString());
//                            }
//                        }
                        break;
                    case "player_actions":
                        if (dataSnapshot.getValue().toString().equalsIgnoreCase("none")) {
                            voting = false;
                            //player type will be different for each game instance, so we can ensure that we both set vote value on false
                            myRef.child("users").child(GameActivity.hostName).child("inGameActions").child(playerType).setValue(false);
                            Log.d("GameActivity", "Was changed back");
                            adapter.getVoteController().setVoteType("none");
                            Log.d("GameActivity", "Vote type is" + adapter.getVoteController().getVoteType());
                        }
                        else {
                            String voteType = "";
                            if(dataSnapshot.hasChild("resetDiff")){
                                voteType = "resetDiff";
                                Long tempDiff = (Long)dataSnapshot.child("resetDiff").getValue();
                                difficulty = tempDiff.intValue();
                            }
                            else{
                                voteType = "shuffle";
                            }
                            adapter.getVoteController().setVoteType(voteType);
                            Log.d("GameActivity", "Vote type is" + adapter.getVoteController().getVoteType());
                            voting = true;
                            Log.d("GameActivity", "This context is " + getThisContext());
                            initiatePlayerAction(voteType,getThisContext());
                        }

                    default:
                        Log.d("ChildListenerCase", "Nothing is happened");
                        break;
                }
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
        };
        myRef.child("users").child(userName).addChildEventListener(allChildsListener);
    }
    public ChildEventListener getAllChildsListener(){
        return this.allChildsListener;
    }
    public void initiatePlayerAction(final String actionType, Context context){
        Log.d("GameActivity", "Shuffle was initiated");
        if(actionType == "shuffle"){
            dialogTitle = "Tiles will be re-shuffeled";
        }
        else if(actionType == "resetDiff"){
            dialogTitle = "Difficulty will be changed";
        }
        if(!isFinishing()){
            new AlertDialog.Builder(context)
                    .setTitle(dialogTitle)
                    .setMessage("Are you ok with this?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            if(voting){
                                adapter.getVoteController().vote(true);
                            }

                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.getVoteController().vote(false);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        else{
            Log.d("GameActivity", "Activity is in finishing process");
        }

    }
    public boolean checkIfEveryoneVotedYes(DataSnapshot snapshot){
        for (DataSnapshot sn : snapshot.getChildren()){
            if((Boolean)sn.getValue() == false) {
                return false;
            }
        }
        return true;
    }
    public void displayGamePlaceholder(){
        ImageView view = getCustomImageView("dog");

        AlertDialog.Builder builder=new AlertDialog.Builder(GameActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Waiting for opponent");
        builder.setInverseBackgroundForced(true);
        builder.setView(view);
        AlertDialog alert=builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("GameActivity", "Activity is destroyed");

        //adapter = null;
    }
    public Context getThisContext(){
        return GameActivity.this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("GameActivity", "I am resumed!" + GameActivity.hostName);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("GameActivity", "I`m on pauze now" + GameActivity.hostName);
    }
}
