package com.example.katherinenuccio.RoomHunt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;


public class BeachScreen extends AppCompatActivity implements View.OnClickListener {

    private Button beachbutt;
    private HashMap<String, Boolean> flags;
    private TextView shakes;
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
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        flags.put("swordDone", true);
        Log.d("Beach", "Made it");
        beachbutt = (Button) findViewById(R.id.beachbutt);
        beachbutt.setOnClickListener(this);
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
            swordDone();
        } else {
            totalShakes++;
            Log.d("Beach", "Shooketh " + totalShakes);
            shakes.setText("Total: " + totalShakes);
        }
    }

    private void swordDone(){
        Intent i = new Intent(this, PlayScreen.class);
        i.putExtra("flags", flags);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Log.d("Flags During Beach", flags.toString());
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

    @Override
    public void onClick(View view) {
        if (view == beachbutt) {
            swordDone();
        }
    }
}
