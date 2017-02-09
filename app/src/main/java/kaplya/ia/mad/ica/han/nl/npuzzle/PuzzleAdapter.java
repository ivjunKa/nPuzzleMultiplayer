package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.provider.ContactsContract;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.BaseAdapter;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 3-5-2015.
 */
public class PuzzleAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Tile> tiles;
    private ArrayList<Tile> tilesBackup;
    private int PUZZLE_CHUNKS;
    private int imageWidth, imageHeight;
    public static int stepsCount = 0;
    public int[] tileDatabaseArr;

    private boolean myTurn;
    private String myType;
    private String opponentType;
    public ValueEventListener opponentActionEventListener;
    public ValueEventListener darkTileEventListener;
    public ChildEventListener allChildsListener;
    private DarkTile darkTile;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference myRef = database.getInstance().getReference();
    //private InGameActions inGameActions;
    public VoteController voteController;
    public PuzzleAdapter(Context context, int PUZZLE_CHUNKS, final ArrayList<Tile> tiles, String myType) {
        this.context = context;
        this.myType = myType;
        darkTile = new DarkTile();
        opponentType = (myType == "host") ? "guest" : "host";
        myTurn = (myType == "host") ? true : false;
        myRef.child("users").child(GameActivity.hostName).child(myType).setValue(myTurn);
        createListenerForOpponentActions(myType);
        GameActivity.initTurnIndicator(myTurn);
        this.PUZZLE_CHUNKS = PUZZLE_CHUNKS;
        this.tiles = tiles;
        this.tilesBackup = tiles;
        imageWidth = tiles.get(0).getTileBitmap().getWidth() * 2;
        imageHeight = tiles.get(0).getTileBitmap().getHeight() * 2;
        setTileAddrInDatabase();
        //Setting start position of the dark tile
        //inGameActions = new InGameActions(GameActivity.hostName);
        myRef.child("users").child(GameActivity.hostName).child("darkTile").child("darkTileNewPosition").setValue(tiles.size() - 1);
        myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("none");

        //darkTile.setNewPosition(tiles.size() - 1);
        voteController = new VoteController(myType);
        Log.d("PuzzleAdapter", "Adapter was initilized");
        //GameActivity.resetHintValue();
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        shuffleTiles();
                    }
                },
                3000);
        //this one controlls the positions of the tiles
        myRef.child("users").child(GameActivity.hostName).child("hintNotifier").setValue(false);

        //createChildsListenerForSpecifiedUser(GameActivity.hostName);
    }

    public int getCount() {
    return tiles.size();
    }

    public Object getItem(int position) {
        return tiles.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    //sets tiles positions in database instance
    public void setTileAddrInDatabase(){
        tileDatabaseArr = new int[tiles.size()];

        for(int i=0;i<tileDatabaseArr.length;i++){
            tileDatabaseArr[i] = tiles.get(i).getTileId();
        }
        Log.d("PuzzleAdapter", "TileDatabaseArr size before database insert is: "+ tileDatabaseArr.length);
        myRef.child("users").child(GameActivity.hostName).child("tileaddr").setValue(Arrays.toString(tileDatabaseArr));
        Log.d("PuzzleAdapter", "Data is inserted " + tileDatabaseArr.length);
    }
    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        //Log.d("PuzzleAdapter","This is getView method of the Puzzle adapter");

        final ImageView imageView;
        //ConvertView check temporarely disabled due to bug with the first tile LayoutParams after reshuffling
//        if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(imageWidth - 10, imageHeight));
                //Log.d("PuzzleAdapter", "Tile width: " + imageWidth);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(1, 1, 1, 1);
                imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d("PuzzleAdapter",Integer.toString(position));
                    //we kan do the turn AND we did`n made the turn in the previous time
                    if(myTurn){
                        Integer result = getBlankPuzzle(position);
                        if(result == null){
                            //Log.d("PuzzleAdapter","anything like blank tile were founded,can`t swap");
                        }
                        // TODO blank tile founded, swap
                        else {
                            //in this clause we only need to add cnahged values to the database,
                            // the database controller does the notifying
                            myRef.child("users").child(GameActivity.hostName).child("darkTile").child("darkTileOldPosition").setValue(result);
                            myRef.child("users").child(GameActivity.hostName).child("darkTile").child("darkTileNewPosition").setValue(position);
                            stepsCount++;
                            myRef.child("users").child(GameActivity.hostName).child(myType).setValue(false);
                            myRef.child("users").child(GameActivity.hostName).child(opponentType).setValue(true);
                            //checkWin();
                            //madeTurn = true;
                        }
                    }
                    else{
                        setHint(position,80);
                    }
                }
            });
