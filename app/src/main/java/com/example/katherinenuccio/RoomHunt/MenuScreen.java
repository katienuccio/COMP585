package com.example.katherinenuccio.RoomHunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

import java.util.HashMap;

public class MenuScreen extends AppCompatActivity implements View.OnClickListener{

    private Button play;
    private Button helper;
    private Button profile;

    private HashMap<String, Boolean> flags;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_screen);

        play = (Button) findViewById(R.id.play_button);
        play.setOnClickListener(this);

        flags = new HashMap<String, Boolean>();
        title = (TextView) findViewById(R.id.main_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }


    @Override
    public void onClick(View view) {
        if (view == play) {
            Intent i = new Intent(this, PlayScreen.class);
            i.putExtra("flags", flags);
            startActivity(i);
        }
    }
}