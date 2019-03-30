package com.smile.groundhoghunter.Utilities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by chaolee on 2017-10-19.
 */

public class SoundUtil {

    private static MediaPlayer mediaPlayer = null;

    public static void playSound(final Context ctx, final int rawResourceId) {
        Thread mediaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
                mediaPlayer = MediaPlayer.create(ctx,rawResourceId);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        });

        // mediaThread.run();
        mediaThread.start();
    }

    public static void releaseMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