//        } else {
//            //Log.d("PuzzleAdapter","This called in case of convertedView == null");
//            imageView = (ImageView) convertView;
//        }
        imageView.setImageBitmap(tiles.get(position).getTileBitmap());
        return imageView;
    }
    public Integer getBlankPuzzle(int pos){
        int divider = (int) Math.sqrt(PUZZLE_CHUNKS);
        //find horizontal
        if(checkHorizontalPosition(pos, divider) != null) {
            return checkHorizontalPosition(pos, divider);
        }
        //find vertical
        else if(checkVerticalPosition(pos,divider) != null) {
            return checkVerticalPosition(pos, divider);
        }
        return null;
    }

    public Integer checkHorizontalPosition(int pos, int divider) {
        int next = 0;
        if (pos == 0 || pos % divider == 0) {
            //Log.d("PuzzleAdapter", "we are in the left side of the screen,needs to check only the right tile next to this");
            next = pos + 1;
            return checkNext(next);
        } else if (pos == tiles.size() - 1 || (pos + 1) % divider == 0) {
            //Log.d("PuzzleAdapter", "we are in the right side of the screen,needs to check only the left tile next to this");
            next = pos - 1;
            return checkNext(next);
        } else {
            //Log.d("PuzzleAdapter", "we are in the middle,needs to check both sides");
            //check left side
            next = pos - 1;
            if (checkNext(next) == null) { // nothing found? check right
                next = pos + 1;
            }
            return checkNext(next);
        }
    }
    private Integer checkVerticalPosition(int pos, int divider) {
        int next = 0;
        //Log.d("Puzzle","pos is" + pos);
        if(pos == 0 || pos < divider){
            //Log.d("PuzzleAdapter","We are in top");
            next = pos + divider;
            return checkNext(next);
        }
        else if(pos > ((tiles.size()-1) - divider)){
            //Log.d("PuzzleAdapter","We are in bottom");
            next = pos - divider;
            return checkNext(next);
        }
        else {
            //check top first
            //Log.d("PuzzleAdapter", "We are in the middle");
            next = pos - divider;
            if(checkNext(next) == null){ // nothing found? check bottom
                next = pos + divider;
            }
            return checkNext(next);
        }
    }
    private Integer checkNext(int next){
        //Log.d("PuzzleAdapter", "BLAH");
        if(next == darkTile.getNewPosition()){
            return next;
        }
        return null;
    }

    public static boolean isSorted(int[] a) {
        //assume is sorted, attempt to prove otherwise
        for (int i = 0; i < a.length - 1; i++) { //because we are always comparing to the next one and the last one doesn't have a next one we end the loop 1 earlier than usual
            if (a[i] > a[i + 1]) {
                return false; //proven not sorted
            }
        }
        return true; //got to the end, must be sorted
    }
    public void resolvePuzzle(){
        //Log.d("Puzzle","RESOLVE");
        Tile temp;
        for(int i= 0; i< tiles.size() -1; i++){
            for(int j =1; j< tiles.size() - i; j++){
                if(tiles.get(j-1).getTileId() > tiles.get(j).getTileId()){
                    temp = tiles.get(j-1);
                    tiles.set(j-1,tiles.get(j));
                    tiles.set(j,temp);
                }
            }
        }
        notifyDataSetChanged();
        //darkTile.setNewPosition(tiles.size() - 1);
        setTileAddrInDatabase();
        //checkWin();
    }

    public void checkWin(){
        int[] tileIdArr = new int[tiles.size()-1];
        for(int i=0;i<tiles.size()-1;i++){
            tileIdArr[i] = tiles.get(i).getTileId();
        }
        if(isSorted(tileIdArr)){
            //Log.d("Puzzle","WIN!!");
            myRef.child("users").child(GameActivity.hostName).child("status").setValue("game_win");
            //GameActivity.gotoWin(GameActivity.getAppContext());
        }
    }
    public void shuffleTiles(){
//        Collections.shuffle(tiles,new Random(System.nanoTime()));
        Collections.reverse(tiles);
        int blankPos = 0;
        for(int i = 0; i<tiles.size();i++){
            if(tiles.get(i).isEmpty()){
                blankPos = i;
            }
        }
        Tile lastTile = tiles.get(tiles.size()-1);
        Tile blankTile = tiles.get(blankPos);
        tiles.set(blankPos,lastTile);
        tiles.set(tiles.size() - 1, blankTile);
        //setting the last tile an id of empty tile
        for(int i =0;i<tiles.size();i++){
            //Log.d("Puzzle","Shuffled tiles are+"+tiles.get(i).getTileId());
        }
        darkTile.setNewPosition(tiles.size() - 1);
        Log.d("PuzzleAdapter", "Trying to renew tileaddr in database" + tileDatabaseArr.length);
        setTileAddrInDatabase();
        stepsCount = 0;
        notifyDataSetChanged();
    }
    //this function swaps founded dark tile with clicked tile
    public void setTilesFromDatabase(){
        Log.d("PuzzleAdapter", "Trying to swap now");
        Tile foundedDarkTile = tiles.get(darkTile.getOldPosition());
        Tile clickedTile = tiles.get(darkTile.getNewPosition());
        tiles.set(darkTile.getNewPosition(), foundedDarkTile);
        tiles.set(darkTile.getOldPosition(), clickedTile);
        //user have swapped the tiles,next user can do something
        notifyDataSetChanged();
        clearHints();
        setTileAddrInDatabase();
    }

    public static int getStepsCount(){
        return stepsCount;
    }
    public void createListenerForOpponentActions(String myType){
        opponentActionEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("PuzzleAdapter", "this is Listener for my type : " + myTurn);
                myTurn = (Boolean) dataSnapshot.getValue();
                GameActivity.initTurnIndicator(myTurn);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } ;
        myRef.child("users").child(GameActivity.hostName).child(myType).addValueEventListener(opponentActionEventListener);
    }
    /**
     * Sets hint value in the database and highlights the hint puzzle to the person who wants to send the hint to opponent.
     *
     * @param position
     * @param opacity
     */
    public void setHint(int position,int opacity) {
        boolean foundedChanged = clearHints();
        //if clicked hint is still clicked we don`t need to mark it as clicked again
        if(position != GameActivity.hintValue || foundedChanged == false){
            setOpacityToHintPuzzle(position);
            myRef.child("users").child(GameActivity.hostName).child("hintValue").setValue(position);
            GameActivity.hintGiven = true;
        }
    }

    /**
     * Clearing all the highlighted(hint) puzzles and setting static Boolean hintGiven(of the GameActivity class) back to false.
     * When hintGiven set to false the button "Give hint" will not responde to click events so we can`t basically send any hints
     * anymore
     * @return
     */
    public boolean clearHints(){
        boolean foundedChanged = false;
        for(int i = 0; i<tiles.size(); i++){
            if(tiles.get(i).wasChanged()){
                tiles.get(i).restoreOriginalImage();
                foundedChanged = true;
            }
        }
        GameActivity.hintGiven = false;
        notifyDataSetChanged();
        return foundedChanged;
    }
    public void notifyHint(int position){
        setOpacityToHintPuzzle(position);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        myRef.child("users").child(GameActivity.hostName).child("hintNotifier").setValue(false);
                    }
                },
                3000);
    }

    /**
     * Notifies opponent about the hint that were selected,the database listener binded with hintNotifier column will take care about getting it on screen
     */
    public void notifyOpponent(){
        myRef.child("users").child(GameActivity.hostName).child("hintNotifier").setValue(true);
    }

    /**
     * Sets opacity to specified tile element marking it as an "hint"
     * @param position
     */
    public void setOpacityToHintPuzzle(int position){
        Bitmap bitmap = tiles.get(position).getTileBitmap();
        Bitmap mutableBitmap = bitmap.isMutable()
                ? bitmap
                : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (80 & 0xFF) << 24;
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN);

        tiles.get(position).setTemporaryImage(mutableBitmap);
        tiles.get(position).setIsChanged();
        notifyDataSetChanged();
    }

    public void handleDBTileArr(String dataSnapshotValue){
        Log.d("PuzzleAdapter", "Tileaddr was changed");
        String[] items = dataSnapshotValue.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
        int[] results = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            try {
                results[i] = Integer.parseInt(items[i]);
            } catch (NumberFormatException nfe) {
                //NOTE: write something here if you need to recover from formatting errors
            }
            ;
        }
        Log.d("PuzzleAdapter", "TilebaseArr length is: " + tileDatabaseArr.length + " Result set from database length is: "+ results.length);
