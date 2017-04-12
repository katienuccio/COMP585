package com.example.katherinenuccio.RoomHunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;


public class ForestScreen extends AppCompatActivity implements View.OnClickListener {

    private Button forestbutt;
    private HashMap<String, Boolean> flags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forest_screen);
        Intent intent = getIntent();
//        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");

        forestbutt = (Button) findViewById(R.id.forestbutt);
        forestbutt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == forestbutt) {
            Intent i = new Intent(this, PlayScreen.class);
//            i.putExtra("flags", flags);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }
}
