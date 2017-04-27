package com.example.katherinenuccio.RoomHunt;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.CountDownTimer;
import java.util.HashMap;
import java.util.Locale;


public class ForestScreen extends AppCompatActivity implements View.OnClickListener {

    private ImageButton appleButton;
    private TextView newText, instructionText, timeLeft;
    private HashMap<String, Boolean> flags;
    private int counter = -1;
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
        instructionText = (TextView) findViewById(R.id.instructionsForest);
        timeLeft = (TextView) findViewById(R.id.timeID);
        appleButton = (ImageButton) findViewById(R.id.appleButton);

        // Text to Speech
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    String instructions = "Welcome to the Forest. Tap the screen to gather as many apples as you can, but hurry! We don't have much time. Tap to Begin!";
                    instructionText.setText(instructions);
                    speak(instructions);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });



        // Buttons
        appleButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == appleButton) {
            if (counter == -1) {
                startTimer();
                counter++;
            } else {
                counter++;
                newText.setText("Apples Picked: " + counter);
            }

        }
    }

    private void appleDone() {
        Intent i = new Intent(this, PlayScreen.class);
        i.putExtra("flags", flags);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Log.d("Flags During Forest", flags.toString());
        startActivity(i);
    }

    private void startTimer() {
        // Timer
        timer = new CountDownTimer(45000, 1000) {
            @Override
            public void onTick(long l) {
                timeLeft.setText("Time: " + (l/1000));
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
        }.start();
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
