/**
 * 
 */
package com.hp.myidea.guidedroid.base;

import java.util.Locale;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.speech.tts.TextToSpeech;
import android.util.Log;

/**
 * @author mauricio
 *
 */
public class Communicator implements TextToSpeech.OnInitListener {

    private static final String TAG = Communicator.class.getSimpleName();

    private AudioTrack track;
	private final int sampleRate = 44100; // in Hertz

	private TextToSpeech mTts;

    public Communicator(Context owner) {
    	super();
        // Initialize text-to-speech. This is an asynchronous operation.
        // The OnInitListener (second argument) is called after initialization completes.
        mTts = new TextToSpeech(owner, this);
        this.initAudioDevice();
    }

    public void sayIt(String text) {
    	mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);    	
    }

	public void playTone(int freq, float duration) {
		short[] audioData = this.generateTone(freq, duration);
		this.track.write(audioData, 0, audioData.length);
		track.play();
	}

    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Language data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                // Greet the user.
                sayHello();
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    private void sayHello() {
        String hello = "Hi, this is your guide droid starting.";
        mTts.speak(hello,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
    }

	private void initAudioDevice() {
		int minSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
	}

	private short[] generateTone(int freq, float duration) {
		short[] audioData = new short[(int) (duration * sampleRate)];

		float increment = (float) (2 * Math.PI) * freq / sampleRate; // angular increment for each sample
		float angle = 0;
		for (int i = 0; i < audioData.length; i++) {
			audioData[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
			angle += increment;
		}
		return audioData;
	}

}
