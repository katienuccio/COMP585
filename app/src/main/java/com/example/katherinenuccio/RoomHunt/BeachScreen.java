package com.example.katherinenuccio.RoomHunt;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.speech.tts.TextToSpeech;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;


public class BeachScreen extends AppCompatActivity  {

    private HashMap<String, Boolean> flags;
    private HashMap<String, String> instructions;
    private TextView shakes, beachText;
    private TextToSpeech tts;
    private String beachInstructions;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private int totalShakes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beach_screen);
        shakes = (TextView) findViewById(R.id.totalShakes);
        beachText = (TextView) findViewById(R.id.beachText);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        instructions = new HashMap<String, String>();
        flags.put("swordDone", true);
        instructions.put("instructions", "Return to the village");
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    beachInstructions = ("You're not sure where the sword may be hidden, so you just pick a spot and begin digging. Use your phone to dig for the sword.");
                    speak(beachInstructions);
                    beachText.setText(beachInstructions);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
				/*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                handleShakeEvent(count);

                // We should consider adding a dig sound here. Added CLICK and vibrate for now
                shakes.playSoundEffect(SoundEffectConstants.CLICK);
                shakes.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });
    }

    private void handleShakeEvent(int count) {
        if(totalShakes >= 16) {
            beachInstructions = ("You got it! The sword of legends is now yours!");
            speak(beachInstructions);
            beachText.setText(beachInstructions);
            swordDone();
        } else {
            totalShakes++;
            if(totalShakes == 3){
                beachInstructions = ("You begin to feel something sharp in the sand! Keep digging.");
                speak(beachInstructions);
                beachText.setText(beachInstructions);
            }
            if(totalShakes == 10){
                beachInstructions = ("You can feel most of the sword now. Keep digging, you're almost there!");
                speak(beachInstructions);
                beachText.setText(beachInstructions);
            }
            shakes.setText("Digs: " + totalShakes);
        }
    }

    private void swordDone(){
        Intent i = new Intent(this, PlayScreen.class);
        i.putExtra("flags", flags);
        i.putExtra("instructions", instructions);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
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
        finish();
        return;
    }

}
