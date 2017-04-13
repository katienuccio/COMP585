package com.example.katherinenuccio.RoomHunt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;


public class MountainScreen extends AppCompatActivity implements View.OnClickListener {

    private Button mountainbutt;
    private HashMap<String, Boolean> flags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mountain_screen);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        flags.put("dragonDone", true);
        mountainbutt = (Button) findViewById(R.id.mountainbutt);
        mountainbutt.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == mountainbutt) {
            Intent i = new Intent(this, PlayScreen.class);
            i.putExtra("flags", flags);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.d("Flags During Mountain", flags.toString());
            startActivity(i);
        }
    }
}
