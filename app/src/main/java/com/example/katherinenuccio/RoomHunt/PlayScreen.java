package com.example.katherinenuccio.RoomHunt;

import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.content.Intent;
import android.media.MediaPlayer;
import android.view.HapticFeedbackConstants;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
        placesByBeacons.put("2:1", "Village");
        placesByBeacons.put("2:2", "Mountains");
        placesByBeacons.put("2:3", "Cove");
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
    private ImageButton instructionButton;

    // Variables to make beacons work
    private BeaconManager beaconManager;
    private Region region;

    // Variables for sound, including text to speech
    private MediaPlayer roomSound;
    private String currSound = "";
    private String newSound;
    private TextToSpeech tts;

    // Other
    private TextView mainText;
    private String mainInstructions, roomSpeech;
    private int mS, fS, bS, tS = 0;

    // Logic to flag game progression properly
    // This helps us not repeat mini-games or give a player the same quest over and over
    // We use a Map to store each boolean value in order to easily pass the variables through
    // Intents and into our other activities (mini-games)
    private HashMap<String, Boolean> flags;
    private HashMap<String, String> instructions;

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
        instructionButton = (ImageButton) findViewById(R.id.speechButton);
        instructionButton.setOnClickListener(this);

        flags = new HashMap<String, Boolean>();
        instructions = new HashMap<String, String>();

        Intent intent = getIntent();
        if (intent != null){
            try{
                HashMap<String, Boolean> newFlags = (HashMap<String, Boolean>) intent.getSerializableExtra("flags");
                if (!newFlags.isEmpty()) {
                    flags = newFlags;
                }
                instructions = (HashMap<String, String>) intent.getSerializableExtra("instructions");
                mainInstructions = instructions.get("instructions");
                if(!flags.get("exploreMode")){
                    bS = 1; fS = 1; mS = 1; tS = 1;
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
                    speak(mainInstructions);
                    if (flags.get("exploreMode")) {
                        mainInstructions = "Resume exploring";
                    }
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });



        // Main game code.
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    String places = placesNearBeacon(nearestBeacon);
                    TextView newText = (TextView) findViewById(R.id.roomName);

                    switch(places){
                        case "Cove":
                            if (!flags.get("coveDone") && flags.get("appleDone")) {
                                // Go to dance party!

                                Intent coveIntent = new Intent(PlayScreen.this, CoveScreen.class);
                                coveIntent.putExtra("flags", flags);
                                coveIntent.putExtra("instructions", mainInstructions);
                                startActivity(coveIntent);
                            }
                            break;
                        case "Beach":
                            newSound = "beach";
                            pauseMediaPlayer();
                            checkRoom();
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.ocean);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.ocean);
                                roomSound.start();
                            }

                            if (!flags.get("exploreMode")) {
                                if ((flags.get("duneDigging") && !flags.get("swordDone")) || flags.get("cheats")) {
                                    // Switch to beach minigame
                                    Intent beachIntent = new Intent(PlayScreen.this, BeachScreen.class);
                                    beachIntent.putExtra("flags", flags);
                                    startActivity(beachIntent);
                                }
                            } else {
                                if(bS == 0){
                                    mainInstructions = "You've reached the beach. You can hear waves crashing in the distance!";
                                    speak(mainInstructions);
                                    roomSpeech = "You're at the beach";
                                    mainInstructions = "Resume Exploring";
                                    bS = 2;
                                    if(fS == 2){fS = 1;}
                                    if(mS == 2){mS = 1;}
                                    if(tS == 2){tS = 1;}
                                }
                            }
                            flags.put("visitBeach", true);
                            break;
                        case "Forest":
                            newSound = "forest";
                            pauseMediaPlayer();
                            checkRoom();
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.forest);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.forest);
                                roomSound.start();
                            }

                            if (!flags.get("exploreMode")) {
                                if ((flags.get("applePicking") && !flags.get("appleDone")) || flags.get("cheats")){
                                    // Switch to the forest minigame
                                    Intent forestIntent = new Intent(PlayScreen.this, ForestScreen.class);
                                    forestIntent.putExtra("flags", flags);
                                    startActivity(forestIntent);

                                }
                            } else {
                                if(fS == 0) {
                                    mainInstructions = "You've reached the forest. Birds are chirping and you begin to smell apple trees.";
                                    speak(mainInstructions);
                                    roomSpeech = "You're at the forest";
                                    mainInstructions = "Resume Exploring";
                                    fS = 2;
                                    if(bS == 2){bS = 1;}
                                    if(mS == 2){mS = 1;}
                                    if(tS == 2){tS = 1;}
                                }
                            }
                            flags.put("visitForest", true);
                            break;
                        case "Village":
                            newSound = "village";
                            pauseMediaPlayer();
                            checkRoom();
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.town);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.town);
                                roomSound.start();
                            }
                            if (!flags.get("exploreMode")) {
                                // Switch to the town dialogues
                                if (!flags.get("applePicking")){
                                    // Dialogue to tell player to go pick apples
                                    mainInstructions = ("The village elder greets you with a sad look on her face. She tells you that their village has recently been attacked by the mighty, yodeling dragon. This dragon, who they call Yodelo, has destroyed their food supplies. The elder asks you to please go to the forest and retrieve some apples.");
                                    speak(mainInstructions);
                                    mainInstructions = "Go to the forest and grab some apples";
                                    mainText.setText(mainInstructions);

                                    flags.put("applePicking", true);
                                }
                                else if (flags.get("appleDone") && !flags.get("duneDigging")){
                                    // Dialogue to tell player to go get sword at beach
                                    mainInstructions = ("As you return to the village, the elder looks at the number of apples you retrieved and says thank you. She looks at you for a while before telling you about a hero of legends, who will one day arrive and slay the mighty Yodelo. You are told about the hero's sword, which is hidden somewhere in the beach.");
                                    speak(mainInstructions);
                                    mainInstructions = "Go to the beach and find the sword";
                                    mainText.setText(mainInstructions);

                                    flags.put("duneDigging", true);
                                }
                                else if (flags.get("swordDone") && !flags.get("bossBeating")){
                                    // Dialogue to tell player to go fight dragon
                                    mainInstructions = ("As you return to the village with the sword, the elder looks at you in amazement. The elder tells you that in order to awaken the sword, you must point it at the dragon and yell 'I HAVE THE POWER'. Go to the mountains now and slay Yodelo.");
                                    speak(mainInstructions);
                                    mainInstructions = "Go to the mountains and defeat Yodelo";
                                    mainText.setText(mainInstructions);

                                    flags.put("bossBeating", true);
                                }
                                else if (flags.get("dragonDone") && !flags.get("gameDone")){
                                    // Dialogue to tell player congratulations
                                    mainInstructions = ("When you return to village, the village elder thanks you for all that you have done. Congratulations on slaying the mighty Yodelo!");
                                    speak(mainInstructions);
                                    mainText.setText(mainInstructions);
                                    flags.put("gameDone", true);
                                }
                            } else {
                                if(tS == 0) {
                                    mainInstructions = "You've arrived at a village. The bells are ringing, and people are hurrying all around you.";
                                    speak(mainInstructions);
                                    roomSpeech = "You're at the village";
                                    mainInstructions = "Resume Exploring";
                                    tS = 2;
                                    if(fS == 2){fS = 1;}
                                    if(mS == 2){mS = 1;}
                                    if(bS == 2){bS = 1;}
                                }
                            }
                            flags.put("visitVillage", true);
                            break;
                        case "Mountains":
                            newSound = "mountain";
                            pauseMediaPlayer();
                            checkRoom();
                            if (roomSound == null) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.mountain);
                                roomSound.setLooping(true);
                                roomSound.start();
                            } else if (!roomSound.isPlaying()) {
                                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.mountain);
                                roomSound.start();
                            }

                            if (!flags.get("exploreMode")) {
                                if ((flags.get("bossBeating") && !flags.get("dragonDone")) || flags.get("cheats")) {
                                    // Switch to dragon minigame
                                    Intent mountainIntent = new Intent(PlayScreen.this, MountainScreen.class);
                                    mountainIntent.putExtra("flags", flags);
                                    startActivity(mountainIntent);
                                }
                            } else {
                                if(mS == 0) {
                                    mainInstructions = "You've arrived at the mountains. You hear ominous yodeling noises coming from the peaks.";
                                    speak(mainInstructions);
                                    mainInstructions = "Resume Exploring";
                                    roomSpeech = "You're at the mountains";
                                    mS = 2;
                                    if(fS == 2){fS = 1;}
                                    if(bS == 2){bS = 1;}
                                    if(tS == 2){tS = 1;}
                                }
                            }


                            flags.put("visitMountain", true);
                            break;
                    }

                    // This sets the explore mode to false so that we can begin giving quests
                    if (flags.get("visitBeach") && flags.get("visitForest") && flags.get("visitMountain") && flags.get("visitVillage") && flags.get("exploreMode")) {
                        flags.put("exploreMode", false);
                        if(!places.equals("town")){
                            speak("Return to the village");
                            mainInstructions = "Return to the village";
                            mainText.setText(mainInstructions);
                        }
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
        Log.d("SOUND", "Where" + newSound + currSound);
        if (roomSound != null){
            Log.d("SOUND", "There" + newSound + currSound);
            if (!currSound.equals(newSound)) {
                Log.d("SOUND", "Here" + newSound + currSound);
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
        mainText.setText(mainInstructions);
        // Should've done this with ! and not use the else. Oh well.
        while(tts.isSpeaking()){/*Do Nothing*/}
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        while(tts.isSpeaking()){/*Do Nothing*/}

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
        roomSound.pause();
    }

    public void onClick(View view) {
        view.playSoundEffect(SoundEffectConstants.CLICK);
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        if (view == instructionButton) {speak(mainInstructions);}
        if (view == beachButton) {
            newSound = "beach";
            pauseMediaPlayer();
            checkRoom();
            Log.d("BEACH", "bS " + bS + "; fS " + fS + "; mS " + mS + "; tS " + tS);
            if (roomSound == null) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.ocean);
                roomSound.setLooping(true);
                roomSound.start();
            } else if (!roomSound.isPlaying()) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.ocean);
                roomSound.start();
            }
            if (!flags.get("exploreMode")) {
                if ((flags.get("duneDigging") && !flags.get("swordDone")) || flags.get("cheats")) {
                    // Switch to beach minigame
                    Intent beachIntent = new Intent(PlayScreen.this, BeachScreen.class);
                    beachIntent.putExtra("flags", flags);
                    startActivity(beachIntent);
                }
            } else {
                if(bS == 0){
                    mainInstructions = "You've reached the beach. You can hear waves crashing in the distance!";
                    speak(mainInstructions);
                    roomSpeech = "You're at the beach";
                    mainInstructions = "Resume exploring";
                    bS = 2;
                    if(fS == 2){fS = 1;}
                    if(mS == 2){mS = 1;}
                    if(tS == 2){tS = 1;}
                }
            }
            flags.put("visitBeach", true);
        } else if(view == forestButton){
            newSound = "forest";
            pauseMediaPlayer();
            checkRoom();
            if (roomSound == null) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.forest);
                roomSound.setLooping(true);
                roomSound.start();
            } else if (!roomSound.isPlaying()) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.forest);
                roomSound.start();
            }
            if (!flags.get("exploreMode")) {
                if ((flags.get("applePicking") && !flags.get("appleDone")) || flags.get("cheats")){
                    // Switch to the forest minigame
                    Intent forestIntent = new Intent(PlayScreen.this, ForestScreen.class);
                    forestIntent.putExtra("flags", flags);
                    startActivity(forestIntent);

                }
            } else {
                if(fS == 0) {
                    mainInstructions = "You've reached the forest. Birds are chirping and you begin to smell apple trees.";
                    speak(mainInstructions);
                    roomSpeech = "You're at the forest";
                    mainInstructions = "Resume exploring";
                    fS = 2;
                    if(bS == 2){bS = 1;}
                    if(mS == 2){mS = 1;}
                    if(tS == 2){tS = 1;}
                }
            }
            flags.put("visitForest", true);
        }
        else if(view == mountainButton){
            newSound = "mountain";
            pauseMediaPlayer();
            checkRoom();
            if (roomSound == null) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.mountain);
                roomSound.setLooping(true);
                roomSound.start();
            } else if (!roomSound.isPlaying()) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.mountain);
                roomSound.start();
            }
            if (!flags.get("exploreMode")) {
                if ((flags.get("bossBeating") && !flags.get("dragonDone")) || flags.get("cheats")) {
                    // Switch to dragon minigame
                    Intent mountainIntent = new Intent(PlayScreen.this, MountainScreen.class);
                    mountainIntent.putExtra("flags", flags);
                    startActivity(mountainIntent);
                }
            } else {
                if(mS == 0) {
                    mainInstructions = "You've arrived at the mountains. You hear ominous yodeling noises coming from the peaks.";
                    speak(mainInstructions);
                    roomSpeech = "You're at the mountains";
                    mainInstructions = "Resume exploring";
                    mS = 2;
                    if(fS == 2){fS = 1;}
                    if(bS == 2){bS = 1;}
                    if(tS == 2){tS = 1;}
                }
            }
            flags.put("visitMountain", true);
        }
        else if(view == townButton){
            newSound = "village";
            pauseMediaPlayer();
            checkRoom();
            if (roomSound == null) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.town);
                roomSound.setLooping(true);
                roomSound.start();
            } else if (!roomSound.isPlaying()) {
                roomSound = new MediaPlayer().create(PlayScreen.this, R.raw.town);
                roomSound.start();
            }
            if (!flags.get("exploreMode")) {
                // Switch to the town dialogues
                if (!flags.get("applePicking")){
                    // Dialogue to tell player to go pick apples
                    mainInstructions = ("The village elder greets you with a sad look on her face. She tells you that their village has recently been attacked by the mighty, yodeling dragon. This dragon, who they call Yodelo, has destroyed their food supplies. The elder asks you to please go to the forest and retrieve some apples.");
                    speak(mainInstructions);
                    mainInstructions = "Go to the forest and grab some apples";
                    mainText.setText(mainInstructions);

                    flags.put("applePicking", true);
                }
                else if (flags.get("appleDone") && !flags.get("duneDigging")){
                    // Dialogue to tell player to go get sword at beach
                    mainInstructions = ("As you return, the elder looks at the number of apples you retrieved and says thank you. She looks at you for a while before telling you about a hero of legends, who will one day arrive and slay the mighty Yodelo. You are told about the hero's sword, which is hidden somewhere in the beach.");
                    speak(mainInstructions);
                    mainInstructions = "Go to the beach and find the sword";
                    mainText.setText(mainInstructions);

                    flags.put("duneDigging", true);
                }
                else if (flags.get("swordDone") && !flags.get("bossBeating")){
                    // Dialogue to tell player to go fight dragon
                    mainInstructions = ("As you return with the sword, the elder looks at you in amazement. The elder tells you that in order to awaken the sword, you must point it at the dragon and yell 'I HAVE THE POWER'. Go to the mountains now and slay Yodelo.");
                    speak(mainInstructions);
                    mainInstructions = "Go to the mountains and defeat Yodelo";
                    mainText.setText(mainInstructions);

                    flags.put("bossBeating", true);
                }
                else if (flags.get("dragonDone") && !flags.get("gameDone")){
                    // Dialogue to tell player congratulations
                    mainInstructions = ("The village elder thanks you for all that you have done. Congratulations on slaying the mighty Yodelo!");
                    speak(mainInstructions);
                    mainText.setText(mainInstructions);
                    flags.put("gameDone", true);
                }
            } else {
                if(tS == 0) {
                    mainInstructions = "You've arrived at a village. The bells are ringing, and people are hurrying all around you.";
                    speak(mainInstructions);
                    roomSpeech = "You're at the village";
                    mainInstructions = "Resume exploring";
                    tS = 2;
                    if(fS == 2){fS = 1;}
                    if(mS == 2){mS = 1;}
                    if(bS == 2){bS = 1;}
                }
            }
            flags.put("visitVillage", true);
        }
        // This sets the explore mode to false so that we can begin giving quests
        if (flags.get("visitBeach") && flags.get("visitForest") && flags.get("visitMountain") && flags.get("visitVillage") && flags.get("exploreMode")) {
            flags.put("exploreMode", false);
            if(!newSound.equals("village")){
                speak("Return to the village");
                mainInstructions = "Return to the village";
                mainText.setText(mainInstructions);
            }
        }
        TextView newText = (TextView) findViewById(R.id.roomName);
        // This could be removed, mainly used to make sure beacon switching is working.
        newText.setText(newSound);
        // Log.d("Beacon", "Nearest = " + places);
    }

    protected void checkRoom(){
        switch(newSound){
            case "beach":
                if (bS != 0) {
                    roomSpeech = "You're at the beach";
                    if (bS == 1) {
                        speak(roomSpeech);
                        bS = 2;
                        if (fS == 0) {
                        } else {
                            fS = 1;
                        }
                        if (mS == 0) {
                        } else {
                            mS = 1;
                        }
                        if (tS == 0) {
                        } else {
                            tS = 1;
                        }
                    } else if (bS == 2) {
                        if (fS == 0) {
                        } else {
                            fS = 1;
                        }
                        if (mS == 0) {
                        } else {
                            mS = 1;
                        }
                        if (tS == 0) {
                        } else {
                            tS = 1;
                        }
                    }
                }
                break;
            case "forest":
                if (fS != 0) {
                    roomSpeech = "You're at the forest";
                    if (fS == 1) {
                        speak(roomSpeech);
                        fS = 2;
                        if (bS == 0) {
                        } else {
                            bS = 1;
                        }
                        if (mS == 0) {
                        } else {
                            mS = 1;
                        }
                        if (tS == 0) {
                        } else {
                            tS = 1;
                        }
                    } else if (fS == 2) {
                        if (bS == 0) {
                        } else {
                            bS = 1;
                        }
                        if (mS == 0) {
                        } else {
                            mS = 1;
                        }
                        if (tS == 0) {
                        } else {
                            tS = 1;
                        }
                    }
                }
                break;
            case "mountain":
                if (mS != 0) {
                    roomSpeech = "You're at the mountains";
                    if (mS == 1) {
                        speak(roomSpeech);
                        mS = 2;
                        if (fS == 0) {
                        } else {
                            fS = 1;
                        }
                        if (bS == 0) {
                        } else {
                            bS = 1;
                        }
                        if (tS == 0) {
                        } else {
                            tS = 1;
                        }
                    } else if (mS == 2) {
                        if (fS == 0) {
                        } else {
                            fS = 1;
                        }
                        if (bS == 0) {
                        } else {
                            bS = 1;
                        }
                        if (tS == 0) {
                        } else {
                            tS = 1;
                        }
                    }
                }
                break;
            case "village":
                if (tS != 0) {
                    roomSpeech = "You're at the village";
                    if (tS == 1) {
                        speak(roomSpeech);
                        tS = 2;
                        if (fS == 0) {
                        } else {
                            fS = 1;
                        }
                        if (mS == 0) {
                        } else {
                            mS = 1;
                        }
                        if (bS == 0) {
                        } else {
                            bS = 1;
                        }
                    } else if (tS == 2) {
                        if (fS == 0) {
                        } else {
                            fS = 1;
                        }
                        if (mS == 0) {
                        } else {
                            mS = 1;
                        }
                        if (bS == 0) {
                        } else {
                            bS = 1;
                        }
                    }
                }
                break;
        }
        mainText.setText(mainInstructions);
    }

    @Override
    public void onBackPressed() {
        roomSound.release();
        finish();
        return;
    }

}
