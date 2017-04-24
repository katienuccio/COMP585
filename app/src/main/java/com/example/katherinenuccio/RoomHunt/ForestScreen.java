package com.example.katherinenuccio.RoomHunt;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.CountDownTimer;
import java.util.HashMap;
import java.util.Locale;


public class ForestScreen extends AppCompatActivity implements View.OnClickListener {

    private Button forestbutt;
    private ImageButton appleButton;
    private TextView newText;
    private HashMap<String, Boolean> flags;
    private int counter = 0;
    private TextToSpeech tts;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forest_screen);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        flags.put("appleDone", true);
        newText = (TextView) findViewById(R.id.totalClicks);
        appleButton = (ImageButton) findViewById(R.id.appleButton);

        // Text to Speech
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    speak("Welcome to the Forest. Tap the screen to gather as many apples as you can in, but hurry! We don't have much time. Begin!");
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });

        // Timer
        timer = new CountDownTimer(45000, 1000) {
            @Override
            public void onTick(long l) {
                newText.setText("Clicks: " + counter);
            }
            @Override
            public void onFinish() {
                // Read out how many apples were picked (counter)
                if(counter > 50){
                    speak("Wow! You're a natural at this! You got a whopping " + " apples. The village will never need food again! Go back to share the good news.");
                    appleDone();
                } else if (counter > 30) {
                    speak("Great job! You got " + counter + " apples! Woooo!");
                    appleDone();
                } else if (counter > 10){
                    speak("Thanks adventurer! You got " + counter + " apples. Return to the village");
                    appleDone();
                } else {
                    speak("Not enough! You only got " + counter +  "apples. Please try again.");
                    restart();
                }

            }
        };

        // Buttons
        forestbutt = (Button) findViewById(R.id.forestbutt);
        forestbutt.setOnClickListener(this);
        appleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == forestbutt) {
            Intent i = new Intent(this, PlayScreen.class);
            i.putExtra("flags", flags);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.d("Flags During Forest", flags.toString());
            startActivity(i);
        }
        if (view == appleButton) {
            counter++;
        }
    }

    private void appleDone() {
        Intent i = new Intent(this, PlayScreen.class);
        i.putExtra("flags", flags);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Log.d("Flags During Forest", flags.toString());
        startActivity(i);
    }

    private void restart() {
        Activity test = new Activity();
        test.recreate();
    }

    // Text to speech code. For deprecation/compatibility purposes.
    private void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
