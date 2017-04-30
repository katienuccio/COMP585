package com.example.katherinenuccio.RoomHunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.HashMap;

public class MenuScreen extends AppCompatActivity implements View.OnClickListener{

    private Button play, games;

    private HashMap<String, Boolean> flags;
    private HashMap<String, String> instructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        play = (Button) findViewById(R.id.play_button);
        play.setOnClickListener(this);
        games = (Button) findViewById(R.id.games_button);
        games.setOnClickListener(this);

        flags = new HashMap<String, Boolean>();
        flags.put("visitBeach", false);
        flags.put("visitForest", false);
        flags.put("visitMountain", false);
        flags.put("visitTown", false);
        flags.put("exploreMode", true);
        flags.put("applePicking", false);
        flags.put("duneDigging", false);
        flags.put("bossBeating", false);
        flags.put("appleDone", false);
        flags.put("swordDone", false);
        flags.put("dragonDone", false);
        flags.put("coveDone", false);
        flags.put("gameDone", false);
        flags.put("cheats", false);

        instructions = new HashMap<String, String>();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }


    @Override
    public void onClick(View view) {
        view.playSoundEffect(SoundEffectConstants.CLICK);
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (view == play) {
            instructions.put("instructions", "Welcome to Room Hunt, please explore the room and find all four locations at the various walls around the room.");
            Intent i = new Intent(this, PlayScreen.class);
            i.putExtra("flags", flags);
            i.putExtra("instructions", instructions);
            startActivity(i);
        } else if(view == games){
            instructions.put("instructions", "Go to any location to play the minigame at that location.");
            Intent i = new Intent(this, PlayScreen.class);
            flags.put("exploreMode", false);
            flags.put("cheats", true);
            i.putExtra("flags", flags);
            i.putExtra("instructions", instructions);
            startActivity(i);
        }
    }
}