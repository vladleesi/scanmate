package ru.vladleesi.ultimatescanner.ui.accessibility;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import ru.vladleesi.ultimatescanner.R;


public class SoundMaker {

    private static final String TAG = "SoundMaker";
    private final int durationInSeconds = 1;
    private final int sampleRate = 8000;
    private final int numSamples = durationInSeconds * sampleRate;
    private final double[] sample = new double[numSamples];
    private final byte[] generatedSnd = new byte[2 * numSamples];
    private final SharedPreferences defaultPreferences;
    private final String prefsKey;

    private SoundMaker(@NonNull Context context) {
        defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        prefsKey = context.getString(R.string.settings_sound_maker);
    }

    public static SoundMaker getInstance(@NonNull Context context) {
        return new SoundMaker(context);
    }

    private void genTone() {
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            // hz
            double freqOfTone = 440;
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    public void playSound() {
        boolean isEnable = defaultPreferences.getBoolean(prefsKey, false);
        if (isEnable) {
            genTone();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                        AudioTrack.MODE_STATIC);
                audioTrack.write(generatedSnd, 0, generatedSnd.length);
                try {
                    audioTrack.play();
                } catch (IllegalStateException e) {
                    Log.d(TAG, e.getMessage());
                }
            });
        }
    }
}
