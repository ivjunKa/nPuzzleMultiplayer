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
public class MultiPlayerStartScreen extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multiplayer_game_list);

        Button addNewGameButton = (Button)findViewById(R.id.button_new_game);
        addNewGameButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MultiPlayerStartScreen.this, MainActivity.class);
                intent.putExtra("multiplayer", true);
                startActivity(intent);
            }
        });
    }
}
