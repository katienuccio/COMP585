package com.example.katherinenuccio.RoomHunt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MountainScreen extends Activity implements RecognitionListener {

    private TextView returnedText, myText;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognition";
    private String mResult;
    private HashMap<String, Boolean> flags;
    private HashMap<String, String> instructions;
    private boolean listening;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mountain_screen);
        returnedText = (TextView) findViewById(R.id.textView1);
        myText = (TextView) findViewById(R.id.textView2);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        Intent intent = getIntent();
        flags = (HashMap<String, Boolean>)intent.getSerializableExtra("flags");
        flags.put("dragonDone", true);
        instructions = new HashMap<String, String>();
        instructions.put("instructions", "Return to the village");
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("Test", "Init");
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    Log.e("TTS", "Initialization Succeeded");
                    returnedText.setText("You've arrived at Yodelo's lair. Tap the screen and shout the magic phrase to slay Yodelo. Remember, the phrase is 'I HAVE THE POWER'.");
                    speak("You've arrived at Yodelo's lair. Tap the screen and shout the magic phrase to slay Yodelo. Remember, the phrase is 'I HAVE THE POWER'.");
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language Is Not Supported");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed");
                }
            }
        });
        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,true);
        listening = false;

        ConstraintLayout rlayout = (ConstraintLayout) findViewById(R.id.mountainlayout);
        rlayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!listening) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                    listening = true;
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                    listening = false;
                }
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
        listening = true;
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        speech.stopListening();
        listening = false;
    }

    @Override
    public void onError(int errorCode) {
        Log.d("VOICE", getErrorText(errorCode));
        if (mResult != null) {
            myText.setText(mResult);
            if (mResult.equals("I have the power")) {
                returnedText.setText("Congratulations! You have unlocked the sword.");
                speak("Congratulations! You have unlocked the sword.");
                beatBoss();
            } else {
                returnedText.setText("Sorry, that's not the right phrase. Please try again.");
                speak("Sorry, that's not the right phrase. Please try again.");
            }
        } else {
            returnedText.setText("I'm sorry, I couldn't hear you. Please try again.");
            speak("I'm sorry, I couldn't hear you. Please try again.");

        }
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        ArrayList<String> unstableData = partialResults.getStringArrayList("android.speech.extra.UNSTABLE_TEXT");
        mResult = data.get(0) + unstableData.get(0);
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        if (results != null) {
            myText.setText(mResult);
            if (mResult.equals("I have the power")) {
                returnedText.setText("Congratulations! You have slain the dragon!");
                speak("Congratulations! You have slain the dragon!");
                beatBoss();
            } else {
                returnedText.setText("Sorry, that's not the right phrase. Please try again.");
                speak("Sorry, that's not the right phrase. Please try again.");
            }
        } else {
            returnedText.setText("I'm sorry, I couldn't hear you. Please try again.");
            speak("I'm sorry, I couldn't hear you. Please try again.");
        }
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public void beatBoss() {
        Intent i = new Intent(this, PlayScreen.class);
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

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onBackPressed() {
        finish();
        return;
    }
}