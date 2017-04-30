package com.example.katherinenuccio.RoomHunt;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.widget.MediaController;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;


public class CoveScreen extends AppCompatActivity  {

    private HashMap<String, Boolean> flags;
    private HashMap<String, String> instructions;
    private TextView shakes, coveText;
    private TextToSpeech tts;
    private String coveInstructions;
    private MediaPlayer coveSound;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private int totalShakes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cove_screen);
        shakes = (TextView) findViewById(R.id.totalShakes);
        coveText = (TextView) findViewById(R.id.coveText);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        instructions = new HashMap<String, String>();
        flags.put("coveDone", true);
        instructions.put("instructions", "Return to town");
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    coveInstructions = ("You found a secret cove! Inside is a treasure chest, the only way it will open is if you dance. Quick, start dancing!");
                    speak(coveInstructions);
                    coveText.setText(coveInstructions);
                    coveSound = new MediaPlayer().create(CoveScreen.this, R.raw.sandstorm);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });
        coveSound.setLooping(true);
        coveSound.start();

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
                shakes.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            }
        });
    }

    private void handleShakeEvent(int count) {
        if(totalShakes >= 15) {
            coveInstructions = ("You did it!");
            speak(coveInstructions);
            coveText.setText(coveInstructions);
            coveDone();
        } else {
            totalShakes++;
            if(totalShakes == 4){
                coveInstructions = ("The chest is beginning to open. Keep it up!");
                speak(coveInstructions);
                coveText.setText(coveInstructions);
            }
            if(totalShakes == 10){
                coveInstructions = ("You can see gold coins glittering inside the chest! You're almost there!");
                speak(coveInstructions);
                coveText.setText(coveInstructions);
            }
            shakes.setText("Dance Moves: " + totalShakes);
        }
    }

    private void coveDone(){
        coveSound.stop();
        coveSound.reset();
        coveSound.release();
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
