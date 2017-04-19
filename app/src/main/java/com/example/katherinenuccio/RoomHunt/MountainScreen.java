package com.example.katherinenuccio.RoomHunt;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class MountainScreen extends AppCompatActivity implements View.OnClickListener {

    private Button mountainbutt;
    private HashMap<String, Boolean> flags;
    private TextView resultTEXT;
    private TextView newText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mountain_screen);
        resultTEXT = (TextView) findViewById(R.id.TVresult);
        newText = (TextView) findViewById(R.id.result);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        flags.put("dragonDone", true);
        mountainbutt = (Button) findViewById(R.id.mountainbutt);
        mountainbutt.setOnClickListener(this);
    }

    public void onButtonClick(View v) {
        if (v.getId() == R.id.imageButton) {
            promptSpeechInput();
        }
    }

    public void promptSpeechInput() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(com.example.katherinenuccio.RoomHunt.MountainScreen.this, "Sorry! Your device doesn't support speech language", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int request_code, int result_code, Intent i) {
        super.onActivityResult(request_code, result_code, i);
        switch (request_code) {
            case 100: if (result_code == RESULT_OK && i != null) {
                ArrayList<String> result = i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                resultTEXT.setText("You said: " + result.get(0));
                if (result.get(0).toLowerCase().equals("i have the power")){
                    beatBoss();
                }
                else {
                    newText.setText("Sorry, that's not the right phrase");
                }
            }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mountainbutt) {
            beatBoss();
        }
    }

    public void beatBoss() {
        Intent i = new Intent(this, PlayScreen.class);
        i.putExtra("flags", flags);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Log.d("Flags During Mountain", flags.toString());
        startActivity(i);
    }
}
