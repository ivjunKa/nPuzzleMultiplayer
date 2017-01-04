package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import kaplya.ia.mad.ica.han.nl.myapplication.R;
import org.w3c.dom.Text;
import java.util.ArrayList;
public class MainActivity extends ActionBarActivity {
    private RadioGroup imgSelectorGroup;
    private Button startButton;
    private int numberOfChunks = 0;
    private String difficulty = "Medium";
    private TextView textDifficulty = null;
    private int selectedDrawableResource = 0;
    private EditText roomName = null;
    private boolean GameTypeMultiplayer = false;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //connecting to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //adding reference to table
        final DatabaseReference myRef = database.getReference("npuzzlemultiplayer");


        roomName = (EditText)findViewById(R.id.room_name);
        Intent previousIntent = getIntent();
        if (previousIntent.hasExtra("multiplayer")) {
            GameTypeMultiplayer = true;
            userName = previousIntent.getStringExtra("username");
        }
        else {
            GameTypeMultiplayer = false;
            roomName.setVisibility(View.GONE);
        }
        textDifficulty = (TextView)findViewById(R.id.text_difficulty);
        textDifficulty.setText(difficulty);
        startButton = (Button)findViewById(R.id.buttonStart);
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int drawableId = getSelectedImage(imgSelectorGroup.getCheckedRadioButtonId());
                ImageView view = (ImageView)findViewById(drawableId);
                numberOfChunks = getDifficulty();
                ArrayList<Bitmap> chunkedImage = splitImage(view, numberOfChunks);
                //Intent previousIntent = getIntent();
                Intent nextIntent = new Intent();
                if (GameTypeMultiplayer) {
                    nextIntent.setClass(MainActivity.this,GameActivity.class);
                    //DatabaseReference childRef = myRef.push();
                    //while using push we basically say that this is an subobject and everytning setValue adds some random key with given value
                    DatabaseReference statusRef = myRef.child("users").child(userName).child("status");
                    DatabaseReference imgName = myRef.child("users").child(userName).child("selected_image");
                    //MultiPlayerStartScreen.updateList(userName);
                    statusRef.setValue("creator");
                    // Set the child's data to the value passed in from the text box.
                    //childRef.setValue("Something");
                }
                else {
                    nextIntent.setClass(MainActivity.this,GameActivity.class);
                }
                nextIntent.putParcelableArrayListExtra("chunkedImage",chunkedImage);
                nextIntent.putExtra("imgDrawableResource", selectedDrawableResource);
                nextIntent.putExtra("chunksTotal",numberOfChunks);
                startActivity(nextIntent);
            }
        });
        attachRadioListeners();
    }

    private void attachRadioListeners() {
        imgSelectorGroup = (RadioGroup)findViewById(R.id.imageSelector);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //noinspection SimplifiableIfStatement

        switch(id){
            case R.id.menu_easy:
                difficulty = "Easy";
                break;
            case R.id.menu_normal:
                difficulty = "Medium";
                break;
            case R.id.menu_hard:
                difficulty = "Hard";
                break;
            default: break;
        }
        textDifficulty.setText(difficulty);
        return super.onOptionsItemSelected(item);
    }

    public void startGame(View v){
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }
    public int getSelectedImage(int button){
        switch (button){
            case R.id.radioImg2:
                selectedDrawableResource = R.drawable.malevitsj;
                return R.id.img_malevitch;
            case R.id.radioDog:
                selectedDrawableResource = R.drawable.dog;
                return R.id.img_dog;
            case R.id.radioDude:
                selectedDrawableResource = R.drawable.dude;
                return R.id.img_dude;
        }
        return 0;
    }
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
    private int getDifficulty(){
        switch (difficulty){
            case "Easy":
                return 9;
            case "Medium":
                return 16;
            case "Hard":
                return 25;
            default: return 16;
        }
    }

}
