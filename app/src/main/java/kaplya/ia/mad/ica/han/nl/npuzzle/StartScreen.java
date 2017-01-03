package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 3-1-2017.
 */
public class StartScreen extends ActionBarActivity {
    boolean multiplayerMode = false;
   @Override
   protected void onCreate(Bundle savedInstanceState){
       super.onCreate(savedInstanceState);
       setContentView(R.layout.start_screen);

       Button singlePlayerButton = (Button)findViewById(R.id.button_singleplayer);
       singlePlayerButton.setOnClickListener(new Button.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(StartScreen.this, MainActivity.class);
               //intent.putExtra("multiplayer", false);
               startActivity(intent);
           }
       });
       Button multiPlayerButton = (Button)findViewById(R.id.button_multiplayer);
       multiPlayerButton.setOnClickListener(new Button.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent intent = new Intent(StartScreen.this,MultiPlayerStartScreen.class);
               startActivity(intent);
           }
       });
   }
}
