package com.example.katherinenuccio.RoomHunt;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Locale;


public class BeachScreen extends AppCompatActivity  {

    private HashMap<String, Boolean> flags;
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
        flags.put("swordDone", true);
        Log.d("Beach", "Made it");
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("Test", "Init");
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    beachInstructions = ("Welcome to the Beach. Dig with your phone to find the sword of legends.");
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
				Log.d("Beach", "Here");
                handleShakeEvent(count);
            }
        });
    }

    private void handleShakeEvent(int count) {
        if(totalShakes >= 10) {
            beachInstructions = ("You did it! Return to town.");
            speak(beachInstructions);
            beachText.setText(beachInstructions);
            swordDone();
        } else {
            totalShakes++;
            if(totalShakes == 3){
                beachInstructions = ("You see something in the sand! Keep digging.");
                speak(beachInstructions);
                beachText.setText(beachInstructions);
            }
            if(totalShakes == 8){
                beachInstructions = ("You're almost there!");
                speak(beachInstructions);
                beachText.setText(beachInstructions);
            }
            shakes.setText("Total: " + totalShakes);
        }
    }

    private void swordDone(){
        Intent i = new Intent(this, PlayScreen.class);
        i.putExtra("flags", flags);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}
