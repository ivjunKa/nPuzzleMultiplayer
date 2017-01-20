package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 3-5-2015.
 */
public class PuzzleAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Tile> tiles;

    private int PUZZLE_CHUNKS;
    private int imageWidth, imageHeight;
    public static int stepsCount = 0;
    public int[] tileDatabaseArr;

    private boolean myTurn;
    private String myType;
    private String opponentType;
    private ViewGroup parent;


    private DarkTile darkTile = new DarkTile();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    final DatabaseReference myRef = database.getInstance().getReference();

    public PuzzleAdapter(Context context, int PUZZLE_CHUNKS, final ArrayList<Tile> tiles, String myType, ViewGroup parent) {
        this.context = context;
        this.myType = myType;
        this.parent = parent;
        Log.d("PuzzleAdapter", "this is myType : " + myType);

        opponentType = (myType == "host") ? "guest" : "host";

        Log.d("PuzzleAdapter", "this is opponentType : " + opponentType);

        myTurn = (myType == "host") ? true : false;

        Log.d("PuzzleAdapter", "this is myTurn : " + myTurn);

        myRef.child("users").child("siv").child(myType).setValue(myTurn);

        createListenerForOpponentActions(myType);
        GameActivity.initTurnIndicator(myTurn);
        this.PUZZLE_CHUNKS = PUZZLE_CHUNKS;
        this.tiles = tiles;
        imageWidth = tiles.get(0).getTileBitmap().getWidth() * 2;
        imageHeight = tiles.get(0).getTileBitmap().getHeight() * 2;
        //Log.d("PuzzleAdapter", "My turn is" + myTurn);


        setTileAddrInDatabase();


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        shuffleTiles();
                    }
                },
                3000);
        //this one controlls the positions of the tiles
        myRef.child("users").child("siv").child("tileaddr").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String[] items = dataSnapshot.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
                int[] results = new int[items.length];
                for (int i = 0; i < items.length; i++) {
                    try {
                        results[i] = Integer.parseInt(items[i]);
                    } catch (NumberFormatException nfe) {
                        //NOTE: write something here if you need to recover from formatting errors
                    };
                }
                for(int i = 0; i< tileDatabaseArr.length; i++) {
                    tileDatabaseArr[i] = results[i];
                }
                for(int i = 0; i< tileDatabaseArr.length; i++) {
                    //Log.d("PuzzleAdapter", "These is new tempArr from database : " + tileDatabaseArr[i]);
                }
                //setTilesFromDatabase();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        myRef.child("users").child("siv").child("darkTileOldPosition").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Log.d("PuzzleAdapter", "Setting oldValue from database");
//                Long tempDarkTilePos = (Long)dataSnapshot.getValue();
//                darkTileOldPos = tempDarkTilePos.intValue();
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

    }

    public int getCount() {
    return tiles.size();
    }

    public Object getItem(int position) {
        return tiles.get(position);
    }
    //sets tiles positions in database instance
    public void setTileAddrInDatabase(){
        tileDatabaseArr = new int[tiles.size()];
        for(int i=0;i<tileDatabaseArr.length;i++){
            tileDatabaseArr[i] = tiles.get(i).getTileId();
        }
        DatabaseReference imageArray = myRef.child("users").child("siv").child("tileaddr");
        myRef.child("users").child("siv").child("darkTile").child("darkTileNewPosition").setValue(tiles.size()-1);
        darkTile.setNewPosition(tiles.size()-1);

        imageArray.setValue(Arrays.toString(tileDatabaseArr));
        attachGlobalUserListener();

    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        //Log.d("PuzzleAdapter","This is getView method of the Puzzle adapter");

        final ImageView imageView;
        if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(imageWidth - 10, imageHeight));
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
                            myRef.child("users").child("siv").child("darkTile").child("darkTileOldPosition").setValue(result);
                            myRef.child("users").child("siv").child("darkTile").child("darkTileNewPosition").setValue(position);
                            stepsCount++;
                            myRef.child("users").child("siv").child(myType).setValue(false);
                            myRef.child("users").child("siv").child(opponentType).setValue(true);
                            //checkWin();
                            //madeTurn = true;
                        }
                    }
                    else{
                        setHint(position, 50);
                    }
                }
            });
        } else {
            //Log.d("PuzzleAdapter","This called in case of convertedView == null");
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(tiles.get(position).getTileBitmap());
        return imageView;
    }
    public Integer getBlankPuzzle(int pos){
        int divider = (int) Math.sqrt(PUZZLE_CHUNKS);
        //find horizontal
        if(checkHorizontalPosition(pos, divider)!=null){
            return checkHorizontalPosition(pos, divider);
        }
        //find vertical
        else if(checkVerticalPosition(pos,divider)!=null){
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
        darkTile.setNewPosition(tiles.size() - 1);
        checkWin();
    }

    public void checkWin(){
        int[] tileIdArr = new int[tiles.size()-1];
        for(int i=0;i<tiles.size()-1;i++){
            tileIdArr[i] = tiles.get(i).getTileId();
        }
        if(isSorted(tileIdArr)){
            //Log.d("Puzzle","WIN!!");
            GameActivity.gotoWin(GameActivity.getAppContext());
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
        tiles.set(tiles.size()-1,blankTile);
        //setting the last tile an id of empty tile
        for(int i =0;i<tiles.size();i++){
            //Log.d("Puzzle","Shuffled tiles are+"+tiles.get(i).getTileId());
        }
        darkTile.setNewPosition(tiles.size() - 1);

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
    }

    public static int getStepsCount(){
        return stepsCount;
    }


    public void attachGlobalUserListener(){
        myRef.child("users").child("siv").child("darkTile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("PuzzleAdapter", "This is the value of tile old position: " + dataSnapshot.child("darkTileOldPosition").getValue());
                if(dataSnapshot.hasChild("darkTileOldPosition") && dataSnapshot.hasChild("darkTileNewPosition")){
                    Long darkTileOPos = (Long)dataSnapshot.child("darkTileOldPosition").getValue();
                    Long darkTileNPos = (Long)dataSnapshot.child("darkTileNewPosition").getValue();
                    darkTile.setOldPosition(darkTileOPos.intValue());
                    if(darkTile.getNewPosition() != darkTileNPos.intValue()){
                        Log.d("PuzzleAdapter", "New position changed, need to do the swap now");
                        darkTile.setNewPosition(darkTileNPos.intValue());
                        //myTurn = true;
                        setTilesFromDatabase();
                    }
                    //setTilesFromDatabase(dataSnapshot.child("darkTileOldPosition").getValue().toString(), dataSnapshot.child("darkTileNewPosition").getValue().toString());

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public void setUserType(String myType){
        this.myType = myType;
    }
    public void createListenerForOpponentActions(String myType){
        myRef.child("users").child("siv").child(myType).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("PuzzleAdapter", "this is Listener for my type : " + myTurn);
                myTurn = (Boolean)dataSnapshot.getValue();
                GameActivity.initTurnIndicator(myTurn);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void setHint(int position,int opacity) {
        for(int i = 0; i<tiles.size(); i++){
            if(tiles.get(i).wasChanged()){
                tiles.get(i).restoreOriginalImage();
            }
        }
        notifyDataSetChanged();
        Bitmap bitmap = tiles.get(position).getTileBitmap();

        Bitmap mutableBitmap = bitmap.isMutable()
                ? bitmap
                : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (opacity & 0xFF) << 24;
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN);

        tiles.get(position).setTemporaryImage(mutableBitmap);
        tiles.get(position).setIsChanged();
        notifyDataSetChanged();
    }
}
