package com.example.katherinenuccio.RoomHunt;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.content.Intent;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

// Add cheats
public class PlayScreen extends AppCompatActivity implements View.OnClickListener {

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

    // Cheat Buttons
    private Button beachButton, mountainButton, forestButton, townButton;

    // Variables to make beacons work
    private BeaconManager beaconManager;
    private Region region;

    // Variables for sound, including text to speech
    private MediaPlayer roomSound;
    private String currSound = "beach";
    private String newSound;
    private TextToSpeech tts;

    // Other
    private TextView mainText;
    private String mainInstructions;

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

        mainText = (TextView) findViewById(R.id.mainText);


        beachButton = (Button) findViewById(R.id.beach_button);
        beachButton.setOnClickListener(this);
        forestButton = (Button) findViewById(R.id.forest_button);
        forestButton.setOnClickListener(this);
        mountainButton = (Button) findViewById(R.id.mountain_button);
        mountainButton.setOnClickListener(this);
        townButton = (Button) findViewById(R.id.town_button);
        townButton.setOnClickListener(this);

        flags = new HashMap<String, Boolean>();

        Intent intent = getIntent();
        if (intent != null){
            try{
                HashMap<String, Boolean> newFlags = (HashMap<String, Boolean>) intent.getSerializableExtra("flags");
                if (!newFlags.isEmpty()) {
                    flags = newFlags;
                }
            }
            catch (Exception ex){
            }
        }
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    if(flags.get("exploreMode")) {
                        mainInstructions = ("Welcome to Room Hunt, please explore the room and find all four locations at the various walls around the room.");
                        speak(mainInstructions);
                        mainText.setText(mainInstructions);
                    }
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });

        Log.d("Flags Begin", flags.toString());


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
                                if ((flags.get("duneDigging") && !flags.get("swordDone")) || flags.get("cheats")) {
                                    // Switch to beach minigame
                                    Intent beachIntent = new Intent(PlayScreen.this, BeachScreen.class);
                                    beachIntent.putExtra("flags", flags);
                                    Log.d("Flags Before Beach", flags.toString());
                                    startActivity(beachIntent);
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
                                if ((flags.get("applePicking") && !flags.get("appleDone")) || flags.get("cheats")){
                                    // Switch to the forest minigame
                                    Intent forestIntent = new Intent(PlayScreen.this, ForestScreen.class);
                                    forestIntent.putExtra("flags", flags);
                                    Log.d("Flags Before Forest", flags.toString());
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
                                    mainInstructions = ("Greetings explorer! Would you mind going to the forest and picking some apples for us? We're running out of food!");
                                    speak(mainInstructions);
                                    mainText.setText(mainInstructions);
                                    flags.put("applePicking", true);
                                }
                                else if (flags.get("appleDone") && !flags.get("duneDigging")){
                                    // Dialogue to tell player to go get sword at beach
                                    mainInstructions = ("Welcome back! Thanks for getting some apples, you should go to the beach and find the sword of legends!");
                                    speak(mainInstructions);
                                    mainText.setText(mainInstructions);
                                    flags.put("duneDigging", true);
                                }
                                else if (flags.get("swordDone") && !flags.get("bossBeating")){
                                    // Dialogue to tell player to go fight dragon
                                    mainInstructions = ("Wow, you got the sword! You really are the hero of legends! Quickly, go to the mountain and slay the dragon!");
                                    speak(mainInstructions);
                                    mainText.setText(mainInstructions);
                                    flags.put("bossBeating", true);
                                }
                                else if (flags.get("dragonDone") && !flags.get("gameDone")){
                                    // Dialogue to tell player congratulations
                                    mainInstructions = ("You did it! I don't know how we can ever repay you.");
                                    speak(mainInstructions);
                                    mainText.setText(mainInstructions);
                                    int count = 0;
                                    while(count < 5) {
                                        speak("Love me");
                                        count++;
                                    }

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
                                if ((flags.get("bossBeating") && !flags.get("dragonDone")) || flags.get("cheats")) {
                                    // Switch to dragon minigame
                                    Intent mountainIntent = new Intent(PlayScreen.this, MountainScreen.class);
                                    mountainIntent.putExtra("flags", flags);
                                    Log.d("Flags Before Mountain", flags.toString());
                                    startActivity(mountainIntent);
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
                    newText.setText(places);
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
        }
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
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

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

    public void onClick(View view) {
        if (view == beachButton) {
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
                if ((flags.get("duneDigging") && !flags.get("swordDone")) || flags.get("cheats")) {
                    // Switch to beach minigame
                    Intent beachIntent = new Intent(PlayScreen.this, BeachScreen.class);
                    beachIntent.putExtra("flags", flags);
                    Log.d("Flags Before Beach", flags.toString());
                    startActivity(beachIntent);
                }
            }
            pauseMediaPlayer();
            flags.put("visitBeach", true);
        } else if(view == forestButton){
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
                if ((flags.get("applePicking") && !flags.get("appleDone")) || flags.get("cheats")){
                    // Switch to the forest minigame
                    Intent forestIntent = new Intent(PlayScreen.this, ForestScreen.class);
                    forestIntent.putExtra("flags", flags);
                    Log.d("Flags Before Forest", flags.toString());
                    startActivity(forestIntent);

                }
            }
            pauseMediaPlayer();
            flags.put("visitForest", true);
        }
        else if(view == mountainButton){
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
                if ((flags.get("bossBeating") && !flags.get("dragonDone")) || flags.get("cheats")) {
                    // Switch to dragon minigame
                    Intent mountainIntent = new Intent(PlayScreen.this, MountainScreen.class);
                    mountainIntent.putExtra("flags", flags);
                    Log.d("Flags Before Mountain", flags.toString());
                    startActivity(mountainIntent);
                }
            }
            pauseMediaPlayer();
            flags.put("visitMountain", true);
        }
        else if(view == townButton){
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
                    mainInstructions = ("Greetings explorer! Would you mind going to the forest and picking some apples for us? We're running out of food!");
                    speak(mainInstructions);
                    mainText.setText(mainInstructions);
                    flags.put("applePicking", true);
                }
                else if (flags.get("appleDone") && !flags.get("duneDigging")){
                    // Dialogue to tell player to go get sword at beach
                    mainInstructions = ("Welcome back! Thanks for getting some apples, you should go to the beach and find the sword of legends!");
                    speak(mainInstructions);
                    mainText.setText(mainInstructions);
                    flags.put("duneDigging", true);
                }
                else if (flags.get("swordDone") && !flags.get("bossBeating")){
                    // Dialogue to tell player to go fight dragon
                    mainInstructions = ("Wow, you got the sword! You really are the hero of legends! Quickly, go to the mountain and slay the dragon!");
                    speak(mainInstructions);
                    mainText.setText(mainInstructions);
                    flags.put("bossBeating", true);
                }
                else if (flags.get("dragonDone") && !flags.get("gameDone")){
                    // Dialogue to tell player congratulations
                    mainInstructions = ("You did it! I don't know how we can ever repay you.");
                    speak(mainInstructions);
                    mainText.setText(mainInstructions);
                    int count = 0;
                    while(count < 5) {
                        speak("Love me");
                        count++;
                    }

                    flags.put("gameDone", true);
                }
            }
            pauseMediaPlayer();
            flags.put("visitTown", true);
        }
        // This sets the explore mode to false so that we can begin giving quests
        if (flags.get("visitBeach") && flags.get("visitForest") && flags.get("visitMountain") && flags.get("visitTown")) {
            flags.put("exploreMode", false);
        }
        TextView newText = (TextView) findViewById(R.id.roomName);
        // This could be removed, mainly used to make sure beacon switching is working.
        newText.setText(newSound);
        // Log.d("Beacon", "Nearest = " + places);
    }

}
