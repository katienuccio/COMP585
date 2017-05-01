package com.example.katherinenuccio.RoomHunt;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.CountDownTimer;
import java.util.HashMap;
import java.util.Locale;


public class ForestScreen extends AppCompatActivity {

    private TextView newText, instructionText, timeLeft;
    private HashMap<String, Boolean> flags;
    private HashMap<String, String> instructions;
    private int counter = -1;
    private TextToSpeech tts;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forest_screen);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        instructions = new HashMap<String, String>();
        flags.put("appleDone", true);
        instructions.put("instructions", "Return to the village");
        newText = (TextView) findViewById(R.id.totalClicks);
        instructionText = (TextView) findViewById(R.id.instructionsForest);
        timeLeft = (TextView) findViewById(R.id.timeID);

        // Text to Speech
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    String instructionsText = "You've returned to the Forest. You feel a mighty breeze shake the trees and tons of apples fall to the ground. Tap the screen to gather as many apples as you can, but hurry, we don't have much time before the animals get it. Tap to Begin!";
                    instructionText.setText(instructionsText);
                    speak(instructionsText);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });
        final ConstraintLayout rlayout = (ConstraintLayout) findViewById(R.id.forestscreenlayout);
        rlayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (counter == -1) {
                    startTimer();
                    counter++;
                } else {
                    counter++;
                    newText.setText("Apples Picked: " + counter);
                }
                rlayout.playSoundEffect(SoundEffectConstants.CLICK);
                rlayout.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }

        });


    }



    private void appleDone() {
        Intent i = new Intent(this, PlayScreen.class);
        i.putExtra("flags", flags);
        i.putExtra("instructions", instructions);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void startTimer() {
        // Timer
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                timeLeft.setText("Time: " + (l/1000));
            }
            @Override
            public void onFinish() {
                // Read out how many apples were picked (counter)
                if (counter > 300) {
                    speak("Woohoo! You got " + counter + " apples. That's more apples than I've ever seen before.");
                    appleDone();
                }
                else if (counter > 150) {
                    speak("Holy Cow! You got " + counter + " apples! That's a new record!.");
                    appleDone();
                }
                else if(counter > 50){
                    speak("Wow! You're a natural at this! You got a whopping " + counter +  " apples. That's more than enough!");
                    appleDone();
                } else if (counter > 30) {
                    speak("Great job! You got " + counter + " apples! Woooo!");
                    appleDone();
                } else if (counter > 10){
                    speak("Thanks adventurer! You got " + counter + " apples.");
                    appleDone();
                } else {
                    speak("Not enough! You only got " + counter +  "apples. Please try again.");
                    retryForest();
                }

            }
        }.start();
    }

    private void retryForest(){
        Intent i = new Intent(this, ForestScreen.class);
        i.putExtra("flags", flags);
        i.putExtra("instructions", instructions);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    // Text to speech code. For deprecation/compatibility purposes.
    private void speak(String text) {
        while(tts.isSpeaking()){/*Do Nothing*/}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }

    }

    @Override
    public void onBackPressed() {
        timer.cancel();
        finish();
        return;
    }
}
