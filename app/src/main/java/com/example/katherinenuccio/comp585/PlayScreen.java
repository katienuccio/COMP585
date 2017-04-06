package com.example.katherinenuccio.comp585;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.connection.internal.protocols.Operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PlayScreen extends AppCompatActivity {

    private static final Map<String, String> PLACES_BY_BEACONS;

    static {
        Map<String, String> placesByBeacons = new HashMap<>();
        placesByBeacons.put("1:1", "Beach");
        placesByBeacons.put("1:2","Forest");
        placesByBeacons.put("2:1", "Town");
        placesByBeacons.put("2:2", "Mountains");
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private String placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return "";
    }

    private BeaconManager beaconManager;
    private Region region;
    private MediaPlayer roomSound;
    private String currSound = "";
    private String newSound;
    private TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);
        beaconManager = new BeaconManager(this);
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                    t1.speak("Welcome to Room Hunt, please explore the room and find all four locations at the various walls around the room.", TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });







        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    String places = placesNearBeacon(nearestBeacon);
                    TextView newText = (TextView) findViewById(R.id.roomName);

                    switch(places){
                        case "Beach":
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.ocean);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.ocean);
                                roomSound.start();
                            }
                            newSound = "beach";
                            break;
                        case "Forest":
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.forest);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.forest);
                                roomSound.start();
                            }
                            newSound = "forest";
                            break;
                        case "Town":
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.town);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.town);
                                roomSound.start();
                            }
                            newSound = "town";
                            break;
                        case "Mountains":
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.mountain);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.mountain);
                                roomSound.start();
                            }
                            newSound = "mountain";
                            break;
                    }
                    pauseMediaPlayer();
                    newText.setText("Nearest Beacon is " + places);
//                    if (roomSound != null){
//                        if (currSound.equals(newSound)) {
//                            currSound = newSound;
//                            roomSound.stop();
//                            roomSound.reset();
//                            roomSound.release();
//                            roomSound = null;
//                        }
//                    };
                    //+ nearestBeacon.getMajor() + " " + nearestBeacon.getMinor());
                   // Log.d("Beacon", "Nearest = " + places);
                }
            }
        });
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),null,null);

    }

    protected void pauseMediaPlayer() {
        if (roomSound != null){
            if (!currSound.equals(newSound)) {
                currSound = newSound;
                roomSound.stop();
                roomSound.reset();
                roomSound.release();
                roomSound = null;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }
}
