package com.example.katherinenuccio.RoomHunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

public class MainScreen extends AppCompatActivity implements View.OnClickListener{

    private Button play;
    private Button helper;
    private Button profile;

    private TextView title;
    private TextView currRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        play = (Button) findViewById(R.id.play_button);
        play.setOnClickListener(this);

        title = (TextView) findViewById(R.id.main_title);
        currRoom = (TextView) findViewById(R.id.currRoom);
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
            startActivity(i);
        }
    }
}