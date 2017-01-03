package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Iv on 3-5-2015.
 */
public class PuzzleAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Tile> tiles;
    private Integer darkTilePos = null;
    private int PUZZLE_CHUNKS;
    private int imageWidth, imageHeight;
    public static int stepsCount = 0;

    public PuzzleAdapter(Context context, int PUZZLE_CHUNKS, final ArrayList<Tile> tiles) {
        this.context = context;
        this.PUZZLE_CHUNKS = PUZZLE_CHUNKS;
        this.tiles = tiles;
        imageWidth = tiles.get(0).getTileBitmap().getWidth() * 2;
        imageHeight = tiles.get(0).getTileBitmap().getHeight() * 2;
        for(int i =0;i<tiles.size();i++){
            Log.d("Puzzle","UnShuffled tiles are+"+tiles.get(i).getTileId());
        }
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        shuffleTiles();
                    }
                },
                3000);
    }

    public int getCount() {
    return tiles.size();
    }

    public Object getItem(int position) {
        return tiles.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
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
                    Integer result = getBlankPuzzle(position);
                    if(result == null){
                        Log.d("PuzzleAdapter","anything like blank tile were founded,can`t swap");
                    }
                    // TODO blank tile founded, swap
                    else {
                          Tile foundedDarkTile = tiles.get(result);
                          Tile clickedTile = tiles.get(position);
                          tiles.set(position,foundedDarkTile);
                          tiles.set(result,clickedTile);
                          darkTilePos = position;
                          notifyDataSetChanged();
                          stepsCount++;
                          checkWin();
                    }
                }
            });
        } else {
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
            return checkVerticalPosition(pos,divider);
        }
        return null;
    }

    public Integer checkHorizontalPosition(int pos, int divider) {
        int next = 0;
        if (pos == 0 || pos % divider == 0) {
            Log.d("PuzzleAdapter", "we are in the left side of the screen,needs to check only the right tile next to this");
            next = pos + 1;
            return checkNext(next);
        } else if (pos == tiles.size() - 1 || (pos + 1) % divider == 0) {
            Log.d("PuzzleAdapter", "we are in the right side of the screen,needs to check only the left tile next to this");
            next = pos - 1;
            return checkNext(next);
        } else {
            Log.d("PuzzleAdapter", "we are in the middle,needs to check both sides");
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
        Log.d("Puzzle","pos is" + pos);
        if(pos == 0 || pos < divider){
            Log.d("PuzzleAdapter","We are in top");
            next = pos + divider;
            return checkNext(next);
        }
        else if(pos > ((tiles.size()-1) - divider)){
            Log.d("PuzzleAdapter","We are in bottom");
            next = pos - divider;
            return checkNext(next);
        }
        else {
            //check top first
            Log.d("PuzzleAdapter", "We are in the middle");
            next = pos - divider;
            if(checkNext(next) == null){ // nothing found? check bottom
                next = pos + divider;
            }
            return checkNext(next);
        }
    }
    private Integer checkNext(int next){
        Log.d("PuzzleAdapter", "BLAH");
        if(next == darkTilePos){
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
        Log.d("Puzzle","RESOLVE");
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
        darkTilePos = tiles.size() - 1;
        checkWin();
    }

    public void checkWin(){
        int[] tileIdArr = new int[tiles.size()-1];
        for(int i=0;i<tiles.size()-1;i++){
            tileIdArr[i] = tiles.get(i).getTileId();
        }
        if(isSorted(tileIdArr)){
            Log.d("Puzzle","WIN!!");
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
            Log.d("Puzzle","Shuffled tiles are+"+tiles.get(i).getTileId());
        }
        darkTilePos = tiles.size() - 1;
        Log.d("PuzzleAdapter", Integer.toString(tiles.size()-1));
        stepsCount = 0;
        notifyDataSetChanged();
    }
    public static int getStepsCount(){
        return stepsCount;
    }
}
