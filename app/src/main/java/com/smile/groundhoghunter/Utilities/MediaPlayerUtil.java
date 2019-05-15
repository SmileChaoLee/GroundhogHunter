package com.smile.groundhoghunter.Utilities;

import android.content.Context;
import android.media.MediaPlayer;
/**
 * Created by chaolee on 2017-10-19.
 */

public class MediaPlayerUtil {

    private static MediaPlayer mediaPlayer;

    public static void playSound(final Context ctx, final int rawResourceId) {
        Thread mediaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                    mediaPlayer = MediaPlayer.create(ctx, rawResourceId);
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

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
