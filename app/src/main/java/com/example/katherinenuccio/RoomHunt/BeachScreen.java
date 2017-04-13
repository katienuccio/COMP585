package com.example.katherinenuccio.RoomHunt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;


public class BeachScreen extends AppCompatActivity implements View.OnClickListener {

    private Button beachbutt;
    private HashMap<String, Boolean> flags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beach_screen);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        flags.put("swordDone", true);
        beachbutt = (Button) findViewById(R.id.beachbutt);
        beachbutt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == beachbutt) {
            Intent i = new Intent(this, PlayScreen.class);
            i.putExtra("flags", flags);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.d("Flags During Beach", flags.toString());
            startActivity(i);
        }
    }
}
