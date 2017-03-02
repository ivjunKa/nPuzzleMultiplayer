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

    private boolean GameTypeMultiplayer = false;
    private String userName;
    private GPSTracker gpsTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //super.onResume();
        setContentView(R.layout.activity_main);
        gpsTracker = new GPSTracker(MainActivity.this);

        //connecting to firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //adding reference to table
        final DatabaseReference myRef = database.getInstance().getReference();

        final Intent previousIntent = getIntent();
        if (previousIntent.hasExtra("multiplayer")) {
            GameTypeMultiplayer = true;
            userName = previousIntent.getStringExtra("username");
        }
        else {
            GameTypeMultiplayer = false;

        }
        textDifficulty = (TextView)findViewById(R.id.text_difficulty);
        textDifficulty.setText(difficulty);
        startButton = (Button)findViewById(R.id.buttonStart);
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String drawableName = getSelectedImageName(imgSelectorGroup.getCheckedRadioButtonId());
                numberOfChunks = getDifficulty();
                Intent nextIntent = new Intent();
                nextIntent.setClass(MainActivity.this, GameActivity.class);
                gpsTracker.getLocation();
                myRef.child("users").child(userName).child("location").child("lat").setValue(gpsTracker.getLatitude());
                myRef.child("users").child(userName).child("location").child("lon").setValue(gpsTracker.getLongitude());
                DatabaseReference statusRef = myRef.child("users").child(userName).child("status");
                DatabaseReference imageName = myRef.child("users").child(userName).child("imgname");
                DatabaseReference difficulty = myRef.child("users").child(userName).child("difficulty");
                difficulty.setValue(numberOfChunks);
                statusRef.setValue("waiting_for_guest");
                imageName.setValue(drawableName);
                nextIntent.putExtra("username", userName);
                nextIntent.putExtra("host", true);
                nextIntent.putExtra("selectedHostName", previousIntent.getStringExtra("selectedHostName"));
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
    public String getSelectedImageName(int button){
        switch (button){
            case R.id.radioImg2:
                selectedDrawableResource = R.drawable.malevitsj;
                return "malevitsj";
            case R.id.radioDog:
                selectedDrawableResource = R.drawable.dog;
                return "dog";
            case R.id.radioDude:
                selectedDrawableResource = R.drawable.dude;
                return "dude";
        }
        return "dude";
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