//        for (int i = 0; i < tileDatabaseArr.length; i++) {
//            tileDatabaseArr[i] = results[i];
//        }
        if(isSorted(results)){
        myRef.child("users").child(GameActivity.hostName).child("status").setValue("game_win");
        }
    }
    public void handleDBDarkTileSwapping(Long darkTileOPos, Long darkTileNPos){
        //Log.d("PuzzleAdapter", "I`m in the swap handler right now");
        darkTile.setOldPosition(darkTileOPos.intValue());
        if (darkTile.getNewPosition() != darkTileNPos.intValue()) {
            //Log.d("PuzzleAdapter", "New position changed, need to do the swap now");
            darkTile.setNewPosition(darkTileNPos.intValue());
            //myTurn = true;
            setTilesFromDatabase();
        }
    }
    public ValueEventListener getDarkTileListener(){
        return darkTileEventListener;
    }
    public ValueEventListener getOpponentActionEventListener(){
        return opponentActionEventListener;
    }
    public ChildEventListener getAllChildsListener(){
        return allChildsListener;
    }
    public void shuffleWhilePlaying(){
        Tile temp;
        for(int i= 0; i< tiles.size() -1; i++){
            for(int j =1; j< tiles.size() - i; j++){
                if(tiles.get(j-1).getTileId() > tiles.get(j).getTileId()){
                    temp = tiles.get(j-1);
                    tiles.set(j-1,tiles.get(j));
                    tiles.set(j,temp);
                }
            }
        }
        myRef.child("users").child(GameActivity.hostName).child("darkTile").child("darkTileNewPosition").setValue(tiles.size() - 1);
        myRef.child("users").child(GameActivity.hostName).child("player_actions").setValue("none");
        shuffleTiles();
    }

    public void handleDBInGameActions(String actionType){
        switch (actionType){
            case "shuffle":
                shuffleWhilePlaying();
                break;
            case "changeDiff":
                Log.d("PuzzleAdapter","Do reset diff" );
                break;
            default: Log.d("PuzzleAdapter", "Do nothing");
                break;
        }
    }

    public VoteController getVoteController(){
        return this.voteController;
    }
    public String getOpponentType(){
        return this.opponentType;
    }
    public void reinit(int PUZZLE_CHUNKS, final ArrayList<Tile> tiles){
        this.PUZZLE_CHUNKS = PUZZLE_CHUNKS;
        this.tiles = tiles;
        this.tilesBackup = tiles;
        imageWidth = tiles.get(0).getTileBitmap().getWidth() * 2;
        imageHeight = tiles.get(0).getTileBitmap().getHeight() * 2;
        myRef.child("users").child(GameActivity.hostName).child("darkTile").child("darkTileOldPosition").removeValue();
        myRef.child("users").child(GameActivity.hostName).child("darkTile").child("darkTileNewPosition").setValue(tiles.size() - 1);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        shuffleTiles();
                        //setTileAddrInDatabase();
                    }
                },
                3000);
    }

}
