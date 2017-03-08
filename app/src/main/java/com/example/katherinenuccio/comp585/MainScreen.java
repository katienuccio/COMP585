package com.example.katherinenuccio.comp585;

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
        helper = (Button) findViewById(R.id.helper_button);
        helper.setOnClickListener(this);
        profile = (Button) findViewById(R.id.profile_button);
        profile.setOnClickListener(this);

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
        }else if (view == helper){
            Intent i = new Intent(this, HelperScreen.class);
            startActivity(i);
        }else if (view == profile){
            Intent i = new Intent(this, ProfileScreen.class);
            startActivity(i);
        }
    }
}