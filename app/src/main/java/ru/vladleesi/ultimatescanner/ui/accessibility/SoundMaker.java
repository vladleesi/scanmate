package ru.vladleesi.ultimatescanner.ui.accessibility;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public class SoundMaker {

    private static final String TAG = "SoundMaker";
    private final int durationInSeconds = 1;
    private final int sampleRate = 8000;
    private final int numSamples = durationInSeconds * sampleRate;
    private final double[] sample = new double[numSamples];
    private final byte[] generatedSnd = new byte[2 * numSamples];

    private boolean isEnable = false;

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
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
