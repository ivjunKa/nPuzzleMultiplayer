package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 3-5-2015.
 */
public class GameActivity extends ActionBarActivity {

    private ArrayList<Bitmap> chunked = null;
    private static int imgResource = 0;
    private static Context mContext;
    private PuzzleAdapter adapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game);
        mContext = this.getApplicationContext();
        Intent intent = getIntent();
        chunked = intent.getParcelableArrayListExtra("chunkedImage");
        imgResource = intent.getIntExtra("imgDrawableResource",0);
        int difficulty = intent.getIntExtra("chunksTotal",0);

        initGame(difficulty,chunked);

        Button resolve = (Button)findViewById(R.id.resolveButton);
        resolve.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.resolvePuzzle();
            }
        });

    }
    private void initGame(int difficulty, ArrayList<Bitmap> chunked ){
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        setBlankTile(chunked,tiles);
        GridView grid = (GridView)findViewById(R.id.gridView);
        adapter = new PuzzleAdapter(this,difficulty,tiles);
        grid.setAdapter(adapter);
        grid.setNumColumns((int) Math.sqrt(chunked.size()));
    }
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
        Intent intent = new Intent(c,WinActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("stepsCount",PuzzleAdapter.getStepsCount());
        intent.putExtra("imgSolved", imgResource);
        c.startActivity(intent);
    }
    public static Context getAppContext(){
        return mContext;
    }

    private void resetDiff(int difficulty){
        ImageView newImg = new ImageView(this);
        newImg.setImageResource(imgResource);
        ArrayList<Bitmap> newChunked = MainActivity.splitImage(newImg,difficulty);
        initGame(difficulty,newChunked);
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

}
