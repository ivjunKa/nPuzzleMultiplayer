package kaplya.ia.mad.ica.han.nl.npuzzle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import kaplya.ia.mad.ica.han.nl.myapplication.R;

/**
 * Created by Iv on 20-5-2015.
 */
public class WinActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);
        TextView steps = (TextView)findViewById(R.id.steps_amount);
        ImageView solvedImg = (ImageView)findViewById(R.id.imgWin);
        Intent intent = getIntent();
        int stepsCount = intent.getIntExtra("stepsCount",0);
        steps.setText("Amount of steps:"+ stepsCount);
        solvedImg.setImageResource(intent.getIntExtra("imgSolved",0));
        solvedImg.getLayoutParams().width = 500;
        solvedImg.getLayoutParams().height = 500;
    }
    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(WinActivity.this,MainActivity.class);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}
