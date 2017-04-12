package com.example.katherinenuccio.RoomHunt;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PlayScreen extends AppCompatActivity {

    private static final Map<String, String> PLACES_BY_BEACONS;

    // Lets us know which beacon corresponds to which location. Hardcoded on here.
    static {
        Map<String, String> placesByBeacons = new HashMap<>();
        placesByBeacons.put("1:1", "Beach");
        placesByBeacons.put("1:2","Forest");
        placesByBeacons.put("2:1", "Town");
        placesByBeacons.put("2:2", "Mountains");
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    // This lets us know which beacon is closest to the phone
    private String placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return "";
    }

    // Variables to make beacons work
    private BeaconManager beaconManager;
    private Region region;

    // Variables for sound, including text to speech
    private MediaPlayer roomSound;
    private String currSound = "beach";
    private String newSound;
    private TextToSpeech t1;

    // Logic to flag game progression properly
    // This helps us not repeat mini-games or give a player the same quest over and over
    // We use a Map to store each boolean value in order to easily pass the variables through
    // Intents and into our other activities (mini-games)
    private HashMap<String, Boolean> flags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_screen);
        beaconManager = new BeaconManager(this);
        flags = new HashMap<String, Boolean>();
        flags.put("visitBeach", false);
        flags.put("visitForest", false);
        flags.put("visitMountain", false);
        flags.put("visitTown", false);
        flags.put("exploreMode", true);
        flags.put("applePicking", false);
        flags.put("duneDigging", false);
        flags.put("bossBeating", false);
        flags.put("appleDone", false);
        flags.put("swordDone", false);
        flags.put("dragonDone", false);
        flags.put("gameDone", false);
        Intent intent = getIntent();
        if (intent != null){
            try{
                HashMap<String, Boolean> newFlags = (HashMap<String, Boolean>) intent.getSerializableExtra("flags");
                if (!newFlags.isEmpty()) {
                    flags = newFlags;
                    Log.d("Flags After", flags.toString());
                }
            }
            catch (Exception ex){
            }
        }
        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        if(flags.get("exploreMode")) {
            t1.speak("Welcome to Room Hunt, please explore the room and find all four locations at the various walls around the room.", TextToSpeech.QUEUE_FLUSH, null);
        }

        // Main game code.
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
                            if (!flags.get("exploreMode")) {
                                if (flags.get("duneDigging") && !flags.get("swordDone")) {
                                    // Switch to beach minigame

                                    flags.put("swordDone", true);
                                }
                            }
                            pauseMediaPlayer();
                            flags.put("visitBeach", true);
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
                            if (!flags.get("exploreMode")) {
                                if (flags.get("applePicking") && !flags.get("appleDone")){
                                    // Switch to the forest minigame
                                    Intent forestIntent = new Intent(PlayScreen.this, ForestScreen.class);
                                    forestIntent.putExtra("flags", flags);
                                    Log.d("Flags Before", flags.toString());
                                    startActivity(forestIntent);

                                }
                            }
                            pauseMediaPlayer();
                            flags.put("visitForest", true);
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
                            if (!flags.get("exploreMode")) {
                                // Switch to the town dialogues
                                if (!flags.get("applePicking")){
                                    // Dialogue to tell player to go pick apples
                                    t1.speak("Greetings explorer! Would you mind going to the forest and picking some apples for us? We're running out of food!", TextToSpeech.QUEUE_FLUSH, null);
                                    flags.put("applePicking", true);
                                }
                                else if (flags.get("appleDone") && !flags.get("duneDigging")){
                                    // Dialogue to tell player to go get sword at beach
                                    t1.speak("Welcome back! Thanks for getting some apples, you should go to the beach and find the sword of legends!", TextToSpeech.QUEUE_FLUSH, null);
                                    flags.put("duneDigging", true);
                                }
                                else if (flags.get("swordDone") && !flags.get("bossBeating")){
                                    // Dialogue to tell player to go fight dragon
                                    t1.speak("Wow, you got the sword! You really are the hero of legends! Quickly, go to the mountain and slay the dragon!", TextToSpeech.QUEUE_FLUSH, null);
                                    flags.put("bossBeating", true);
                                }
                                else if (flags.get("dragonDone") && !flags.get("gameDone")){
                                    // Dialogue to tell player congratulations
                                    t1.speak("You did it! I don't know how we can ever repay you.", TextToSpeech.QUEUE_FLUSH, null);

                                    flags.put("gameDone", true);
                                }
                            }
                            pauseMediaPlayer();
                            flags.put("visitTown", true);
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
                            if (!flags.get("exploreMode")) {
                                if (flags.get("bossBeating")) {
                                    // Switch to dragon minigame

                                    flags.put("dragonDone", true);
                                }
                            }
                            pauseMediaPlayer();
                            flags.put("visitMountain", true);
                            break;
                    }

                    // This sets the explore mode to false so that we can begin giving quests
                    if (flags.get("visitBeach") && flags.get("visitForest") && flags.get("visitMountain") && flags.get("visitTown")) {
                        flags.put("exploreMode", false);
                    }

                    // This could be removed, mainly used to make sure beacon switching is working.
                    newText.setText("Nearest Beacon is " + places);
                    // Log.d("Beacon", "Nearest = " + places);
                }
            }
        });
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),null,null);

    }

    // This allows us to stop the current sound and begin a new one, only if it's
    // a different sound. This prevents a sound from constantly looping at the 00:01 mark.
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

        super.onPause();
    }

}
